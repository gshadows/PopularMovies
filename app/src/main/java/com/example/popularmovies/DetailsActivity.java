package com.example.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.example.popularmovies.databinding.ActivityDetailsBinding;
import com.example.popularmovies.db.SavedMovieInfo;
import com.example.popularmovies.themoviedb.Api3;
import com.example.popularmovies.themoviedb.TmdbMovieDetails;
import com.example.popularmovies.utils.Options;
import com.squareup.picasso.Picasso;


public class DetailsActivity extends AppCompatActivity
  implements Response.ErrorListener, Response.Listener<TmdbMovieDetails> {

  public static final String TAG = Options.XTAG + DetailsActivity.class.getSimpleName();
  
  public static final String EXTRA_MOVIE       = "movie";
  public static final String EXTRA_IS_FAVORITE = "is_fav";
  
  private ActivityDetailsBinding mBinding;
  private SavedMovieInfo mMovieInfo;
  //private Set<Integer> favorites = new HashSet<>(); // Will be used if details activity allows swipe to multiple movies.
  private boolean mIsFavorite;
  
  private Api3 api3;
  private Request<TmdbMovieDetails> mDetailsRequest = null;
  
  private boolean mIsLandscape;
  

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mBinding = DataBindingUtil.setContentView(this, R.layout.activity_details);

    // Detect screen orientation to decide on columns count.
    mIsLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    
    // Read and display base information from the Intent.
    if (!readIntentMovieExtra()) {
      // Useless to open details without movie.
      finish();
      return;
    }
    showMoveInfo();
    
    // Favorite button star.
    updateStarButton();
    
    // Setup tabs / view pager.
    ViewPager viewPager = findViewById(R.id.details_pager);
    viewPager.setAdapter (new DetailsTabAdapter(this));
    TabLayout tabLayout = findViewById(R.id.details_tabs);
    tabLayout.setupWithViewPager(viewPager);
    //viewPager.setCurrentItem(savedTab...);
    
    // Begin network request for movie details.
    api3 = new Api3(Secrets.THEMOVIEDB_API_KEY, this);
    requireMovieDetails();
  }
  
  
  private static final class DetailsTabAdapter extends PagerAdapter {
  
    public static final int PAGE_DESCRIPTION = 0;
    public static final int PAGE_REVIEWS     = 1;
    public static final int PAGE_VIDEOS      = 2;
    
    private final Context mContext;
    private String mDescription = null;
  
    public DetailsTabAdapter (Context context) { mContext = context; }
    
    @Override public int getCount() { return 3; } // Description, Reviews and Videos.
    
    @Override public boolean isViewFromObject (View view, Object object) { return view == object; }
    
    @Override public void destroyItem (ViewGroup container, int position, Object object) { container.removeView((View)object); }
    
    @Override public Object instantiateItem (ViewGroup container, int position) {
      LayoutInflater inflater = LayoutInflater.from(mContext);
      View view;
      switch (position) {
        case PAGE_DESCRIPTION:
          view = inflater.inflate(R.layout.details_page_description, container, true);
          TextView tvDescription = container.findViewById(R.id.description_tv);
          if (tvDescription != null) tvDescription.setText(mDescription);
          break;
        case PAGE_REVIEWS:
          view = inflater.inflate(R.layout.activity_with_recyclerview, container, true);
          RecyclerView rv
          break;
        case PAGE_VIDEOS:
          view = inflater.inflate(R.layout.activity_with_recyclerview, container, true);
          break;
        default: return null; // No such page.
      }
      return view;
    }
  
    @Override public CharSequence getPageTitle (int position) {
      switch (position) {
        case PAGE_DESCRIPTION: return mContext.getString(R.string.summary);
        case PAGE_REVIEWS:     return mContext.getString(R.string.reviews);
        case PAGE_VIDEOS:      return mContext.getString(R.string.videos);
        default: return null;
      }
    }
  
    @Override public void setPrimaryItem (ViewGroup container, int position, Object object) {
      super.setPrimaryItem (container, position, object);
      if (position == PAGE_DESCRIPTION) {
        TextView tvDescription = (TextView)object; // object - The same object that was returned by instantiateItem().
        tvDescription.setText(mDescription);
      }
    }
  
    public void setDescription (String text) {
      mDescription = text;
      notifyDataSetChanged();
    }
  }
  
  
  private void updateStarButton() {
    mBinding.starButton.setImageResource(mIsFavorite ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
  }


  @Override
  protected void onStop () {
    super.onStop();
    if (mDetailsRequest != null) mDetailsRequest.cancel(); // Stop fetching movies list from the network.
  }
  
  
  private void requireMovieDetails() {
    mDetailsRequest = api3.requireMovieDetails(mMovieInfo.id, this, this);
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
      mBinding.titleTv.setText(mMovieInfo.title);
    }
    
    // Add to favorites button.
    //boolean isFavorite = favorites.contains(mMovieInfo.id);
    mBinding.starButton.setPressed(mIsFavorite);
    mBinding.starButton.setContentDescription(mIsFavorite ? getString(R.string.remove_from_fav) : getString(R.string.add_to_fav));
    
    // Start background image loading.
    if ((mMovieInfo.backdrop_path != null) && !mMovieInfo.backdrop_path.isEmpty()) {
      String imagePathBG = Api3.getImageURL(mMovieInfo.backdrop_path, Options.getInstance(this).getBackgroundResolution());
      Picasso.with(this)
        .load(imagePathBG)
        .into(mBinding.backImageIv, new com.squareup.picasso.Callback() {
          @Override public void onSuccess () {
            // When no background loaded (in case of network error or absence of image) empty
            // background looks ugly with a large gap between top of the sceen and a title.
            // This why I bind title to the top of screen initially and background image are invisible.
            // After successful loading I enable background and re-bind title to the bottom of it.
            // The only disadvantage is title "jumps" down if internet works good and fast. Assuming
            // success looks bad in case of slow internet (or none). Sorry for long comment :)
  
            // Enable background.
            mBinding.backImageIv.setVisibility(View.VISIBLE);
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
      mBinding.posterIv.setColorFilter(0);
      Picasso.with(this)
        .load(imagePathPoster)
        .placeholder(android.R.drawable.progress_indeterminate_horizontal)
        .error(android.R.drawable.stat_notify_error)
        .into(mBinding.posterIv, new com.squareup.picasso.Callback() {
          @Override public void onSuccess () { mBinding.posterIv.setContentDescription(getString(R.string.poster_content_description)); }
          @Override public void onError () {
            mBinding.posterIv.setColorFilter(Color.RED);
            mBinding.posterIv.setContentDescription(getString(R.string.poster_error_content_description));
          }
        });
    }
    
    // Set release year.
    if (mMovieInfo.release_date != null) mBinding.yearTv.setText(getString(R.string.year) + mMovieInfo.release_date.substring(0, 4));
    
    // Set rate.
    mBinding.rateTv.setText(getString(R.string.rate) + String.valueOf(mMovieInfo.vote_average) + " / 10");
    
    // Set description.
    if (mMovieInfo.overview != null) {
      DetailsTabAdapter adapter = (DetailsTabAdapter)mBinding.detailsPager.getAdapter();
      if (adapter != null) adapter.setDescription(mMovieInfo.overview);
    }
    //if (mMovieInfo.overview != null) mBinding.descriptionTv.setText(mMovieInfo.overview);
    
    // Try to show details if it was passed from the DB.
    showDetailedInfo();
  }


  /**
   * Show detailed movie information that is generally received from network asyncronously.
   * However for favorite movies it could be available immediately from DB.
   */
  private void showDetailedInfo() {
    // Set movie length.
    if (mMovieInfo.runtime != null) mBinding.lengthTv.setText(getString(R.string.length) + mMovieInfo.runtime + getString(R.string.minutes_short));
  }
  
  
  /**
   * Called if some error occurred while executing network request.
   * @param error
   */
  @Override
  public void onErrorResponse (final VolleyError error) {
    Log.w(TAG, "onErrorResponse(): " + error.getMessage());
    Snackbar.make(mBinding.titleTv, R.string.fail_get_movie_info, Snackbar.LENGTH_LONG)
      .setAction(R.string.refresh_big, new View.OnClickListener() { @Override public void onClick (View v) {
        requireMovieDetails();
      }})
      .show();
  }


  /**
   * Called if TMDb network request succeeds.
   * @param response
   */
  @Override
  public void onResponse (final TmdbMovieDetails response) {
    mMovieInfo.setDetails(response);
    showDetailedInfo();
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
}
