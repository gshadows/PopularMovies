package com.example.popularmovies.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import static com.example.popularmovies.db.MoviesContract.FavoriteMovies;


public class MoviesContentProvider extends ContentProvider {
  public static final String TAG = MoviesContentProvider.class.getSimpleName();
  
  private static final int CODE_MOVIES       = 100;
  private static final int CODE_MOVIE_BY_ID  = CODE_MOVIES + 1;
  
  
  private static final UriMatcher mUriMatcher = createUriMatcher();
  private MoviesDbHelper mDbHelper;
  
  
  private static UriMatcher createUriMatcher() {
    final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    matcher.addURI (MoviesContract.CONTENT_AUTHORITY, FavoriteMovies.CONTENT_PATH, CODE_MOVIES);
    matcher.addURI (MoviesContract.CONTENT_AUTHORITY, FavoriteMovies.CONTENT_PATH + "/#", CODE_MOVIE_BY_ID);
    return matcher;
  }
  
  
  @Override
  public boolean onCreate () {
    mDbHelper = new MoviesDbHelper(getContext());
    return true;
  }
  
  
  /**
   * Query from content provider.
   * @param uri           URI for a whole table or single row.
   * @param projection    Requested column names.
   * @param selection     WHERE clause.
   * @param selectionArgs WHERE arguments.
   * @param sortOrder     ORDER BY clause.
   * @return Query results as Cursor
   */
  @Override
  public Cursor query (Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    switch (mUriMatcher.match(uri)) {
      case CODE_MOVIES:
        // Require all records "as is".
        break;
    
      case CODE_MOVIE_BY_ID:
        // Add filtering by ID.
        selection = FavoriteMovies._ID + " = ?";
        selectionArgs = new String[]{ uri.getLastPathSegment() };
        break;
    
      default:
        throw new UnsupportedOperationException(uri.toString());
    }
    
    Cursor cursor = mDbHelper.getReadableDatabase().query(FavoriteMovies.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
    cursor.setNotificationUri(getContext().getContentResolver(), uri);
    return cursor;
  }
  
  
  /**
   * Update specified values.
   * @param uri           URI.
   * @param values        Values to update row with.
   * @param selection     WHERE clause.
   * @param selectionArgs WHERE arguments.
   * @return Affected rows count.
   */
  @Override
  public int update (Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  
  /**
   * Insert new values.
   * @param uri    URI for a single row.
   * @param values Values to insert.
   * @return Inserted row URI.
   */
  @Override
  public Uri insert (Uri uri, ContentValues values) {
    switch (mUriMatcher.match(uri)) {
      case CODE_MOVIE_BY_ID:
        // Add filtering by ID.
        long id = mDbHelper.getWritableDatabase().insert(FavoriteMovies.TABLE_NAME, null, values);
        if (id >= 0) {
          uri = Uri.withAppendedPath(uri, String.valueOf(id));
          getContext().getContentResolver().notifyChange(uri, null);
          return uri;
        }
        throw new SQLException("Failed to insert: " + uri);
        
      default:
        throw new UnsupportedOperationException(uri.toString());
    }
  }
  
  
  /**
   * 
   * @param uri           URI for a whole table or single row.
   * @param selection     WHERE clause.
   * @param selectionArgs WHERE arguments.
   * @return Number of rows affected.
   */
  @Override
  public int delete (Uri uri, String selection, String[] selectionArgs) {
    switch (mUriMatcher.match(uri)) {
      case CODE_MOVIES:
        // Delete all records specified in WHERE clause.
        break;
        
      case CODE_MOVIE_BY_ID:
        // Add filtering by ID.
        selection = FavoriteMovies._ID + " = ?";
        selectionArgs = new String[]{ uri.getLastPathSegment() };
        break;
    
      default:
        throw new UnsupportedOperationException(uri.toString());
    }
    
    int rowsAffected = mDbHelper.getWritableDatabase().delete(FavoriteMovies.TABLE_NAME, selection, selectionArgs);
    if (rowsAffected > 0) {
      getContext().getContentResolver().notifyChange(uri, null);
    }
    return rowsAffected;
  }
  
  
  @Override
  public String getType (Uri uri) {
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
