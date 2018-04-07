package com.example.popularmovies;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.popularmovies.themoviedb.TmdbVideo;


/**
 * Adapter for the main activity's RecyclerView grid.
 */
public class VideosAdapter extends RecyclerView.Adapter<VideosAdapter.ViewHolder> {
  
  private static final String TAG = VideosAdapter.class.getSimpleName();
  
  public interface OnClickListener { void onClick (int item); }
  
  private final Context mContext;
  private final OnClickListener mClickListener;
  
  private TmdbVideo[] mVideos = null;
  
  
  /**
   * Constructor
   * @param context Context.
   * @param listener Click events listener.
   */
  public VideosAdapter (Context context, OnClickListener listener) {
    mContext = context;
    mClickListener = listener;
  }
  
  
  /**
   * Store Videos list inside adapter.
   * @param Videos Videos list.
   */
  public void setVideos (@NonNull TmdbVideo[] Videos) {
    mVideos = Videos;
    notifyDataSetChanged();
  }

  
  /**
   * Return video information at sepcified positon.
   * @param position Video position.
   * @return Requested video information.
   */
  public TmdbVideo getVideo(int position) {
    return mVideos[position];
  }

  
  
  /**
   * Generate URL for the video at sepcified positon.
   * @param position Video position.
   * @return Generated URL string or null if video type is unknown.
   */
  public String getVideoURL (int position) {
    TmdbVideo vid = getVideo(position);
    switch (vid.site.toLowerCase()) {
      case "youtube": return "https://www.youtube.com/watch?v=" + vid.key;
      default: return null; // Unknown video site.
    }
  }

  
  
  @Override
  public VideosAdapter.ViewHolder onCreateViewHolder (ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(mContext).inflate(R.layout.rv_videos_item, parent, false);
    view.setFocusable(true);
    return new ViewHolder(view);
  }


  @Override
  public void onBindViewHolder (final VideosAdapter.ViewHolder holder, final int position) {
    TmdbVideo vid = mVideos[position];
    String text = new StringBuilder()
      .append(vid.type)
      .append(" (")
      .append(vid.site)
      .append(", ")
      .append(vid.size)
      .append(")\n")
      .append(vid.name)
      .toString();
    holder.mDescriptionTV.setText(text);
    
    // For unknown video types override "play" icon with error.
    if (getVideoURL(position) == null) {
      holder.mImageView.setImageResource(android.R.drawable.stat_notify_error);
      holder.mImageView.setColorFilter(Color.RED);
    }
  }
  
  
  @Override
  public int getItemCount () {
    return (mVideos == null) ? 0 : mVideos.length;
  }
  
  
  class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    
    public TextView mDescriptionTV;
    public ImageView mImageView;
    
    public ViewHolder (View view) {
      super(view);
  
      mDescriptionTV = view.findViewById(R.id.description_tv);
      mImageView = view.findViewById(R.id.play_iv);
      
      view.setOnClickListener(this);
    }
    
    
    @Override
    public void onClick (View view) {
      mClickListener.onClick(getAdapterPosition());
    }
  }
}
