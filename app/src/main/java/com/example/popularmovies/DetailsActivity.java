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

import lombok.Getter;


public class DetailsActivity extends AppCompatActivity
  implements  Response.ErrorListener,
              Response.Listener<TmdbMovieDetails> {

  public static final String TAG = Options.XTAG + DetailsActivity.class.getSimpleName();
  
  public static final String EXTRA_MOVIE       = "movie";
  public static final String EXTRA_IS_FAVORITE = "is_fav";
  
  
  /**
   * Details activity pages description enumeration.
   */
  public enum Page {
    DESCRIPTION (R.string.summary, R.layout.details_page_description),
    REVIEWS     (R.string.reviews, R.layout.activity_with_recyclerview),
    VIDEOS      (R.string.videos,  R.layout.activity_with_recyclerview);
    
    @Getter private final int titleId, layoutId;
    Page (int titleId, int layoutId) { this.titleId = titleId; this.layoutId = layoutId; }
  }
  
  private ActivityDetailsBinding mBinding;
  private SavedMovieInfo mMovieInfo;
  private boolean mIsFavorite;
  
  private Api3 api3;
  private Request<TmdbMovieDetails> mDetailsRequest = null;
  
  private DetailsTabAdapter mTabAdapter;
  
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
    
    // Setup movie information pages (tabs).
    ViewPager viewPager = findViewById(R.id.details_pager);
    mTabAdapter = new DetailsTabAdapter(this, new DetailsTabAdapter.PageSwitchedListener() {
      @Override public void onPageSwitched (Page page) {
        if (page == Page.DESCRIPTION) {
          updateDescription();
        }
      }
    });
    viewPager.setAdapter (mTabAdapter);
    TabLayout tabLayout = findViewById(R.id.details_tabs);
    tabLayout.setupWithViewPager(viewPager);
    //viewPager.setCurrentItem(savedTab...);
    
    // Begin network request for movie details.
    api3 = new Api3(Secrets.THEMOVIEDB_API_KEY, this);
    requireMovieDetails();
  }
  
  
  private static final class DetailsTabAdapter extends PagerAdapter {
    interface PageSwitchedListener { void onPageSwitched (Page page); }
  
    private final Context mContext;
    private PageSwitchedListener mListener;
    
    public TextView mDescriptionTV = null;
    public RecyclerView mReviewsRV = null;
    public RecyclerView mVideosRV  = null;
  
    public DetailsTabAdapter (Context context, PageSwitchedListener listener) {
      mContext = context;
      mListener = listener;
    }
    
    @Override public int getCount() { return Page.values().length; }
    @Override public boolean isViewFromObject (View view, Object object) { return view == object; }
    
    @Override public void destroyItem (ViewGroup container, int position, Object object) {
      Log.d(TAG, "DetailsTabAdapter.destroyItem() pos " + position);
      container.removeView((View)object);
    }
    
    private boolean checkPosition (String source, int position) {
      if ((position < 0) || (position >= Page.values().length)) {
        Log.e(TAG, String.format("DetailsTabAdapter.%s() bad position %d", source, position));
        return false;
      }
      return true;
    }
    
    @Override public Object instantiateItem (ViewGroup container, int position) {
      if (!checkPosition ("instantiateItem", position)) return null;
      Log.d(TAG, "DetailsTabAdapter.instantiateItem() pos " + position);
      
      Page page = Page.values()[position];
      View view = LayoutInflater.from(mContext).inflate(page.getLayoutId(), container, true);
      
      Log.d(TAG, "DetailsTabAdapter.instantiateItem() " + position + " -> " + mContext.getString(page.getTitleId()));
      
      // Initialize pages.
      if (page == Page.DESCRIPTION) {
        
        // Description contains a text field.
        mDescriptionTV = container.findViewById(R.id.description_tv);
        if (mDescriptionTV == null) Log.w (TAG, "instantiateItem(): description_tv not found");
        
      } else {
        
        RecyclerView rv = container.findViewById(R.id.main_recyclerview);
        if (rv == null) {
          Log.w (TAG, "instantiateItem(): main_recyclerview not found");
        } else if (page == Page.REVIEWS) {
          mReviewsRV = rv;
        } else if (page == Page.VIDEOS) {
          mVideosRV = rv;
        } else {
          Log.w (TAG, "instantiateItem(): this page number was missed in code: " + position);
        }
      }
      
      return view;
    }
  
    @Override public CharSequence getPageTitle (int position) {
      if (!checkPosition ("getPageTitle", position)) return null;
      Log.d(TAG, "DetailsTabAdapter.getPageTitle() " + position + " -> " + mContext.getString(Page.values()[position].getTitleId()));
      return mContext.getString(Page.values()[position].getTitleId());
    }
  
    @Override public void setPrimaryItem (ViewGroup container, int position, Object object) {
      Log.d(TAG, "DetailsTabAdapter.setPrimaryItem() pos " + position);
      super.setPrimaryItem (container, position, object);
      mListener.onPageSwitched(Page.values()[position]);
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
    updateDescription();
    
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
   * Show movie description when corresponding page dislayed.
   */
  private void updateDescription() {
    Log.d(TAG, "updateDescription()");
    if (mMovieInfo == null) return;
    if (mMovieInfo.overview == null) return;
    DetailsTabAdapter adapter = (DetailsTabAdapter)mBinding.detailsPager.getAdapter();
    Log.d(TAG, "updateDescription() will set description to the adapter " + adapter);
    if (adapter == null) return;
    adapter.mDescriptionTV.setText(mMovieInfo.overview);
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
