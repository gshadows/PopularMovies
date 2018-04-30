# Popular Movies
Student Project for [Android Developer Nanodegree](https://www.udacity.com/course/android-developer-nanodegree-by-google--nd801) in [Udacity](http://udacity.com).

## Project Overview
Allow user to list popular movies and look infromation about it.
The sort order can be by most popular, or by top rated.

## How to add an API key?
API key is removed from project because it is illegal to share.
1. You should obtain your own key at [this site](https://www.themoviedb.org/settings/api) (you need to create an account).
2. Use [this instruction](https://technobells.com/best-way-to-store-your-api-keys-for-your-android-studio-project-e4b5e8bb7d23) to add global Gradle variable with name **THEMOVIEDB_API_KEY**.

## Why it shows reviews and videos in English?
This is because many movies has no localized reviews and/or videos at all.
And to pass project review I must be sure reviewer will see something.
Better solution should be load English if localized is not available (or even load both localized AND English) but I have no more time now.

## Used external resources
1. [Launcher icon](http://pixabay.com/en/movie-film-reel-cinema-video-297135/) (CC0 Creative Commons)
