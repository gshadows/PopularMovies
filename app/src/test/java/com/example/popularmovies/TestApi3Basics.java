package com.example.popularmovies;

import android.test.mock.MockContext;

import com.example.popularmovies.themoviedb.Api3;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;


public class TestApi3Basics {
  
  private MockContext mContext;
  //private @Mock android.content.Context mContext = mock(android.content.Context.class);
  private Api3 mApi3;
  
  
  @Before
  public void before() {
    mContext = new MockContext();
    mApi3 = new Api3 (Secrets.THEMOVIEDB_API_KEY, mContext);
  }
  
  
  @Test
  public void TestConstructorArgumentsProtection () throws Exception {
    Api3 badApi3;
    
    // Pass null API key.
    try {
      //noinspection UnusedAssignment,ConstantConditions
      badApi3 = new Api3(null, mContext);
      assertTrue("API3 constructor should throw exception if API key is null", false);
    }
    catch (IllegalArgumentException ex) {} // Success.
    catch (Exception ex) { assertTrue("API3 constructor throws unexpected exception", false); }

    // Pass empty API key.
    try {
      //noinspection UnusedAssignment
      badApi3 = new Api3("", mContext);
      assertTrue("API3 constructor should throw exception if API key is empty", false);
    }
    catch (IllegalArgumentException ex) {} // Success.
    catch (Exception ex) { assertTrue("API3 constructor throws unexpected exception", false); }
    
    // Pass null context.
    try {
      //noinspection UnusedAssignment,ConstantConditions
      badApi3 = new Api3(Secrets.THEMOVIEDB_API_KEY, null);
      assertTrue("API3 constructor should throw exception if context is null", false);
    }
    catch (IllegalArgumentException ex) {} // Success.
    catch (Exception ex) { assertTrue("API3 constructor throws unexpected exception", false); }
  }
  
  
  @Test
  public void TestCorrectUrlGeneration() throws Exception {
    final String apiKey = Secrets.THEMOVIEDB_API_KEY;
    
    // Generate "popular movies" page 3 request.
    assertEquals("Wrong popular movies request URL: ",
      "https://api.themoviedb.org/3/movie/popular?api_key=" + apiKey + "&page=3",
      mApi3.getURL_PopularMovies(3)
      );

    // Generate "top rated movies" page 2 request.
    assertEquals("Wrong top rated movies request URL",
      "https://api.themoviedb.org/3/movie/top_rated?api_key=" + apiKey + "&page=2",
      mApi3.getURL_TopRatedMovies(2)
      );

    // Generate movie request with ID = 17.
    assertEquals("Wrong movie details request URL",
      "https://api.themoviedb.org/3/movie/17?api_key=" + apiKey, 
      mApi3.getURL_MovieDetails(17)
      );
  }
  
  
  @Test
  public void TestWrongPages () throws Exception {
    int[] badPages = { 0, -1, -12, -1000, 1001, 123456 }; 
    
    for (int page : badPages) {
      // Popular movies.
      try {
        String test = mApi3.getURL_PopularMovies(page);
        assertTrue("getURL_PopularMovies() allowed page number " + page, false);
      } catch (IllegalArgumentException ex) {} // Success.
      catch (Exception ex) {
        assertTrue("getURL_PopularMovies() throws unexpected exception", false);
      }
      
      // Top rated movies.
      try {
        String test = mApi3.getURL_TopRatedMovies(page);
        assertTrue("getURL_TopRatedMovies() allowed page number " + page, false);
      } catch (IllegalArgumentException ex) {} // Success.
      catch (Exception ex) {
        assertTrue("getURL_TopRatedMovies() throws unexpected exception", false);
      }
    }
  }
  

}
