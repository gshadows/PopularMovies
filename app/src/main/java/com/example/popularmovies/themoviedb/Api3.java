package com.example.popularmovies.themoviedb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;


/**
 * Simple TheMovieDB.com site API version 3 implementation.
 */
public class Api3 {
  private static final String TAG = Api3.class.getSimpleName();
  
  private static final String DEFAULT_BASE_ADDRESS = "https://api.themoviedb.org/3";
  
  private static final String REQ_MOVIES_POPULER   = "/movie/popular";
  private static final String REQ_MOVIES_TOP_RATED = "/movie/top_rated";
  private static final String REQ_MOVIE_DETAILS    = "/movie";
  
  private String mApiKey;
  private RequestQueue mRequestQueue = null;
  
  
  /**
   * Obtain Api3 singleton instance.
   * @param apiKey  API key that is required to use TheMovieDB API.
   * @param context Context.
   */
  public Api3 (@NonNull String apiKey, @NonNull Context context) {
    // Check arguments.
    if (isStringEmptyOrNull(apiKey)) throw new IllegalArgumentException("API key couldn't be empty");
    if (context == null) throw new IllegalArgumentException("Context is null");
    
    // Apply arguments.
    mApiKey = apiKey;
    
    // Prepare Volley.
    if (mRequestQueue == null) {
      //TODO: Volley not accessible in unit tests :( Comment next line to run tests. Move to separate class?
      mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }
  }
  
  
  @SuppressLint("DefaultLocale")
  public String getURL_PopularMovies (int page) {
    if ((page <= 0) || (page > 1000)) throw new IllegalArgumentException("Page outside 1..1000");
    return String.format ("%s%s?api_key=%s&page=%d", DEFAULT_BASE_ADDRESS, REQ_MOVIES_POPULER, mApiKey, page);
  }
  @SuppressLint("DefaultLocale")
  public String getURL_TopRatedMovies (int page) {
    if ((page <= 0) || (page > 1000)) throw new IllegalArgumentException("Page outside 1..1000");
    return String.format ("%s%s?api_key=%s&page=%d", DEFAULT_BASE_ADDRESS, REQ_MOVIES_TOP_RATED, mApiKey, page);
  }
  @SuppressLint("DefaultLocale")
  public String getURL_MovieDetails (int movieId) {
    return String.format ("%s%s/%d?api_key=%s", DEFAULT_BASE_ADDRESS, REQ_MOVIE_DETAILS, movieId, mApiKey);
  }
  
  
  public Request<TmdbMoviesPage> requirePopularMovies (int page, Response.Listener<TmdbMoviesPage> listener, Response.ErrorListener errorListener) {
    return mRequestQueue.add(new GsonRequest<> (getURL_PopularMovies(page), TmdbMoviesPage.class, listener, errorListener));
  }
  public Request<TmdbMoviesPage> requireTopRatedMovies (int page, Response.Listener<TmdbMoviesPage> listener, Response.ErrorListener errorListener) {
    return mRequestQueue.add(new GsonRequest<> (getURL_TopRatedMovies(page), TmdbMoviesPage.class, listener, errorListener));
  }


  /**
   * I use this method instead of TextUtils.isEmpty() because TextUtils is not accessible in local tests.
   * @param str String to check.
   * @return True if string is null or empty.
   */
  private static boolean isStringEmptyOrNull (final String str) {
    return (str == null) || str.isEmpty();
  }

}
