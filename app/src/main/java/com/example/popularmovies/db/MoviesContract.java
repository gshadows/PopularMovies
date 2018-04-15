package com.example.popularmovies.db;

import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

import lombok.Getter;


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
    public static final String CONTENT_PATH   = "movies";     // Path for content provider.
    public static final Uri    CONTENT_URI    = Uri.parse("content://" + CONTENT_AUTHORITY + "/" + CONTENT_PATH);
    
    // Mandatory.
    public static final String COL_TITLE      = "title";      // Movie title.
    public static final String COL_ORIG_TITLE = "orig_title"; // Movie original title.
    public static final String COL_POSTER     = "poster";     // Poster image.
    public static final String COL_BACKDROP   = "backdrop";   // Background image.
    public static final String COL_ORIG_LANG  = "orig_lang";  // Original language.
    public static final String COL_OVERVIEW   = "overview";   // Movie overview.
    public static final String COL_RELEASE    = "rel_date";   // Release date.
    public static final String COL_VOTE_AVG   = "vote_avg";   // Vote average.
    public static final String COL_VOTE_CNT   = "vote_cnt";   // Vote count.
    // Optional.
    public static final String COL_RUNTIME    = "runtime";    // Run time.
    
    public static Uri buildMovieUriById (int id) { return Uri.withAppendedPath(CONTENT_URI, String.valueOf(id)); }
    
    public static final class ColumnIdHolder extends ColumnIdHolderBase {
      @Getter private int id, title, origTitle, poster, backdrop, origLang, overview, relDate, voteAvg, voteCnt, runtime;
      @Override public boolean update (@Nullable Cursor cursor) {
        invalidate();
        if (cursor == null) return false;
        // We do not expect absence of predefined columns, so App will crash here because DB corrupted.
        id       = cursor.getColumnIndexOrThrow(_ID           );
        title    = cursor.getColumnIndexOrThrow(COL_TITLE     );
        origTitle= cursor.getColumnIndexOrThrow(COL_ORIG_TITLE);
        poster   = cursor.getColumnIndexOrThrow(COL_POSTER    );
        backdrop = cursor.getColumnIndexOrThrow(COL_BACKDROP  );
        origLang = cursor.getColumnIndexOrThrow(COL_ORIG_LANG );
        overview = cursor.getColumnIndexOrThrow(COL_OVERVIEW  );
        relDate  = cursor.getColumnIndexOrThrow(COL_RELEASE   );
        voteAvg  = cursor.getColumnIndexOrThrow(COL_VOTE_AVG  );
        voteCnt  = cursor.getColumnIndexOrThrow(COL_VOTE_CNT  );
        runtime  = cursor.getColumnIndexOrThrow(COL_RUNTIME   );
        validate();
        return true;
      }
    }
  }

}
