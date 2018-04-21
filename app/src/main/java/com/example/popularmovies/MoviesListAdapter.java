package com.example.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.popularmovies.db.MoviesContract;
import com.example.popularmovies.db.SavedMovieInfo;
import com.example.popularmovies.themoviedb.Api3;
import com.example.popularmovies.themoviedb.TmdbMovieShort;
import com.example.popularmovies.utils.Options;
import com.squareup.picasso.Picasso;

import java.util.HashSet;
import java.util.Set;

import static android.support.v7.widget.RecyclerView.NO_ID;


/**
 * Adapter for the main activity's RecyclerView grid.
 */
public class MoviesListAdapter extends RecyclerView.Adapter<MoviesListAdapter.ViewHolder> {
  private static final String TAG = Options.XTAG + MoviesListAdapter.class.getSimpleName();
  
  public interface OnClickListener {
    void onClickStar (int item);
    void onClickItem (int item);
  }
  
  private final Context mContext;
  private final OnClickListener mClickListener;
  
  // LOGIC: HOW IT WORKS.
  // * When main activity shows favorite movies (that is stored locally in the DB) we use Cursor to
  //   access all it's information.
  // * When main activity shows other tab, we use an array of movie information downloaded from the
  //   TMDb server. At the same time we still need Favorite movies list (as a Cursor) to show "star"
  //   marks over the posters and allow user to add/remove movies from Favorites.
  
  // Favorite movies access.
  private Cursor mCursorFavs = null;
  MoviesContract.FavoriteMovies.ColumnIdHolder mColumnIDs = new MoviesContract.FavoriteMovies.ColumnIdHolder(); // Column IDs cached here to prevent requesting it every time.
  private Set<Integer> mFavorites = new HashSet<>();
  
  // Non-favorite movies access.
  private SavedMovieInfo[] mMovies = null;
  
  // Next determines if we are on Favorites tab or some other movies list.
  private boolean mIsFavoritesTab = false;
  
  
  /**
   * Constructor
   * @param context Context.
   * @param listener Click events listener.
   */
  public MoviesListAdapter (Context context, OnClickListener listener) {
    mContext = context;
    mClickListener = listener;
    setHasStableIds(true);
    getItemId(0);
  }
  
  
  /**
   * Store received movies list (single page contents) inside adapter.
   * @param movies Movies list.
   * @param isFavoritesTab Is we currently show Favorites tab to the user or some other movies list?
   */
  public void setMovies (@NonNull SavedMovieInfo[] movies, boolean isFavoritesTab) {
    mIsFavoritesTab = isFavoritesTab;
    mMovies = movies;
    notifyDataSetChanged();
  }
  
  
  /**
   * Add all favorite movie IDs to the set for fast search.
   */
  private void generateFavoritesSet() {
    mFavorites.clear();
    if (mCursorFavs == null) return; // Not loaded yet.
    if (!mColumnIDs.isValid()) {
      Log.e(TAG, "Invalid column IDs with valid cursor?!"); // This shouldn't normally happen!
      return;
    }
    if (!mCursorFavs.moveToFirst()) return; // Empty cursor.
    // Add all favorite movie IDs to the set for fast search.
    do {
      int movieId = mCursorFavs.getInt(mColumnIDs.getId());
      mFavorites.add(movieId);
    } while (mCursorFavs.moveToNext());
  }
  
  
  /**
   * Set or remove favorite movies list table cursor.
   * @param cursor Favorites list cursor or null.
   */
  public void swapCursor (Cursor cursor) {
    if (mCursorFavs != null) mCursorFavs.close(); // Close old Cursor.
    mCursorFavs = cursor;
    mColumnIDs.update(cursor);
    generateFavoritesSet();
    notifyDataSetChanged();
  }
  
  
  @Override
  public int getItemCount () {
    if (mIsFavoritesTab) {
      return (mCursorFavs == null) ? 0 : mCursorFavs.getCount();
    } else {
      return (mMovies == null) ? 0 : mMovies.length;
    }
  }
  
  
  @Override public long getItemId (int position) { return getMovieId(position); }
  
  
  /**
   * Return movie ID at specified position.
   * @param position Movie position.
   * @return Movie ID or -1 if not found or no movies at all.
   */
  public int getMovieId (int position) {
    if (position < 0) return (int)NO_ID;
    if (mIsFavoritesTab) {
      if (mCursorFavs == null) return (int)NO_ID;
      if (!mCursorFavs.moveToPosition(position)) return (int)NO_ID;
      return mCursorFavs.getInt (mColumnIDs.getId());
    } else {
      return ((mMovies == null) || (position >= mMovies.length)) ? (int)NO_ID : mMovies[position].id;
    }
  }
  

