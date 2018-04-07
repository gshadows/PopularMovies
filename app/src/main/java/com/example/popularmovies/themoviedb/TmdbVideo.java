package com.example.popularmovies.themoviedb;


public class TmdbVideo extends TmdbBase {
  
//public String id;         // Some ID.
//public String iso_639_1;  // Language ID: "en", "ru-RU".
//public String iso_3166_1; // Country ID: "US".
  public String key;        // Some key. Probably, for YouTube - part of URL: https://www.youtube.com/watch?v={key}
  public String name;       // Example: "Trailer 1".
  public String site;       // Example: "YouTube".
  public int    size;       // Allowed Values: 360, 480, 720, 1080.
  public String type;       // Allowed Values: Trailer, Teaser, Clip, Featurette.
  
}
