package com.example.popularmovies;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class AboutActivity extends AppCompatActivity {


  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_about);
    
    // Apply HTML formatting for all TextViews.
    ViewGroup vg = findViewById(R.id.about_layout);
    for (int i = 0; i < vg.getChildCount(); i++) {
      View view = vg.getChildAt(i);
      if (view instanceof TextView) {
        TextView tv = (TextView)view;
        tv.setText(Html.fromHtml(tv.getText().toString()));
      }
    }
  }
}
