package com.example.popularmovies.db;


import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.example.popularmovies.themoviedb.TmdbMovieDetails;
import com.example.popularmovies.themoviedb.TmdbMovieShort;


/**
 * This class represents all movie information that is stored locally in the DB.
 */
public class SavedMovieInfo implements Parcelable {
  
  // Basic information (should always be available).
  public int     id;
  
  public String  poster_path;
  public String  backdrop_path;
  
  public String  title;
  public String  original_title;
  public String  original_language;
  public String  overview;
  
  public String  release_date;
  public double  vote_average;
  public int     vote_count;
  
  // Extended information (could be not cached).
  public Integer runtime;
  
  
  /**
   * Fill basic information from the short movie information.
   * @param info Short movie information.
   */
  public SavedMovieInfo (@NonNull TmdbMovieShort info) {
    // Fill basic information.
    id                = info.id;
    poster_path       = info.poster_path;
    backdrop_path     = info.backdrop_path;
    title             = info.title;
    original_title    = info.original_title;
    original_language = info.original_language;
    overview          = info.overview;
    release_date      = info.release_date;
    vote_average      = info.vote_average;
    vote_count        = info.vote_count;
    
    // Clear optional fields.
    runtime = null;
  }
  
  
  /**
   * Fill movie details information.
   * @param details Movie details.
   */
  public void setDetails (@NonNull TmdbMovieDetails details) {
    runtime = details.runtime;
  }
  
  
  public static final Creator<SavedMovieInfo> CREATOR = new Creator<SavedMovieInfo>() {
    @Override
    public SavedMovieInfo createFromParcel(Parcel in) {
      return new SavedMovieInfo(in);
    }
    
    @Override
    public SavedMovieInfo[] newArray(int size) {
      return new SavedMovieInfo[size];
    }
  };
  
  @Override public int describeContents() { return 0; }
  
  
  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString  (poster_path);
    parcel.writeString  (overview);
    parcel.writeString  (release_date);
    parcel.writeInt     (id);
    parcel.writeString  (original_title);
    parcel.writeString  (original_language);
    parcel.writeString  (title);
    parcel.writeString  (backdrop_path);
    parcel.writeInt     (vote_count);
    parcel.writeDouble  (vote_average);
    parcel.writeInt     ((runtime == null) ? -1 : runtime);
  }
  
  
  public SavedMovieInfo (Parcel parcel) {
    poster_path       = parcel.readString();
    overview          = parcel.readString();
    release_date      = parcel.readString();
    id                = parcel.readInt();
    original_title    = parcel.readString();
    original_language = parcel.readString();
    title             = parcel.readString();
    backdrop_path     = parcel.readString();
    vote_count        = parcel.readInt();
    vote_average      = parcel.readDouble();
    
    // Optional arguments should be checked for "not present" special values.
    int runtimeOptional = parcel.readInt();
    runtime = (runtimeOptional >= 0) ? runtimeOptional : null;
  }
  
  
  private SavedMovieInfo(){} // Forbid creation without basic information.
  
}
