package com.example.popularmovies;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.popularmovies.themoviedb.TmdbMovieShort;


/**
 * Adapter for the main activity's RecyclerView grid.
 */
public class MoviesListAdapter extends RecyclerView.Adapter<MoviesListAdapter.ViewHolder> {
  
  public interface OnClickListener {
    void onClickStar (int item);
    void onClickItem (int item);
  }
  
  private final Context mContext;
  private final OnClickListener mClickListener;
  
  //private Cursor mCursor;
  private static TmdbMovieShort[] mMovies = null;


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
  public void setMovies (TmdbMovieShort[] movies) {
    mMovies = movies;
    notifyDataSetChanged();
  }
  
  
  @Override
  public MoviesListAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
    return null;
  }


  @Override
  public void onBindViewHolder (MoviesListAdapter.ViewHolder holder, int position) {
    
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
      mStarIB = view.findViewById(R.id.star_ib);
      mPosterIV = view.findViewById(R.id.poster_iv);
      view.setOnClickListener(this);
      mStarIB.setOnClickListener(this); // To select favorite movie.
    }
    

    @Override
    public void onClick (View view) {
      int position = getAdapterPosition();
      if (view == mStarIB) {
        Log.d("ViewHolder", "onClick(star): " + position);
        mClickListener.onClickStar(position);
      } else {
        Log.d("ViewHolder", "onClick(item): " + position);
        mClickListener.onClickItem(position);
      }
    }
  }
}
