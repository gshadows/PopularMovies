package com.example.popularmovies.db;

import android.database.Cursor;


/**
 * Base class for column indexes storage.
 * Storing column indexes such way prevents requesting it from the Cursor every time it need.
 * Moreover it moves all index work code from adapters to the Contract classes.
 */
public abstract class ColumnIdHolderBase {
  
  
  private boolean mValid = false;
  
  
  /**
   * Checks if indexes valid.
   * @return Stored column IDs validity state.
   */
  public boolean isValid() { return mValid; }
  
  
  /**
   * Mark stored column indexes as valid.
   */
  protected void validate() { mValid = true; }
  
  
  /**
   * Mark stored column indexes as invalid.
   */
  public void invalidate() { mValid = false; }
  
  
  /**
   * Update indexes from specified Cursor. Invalidates current indexes before attempt.
   * @param cursor Cursor to obtain indexes from.
   * @return Returns only if update was successful and indexes are all valid.
   */
  abstract public boolean update (Cursor cursor);
  
}
