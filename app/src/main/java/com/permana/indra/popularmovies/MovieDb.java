package com.permana.indra.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.permana.indra.popularmovies.db.MovieContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

/**
 * Created by My on 01/07/2017.
 */

public class MovieDb implements Parcelable {
    public static final String EXTRA_MOVIE = "com.permana.indra.popularmovies.EXTRA_MOVIE";
    private static final String KEY_ID = "id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_OVERVIEW = "overview";
    private static final String KEY_POSTER_PATH = "poster_path";
    private static final String KEY_VOTE_AVERAGE = "vote_average";
    private static final String KEY_RELEASE_DATE = "release_date";

    public final long id;
    public final String originalTitle;
    public final String overview;
    public final String releaseDate;
    private final String posterPath;
    public final Double voteAverage;
    private ArrayList<Trailers> trailers;
    private ArrayList<Reviews> reviews;
    private Bitmap poster;

    public MovieDb(long id, String originalTitle, String overview, String posterPath, double voteAverage, String releaseDate) {
        this.id = id;
        this.originalTitle = originalTitle;
        this.overview = overview;
        this.posterPath = posterPath;
        this.voteAverage = voteAverage;
        this.releaseDate = releaseDate;
        this.trailers = new ArrayList<>();
        this.reviews = new ArrayList<>();
    }

    public Bitmap getPoster() {
        return poster;
    }

    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }

    void setPosterFromCursor(Cursor cursor) {
        byte[] bytes = cursor.getBlob(cursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_POSTER));
        ByteArrayInputStream posterStream = new ByteArrayInputStream(bytes);
        this.poster = BitmapFactory.decodeStream(posterStream);
    }

    public void setTrailers(ArrayList<Trailers> trailers) {
        this.trailers = trailers;
    }

    public void setReviews(ArrayList<Reviews> reviews) {
        this.reviews = reviews;
    }


    public Uri getDetailPosterUri() {
        final String detailPosterUrl = "https://image.tmdb.org/t/p/w500";

        return Uri.parse(detailPosterUrl).buildUpon().appendEncodedPath(posterPath).build();
    }

    public MovieDb(Bundle bundle) {
        this(bundle.getLong(KEY_ID),
                bundle.getString(KEY_TITLE),
                bundle.getString(KEY_OVERVIEW),
                bundle.getString(KEY_POSTER_PATH),
                bundle.getDouble(KEY_VOTE_AVERAGE),
                bundle.getString(KEY_RELEASE_DATE));

    }

    public MovieDb(Parcel parcel) {
        id = parcel.readLong();
        originalTitle = parcel.readString();
        overview = parcel.readString();
        releaseDate = parcel.readString();
        posterPath = parcel.readString();
        voteAverage = parcel.readDouble();
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
        parcel.writeDouble(voteAverage);
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        bundle.putLong(KEY_ID, id);
        bundle.putString(KEY_TITLE, originalTitle);
        bundle.putString(KEY_OVERVIEW, overview);
        bundle.putString(KEY_POSTER_PATH, posterPath);
        bundle.putDouble(KEY_VOTE_AVERAGE, voteAverage);
        bundle.putString(KEY_RELEASE_DATE, releaseDate);
        return bundle;
    }

    public static final Parcelable.Creator<MovieDb> CREATOR = new Parcelable.Creator<MovieDb>() {
        public MovieDb createFromParcel(Parcel source) {
            return new MovieDb(source);
        }

        public MovieDb[] newArray(int size) {
            return new MovieDb[size];
        }
    };

    static MovieDb getMovieFromJson(JSONObject jsonObject) throws JSONException {
        return new MovieDb(jsonObject.getLong(KEY_ID),
                jsonObject.getString(KEY_TITLE),
                jsonObject.getString(KEY_OVERVIEW),
                jsonObject.getString(KEY_POSTER_PATH),
                jsonObject.getDouble(KEY_VOTE_AVERAGE),
                jsonObject.getString(KEY_RELEASE_DATE));
    }

    boolean saveToFavourite(Context context) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MovieContract.MovieEntry.MOVIE_ID, this.id);
        contentValues.put(MovieContract.MovieEntry.MOVIE_TITLE, this.originalTitle);
        contentValues.put(MovieContract.MovieEntry.MOVIE_OVERVIEW, this.overview);
        contentValues.put(MovieContract.MovieEntry.MOVIE_POSTER_PATH, this.posterPath);
        contentValues.put(MovieContract.MovieEntry.MOVIE_AVG, this.voteAverage);
        contentValues.put(MovieContract.MovieEntry.MOVIE_RELEASE_DATE, this.releaseDate);
        contentValues.put(MovieContract.MovieEntry.MOVIE_TRAILERS, Trailers.arrayToString(trailers));
        contentValues.put(MovieContract.MovieEntry.MOVIE_REVIEWS, Reviews.arrayToString(reviews));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.poster.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bytes = baos.toByteArray();

        contentValues.put(MovieContract.MovieEntry.MOVIE_POSTER, bytes);

        if (context.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, contentValues) != null) {
            Toast.makeText(context, R.string.added_favourite, Toast.LENGTH_SHORT).show();

            return true;
        } else {
            Toast.makeText(context, R.string.add_favourite_error, Toast.LENGTH_SHORT).show();
            return false;
        }

    }

    boolean removeFromFavourite(Context context) {
        long deletedRows = context.getContentResolver().delete(MovieContract.MovieEntry.CONTENT_URI,
                MovieContract.MovieEntry.MOVIE_ID + "=?", new String[]{Long.toString(this.id)});
        if (deletedRows > 0) {
            Toast.makeText(context, R.string.delete_favourite, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Toast.makeText(context, R.string.delete_favourite_error, Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    boolean isFavourite(Context context) {
        Cursor cursor = context.getContentResolver()
                .query(MovieContract.MovieEntry.CONTENT_URI,
                        new String[]{MovieContract.MovieEntry.MOVIE_ID},
                        MovieContract.MovieEntry.MOVIE_ID + "=?",
                        new String[]{Long.toString(this.id)}, null);
        if (cursor != null) {
            boolean bookmarked = cursor.getCount() > 0;
            cursor.close();
            return bookmarked;
        }
        return false;
    }
}
