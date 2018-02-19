# Popular Movies
Student Project for [Android Developer Nanodegree](https://www.udacity.com/course/android-developer-nanodegree-by-google--nd801) in [Udacity](http://udacity.com).

## Project Overview
Allow user to list popular movies and look infromation about it.
The sort order can be by most popular, or by top rated.

## How to add an API key?
API key is removed from project because it is illegal to share.
1. You should obtain your own key at [this site](https://www.themoviedb.org/settings/api) (you need to create an account).
2. Create new class Secret: **\app\src\main\java\com\example\popularmovies\Secrets.java**
3. Class should look like this:
```java
package com.example.popularmovies;

public final class Secrets {
  public static final String THEMOVIEDB_API_KEY = "...";
}
```

## Used external resources
1. [Launcher icon](http://pixabay.com/en/movie-film-reel-cinema-video-297135/) (CC0 Creative Commons)
