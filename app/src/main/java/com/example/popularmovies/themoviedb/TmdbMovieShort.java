package com.example.popularmovies.themoviedb;


/**
 * TMDB response containing movie short information.
 * Never returned separately, only as a part of another responses, such as TmdbMoviesPage.
 */
public class TmdbMovieShort extends TmdbBase {
  
  public String  poster_path;
  public boolean adult;
  public String  overview;
  public String  release_date;
  public int[]   genre_ids;
  public int     id;
  public String  original_title;
  public String  original_language;
  public String  title;
  public String  backdrop_path;
  public double  popularity;
  public int     vote_count;
  public boolean video;
  public double  vote_average;
  
}
