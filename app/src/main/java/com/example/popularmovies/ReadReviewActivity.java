package com.example.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;


public class ReadReviewActivity extends AppCompatActivity {
  public static final String TAG = ReadReviewActivity.class.getSimpleName();
  
  public static final String EXTRA_TITLE  = "title";
  public static final String EXTRA_AUTHOR = "author";
  public static final String EXTRA_REVIEW = "review";
  
  
  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_read_review);
  
    // Obtain movie name, review author and text.
    if (!readIntentExtras()) {
      finish();
      return;
    }
  }
  
  
  private boolean readIntentExtras() {
    Intent intent = getIntent();
    if (intent == null) {
      Log.e(TAG, "No intent passed");
      return false;
    }
    
    // Get title.
    String title = intent.getStringExtra(EXTRA_TITLE);
    if (title == null) {
      Log.e(TAG, "Bad movie title intent extra");
      return false;
    }
    setTitle(title);
    
    // Get author name.
    String author = intent.getStringExtra(EXTRA_AUTHOR);
    if (title == null) {
      Log.e(TAG, "Bad movie title intent extra");
      return false;
    }
    ((TextView)findViewById(R.id.author_tv)).setText(author);
    
    // Get review text.
    String review = intent.getStringExtra(EXTRA_REVIEW);
    if (title == null) {
      Log.e(TAG, "Bad movie title intent extra");
      return false;
    }
    ((TextView)findViewById(R.id.review_tv)).setText(review);
    
    return true;
  }
  
  
}
