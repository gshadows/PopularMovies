package com.example.popularmovies.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.example.popularmovies.R;


/**
 * This class stores application options and handles it's save and restore.
 */
public class Options implements SharedPreferences.OnSharedPreferenceChangeListener {
  
  // Preference access keys.
  private static final String KEY_DISPLAY_POPULARS = "popular";
  
  private boolean mDisplayPopularList; // true - popular, false - top rated.
  public boolean isPopularDisplayed()  { return  mDisplayPopularList; }
  public boolean isTopRatedDisplayed() { return !mDisplayPopularList; }


  private static volatile Options mInstance;
  private SharedPreferences mSharedPrefs;


  /**
   * Obtain instance of application options storage.
   * @param context Context. Never saved inside. Pass null only if you sure instance was already created before.
   * @return Instance of Options class.
   */
  public synchronized static Options getInstance (Context context) {
    if (mInstance == null) synchronized (Options.class) { if (mInstance == null) mInstance = new Options(context); }
    return mInstance;
  }
  
  
  // Private constructor.
  private Options (@NonNull Context context) {
    if (context == null) throw new IllegalArgumentException("Context could't be null");
    mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
    if (mSharedPrefs == null) {
      throw new UnknownError("Couldn't obtain shared preferences!");
    }
    
    // TODO: Is this object needs to be unregistered?
    // It lives while the app lives, so it will be destroyed after last activity/service destroyed
    // and loose last reference to it (unless we have some leaks). So no preference changes
    // should happen after destruction.
    mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
    
    reloadEverything(context);
  }
  
  
  /**
   * Reload all settings from the storage. Always called from the constructor.
   * @param context Context need to read default option if none was stored yet. Not saved inside.
   */
  public void reloadEverything (@NonNull Context context) {
    if (context == null) throw new IllegalArgumentException("Context could't be null");
    if (mSharedPrefs == null) throw new NullPointerException("Unexpected mSharedPrefs == null");
    
    Resources res = context.getResources();
    mDisplayPopularList = mSharedPrefs.getBoolean(KEY_DISPLAY_POPULARS, res.getBoolean(R.bool.displayPopulars));
  }
  
  
  /**
   * Change display settings.
   * @param isPopular true - display popular movies, false - display top rated.
   */
  public void setPopularDisplayed (boolean isPopular) {
    if (isPopular == mDisplayPopularList) return; // No change here.
    mSharedPrefs.edit()
      .putBoolean (KEY_DISPLAY_POPULARS, isPopular)
      .apply();
    mDisplayPopularList = isPopular;
  }


  @Override
  public void onSharedPreferenceChanged (SharedPreferences sharedPreferences, String key) {
    if (key.equals(KEY_DISPLAY_POPULARS)) {
      mDisplayPopularList = sharedPreferences.getBoolean(key, mDisplayPopularList);
    }
  }
}
