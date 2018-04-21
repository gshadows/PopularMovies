package com.example.popularmovies.themoviedb;

import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.example.popularmovies.utils.Options;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.UnsupportedEncodingException;


/**
 * Volley JSON request with embedded GSON response parsing.
 * @param <T> Response class.
 */
public class GsonRequest<T> extends Request<T> {
  private static final String TAG = Options.XTAG + GsonRequest.class.getSimpleName();

  private final Gson mGson;
  
  private final Class<T> mRequestClass;
  private final Response.Listener<T> mListener;
  

  /**
   * Full constructor with all possible parameters.
   * @param method        Request execution method: {@link Method}
   * @param url           Request URL string.
   * @param cl            Response class to be instantiated and returned by this request.
   * @param listener      Response listener {@link Response.Listener<T>}. Called on succesfull request completion to return response object.
   * @param errorListener Error listener {@link Response.ErrorListener}. Called if network request fails.
   */
  public GsonRequest (int method, String url, Class<T> cl, Response.Listener<T> listener, Response.ErrorListener errorListener) {
    super(method, url, errorListener);
    mGson = new Gson();
    mListener = listener;
    mRequestClass = cl;
  }


  /**
   * Simplified constructor with only mandatory parameters.
   * @param url           Request URL string.
   * @param cl            Response class to be instantiated and returned by this request.
   * @param listener      Response listener {@link Response.Listener<T>}. Called on succesfull request completion to return response object.
   * @param errorListener Error listener {@link Response.ErrorListener}. Called if network request fails.
   */
  public GsonRequest (String url, Class<T> cl, Response.Listener<T> listener, Response.ErrorListener errorListener) {
    this(Method.GET, url, cl, listener, errorListener);
  }


  // This called on worker thread.
  @Override protected Response parseNetworkResponse (NetworkResponse response) {
    try {
      String json = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
      // This should call deliverResponse() on the UI thread.
      return Response.success(parseJson(json, mRequestClass), HttpHeaderParser.parseCacheHeaders(response));
    }
    catch (UnsupportedEncodingException ex) {
      Log.w(TAG, "parseNetworkResponse() unsupported encoding exception");
      // This should call deliverError() on the UI thread.
      return Response.error(new ParseError(ex));
    }
  }
  
  
  // This called on UI thread.
  @Override protected void deliverResponse (T response) {
    mListener.onResponse(response);
  }
  
  
  // This called on UI thread.
  @Override public void deliverError (VolleyError error) {
    Log.w(TAG, "deliverError() - " + error.getMessage());
    getErrorListener().onErrorResponse(error);
  }
  
  
  // For future use.
  /*@Override
  public Map<String, String> getHeaders () throws AuthFailureError {
    return super.getHeaders();
  }*/


  /**
   * Parse JSON and catch exceptions inside.
   * @param json  JSON text.
   * @param cl    Class to instantiale by parsing JSON.
   * @return Parsed object of class cl or null.
   */
  @Nullable
  private Object parseJson (String json, Class cl) {
    try {
      return mGson.fromJson(json, cl);
    }
    catch (JsonSyntaxException ex) {
      Log.w (TAG, ex.getMessage());
      //Log.v (TAG, json);
      return null;
    }
  }

}
