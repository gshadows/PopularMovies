package com.example.popularmovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.popularmovies.db.SavedMovieInfo;
import com.example.popularmovies.themoviedb.Api3;
import com.example.popularmovies.themoviedb.TmdbMoviesPage;
import com.example.popularmovies.utils.Options;

import static android.support.v7.widget.RecyclerView.NO_POSITION;


public class MainActivity extends AppCompatActivity
  implements Response.ErrorListener, Response.Listener<TmdbMoviesPage>, MoviesListAdapter.OnClickListener {
  
  private static final String TAG = MainActivity.class.getSimpleName();
  
  // Saved instance state keys.
  private static final String SAVED_KEY_POSITION = "pos";
  
  private Api3 api3;
  
  private RecyclerView mRecyclerView;
  private SwipeRefreshLayout mSwipeRL;
  
  private int savedPosition = NO_POSITION;  // Saved RecyclerView's position.
  private final int mCurrentPage = 1;       // In future could be increased in some way.
  
  private Request<TmdbMoviesPage> mPageRequest = null;
  private MoviesListAdapter mAdapter;
  
  
  @Override
  protected void onCreate (final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_with_recyclerview);
    
    // Detect screen orientation to decide on columns count.
    boolean isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    
    // Find views.
    mRecyclerView = findViewById(R.id.main_recyclerview);
    mSwipeRL = findViewById(R.id.main_swipe_layout);
    
    // Setup RecyclerView.
    GridLayoutManager layman = new GridLayoutManager(this, isLandscape ? 4 : 2);
    mRecyclerView.setLayoutManager(layman);
    mAdapter = new MoviesListAdapter(this, this);
    mRecyclerView.setAdapter(mAdapter);
    mRecyclerView.setHasFixedSize(true); // All posters assumed to be same size.
    if (savedInstanceState != null) savedPosition = savedInstanceState.getInt(SAVED_KEY_POSITION, NO_POSITION);
    
    // Change activity title.
    Options.CurrentTab currentTab = Options.getInstance(this).getCurrentTab();
    setDynamicTitle(currentTab);
    
    // Begin network request.
    api3 = new Api3(Secrets.THEMOVIEDB_API_KEY, this);
    requireMovies();
    
    // Setup "swipe to refresh".
    mSwipeRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {@Override public void onRefresh () {
      requireMovies();  
    }});
  }
  
  
  private int getRecyclerViewPosition() {
    GridLayoutManager lm = (GridLayoutManager)mRecyclerView.getLayoutManager();
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


  private void requireMovies() {
    switch (Options.getInstance(MainActivity.this).getCurrentTab()) {
      case FAVORITES:
        // TODO: Favorite movies support.
        break;
      case POPULAR:
        mPageRequest = api3.requirePopularMovies(mCurrentPage, this, this);
        break;
      case TOP_RATED:
        mPageRequest =  api3.requireTopRatedMovies(mCurrentPage, this, this);
        break;
    }
    mSwipeRL.setRefreshing(true);
  }


  @Override
  public boolean onCreateOptionsMenu (final Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }


  /**
   * Set activity title based on sort order: Popular or Top Rated movies.
   */
  private void setDynamicTitle (Options.CurrentTab currentTab) {
    setTitle(currentTab.toTranslatableString(this));
  }


  @Override
  public boolean onOptionsItemSelected (final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_about:
        startActivity(new Intent(this, AboutActivity.class));
        return true;
      case R.id.menu_popular:
        savedPosition = 0; // Force reset position to zero.
        Options.getInstance(this).setCurrentTab(Options.CurrentTab.POPULAR);
        setDynamicTitle(Options.CurrentTab.POPULAR);
        requireMovies();
        return true;
      case R.id.menu_top_rated:
        savedPosition = 0; // Force reset position to zero.
        Options.getInstance(this).setCurrentTab(Options.CurrentTab.TOP_RATED);
        setDynamicTitle(Options.CurrentTab.TOP_RATED);
        requireMovies();
        return true;
    }
    return super.onContextItemSelected(item);
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
        requireMovies();
      }})
      .show();
  }

  
  /**
   * Called if TMDb network request succeeds.
   * @param response 
   */
  @Override
  public void onResponse (final TmdbMoviesPage response) {
    mSwipeRL.setRefreshing(false);
    mAdapter.setMovies(response.results, null); // TODO: In stage 2 favorites should be read from DB.
    mRecyclerView.setVisibility(View.VISIBLE);
    if (savedPosition == NO_POSITION) savedPosition = 0;
    mRecyclerView.scrollToPosition(savedPosition);
  }


  /**
   * Called when RecyclerView item was clicked.
   * @param item Clicked item number.
   */
  @Override
  public void onClickItem (int item) {
    Intent intent = new Intent(this, DetailsActivity.class);
    intent.putExtra(DetailsActivity.EXTRA_MOVIE, new SavedMovieInfo(mAdapter.getMovie(item)));
    intent.putExtra(DetailsActivity.EXTRA_IS_FAVORITE, mAdapter.isFavorite(item));
    Log.d(TAG, "onClickItem(): " + item + " - fav is " + mAdapter.isFavorite(item));
    startActivity(intent);
  }
  
  /**
   * Called when RecyclerView item's star (favorites mark) was clicked.
   * @param item Clicked item number.
   */
  @Override
  public void onClickStar (int item) {
    mAdapter.switchFavorite(item);
  }
}
