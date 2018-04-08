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
    
    public static final String TABLE_NAME     = "FAV_MOVIES"; // Table name.
    public static final String COL_TITLE      = "title";      // Movie title.
    public static final String COL_ORIG_TITLE = "orig_title"; // Movie original title.
    public static final String COL_POSTER     = "poster";     // Poster image.
    public static final String COL_BACKDROP   = "backdrop";   // Background image.
    public static final String COL_ORIG_LANG  = "orig_lang";  // Original language.
    public static final String COL_OVERVIEW   = "overview";   // Movie overview.
    public static final String COL_RELEASE    = "rel_date";   // Release date.
    public static final String COL_VOTE_AVG   = "vote_avg";   // Vote average.
    public static final String COL_VOTE_CNT   = "vote_cnt";   // Vote count.
    public static final String COL_RUNTIME    = "runtime";    // Run time.
    
  }

}
