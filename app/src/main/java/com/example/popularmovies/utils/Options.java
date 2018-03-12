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
  private static final String KEY_POSTERS_PREVIEW_RES = "post_pre_res";
  private static final String KEY_POSTERS_DETAILS_RES = "post_det_res";
  private static final String KEY_BACKGROUND_RES      = "back_res";

  private static volatile Options mInstance;
  private SharedPreferences mSharedPrefs;
  
  
  /** Determines movies list sort order: true - popular, false - top rated. */
  private boolean mDisplayPopularList;
  
  /** Determines posters preview image resolution for main activity. */
  private int mPostersPreviewResolution;

  /** Determines posters details image resolution for details activity. */
  private int mPostersDetailsResolution;

  /** Determines posters details image resolution for details activity. */
  private int mBackgroundResolution;

  
  // Getters collection :)
  public boolean isPopularDisplayed()  { return  mDisplayPopularList; }
  public boolean isTopRatedDisplayed() { return !mDisplayPopularList; }
  public int getPostersPreviewResolution() { return mPostersPreviewResolution; }
  public int getPostersDetailsResolution() { return mPostersDetailsResolution; }
  public int getBackgroundResolution()     { return mBackgroundResolution;     }


  /**
   * Reload all settings from the storage. Always called from the constructor.
   * @param context Context need to read default option if none was stored yet. Not saved inside.
   */
  public void reloadEverything (@NonNull Context context) {
    if (context == null) throw new IllegalArgumentException("Context could't be null");
    if (mSharedPrefs == null) throw new NullPointerException("Unexpected mSharedPrefs == null");
    
    Resources res = context.getResources();
    mDisplayPopularList = mSharedPrefs.getBoolean(KEY_DISPLAY_POPULARS, res.getBoolean(R.bool.displayPopulars));
    mPostersPreviewResolution = mSharedPrefs.getInt(KEY_POSTERS_PREVIEW_RES, res.getInteger(R.integer.postersPreviewResolution));
    mPostersDetailsResolution = mSharedPrefs.getInt(KEY_POSTERS_DETAILS_RES, res.getInteger(R.integer.postersDetailsResolution));
    mBackgroundResolution = mSharedPrefs.getInt(KEY_BACKGROUND_RES, res.getInteger(R.integer.backgroundResolution));
  }


  /**
   * @param isPopular true - display popular movies, false - display top rated.
   */
  public void setPopularDisplayed (boolean isPopular) {
    if (isPopular == mDisplayPopularList) return; // No change here.
    mSharedPrefs.edit()
      .putBoolean (KEY_DISPLAY_POPULARS, isPopular)
      .apply();
    mDisplayPopularList = isPopular;
  }


  /**
   * Change posters preview resolution setting.
   * @param resolution Resolution.
   */
  public void setPostersPreviewResolution (int resolution) {
    if (resolution == mPostersPreviewResolution) return; // No change here.
    mSharedPrefs.edit()
      .putInt (KEY_POSTERS_PREVIEW_RES, resolution)
      .apply();
    mPostersPreviewResolution = resolution;
  }


  /**
   * Change posters details resolution setting.
   * @param resolution Resolution.
   */
  public void setPostersDetailsResolution (int resolution) {
    if (resolution == mPostersDetailsResolution) return; // No change here.
    mSharedPrefs.edit()
      .putInt (KEY_POSTERS_DETAILS_RES, resolution)
      .apply();
    mPostersDetailsResolution = resolution;
  }


  /**
   * Change posters preview resolution seting.
   * @param resolution Resolution.
   */
  public void setmBackgroundResolution (int resolution) {
    if (resolution == mPostersPreviewResolution) return; // No change here.
    mSharedPrefs.edit()
      .putInt (KEY_BACKGROUND_RES, resolution)
      .apply();
    mPostersPreviewResolution = resolution;
  }


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


  @Override
  public void onSharedPreferenceChanged (SharedPreferences sharedPreferences, String key) {
    if (key.equals(KEY_DISPLAY_POPULARS)) {
      mDisplayPopularList = sharedPreferences.getBoolean(key, mDisplayPopularList);
    }
  }
}
