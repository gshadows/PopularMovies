package com.example.popularmovies;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import static android.content.Intent.ACTION_VIEW;


public class AboutActivity extends AppCompatActivity {


  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);
    
    // Make all links inside TextViews openable in browser.
    ViewGroup vg = findViewById(R.id.about_layout);
    for (int i = 0; i < vg.getChildCount(); i++) {
      View view = vg.getChildAt(i);
      if (view instanceof TextView) {
        TextView tv = (TextView)view;
        tv.setMovementMethod(LinkMovementMethod.getInstance());
      }
    }
  }


  /**
   * Click on TMDb logo should open TMDb site main page.
   * @param view Unused reference to clicked view.
   */
  public void onTmdbLogoClick (View view) {
    Intent intent = new Intent(ACTION_VIEW, Uri.parse(getString(R.string.tmdb_url)));
    if (intent.resolveActivity(getPackageManager()) != null) {
      startActivity(intent);
    }
  }
}
