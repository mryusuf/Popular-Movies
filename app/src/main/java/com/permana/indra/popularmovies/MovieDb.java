package com.permana.indra.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Movie;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.permana.indra.popularmovies.db.MovieContract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by My on 01/07/2017.
 */

public class MovieDb implements Parcelable{
    static final String EXTRA_MOVIE = "com.permana.indra.popularmovies.EXTRA_MOVIE";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE ="title";
    private static final String KEY_OVERVIEW = "overview";
    private static final String KEY_POSTER_PATH ="poster_path";
    private static final String KEY_VOTE_AVERAGE = "vote_average";
    private static final String KEY_RELEASE_DATE = "release_date";

    public long id;
    private String originalTitle;
    private String overview;
    private String releaseDate;
    private String posterPath;
    private String voteAverage;
    private ArrayList<Trailers> trailers;
    private ArrayList<Reviews> reviews;
    private Bitmap poster;

    public Bitmap getPoster() {
        return poster;
    }

    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }
    void setPosterFromCursor(Cursor cursor){
        byte[] bytes = cursor.getBlob(cursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_POSTER));
        ByteArrayInputStream posterStream = new ByteArrayInputStream(bytes);
        this.poster = BitmapFactory.decodeStream(posterStream);
    }
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
    public void setTrailers(ArrayList<Trailers> trailers) {
        this.trailers = trailers;
    }

    public void setReviews(ArrayList<Reviews> reviews) {
        this.reviews = reviews;
    }

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


    String getDetailPosterUri()
    {
        final String detailPosterUrl = "https://image.tmdb.org/t/p/w500";

        return detailPosterUrl+posterPath;
    }
    public MovieDb(){

    }
    public MovieDb(Parcel parcel){
        id=parcel.readLong();
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
        parcel.writeLong(id);
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

    boolean saveToBookmarks(Context context){
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.MovieEntry.MOVIE_ID, this.id);
        contentValues.put(MovieContract.MovieEntry.MOVIE_TITLE, this.originalTitle);
        contentValues.put(MovieContract.MovieEntry.MOVIE_OVERVIEW, this.overview);
        contentValues.put(MovieContract.MovieEntry.MOVIE_POSTER_PATH, this.posterPath);
        contentValues.put(MovieContract.MovieEntry.MOVIE_AVG, this.voteAverage);
        contentValues.put(MovieContract.MovieEntry.MOVIE_RELEASE_DATE, this.releaseDate);
        contentValues.put(MovieContract.MovieEntry.MOVIE_TRAILERS,Trailers.arrayToString(trailers));
        contentValues.put(MovieContract.MovieEntry.MOVIE_REVIEWS,Reviews.arrayToString(reviews));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.poster.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] bytes = baos.toByteArray();

        contentValues.put(MovieContract.MovieEntry.MOVIE_POSTER,bytes);

        if (context.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI,contentValues)!=null){
            Toast.makeText(context, R.string.added_favourite,Toast.LENGTH_SHORT).show();
            return true;
        }else{
            Toast.makeText(context, R.string.add_favourite_error,Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    boolean removeFromBookmarks(Context context){
        long deletedRows = context.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.MOVIE_ID + "=?",new String[]{Long.toString(this.id)});
        if (deletedRows>0){
            Toast.makeText(context, R.string.delete_bookmark,Toast.LENGTH_SHORT).show();
            return true;
        }else {
            Toast.makeText(context, R.string.delete_bookmark_error, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    boolean isBookmarked(Context context){
        Cursor cursor = context.getContentResolver()
                .query(MovieContract.MovieEntry.CONTENT_URI,
                        new String[]{MovieContract.MovieEntry.MOVIE_ID},
                        MovieContract.MovieEntry.MOVIE_ID + "=?",
                        new String[]{Long.toString(this.id)},null);
        if (cursor!=null) {
            boolean bookmarked = cursor.getCount() > 0;
            cursor.close();
            return bookmarked;
        }
        return false;
    }
}
