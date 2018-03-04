package com.example.popularmovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import com.example.popularmovies.themoviedb.Api3;
import com.example.popularmovies.themoviedb.TmdbMoviesPage;
import com.example.popularmovies.utils.Options;


public class MainActivity extends AppCompatActivity
  implements Response.ErrorListener, Response.Listener<TmdbMoviesPage>, MoviesListAdapter.OnClickListener {
  
  private static final String TAG = MainActivity.class.getSimpleName();
  
  private Api3 api3;
  
  private RecyclerView mRecyclerView;
  private ProgressBar mProgressBar;
  
  private final int mCurrentPage = 1; // In future could be increased in some way.
  
  private Request<TmdbMoviesPage> mPageRequest;
  private MoviesListAdapter mAdapter;
  
  
  @Override
  protected void onCreate (final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    
    // Detect screen orientation to decide on columns count.
    boolean isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    
    // Find views.
    mRecyclerView = findViewById(R.id.movies_rv);
    mProgressBar = findViewById(R.id.movies_pb);
    
    // Setup RecyclerView.
    GridLayoutManager layman = new GridLayoutManager(this, isLandscape ? 3 : 2);
    mRecyclerView.setLayoutManager(layman);
    mAdapter = new MoviesListAdapter(this, this);
    mRecyclerView.setAdapter(mAdapter);
    
    api3 = new Api3(Secrets.THEMOVIEDB_API_KEY, this);
    requireMovies();
  }
  
  
  private void requireMovies() {
    if (Options.getInstance(this).isPopularDisplayed()) {
      mPageRequest = api3.requirePopularMovies(mCurrentPage, this, this);
    } else {
      mPageRequest =  api3.requireTopRatedMovies(mCurrentPage, this, this);
    }
  }


  @Override
  protected void onStop () {
    super.onStop();
    mPageRequest.cancel(); // Stop fetching movies list from the network.
  }
  
  
  private MenuItem mMenuItemPopular;
  private MenuItem mMenuItemTopRated;
  
  @Override
  public boolean onCreateOptionsMenu (final Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    boolean bIsPopular = Options.getInstance(this).isPopularDisplayed();
    mMenuItemPopular = menu.findItem(R.id.menu_popular).setChecked(bIsPopular);
    mMenuItemTopRated = menu.findItem(R.id.menu_top_rated).setChecked(!bIsPopular);
    return true;
  }


  @Override
  public boolean onOptionsItemSelected (final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_about:
        startActivity(new Intent(this, AboutActivity.class));
        return true;
      case R.id.menu_popular:
        item.setChecked(true);
        mMenuItemTopRated.setChecked(false);
        Options.getInstance(this).setPopularDisplayed(true);
        return true;
      case R.id.menu_top_rated:
        item.setChecked(true);
        mMenuItemPopular.setChecked(false);
        Options.getInstance(this).setPopularDisplayed(false);
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
    Log.d(TAG, "onErrorResponse(): " + error.getMessage());
    Toast.makeText(this, getString(R.string.fail_get_movies_list), Toast.LENGTH_SHORT).show();
  }

  
  /**
   * Called if TMDb network request succeeds.
   * @param response 
   */
  @Override
  public void onResponse (final TmdbMoviesPage response) {
    Log.d(TAG, "onResponse()");
    Toast.makeText(this, "onResponse()", Toast.LENGTH_SHORT).show();
    
    mProgressBar.setVisibility(View.INVISIBLE);
    mAdapter.setMovies(response.results, null); // TODO: In stage 2 favorites should be read from DB.
    mRecyclerView.setVisibility(View.VISIBLE);
    
    // Debug only!
    String res = "Page: " + response.page + "/" + response.total_pages + "\n"
      + "Results: " + response.results.length + "/" + response.total_results + "\n"
      + "First: " + ((response.results.length > 0) ? response.results[0].title : "-");
    Log.d(TAG, res);
  }


  /**
   * Called when RecyclerView item was clicked.
   * @param item Clicked item number.
   */
  @Override
  public void onClickItem (int item) {
    startActivity(new Intent(this, DetailsActivity.class));
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
