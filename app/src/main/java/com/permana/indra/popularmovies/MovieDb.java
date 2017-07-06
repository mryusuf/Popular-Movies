package com.permana.indra.popularmovies;

import android.graphics.Movie;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by My on 01/07/2017.
 */

public class MovieDb implements Parcelable{
    private String originalTitle;
    private String overview;
    private String releaseDate;
    private String posterPath;
    private String voteAverage;


    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }


    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getPosterPath() {
        final String posterUrl = "https://image.tmdb.org/t/p/w185";

        return posterUrl+posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(String voteAverage) {
        this.voteAverage = voteAverage;
    }


    public MovieDb(){

    }

    private MovieDb(Parcel parcel){
        originalTitle=parcel.readString();
        overview=parcel.readString();
        releaseDate=parcel.readString();
        posterPath=parcel.readString();
        voteAverage=parcel.readString();
    }
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(originalTitle);
        parcel.writeString(overview);
        parcel.writeString(releaseDate);
        parcel.writeString(posterPath);
        parcel.writeString(voteAverage);
    }

    public static final Parcelable.Creator<MovieDb> CREATOR = new Parcelable.Creator<MovieDb>() {
        public MovieDb createFromParcel(Parcel source) {
            return new MovieDb(source);
        }

        public MovieDb[] newArray(int size) {
            return new MovieDb[size];
        }
    };
}