  /**
   * Return movie at specified position.
   * @param position Movie position.
   * @return Movie or null if not found or no movies at all.
   */
  public SavedMovieInfo getMovie (int position) {
    if (mIsFavoritesTab) {
      if (mCursorFavs == null) return null;
      return new SavedMovieInfo(mCursorFavs, position, mColumnIDs);
    } else {
      return (mMovies == null) ? null : mMovies[position];
    }
  }
  
  
  /**
   * This method made static to reuse it from DetailsActivity.
   * @param context    Context.
   * @param movie      Movie information.
   * @param isFavorite Is this movie should be favorite.
   */
  public static void setFavorite (final Context context, final SavedMovieInfo movie, boolean isFavorite) {
    Runnable runnable;
    if (isFavorite) {
      // Remove from favorites.
      runnable = new Runnable() { @Override public void run () {
        int count = context.getContentResolver().delete(MoviesContract.FavoriteMovies.buildMovieUriById(movie.id), null, null);
        Log.d(TAG, "setFavorite() delete returned " + count);
      }};
    } else {
      // Add to favorites.
      runnable = new Runnable() { @Override public void run () {
        Uri uri = context.getContentResolver().insert(MoviesContract.FavoriteMovies.buildMovieUriById(movie.id), movie.createContentValues());
        Log.d(TAG, "setFavorite() insert returned " + uri.toString());
      }};
    }
    new Thread(runnable).run(); // After completion, CursorLoader should be reloaded automatically, causing notifyDatasetChanged() call.
  }
  
  
  /**
   * Switch favorite flag for specified item.
   * @param position Item position.
   */
  public void switchFavorite (int position) {
    setFavorite (mContext, mMovies[position], mFavorites.contains(mMovies[position].id));
  }


  /**
   * Check if item marked favorite.
   * @param position Item position.
   * @return True if item marked favorite.
   */
  public boolean isFavorite (int position) {
    return mFavorites.contains(getMovieId(position));
  }
  
  
  @Override
  public MoviesListAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(mContext).inflate(R.layout.rv_grid_item_movie, parent, false);
    view.setFocusable(true);
    return new ViewHolder(view);
  }


  @Override
  public void onBindViewHolder (final MoviesListAdapter.ViewHolder holder, final int position) {
    
    SavedMovieInfo movie = getMovie(position);
    
    // Set "favorite" star.
    holder.updateStarButton(position);
    
    // Prepare image URL.
    String posterPath = movie.poster_path;
    if ((posterPath == null) || posterPath.isEmpty()) {
      // No poster available. Show empty.
      // TODO: Show movie title if no poster available.
      holder.mPosterIV.setImageResource(android.R.drawable.stat_notify_error);
    } else {
      // Start poster image loading.
      String imageURL = Api3.getImageURL(posterPath, Options.getInstance(mContext).getPostersPreviewResolution());
  
      // Load image.
      holder.mPosterIV.setColorFilter(0);
      holder.mPosterIV.setContentDescription(movie.title);
      Picasso.with(mContext)
        .load(imageURL)
        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
        .error(android.R.drawable.stat_notify_error)
        .into(holder.mPosterIV, new com.squareup.picasso.Callback() {
          @Override public void onSuccess () {}
          @Override public void onError () { holder.mPosterIV.setColorFilter(Color.RED); } // TODO: Show movie title if fails to download poster.
        });
    }
  }
  
  
  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    
    public ImageView mStarIB;
    public ImageView mPosterIV;
    
    public ViewHolder (View view) {
      super(view);
      
      mStarIB = view.findViewById(R.id.rv_item_star);
      mPosterIV = view.findViewById(R.id.rv_item_poster);
      
      view.setOnClickListener(this);
      mStarIB.setOnClickListener(this); // To select favorite movie.
    }
  
  
    private void updateStarButton(int position) {
      mStarIB.setImageResource(isFavorite(position) ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
    }
    
    
    @Override
    public void onClick (View view) {
      int position = getAdapterPosition();
      if (position < 0) return; // Too late, clicked element already destroyed.
      if (view == mStarIB) {
        updateStarButton(position);
        mClickListener.onClickStar(position);
      } else {
        mClickListener.onClickItem(position);
      }
    }
  }
}
