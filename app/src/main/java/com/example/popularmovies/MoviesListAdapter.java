package com.example.popularmovies;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.popularmovies.themoviedb.TmdbMovieShort;
import com.squareup.picasso.Picasso;


/**
 * Adapter for the main activity's RecyclerView grid.
 */
public class MoviesListAdapter extends RecyclerView.Adapter<MoviesListAdapter.ViewHolder> {
  
  private static final String TAG = MoviesListAdapter.class.getSimpleName();
  
  public interface OnClickListener {
    void onClickStar (int item);
    void onClickItem (int item);
  }
  
  private final Context mContext;
  private final OnClickListener mClickListener;
  
  //private Cursor mCursor;
  private static TmdbMovieShort[] mMovies = null;
  private static boolean[] mFavorites = null;


  /**
   * Constructor
   * @param context Context.
   * @param listener Click events listener.
   */
  public MoviesListAdapter (Context context, OnClickListener listener) {
    mContext = context;
    mClickListener = listener;
  }
  
  
  /**
   * Store movies list inside adapter.
   * @param movies Movies list.
   */
  public void setMovies (@NonNull TmdbMovieShort[] movies, @Nullable boolean[] favorites) {
    mMovies = movies;
    if (favorites == null) {
      mFavorites = new boolean[movies.length];
    } else {
      mFavorites = favorites;
    }
    notifyDataSetChanged();
  }


  /**
   * Switch favorite flag for specified item.
   * @param position Item position.
   */
  public void switchFavorite (int position) {
    mFavorites[position] = !mFavorites[position];
    notifyItemChanged(position);
  }


  /**
   * Allows to mark item as favorite.
   * @param position Item position.
   * @param fav      Favorite flag.
   */
  public void setFavorite (int position, boolean fav) {
    mFavorites[position] = fav;
    notifyItemChanged(position);
  }


  /**
   * Check if item marked favorite.
   * @param position Item position.
   * @return True if item marked favorite.
   */
  public boolean getFavorite (int position) {
    return mFavorites[position];
  }
  
  
  @Override
  public MoviesListAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(mContext).inflate(R.layout.rv_grid_item_movie, parent, false);
    view.setFocusable(true);
    return new ViewHolder(view);
  }


  @Override
  public void onBindViewHolder (final MoviesListAdapter.ViewHolder holder, final int position) {
    
    // Set "favorite" star.
    Log.d(TAG, "onBindViewHolder(" + position + ") fav = " + mFavorites[position]);
    holder.mStarIB.setPressed(mFavorites[position]);
    
    // Prepare image URL.
    String imageURL = "http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg";
    //String imageURL = "http://image.tmdb.org/t/p/w185//nBNZadXqJSdt05SHLqgT0HuC5Gm.jpg";
    //String imageURL = "http://127.0.0.1/djkfhskhdf.jpg";
    Log.d(TAG, "onBindViewHolder(" + position + ") image: " + imageURL);

    // Load image.
    holder.mPosterIV.setColorFilter(0);
    Picasso.with(mContext)
      .load(imageURL)
      .placeholder(android.R.drawable.progress_indeterminate_horizontal)
      .error(android.R.drawable.stat_notify_error)
      .into(holder.mPosterIV, new com.squareup.picasso.Callback() {
        @Override
        public void onSuccess () {
          Log.d (TAG, "onBindViewHolder.Picasso.onSuccess(" + position + ")");
          holder.mPosterIV.setContentDescription(mContext.getString(R.string.poster_content_description));
        }
        @Override
        public void onError () {
          Log.d (TAG, "onBindViewHolder.Picasso.onError(" + position + ")");
          holder.mPosterIV.setColorFilter(Color.RED);
          holder.mPosterIV.setContentDescription(mContext.getString(R.string.poster_error_content_description));
        }
      });
  }
  
  
  @Override
  public int getItemCount () {
    return (mMovies == null) ? 0 : mMovies.length;
  }
  
  
  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    
    public ImageButton mStarIB;
    public ImageView   mPosterIV;
    
    public ViewHolder (View view) {
      super(view);
      
      mStarIB = view.findViewById(R.id.rv_item_star);
      mPosterIV = view.findViewById(R.id.rv_item_poster);
      
      view.setOnClickListener(this);
      mStarIB.setOnClickListener(this); // To select favorite movie.
    }
    
    
    @Override
    public void onClick (View view) {
      int position = getAdapterPosition();
      if (view == mStarIB) {
        mClickListener.onClickStar(position);
      } else {
        mClickListener.onClickItem(position);
      }
    }
  }
}
