package com.permana.indra.popularmovies.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.permana.indra.popularmovies.MovieDb;
import com.permana.indra.popularmovies.Reviews;
import com.permana.indra.popularmovies.Trailers;

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.util.ArrayList;

import static android.R.attr.name;

/**
 * Created by asus on 28/08/2017.
 */

public class MovieDBHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "PopularMovies.db";
    private static final int DB_VERSION = 2;

    public MovieDBHelper(Context context){
        super(context,DATABASE_NAME,null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_QUERY =
                "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME + "(" +
                        MovieContract.MovieEntry.MOVIE_ID + " INTEGER PRIMARY KEY, " +
                        MovieContract.MovieEntry.MOVIE_TITLE + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.MOVIE_OVERVIEW + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.MOVIE_POSTER + " BLOB NOT NULL, " +
                        MovieContract.MovieEntry.MOVIE_POSTER_PATH + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.MOVIE_AVG + " REAL NOT NULL, " +
                        MovieContract.MovieEntry.MOVIE_RELEASE_DATE + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.MOVIE_TRAILERS + " TEXT NOT NULL, " +
                        MovieContract.MovieEntry.MOVIE_REVIEWS + " TEXT NOT NULL" +
                        ")";

        db.execSQL(SQL_CREATE_QUERY);
    }

    public MovieDb[] getAllMovie(){
        String query = "SELECT * FROM " + MovieContract.MovieEntry.TABLE_NAME;

        SQLiteDatabase database = getReadableDatabase();

        Cursor c = database.rawQuery(query, null);
        Log.d("cursor count ",String.valueOf(c.getCount()));
        MovieDb[] movieDbs = new MovieDb[c.getCount()];
        if (c != null) {
            try{
            while (c.moveToNext()) {
                int i = 0;
                long id = c.getLong(c.getColumnIndex(MovieContract.MovieEntry.MOVIE_ID));
                String title = c.getString(c.getColumnIndex(MovieContract.MovieEntry.MOVIE_TITLE));
                String overview = c.getString(c.getColumnIndex(MovieContract.MovieEntry.MOVIE_OVERVIEW));
                byte[] bytes = c.getBlob(c.getColumnIndex(MovieContract.MovieEntry.MOVIE_POSTER));
                ByteArrayInputStream posterStream = new ByteArrayInputStream(bytes);
                Bitmap poster = BitmapFactory.decodeStream(posterStream);

                String posterPath = c.getString(c.getColumnIndex(MovieContract.MovieEntry.MOVIE_POSTER_PATH));
                String avg = c.getString(c.getColumnIndex(MovieContract.MovieEntry.MOVIE_AVG));
                String releaseDate = c.getString(c.getColumnIndex(MovieContract.MovieEntry.MOVIE_RELEASE_DATE));
//                ArrayList<Trailers> trailer = c.getString(c.getColumnIndex(MovieContract.MovieEntry.MOVIE_TRAILERS));
//                ArrayList<Reviews> review = c.getString(c.getColumnIndex(MovieContract.MovieEntry.MOVIE_REVIEWS));

                movieDbs[i] = new MovieDb();
                movieDbs[i].setId(id);
                movieDbs[i].setOriginalTitle(title);
                movieDbs[i].setOverview(overview);
                movieDbs[i].setPoster(poster);
                movieDbs[i].setPosterPath(posterPath);
                movieDbs[i].setReleaseDate(releaseDate);
                movieDbs[i].setVoteAverage(avg);
//                movieDb.setTrailers(trailer);
//                movieDb.setReviews(review);

                i++;
            }
            }finally {
                if (c != null && !c.isClosed())
                    c.close();
                database.close();
            }
        }
        return movieDbs;
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
