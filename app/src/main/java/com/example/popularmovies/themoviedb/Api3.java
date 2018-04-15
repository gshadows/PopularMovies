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
  //private static final String TAG = Options.XTAG + Api3.class.getSimpleName();
  
  private static final String DEFAULT_BASE_ADDRESS = "https://api.themoviedb.org/3";
  private static final String DEFAULT_IMAGES_PATH  = "http://image.tmdb.org/t/p/w";
  
  private static final String REQ_MOVIES_POPULER   = "/movie/popular";
  private static final String REQ_MOVIES_TOP_RATED = "/movie/top_rated";
  private static final String REQ_MOVIE_DETAILS    = "/movie";
  
  private static final String SUB_MOVIE_REVIEWS    = "/reviews";
  private static final String SUB_MOVIE_VIDEOS     = "/videos";
  
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
      //TODO: Volley not accessible in unit tests :( Comment next line to run tests. Need to read more about testing.
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
  @SuppressLint("DefaultLocale")
  public String getURL_MovieReviews (int movieId, int page) {
    return String.format ("%s%s/%d%s?api_key=%s&page=%d", DEFAULT_BASE_ADDRESS, REQ_MOVIE_DETAILS, movieId, SUB_MOVIE_REVIEWS, mApiKey, page);
  }
  @SuppressLint("DefaultLocale")
  public String getURL_MovieVideos (int movieId, int page) {
    return String.format ("%s%s/%d%s?api_key=%s&page=%d", DEFAULT_BASE_ADDRESS, REQ_MOVIE_DETAILS, movieId, SUB_MOVIE_VIDEOS, mApiKey, page);
  }
  
  
  public Request<TmdbMoviesPage> requirePopularMovies (int page, Response.Listener<TmdbMoviesPage> listener, Response.ErrorListener errorListener) {
    return mRequestQueue.add(new GsonRequest<> (getURL_PopularMovies(page), TmdbMoviesPage.class, listener, errorListener));
  }
  public Request<TmdbMoviesPage> requireTopRatedMovies (int page, Response.Listener<TmdbMoviesPage> listener, Response.ErrorListener errorListener) {
    return mRequestQueue.add(new GsonRequest<> (getURL_TopRatedMovies(page), TmdbMoviesPage.class, listener, errorListener));
  }
  public Request<TmdbMovieDetails> requireMovieDetails (int movieId, Response.Listener<TmdbMovieDetails> listener, Response.ErrorListener errorListener) {
    return mRequestQueue.add(new GsonRequest<> (getURL_MovieDetails(movieId), TmdbMovieDetails.class, listener, errorListener));
  }
  public Request<TmdbReviewsPage> requireMovieReviews (int movieId, int page, Response.Listener<TmdbReviewsPage> listener, Response.ErrorListener errorListener) {
    return mRequestQueue.add(new GsonRequest<> (getURL_MovieReviews(movieId, page), TmdbReviewsPage.class, listener, errorListener));
  }
  public Request<TmdbVideosPage> requireMovieVideos (int movieId, int page, Response.Listener<TmdbVideosPage> listener, Response.ErrorListener errorListener) {
    return mRequestQueue.add(new GsonRequest<> (getURL_MovieVideos(movieId, page), TmdbVideosPage.class, listener, errorListener));
  }


  /**
   * Construct image URL for specified image name.
   * This URL valid both for posters (poster_path) and backgrounds (backdrop_path).
   * @param imageName Image name with from poster_path, backdrop_path or other source.
   * @param width Desired image width (use only predefined widths!).
   * @return Constructed image URL.
   */
  public static String getImageURL (String imageName, int width) {
    return String.format("%s%d/%s", DEFAULT_IMAGES_PATH, width, imageName);
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
