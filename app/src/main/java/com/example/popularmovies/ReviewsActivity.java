package com.example.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.popularmovies.themoviedb.Api3;
import com.example.popularmovies.themoviedb.TmdbReview;
import com.example.popularmovies.themoviedb.TmdbReviewsPage;
import com.example.popularmovies.utils.Options;

import static android.support.v7.widget.RecyclerView.NO_POSITION;


public class ReviewsActivity extends AppCompatActivity
  implements Response.ErrorListener, Response.Listener<TmdbReviewsPage>, ReviewsAdapter.OnClickListener {
  
  private static final String TAG = Options.XTAG + ReviewsActivity.class.getSimpleName();
  
  // Saved instance state keys.
  private static final String SAVED_KEY_POSITION = "pos";
  
  // Intent extras keys.
  public static final String EXTRA_MOVIE_TITLE = "movie_title";
  public static final String EXTRA_MOVIE_ID    = "movie_id";
  
  private Api3 api3;
  
  private RecyclerView mRecyclerView;
  private SwipeRefreshLayout mSwipeRL;
  
  private String mMovieTitle;
  private int mMovieId = 1;
  private int savedPosition = NO_POSITION;  // Saved RecyclerView's position.
  private final int mCurrentPage = 1;       // In future could be increased in some way.
  
  private Request<TmdbReviewsPage> mPageRequest = null;
  private ReviewsAdapter mAdapter;
  
  
  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_with_recyclerview);
    setTitle(getString(R.string.reviews));
    
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
    mAdapter = new ReviewsAdapter(this, this);
    mRecyclerView.setAdapter(mAdapter);
    mRecyclerView.setHasFixedSize(false); // Reviews are not guarantied to be same size (because my trimming technique is too simple).
    if (savedInstanceState != null) savedPosition = savedInstanceState.getInt(SAVED_KEY_POSITION, NO_POSITION);
  
    // Begin network request.
    api3 = new Api3(Secrets.THEMOVIEDB_API_KEY, this);
    requireReviews();
  
    // Setup "swipe to refresh".
    mSwipeRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {@Override public void onRefresh () {
      requireReviews();
    }});
  }
  
  
  private int getRecyclerViewPosition() {
    LinearLayoutManager lm = (LinearLayoutManager)mRecyclerView.getLayoutManager();
    int pos = lm.findFirstCompletelyVisibleItemPosition();
    if (pos == NO_POSITION) pos = lm.findFirstVisibleItemPosition(); // If all items partially invisible.
    return pos;
  }
  
  
  @Override
  protected void onSaveInstanceState (Bundle outState) {
    super.onSaveInstanceState(outState);
    if (savedPosition != NO_POSITION) savedPosition = getRecyclerViewPosition();
    outState.putInt(SAVED_KEY_POSITION, savedPosition);
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
  
  
  private void requireReviews() {
    mRecyclerView.setVisibility(View.INVISIBLE);
    mPageRequest =  api3.requireMovieReviews(mMovieId, mCurrentPage, this, this);
    mSwipeRL.setRefreshing(true);
  }
  
  
  /**
   * Called if some error occurred while executing network request.
   * @param error
   */
  @Override
  public void onErrorResponse (final VolleyError error) {
    mSwipeRL.setRefreshing(false);
    Log.w(TAG, "onErrorResponse(): " + error.getMessage());
    
    Snackbar.make(mRecyclerView, R.string.fail_get_movies_list, Snackbar.LENGTH_LONG)
      .setAction(R.string.refresh_big, new View.OnClickListener() { @Override public void onClick (View v) {
        requireReviews();
      }})
      .show();
  }
  
  
  /**
   * Called if TMDb network request succeeds.
   * @param response
   */
  @Override
  public void onResponse (final TmdbReviewsPage response) {
    mSwipeRL.setRefreshing(false);
    mAdapter.setReviews(response.results);
    mRecyclerView.setVisibility(View.VISIBLE);
    if (savedPosition == NO_POSITION) savedPosition = 0;
    mRecyclerView.scrollToPosition(savedPosition);
  }
  
  
  /**
   * Called when RecyclerView item was clicked.
   * @param item Clicked item number.
   */
  @Override
  public void onClick (int item) {
    Intent intent = new Intent(this, ReadReviewActivity.class);
    TmdbReview review = mAdapter.getReview(item);
    intent.putExtra(ReadReviewActivity.EXTRA_TITLE,  mMovieTitle);
    intent.putExtra(ReadReviewActivity.EXTRA_AUTHOR, review.author);
    intent.putExtra(ReadReviewActivity.EXTRA_REVIEW, review.content);
    startActivity(intent);
  }
}
