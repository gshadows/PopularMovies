package com.example.popularmovies;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.example.popularmovies.utils.Options;


public class MainActivity extends AppCompatActivity {
  
  
  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
  }


  @Override
  protected void onStop () {
    super.onStop();
  }


  @Override
  public boolean onCreateOptionsMenu (Menu menu) {
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }


  @Override
  public boolean onOptionsItemSelected (MenuItem item) {
    switch (item.getItemId()) {
      case R.id.menu_about:
        startActivity(new Intent(this, AboutActivity.class));
        return true;
      case R.id.menu_popular:
        item.setChecked(true);
        Options.getInstance(this).setPopularDisplayed(true);
        return true;
      case R.id.menu_top_rated:
        item.setChecked(true);
        Options.getInstance(this).setPopularDisplayed(false);
        return true;
    }
    return super.onContextItemSelected(item);
  }
}
