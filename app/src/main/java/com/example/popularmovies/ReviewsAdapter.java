package com.example.popularmovies;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.popularmovies.themoviedb.TmdbReview;


/**
 * Adapter for the main activity's RecyclerView grid.
 */
public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
  //private static final String TAG = Options.XTAG + ReviewsAdapter.class.getSimpleName();
  
  public interface OnClickListener { void onClick (int item); }
  
  private final Context mContext;
  private final OnClickListener mClickListener;
  
  private TmdbReview[] mReviews = null;
  
  
  /**
   * Constructor
   * @param context Context.
   * @param listener Click events listener.
   */
  public ReviewsAdapter (Context context, OnClickListener listener) {
    mContext = context;
    mClickListener = listener;
  }
  
  
  /**
   * Store reviews list inside adapter.
   * @param reviews Reviews list.
   */
  public void setReviews (@NonNull TmdbReview[] reviews) {
    mReviews = reviews;
    notifyDataSetChanged();
  }

  
  /**
   * Return review at sepcified positon.
   * @param position Review position.
   * @return Review at specified position.
   */
  public TmdbReview getReview(int position) {
    return mReviews[position];
  }

  
  
  @Override
  public ReviewsAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(mContext).inflate(R.layout.rv_reviews_item, parent, false);
    view.setFocusable(true);
    return new ViewHolder(view);
  }


  @Override
  public void onBindViewHolder (final ReviewsAdapter.ViewHolder holder, final int position) {
    holder.mAuthorTV.setText(mReviews[position].author);
    
    String text = mReviews[position].content;
    if (text.length() > 120) {
      text = text.substring(0, 119);
      int lastSpace = text.lastIndexOf(' ');
      text = text.substring(0, lastSpace);
      text += " ...";
    }
    holder.mReviewTV.setText(text);
  }
  
  
  @Override
  public int getItemCount () {
    return (mReviews == null) ? 0 : mReviews.length;
  }
  
  
  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    
    public TextView mAuthorTV;
    public TextView mReviewTV;
    
    
    public ViewHolder (View view) {
      super(view);
  
      mAuthorTV = view.findViewById(R.id.author_tv);
      mReviewTV = view.findViewById(R.id.review_tv);
      
      view.setOnClickListener(this);
    }
    
    
    @Override
    public void onClick (View view) {
      int position = getAdapterPosition();
      if (position < 0) return; // Too late, clicked element already destroyed.
      mClickListener.onClick(position);
    }
  }
}
