package com.example.popularmovies.themoviedb;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * TMDB response containing movie short information.
 * Never returned separately, only as a part of another responses, such as TmdbMoviesPage.
 */
public class TmdbMovieShort extends TmdbBase implements Parcelable {
  
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
  

  public static final Creator<TmdbMovieShort> CREATOR = new Creator<TmdbMovieShort>() {
    @Override
    public TmdbMovieShort createFromParcel(Parcel in) {
      return new TmdbMovieShort(in);
    }

    @Override
    public TmdbMovieShort[] newArray(int size) {
      return new TmdbMovieShort[size];
    }
  };

  @Override public int describeContents() { return 0; }

  
  @Override
  public void writeToParcel(Parcel parcel, int i) {
    parcel.writeString  (poster_path);
    parcel.writeByte    ((byte)(adult ? 1 : 0));
    parcel.writeString  (overview);
    parcel.writeString  (release_date);
    parcel.writeIntArray((genre_ids != null) ? genre_ids : new int[0]);
    parcel.writeInt     (id);
    parcel.writeString  (original_title);
    parcel.writeString  (original_language);
    parcel.writeString  (title);
    parcel.writeString  (backdrop_path);
    parcel.writeDouble  (popularity);
    parcel.writeInt     (vote_count);
    parcel.writeByte    ((byte)(video ? 1 : 0));
    parcel.writeDouble  (vote_average);
  }


  public TmdbMovieShort (Parcel parcel) {
    poster_path       = parcel.readString();
    adult             = (parcel.readByte() != 0);
    overview          = parcel.readString();
    release_date      = parcel.readString();
    genre_ids         = parcel.createIntArray();
    id                = parcel.readInt();
    original_title    = parcel.readString();
    original_language = parcel.readString();
    title             = parcel.readString();
    backdrop_path     = parcel.readString();
    popularity        = parcel.readDouble();
    vote_count        = parcel.readInt();
    video             = (parcel.readByte() != 0);
    vote_average      = parcel.readDouble();
  }
}
