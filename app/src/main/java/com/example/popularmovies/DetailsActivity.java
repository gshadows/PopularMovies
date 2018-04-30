package com.example.popularmovies;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.popularmovies.db.SavedMovieInfo;
import com.example.popularmovies.themoviedb.Api3;
import com.example.popularmovies.themoviedb.TmdbMovieDetails;
import com.example.popularmovies.themoviedb.TmdbReviewsPage;
import com.example.popularmovies.themoviedb.TmdbVideosPage;
import com.example.popularmovies.utils.Options;
import com.squareup.picasso.Picasso;

import lombok.Getter;

import static android.support.v7.widget.RecyclerView.NO_POSITION;


public class DetailsActivity extends AppCompatActivity
  implements Response.ErrorListener,  // Network request failure.
  TabLayout.OnTabSelectedListener,    // Movie information tab (page) switch.
  VideosAdapter.OnClickListener,      // Videos RV item click.
  ReviewsAdapter.OnClickListener      // Reviews RV item click.
{

  public static final String TAG = Options.XTAG + DetailsActivity.class.getSimpleName();
  
  // Saved instance state keys.
  private static final String SAVED_KEY_POSITION    = "pos";
  private static final String SAVED_KEY_CURRENT_TAB = "cur_tab";
  
  // Intent extras keys.
  public static final String EXTRA_MOVIE       = "movie";
  public static final String EXTRA_IS_FAVORITE = "is_fav";
  
  
  /**
   * Details activity pages description enumeration.
   */
  public enum Page {
    DESCRIPTION (R.string.summary, R.id.description_page ),
    REVIEWS     (R.string.reviews, R.id.main_recyclerview),
    VIDEOS      (R.string.videos,  R.id.main_recyclerview);
    
    @Getter @StringRes private final int titleId;
    @Getter @IdRes     private final int mainViewId;
    Page (@StringRes int titleId, @IdRes int mainViewId) { this.titleId = titleId; this.mainViewId = mainViewId; }
  }
  
  private SavedMovieInfo mMovieInfo;
  private boolean mIsFavorite;
  
  private Api3 api3;
  private Request<TmdbMovieDetails> mDetailsRequest = null;
  private Request<TmdbReviewsPage>  mReviewsRequest = null;
  private Request<TmdbVideosPage>   mVideosRequest  = null;
  
  // Views.
  TabLayout     mTabLayout;
  View          mPageDescription;
  RecyclerView  mRecyclerView;
  TextView      mTitleTV, mDescriptionTV, mYearTV, mLengthTV, mRateTV;
  ImageView     mPosterIV, mBackgroundIV, mStarIV;
  SwipeRefreshLayout mSwipeRL;
  
  private final int mCurrentReviewsPage = 1;
  private final int mCurrentVideosPage  = 1;
  
  private Page mCurrentTab = Page.DESCRIPTION;
  private boolean mIsLandscape;
  private int mSavedPosition = NO_POSITION;  // Saved RecyclerView's position.
  
  ReviewsAdapter mReviewsAdapter;
  VideosAdapter  mVideosAdapter;
  
  
  /**
   * This method guaranties requested view existance or throws an exception.
   * @param id  the ID to search for.
   * @return a view with given ID if found, or null otherwise.
   */
  private @NonNull <T extends View> T findViewByIdOrDie (@IdRes int id) {
    View view = findViewById(id);
    if (view == null) {
      String error = "Activity layout missed view id " + id;
      Log.e(TAG, error);
      throw new RuntimeException(error); // It's ok to kill an app, I think. This will happen on developer's machine.
    }
    return (T)view;
  }
  

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_details);
  
    // Read base information from the Intent.
    if (!readIntentMovieExtra()) {
      // Useless to open details without movie.
      finish();
      return;
    }
  
    // Detect screen orientation to decide on columns count.
    mIsLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
  
    // Find all views. Can't use DataBinding because landscape layout has different structure.
    mTabLayout        = findViewByIdOrDie(R.id.details_tabs);
    mPageDescription  = findViewByIdOrDie(R.id.description_page);
    mRecyclerView     = findViewByIdOrDie(R.id.main_recyclerview);
    mTitleTV          = findViewByIdOrDie(R.id.title_tv);
    mDescriptionTV    = findViewByIdOrDie(R.id.description_tv);
    mYearTV           = findViewByIdOrDie(R.id.year_tv);
    mLengthTV         = findViewByIdOrDie(R.id.length_tv);
    mRateTV           = findViewByIdOrDie(R.id.rate_tv);
    mPosterIV         = findViewByIdOrDie(R.id.poster_iv);
    mBackgroundIV     = findViewByIdOrDie(R.id.back_image_iv);
    mStarIV           = findViewByIdOrDie(R.id.star_button);
    mSwipeRL          = findViewByIdOrDie(R.id.details_swipe_layout);
    
    // Display base information read from the Intent.
    showMoveInfo();
  
    // Restore saved activity state.
    if (savedInstanceState != null) {
      // RecyclerView position.
      mSavedPosition = savedInstanceState.getInt(SAVED_KEY_POSITION, NO_POSITION);
      // Displayed tab.
      int tabNumber = savedInstanceState.getInt(SAVED_KEY_CURRENT_TAB, 0);
      if ((tabNumber < 0) || (tabNumber >= Page.values().length)) tabNumber = 0;
      mCurrentTab = Page.values()[tabNumber];
      Log.d(TAG, String.format("onCreate() restore state: rv_pos = %d, tab = %d", mSavedPosition, mCurrentTab.ordinal()));
    }
    
    // RecyclerView setup.
    mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    mVideosAdapter = new VideosAdapter(this, this);
    mReviewsAdapter = new ReviewsAdapter(this, this);
    mRecyclerView.setHasFixedSize(false); // Some video descriptions could be longer then one line.
  
    // Begin network request for movie details.
    api3 = new Api3(Secrets.THEMOVIEDB_API_KEY, this);
    requireMovieDetails();
  
    // Setup tabs.
    mTabLayout.addTab(mTabLayout.newTab().setText(Page.DESCRIPTION.titleId));
    mTabLayout.addTab(mTabLayout.newTab().setText(Page.REVIEWS.titleId));
    mTabLayout.addTab(mTabLayout.newTab().setText(Page.VIDEOS.titleId));
    mTabLayout.addOnTabSelectedListener(this);
    mTabLayout.getTabAt(mCurrentTab.ordinal()).select();
    updateCurrentTab();
    
    // Favorite button star.
    updateStarButton();
    
    // Setup "swipe to refresh".
    mSwipeRL.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {@Override public void onRefresh () {
      requireMovieDetails();
    }});
  }
  
  
  private void updateCurrentTab() {
    if (mCurrentTab == Page.DESCRIPTION) {
      mRecyclerView.setVisibility(View.INVISIBLE);
      mPageDescription.setVisibility(View.VISIBLE);
    } else {
      mPageDescription.setVisibility(View.INVISIBLE);
      mRecyclerView.setAdapter ((mCurrentTab == Page.REVIEWS) ? mReviewsAdapter : mVideosAdapter);
      mRecyclerView.setVisibility(View.VISIBLE);
    }
  }
  
  
  @Override
  public void onTabSelected (TabLayout.Tab tab) {
    mCurrentTab = Page.values()[tab.getPosition()];
    Log.d(TAG, "onTabSelected() - " + mCurrentTab.ordinal());
    updateCurrentTab();
  }
  
  @Override public void onTabUnselected (TabLayout.Tab tab) {} // Unused.
  @Override public void onTabReselected (TabLayout.Tab tab) {} // Unused.
  
  
  private void updateStarButton() {
    mStarIV.setImageResource(mIsFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
  }


  @Override
  protected void onStop () {
    super.onStop();
    if (mDetailsRequest != null) mDetailsRequest.cancel(); // Stop fetching movie details from the network.
    if (mReviewsRequest != null) mReviewsRequest.cancel(); // Stop fetching movie reviews from the network.
    if (mVideosRequest  != null) mVideosRequest.cancel();  // Stop fetching movie videos  from the network.
  }
  
  
  private void requireMovieDetails() {
    mRecyclerView.setVisibility(View.INVISIBLE);
    
    // Require movie details.
    mDetailsRequest = api3.requireMovieDetails(mMovieInfo.id, new Response.Listener<TmdbMovieDetails>() {
      @Override public void onResponse (TmdbMovieDetails response) {
        Log.d(TAG,"onResponse() - details");
        mSwipeRL.setRefreshing(false);
        mMovieInfo.setDetails(response);
        showDetailedInfo();
      }
    }, this);
    
    // Require movie reviews.
    mReviewsRequest = api3.requireMovieReviews(mMovieInfo.id, mCurrentReviewsPage, new Response.Listener<TmdbReviewsPage>() {
      @Override public void onResponse (TmdbReviewsPage response) {
        Log.d(TAG,"onResponse() - reviews");
        mReviewsAdapter.setReviews(response.results);
      }
    }, this);
    
    // Require movie videos.
    mVideosRequest = api3.requireMovieVideos(mMovieInfo.id, mCurrentVideosPage, new Response.Listener<TmdbVideosPage>() {
      @Override public void onResponse (TmdbVideosPage response) {
        Log.d(TAG,"onResponse() - videos");
        mVideosAdapter.setVideos(response.results);
      }
    }, this);
    
    mSwipeRL.setRefreshing(true);
  }
  
  
  /**
   * Attempt to obtain activity parameters from the intent.
   * @return True on success, false otherwise.
   */
  private boolean readIntentMovieExtra() {
    Intent intent = getIntent();
    if (intent == null) {
      Log.e(TAG, "No intent passed");
      return false;
    }
    // Get parcelable movie short information object.
    mMovieInfo = intent.getParcelableExtra(EXTRA_MOVIE);
    if (mMovieInfo == null) {
      Log.e(TAG, "Bad parcelable passed");
      return false;
    }
    
    // get favorite flag.
    Object isFav = intent.getExtras().get(EXTRA_IS_FAVORITE);
    if ((isFav == null) || !(isFav instanceof Boolean)) {
      Log.e(TAG, "Bad favorite flag passed");
      return false;
    }
    mIsFavorite = (boolean)isFav;
    
    return true;
  }


  /**
   * Show minimal movie information that is accessible immediately from the intent.
   */
  private void showMoveInfo() {

    // Set title.
    if (mMovieInfo.title != null) {
      setTitle(mMovieInfo.title);
      mTitleTV.setText(mMovieInfo.title);
    }
    
    // Add to favorites button.
    //boolean isFavorite = favorites.contains(mMovieInfo.id);
    mStarIV.setPressed(mIsFavorite);
    mStarIV.setContentDescription(mIsFavorite ? getString(R.string.remove_from_fav) : getString(R.string.add_to_fav));
    
    // Start background image loading.
    if ((mMovieInfo.backdrop_path != null) && !mMovieInfo.backdrop_path.isEmpty()) {
      String imagePathBG = Api3.getImageURL(mMovieInfo.backdrop_path, Options.getInstance(this).getBackgroundResolution());
      Picasso.with(this)
        .load(imagePathBG)
        .into(mBackgroundIV, new com.squareup.picasso.Callback() {
          @Override public void onSuccess () {
            // When no background loaded (in case of network error or absence of image) empty
            // background looks ugly with a large gap between top of the sceen and a title.
            // This why I bind title to the top of screen initially and background image are invisible.
            // After successful loading I enable background and re-bind title to the bottom of it.
            // The only disadvantage is title "jumps" down if internet works good and fast. Assuming
            // success looks bad in case of slow internet (or none). Sorry for long comment :)
  
            // Enable background.
            mBackgroundIV.setVisibility(View.VISIBLE);
            if (!mIsLandscape) {
              // Re-bind title text to the bottom of background image. Only for portrait orientation.
              ConstraintLayout layout = findViewById(R.id.details_c_layout);
              ConstraintSet constraints = new ConstraintSet();
              constraints.clone(layout);
              constraints.connect(R.id.title_tv, ConstraintSet.BOTTOM, R.id.back_image_iv, ConstraintSet.BOTTOM);
              constraints.clear(R.id.title_tv, ConstraintSet.TOP);
              constraints.applyTo(layout);
            }
          }
  
          @Override public void onError () {} // When no background loaded title is bound to the top of screen.
        });
    }
    
    // Start poster image loading.
    if ((mMovieInfo.poster_path != null) && !mMovieInfo.poster_path.isEmpty()) {
      String imagePathPoster = Api3.getImageURL(mMovieInfo.poster_path, Options.getInstance(this).getPostersDetailsResolution());
      mPosterIV.setColorFilter(0);
      Picasso.with(this)
        .load(imagePathPoster)
        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
        .error(android.R.drawable.stat_notify_error)
        .into(mPosterIV, new com.squareup.picasso.Callback() {
          @Override public void onSuccess () { mPosterIV.setContentDescription(getString(R.string.poster_content_description)); }
          @Override public void onError () {
            mPosterIV.setColorFilter(Color.RED);
            mPosterIV.setContentDescription(getString(R.string.poster_error_content_description));
          }
        });
    }
    
    // Set release year.
    if (mMovieInfo.release_date != null) mYearTV.setText(getString(R.string.year) + mMovieInfo.release_date.substring(0, 4));
    
    // Set rate.
    mRateTV.setText(getString(R.string.rate) + String.valueOf(mMovieInfo.vote_average) + " / 10");
    
    // Set description.
    if (mMovieInfo.overview != null) mDescriptionTV.setText(mMovieInfo.overview);
    
    // Try to show details if it was passed from the DB.
    showDetailedInfo();
  }


  /**
   * Show detailed movie information that is generally received from network asyncronously.
   * However for favorite movies it could be available immediately from DB.
   */
  private void showDetailedInfo() {
    // Set movie length.
    if (mMovieInfo.runtime != null) mLengthTV.setText(getString(R.string.length) + mMovieInfo.runtime + getString(R.string.minutes_short));
  }
  
  
  /**
   * Called if some error occurred while executing network request.
   * @param error
   */
  @Override
  public void onErrorResponse (final VolleyError error) {
    mSwipeRL.setRefreshing(false);
    Log.w(TAG, "onErrorResponse(): " + error.getMessage());
    
    Snackbar.make(mTitleTV, R.string.fail_get_movie_info, Snackbar.LENGTH_LONG)
      .setAction(R.string.refresh_big, new View.OnClickListener() { @Override public void onClick (View v) {
        requireMovieDetails();
      }})
      .show();
  }


  /**
   * "Reviews" button click handler. Used to open user's movie reviews list.
   * @param view Unused.
   */
  public void onReviewsClick (View view) {
    Intent intent = new Intent(this, ReviewsActivity.class);
    intent.putExtra(ReviewsActivity.EXTRA_MOVIE_TITLE, mMovieInfo.title);
    intent.putExtra(ReviewsActivity.EXTRA_MOVIE_ID,    mMovieInfo.id);
    startActivity(intent);
  }
  
  
  /**
   * "Videos" button click handler. Used to open related videos list (trailers etc).
   * @param view Unused.
   */
  public void onVideosClick (View view) {
    Intent intent = new Intent(this, VideosActivity.class);
    intent.putExtra(VideosActivity.EXTRA_MOVIE_TITLE, mMovieInfo.title);
    intent.putExtra(VideosActivity.EXTRA_MOVIE_ID,    mMovieInfo.id);
    startActivity(intent);
  }
  
  
  /**
   * "Star" image button click handler. This is a favorite movie check mark.
   * @param view Unused.
   */
  public void onStarClick (View view) {
    MoviesListAdapter.setFavorite(this, mMovieInfo, mIsFavorite);
    mIsFavorite = !mIsFavorite; // Invert local favorite flag.
    updateStarButton();
  }
  
  
  /**
   * Poster image click handler. Used to show movie images view activity.
   * @param view Unused.
   */
  public void onPosterClick (View view) {
    // TODO: Add images view support.
  }
  
  
  @Override
  public void onVideoClick (int item) {
    
  }
  
  
  @Override
  public void onReviewClick (int item) {
    
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
    if (mSavedPosition != NO_POSITION) mSavedPosition = getRecyclerViewPosition();
    outState.putInt(SAVED_KEY_POSITION, mSavedPosition);
    outState.putInt(SAVED_KEY_CURRENT_TAB, mCurrentTab.ordinal());
    Log.d(TAG, String.format("onSaveInstanceState() rv_pos = %d, tab = %d", mSavedPosition, mCurrentTab.ordinal()));
  }
  
  
}
