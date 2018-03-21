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
      + FavoriteMovies._ID        + " INTEGER PRIMARY KEY, "
      + FavoriteMovies.COL_NAME   + " , "
      + FavoriteMovies.COL_POSTER + " );";

    db.execSQL(SQL_CREATE_DB);
  }


  @Override
  public void onUpgrade (SQLiteDatabase db, int oldVersion, int newVersion) {
    // Nothing here until new DB version appears.
  }
}
