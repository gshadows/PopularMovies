package com.example.popularmovies.themoviedb;


/**
 * TMDB single page of movies list response.
 */
public class TmdbMoviesPage extends TmdbBase {
  
  public int              page;
  public TmdbMovieShort[] results;
  public int              total_results;
  public int              total_pages;
  
}
