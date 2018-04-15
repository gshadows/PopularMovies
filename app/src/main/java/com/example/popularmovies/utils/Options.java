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
  
  public static final String XTAG = "XXX-"; // Global tag prefix to filter only my own logs in logcat.
  
  // Preference access keys.
  private static final String KEY_CURRENT_TAB         = "cur_tab";
  private static final String KEY_POSTERS_PREVIEW_RES = "post_pre_res";
  private static final String KEY_POSTERS_DETAILS_RES = "post_det_res";
  private static final String KEY_BACKGROUND_RES      = "back_res";

  private static volatile Options mInstance;
  private SharedPreferences mSharedPrefs;
  
  
  public enum CurrentTab {
    FAVORITES, POPULAR, TOP_RATED;
  
    public String toTranslatableString (Context context) {
      switch (this) {
        case FAVORITES: return context.getString(R.string.favorites);
        case POPULAR:   return context.getString(R.string.popular);
        case TOP_RATED: return context.getString(R.string.top_rated);
        default: return "";
      }
    }
  };
  
  
  /** Currently selected tab (movies list) in the main activity. */
  private CurrentTab mCurrentTab;
  
  /** Determines posters preview image resolution for main activity. */
  private int mPostersPreviewResolution;

  /** Determines posters details image resolution for details activity. */
  private int mPostersDetailsResolution;

  /** Determines posters details image resolution for details activity. */
  private int mBackgroundResolution;

  
  // Getters collection :)
  public CurrentTab getCurrentTab()        { return mCurrentTab; }
  public boolean    isFavoriteTab()        { return mCurrentTab == CurrentTab.FAVORITES; }
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
    mCurrentTab = CurrentTab.valueOf(mSharedPrefs.getString(KEY_CURRENT_TAB, res.getString(R.string.defaultStartTab)));
    mPostersPreviewResolution = mSharedPrefs.getInt(KEY_POSTERS_PREVIEW_RES, res.getInteger(R.integer.postersPreviewResolution));
    mPostersDetailsResolution = mSharedPrefs.getInt(KEY_POSTERS_DETAILS_RES, res.getInteger(R.integer.postersDetailsResolution));
    mBackgroundResolution = mSharedPrefs.getInt(KEY_BACKGROUND_RES, res.getInteger(R.integer.backgroundResolution));
  }


  /**
   * Change currently selected tab in MainActivity.
   * @param tab Currently selected tab.
   */
  public void setCurrentTab (CurrentTab tab) {
    if (tab == mCurrentTab) return; // No change here.
    mSharedPrefs.edit()
      .putString (KEY_CURRENT_TAB, tab.name())
      .apply();
    mCurrentTab = tab;
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
    
    mSharedPrefs.registerOnSharedPreferenceChangeListener(this);
    reloadEverything(context);
  }


  @Override
  public void onSharedPreferenceChanged (SharedPreferences sharedPreferences, String key) {
    if (key.equals(KEY_CURRENT_TAB)) {
      mCurrentTab = CurrentTab.valueOf(sharedPreferences.getString(key, mCurrentTab.name()));
    } else if (key.equals(KEY_POSTERS_PREVIEW_RES)) {
      mPostersPreviewResolution = mSharedPrefs.getInt(KEY_POSTERS_PREVIEW_RES, mPostersPreviewResolution);
    } else if (key.equals(KEY_POSTERS_DETAILS_RES)) {
      mPostersDetailsResolution = mSharedPrefs.getInt(KEY_POSTERS_DETAILS_RES, mPostersDetailsResolution);
    } else if (key.equals(KEY_BACKGROUND_RES)) {
      mBackgroundResolution = mSharedPrefs.getInt(KEY_BACKGROUND_RES, mBackgroundResolution);
    }
  }
}
