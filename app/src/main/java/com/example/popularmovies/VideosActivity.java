package com.example.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.popularmovies.themoviedb.Api3;
import com.example.popularmovies.themoviedb.TmdbVideosPage;


public class VideosActivity extends AppCompatActivity
  implements Response.ErrorListener, Response.Listener<TmdbVideosPage>, VideosAdapter.OnClickListener {
    
    private static final String TAG = VideosActivity.class.getSimpleName();
    
    public static final String EXTRA_MOVIE_TITLE = "movie_title";
    public static final String EXTRA_MOVIE_ID    = "movie_id";
    
    private Api3 api3;
    
    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRL;
    
    private String mMovieTitle;
    private int mMovieId = 1;
    private final int mCurrentPage = 1; // In future could be increased in some way.
    
    private Request<TmdbVideosPage> mPageRequest = null;
    private VideosAdapter mAdapter;
    
    
    @Override
    protected void onCreate (Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_with_recyclerview);
      setTitle(getString(R.string.videos));
      
      // Obtain movie ID etc.
      if (!readIntentExtras()) {
        finish();
        return;
      }
      
      // Find views.
      mRecyclerView = findViewById(R.id.main_recyclerview);
      mSwipeRL = findViewById(R.id.main_swipe_layout);
      
      // Setup RecyclerView.
      mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
      mAdapter = new VideosAdapter(this, this);
      mRecyclerView.setAdapter(mAdapter);
      mRecyclerView.setHasFixedSize(false); // Some video descriptions could be longer then one line.
      
      // Begin network request.
      api3 = new Api3(Secrets.THEMOVIEDB_API_KEY, this);
      requireVideos();
      
      // Setup "swipe to refresh".
      mSwipeRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {@Override public void onRefresh () {
        requireVideos();
      }});
    }
    
    
    @Override
    protected void onStop () {
      super.onStop();
      if (mPageRequest != null) mPageRequest.cancel(); // Stop fetching movies list from the network.
    }
    
    
    /**
     * Attempt to obtain activity parameters from the intent.
     * @return True on success, false otherwise.
     */
    private boolean readIntentExtras() {
      Intent intent = getIntent();
      if (intent == null) {
        Log.e(TAG, "No intent passed");
        return false;
      }
      // Get movie ID.
      mMovieId = intent.getIntExtra(EXTRA_MOVIE_ID, -1);
      if (mMovieId == -1) {
        Log.e(TAG, "Bad movie ID intent extra");
        return false;
      }
      // Get movie title.
      mMovieTitle = intent.getStringExtra(EXTRA_MOVIE_TITLE);
      if (mMovieTitle == null) {
        Log.e(TAG, "Bad movie name intent extra");
        return false;
      }
      return true;
    }
    
    
    private void requireVideos() {
      mPageRequest =  api3.requireMovieVideos(mMovieId, mCurrentPage, this, this);
      mSwipeRL.setRefreshing(true);
    }
    
    
    /**
     * Called if some error occurred while executing network request.
     * @param error
     */
    @Override
    public void onErrorResponse (final VolleyError error) {
      mSwipeRL.setRefreshing(false);
      Log.d(TAG, "onErrorResponse(): " + error.getMessage());
      
      Snackbar.make(mRecyclerView, R.string.fail_get_movies_list, Snackbar.LENGTH_LONG)
        .setAction(R.string.refresh_big, new View.OnClickListener() { @Override public void onClick (View v) {
          requireVideos();
        }})
        .show();
    }
    
    
    /**
     * Called if TMDb network request succeeds.
     * @param response
     */
    @Override
    public void onResponse (final TmdbVideosPage response) {
      mSwipeRL.setRefreshing(false);
      mAdapter.setVideos(response.results);
      mRecyclerView.setVisibility(View.VISIBLE);
    }
    
    
    /**
     * Called when RecyclerView item was clicked.
     * @param item Clicked item number.
     */
    @Override
    public void onClick (int item) {
      String url = mAdapter.getVideoURL(item);
      if (url == null) {
        
      }
      Intent intent = new Intent(Intent.ACTION_VIEW);
      intent.setData(Uri.parse(url));
      if (intent.resolveActivity(getPackageManager()) != null) {
        startActivity(intent);
      }
    }
}
