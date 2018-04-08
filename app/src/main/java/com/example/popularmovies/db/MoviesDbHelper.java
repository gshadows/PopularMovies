package com.example.popularmovies.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.popularmovies.db.MoviesContract.FavoriteMovies;


public class MoviesDbHelper extends SQLiteOpenHelper {
  
  
  private static final int DATABASE_VERSION = 1;
  private static final String DATABASE_NAME = "movies.db";


  public MoviesDbHelper (Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }
  
  
  @Override
  public void onCreate (SQLiteDatabase db) {
    final String SQL_CREATE_DB = "CREATE TABLE " + FavoriteMovies.TABLE_NAME + " ("
      + FavoriteMovies._ID            + " INTEGER PRIMARY KEY ON CONFLICT REPLACE, "
      + FavoriteMovies.COL_TITLE      + " TEXT NOT NULL, "
      + FavoriteMovies.COL_ORIG_TITLE + " TEXT NOT NULL, "
      + FavoriteMovies.COL_POSTER     + " TEXT NOT NULL, "
      + FavoriteMovies.COL_BACKDROP   + " TEXT NOT NULL, "
      + FavoriteMovies.COL_ORIG_LANG  + " TEXT NOT NULL, "
      + FavoriteMovies.COL_OVERVIEW   + " TEXT NOT NULL, "
      + FavoriteMovies.COL_RELEASE    + " TEXT NOT NULL, "
      + FavoriteMovies.COL_VOTE_AVG   + " REAL NOT NULL, "
      + FavoriteMovies.COL_VOTE_CNT   + " INTEGER NOT NULL, "
      + FavoriteMovies.COL_RUNTIME    + " INTEGER);";
    
    db.execSQL(SQL_CREATE_DB);
  }


  @Override
  public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
    // Nothing here until new DB version appears.
  }
}
