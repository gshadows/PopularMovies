package com.example.popularmovies.themoviedb;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;


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
  private String mBaseURL;
  
  private Gson         mGson = null;
  private RequestQueue mRequestQueue = null;
  
  
  /**
   * Obtain Api3 singleton instance.
   * @param apiKey  API key that is required to use TheMovieDB API.
   * @param baseURL Base UAR for API request. Pass null to use default.
   * @param context Context.
   */
  public Api3 (@NonNull String apiKey, String baseURL, @NonNull Context context) {
    // Check arguments.
    if (TextUtils.isEmpty(apiKey)) throw new IllegalArgumentException("API key couldn't be empty");
    //TODO: TextUtils not accessible in unit tests :( Comment previous line and uncomment next to run tests.
    //if ((apiKey == null) || apiKey.isEmpty()) throw new IllegalArgumentException("API key couldn't be empty");
    if (context == null) throw new IllegalArgumentException("Context is null");

    // Apply arguments.
    mApiKey = apiKey;
    mBaseURL = (TextUtils.isEmpty(baseURL)) ? DEFAULT_BASE_ADDRESS : baseURL;
    //TODO: TextUtils not accessible in unit tests :( Comment previous line and uncomment next to run tests.
    //mBaseURL = ((baseURL == null) || baseURL.isEmpty()) ? DEFAULT_BASE_ADDRESS : baseURL;
    
    // Prepare GSON.
    if (mGson == null) {
      GsonBuilder mGsonBuilder = new GsonBuilder();
      mGson = mGsonBuilder.create();
    }
    
    // Prepare Volley.
    if (mRequestQueue == null) {
      //TODO: Volley not accessible in unit tests :( Comment next line to run tests.
      //mRequestQueue = Volley.newRequestQueue(context.getApplicationContext());
    }
  }
  
  
  @SuppressLint("DefaultLocale")
  public String getURL_PopularMovies (int page) {
    if ((page <= 0) || (page > 1000)) throw new IllegalArgumentException("Page outside 1..1000");
    return String.format ("%s%s?api_key=%s&page=%d", mBaseURL, REQ_MOVIES_POPULER, mApiKey, page);
  }
  @SuppressLint("DefaultLocale")
  public String getURL_TopRatedMovies (int page) {
    if ((page <= 0) || (page > 1000)) throw new IllegalArgumentException("Page outside 1..1000");
    return String.format ("%s%s?api_key=%s&page=%d", mBaseURL, REQ_MOVIES_TOP_RATED, mApiKey, page);
  }
  @SuppressLint("DefaultLocale")
  public String getURL_MovieDetails (int movieId) {
    return String.format ("%s%s/%d?api_key=%s", mBaseURL, REQ_MOVIE_DETAILS, movieId, mApiKey);
  }
  
  
  public void requirePopularMovies (int page, Response.Listener listener, Response.ErrorListener errorListener) {
    mRequestQueue.add(new GsonRequest<TmdbMoviesPage> (getURL_PopularMovies(page), TmdbMoviesPage.class, listener, errorListener));
  }
  public void requireTopRatedMovies (int page, Response.Listener listener, Response.ErrorListener errorListener) {
    mRequestQueue.add(new GsonRequest<TmdbMoviesPage> (getURL_TopRatedMovies(page), TmdbMoviesPage.class, listener, errorListener));
  }


  /**
   * Parse JSON and catch exceptions inside.
   * @param json JSON text.
   * @param cl Class which object to return.
   * @return Parsed object or null.
   */
  @Nullable private Object parseJson (String json, Class cl) {
    try {
      return mGson.fromJson(json, TmdbMovieShort.class);
    }
    catch (JsonSyntaxException ex) {
      Log.d (TAG, ex.getMessage());
      Log.v (TAG, json);
      return null;
    }
  }
  
  
  private class GsonRequest<T extends TmdbBase> extends Request<T> {
    
    private final Class<T> mRequestClass;
    private final Response.Listener<T> mListener;
    private final Response.ErrorListener mErrorListener;
    
    public GsonRequest (String url, Class<T> cl, Response.Listener<T> listener, Response.ErrorListener errorListener) {
      super(Method.GET, url, errorListener);
      mListener = listener;
      mErrorListener = errorListener;
      mRequestClass = cl;
    }

    // This called on worker thread.
    @Override protected Response parseNetworkResponse (NetworkResponse response) {
      try {
        String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        Log.d(TAG, "parseNetworkResponse() success - " + Thread.currentThread().getName());
        // This should call deliverResponse() on the UI thread.
        return Response.success(parseJson(json, mRequestClass), HttpHeaderParser.parseCacheHeaders(response));
      }
      catch (UnsupportedEncodingException ex) {
        Log.d(TAG, "parseNetworkResponse() unsupported encoding exception - " + Thread.currentThread().getName());
        // This should call deliverError() on the UI thread.
        return Response.error(new ParseError(ex));
      }
    }
    
    // This called on UI thread.
    @Override protected void deliverResponse (T response) {
      Log.d(TAG, "deliverResponse() - " + Thread.currentThread().getName());
      mListener.onResponse(response);
    }
    
    // This called on UI thread.
    @Override public void deliverError (VolleyError error) {
      Log.d(TAG, "deliverError() - " + Thread.currentThread().getName() + " -> " + error.getMessage());
      mErrorListener.onErrorResponse(error);
    }
    
  }


}
