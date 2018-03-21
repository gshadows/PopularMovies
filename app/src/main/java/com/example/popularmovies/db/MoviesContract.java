package com.example.popularmovies.db;

import android.provider.BaseColumns;


/**
 * Defines table and column names for the movies database.
 */
public class MoviesContract {
  
  /** Content provider name. */
  public static final String CONTENT_AUTHORITY = "com.example.popularmovies";
  
  
  /**
   * This nested class stores favorite movies database table and it's column names.
   */
  public static final class FavoriteMovies implements BaseColumns {
    
    public static final String TABLE_NAME = "FAV_MOVIES"; // Table name.
    public static final String COL_NAME   = "name";       // Movie name.
    public static final String COL_POSTER = "poster";     // Poster image.
    
  }

}
