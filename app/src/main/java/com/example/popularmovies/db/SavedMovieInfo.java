package com.example.popularmovies.db;


import android.content.ContentValues;
import android.database.Cursor;
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
   * Construct from the short movie information.
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
   * Construct array from array of movies.
   * @param infos Short movie information array.
   */
  public static SavedMovieInfo[] createArray (@NonNull TmdbMovieShort[] infos) {
    SavedMovieInfo[] movies = new SavedMovieInfo[infos.length];
    for (int i = 0; i < infos.length; i++) {
      movies[i] = new SavedMovieInfo(infos[i]);
    }
    return movies;
  }
  
  
  /**
   * Construct from the DB Cursor row.
   * @param cursor    Cursor to read data from.
   * @param position  Cursor row position.
   * @param columnIDs Pre-cached column IDs.
   */
  public SavedMovieInfo (@NonNull Cursor cursor, int position, @NonNull MoviesContract.FavoriteMovies.ColumnIdHolder columnIDs) {
    cursor.moveToPosition(position);
    id                = cursor.getInt   (columnIDs.getId());
    poster_path       = cursor.getString(columnIDs.getPoster());
    backdrop_path     = cursor.getString(columnIDs.getBackdrop());
    title             = cursor.getString(columnIDs.getTitle());
    original_title    = cursor.getString(columnIDs.getOrigTitle());
    original_language = cursor.getString(columnIDs.getOrigLang());
    overview          = cursor.getString(columnIDs.getOverview());
    release_date      = cursor.getString(columnIDs.getRelDate());
    vote_average      = cursor.getDouble(columnIDs.getVoteAvg());
    vote_count        = cursor.getInt   (columnIDs.getVoteCnt());
    // Optional fields.
    runtime = cursor.isNull(columnIDs.getRuntime()) ? null : cursor.getInt(columnIDs.getRuntime());
  }
  
  
  /**
   * Create ContentValues to save movie into database.
   * @return Created ContentValues.
   */
  public ContentValues createContentValues() {
    ContentValues values = new ContentValues();
    values.put (MoviesContract.FavoriteMovies._ID,           id);
    values.put (MoviesContract.FavoriteMovies.COL_TITLE,     title);
    values.put (MoviesContract.FavoriteMovies.COL_ORIG_TITLE,original_title);
    values.put (MoviesContract.FavoriteMovies.COL_POSTER,    poster_path);
    values.put (MoviesContract.FavoriteMovies.COL_BACKDROP,  backdrop_path);
    values.put (MoviesContract.FavoriteMovies.COL_ORIG_LANG, original_language);
    values.put (MoviesContract.FavoriteMovies.COL_OVERVIEW,  overview);
    values.put (MoviesContract.FavoriteMovies.COL_RELEASE,   release_date);
    values.put (MoviesContract.FavoriteMovies.COL_VOTE_AVG,  vote_average);
    values.put (MoviesContract.FavoriteMovies.COL_VOTE_CNT,  vote_count);
    // Optional fields.
    values.put (MoviesContract.FavoriteMovies.COL_RUNTIME,   runtime);
    return values;
  }
  
  
  /**
   * Fill movie details information.
   * @param details Movie details.
   */
  public void setDetails (@NonNull TmdbMovieDetails details) {
    runtime = details.runtime;
  }
  
  
  /**
   * Required for Parcelable implementation.
   */
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
