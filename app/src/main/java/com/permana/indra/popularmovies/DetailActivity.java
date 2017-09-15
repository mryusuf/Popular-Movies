package com.permana.indra.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.permana.indra.popularmovies.db.MovieContract;
import com.permana.indra.popularmovies.utilities.NetworkUtilsTask;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Scanner;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks  {

    private final String TAG = DetailActivity.class.getSimpleName();
    MovieDb movieDb;
    ImageView ivPoster;
    ArrayList<Trailers> mTrailers;
    ArrayList<Reviews> mReviews;
    TrailersAdapter trailersAdapter;
    ListView mTrailersListView;

    private static final int LOADER_ID = 711;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        TextView tvOriginalTitle = (TextView) findViewById(R.id.tv_title);
        ivPoster = (ImageView) findViewById(R.id.poster_detail);
        TextView tvOverView = (TextView) findViewById(R.id.tv_overview);
        TextView tvVoteAverage = (TextView) findViewById(R.id.tv_vote_average);
        TextView tvReleaseDate = (TextView) findViewById(R.id.tv_release_date);
        final Button mFavourite = (Button)findViewById(R.id.favourite_button);
        mTrailersListView = (ListView)findViewById(R.id.trailers_list_view);
        trailersAdapter = new TrailersAdapter(this);
        mTrailersListView.setAdapter(trailersAdapter);
        Intent intent = getIntent();

        movieDb = intent.getParcelableExtra(getString(R.string.parcel_moviedb));
        if(movieDb.isBookmarked(getApplicationContext())){
            mFavourite.setText(getString(R.string.added_favourite));
        }
        mFavourite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getApplicationContext();
                if (!movieDb.isBookmarked(context)) {
                    if (movieDb.saveToBookmarks(context)) {
                        mFavourite.setText(getString(R.string.added_favourite));
                    }
                } else {
                    if (movieDb.removeFromBookmarks(context)) {
                    }
                }
            }
        });

        final Bitmap[] posterBitmap = new Bitmap[1];
        Bundle args = new Bundle();
        if (movieDb.isBookmarked(getApplicationContext())){
            args.putBoolean("local",true);

        }else {
            Picasso.with(this).load(movieDb.getDetailPosterUri())
                    .into(new Target(){
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            posterBitmap[0] = bitmap;
                            movieDb.setPoster(posterBitmap[0]);
                            ivPoster.setImageBitmap(posterBitmap[0]);
                        }

                        @Override
                        public void onBitmapFailed(Drawable errorDrawable) {

                        }
                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {

                        }
                    });
            args.putBoolean("local",false);
        }


        mTrailersListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Uri uri = trailersAdapter.getTrailerUri(position);

                if (uri != null) {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }
        });


        tvOriginalTitle.setText(movieDb.getOriginalTitle());
        tvVoteAverage.setText(movieDb.getVoteAverage());
        ivPoster.setImageBitmap(movieDb.getPoster());


        String releaseDate = movieDb.getReleaseDate();
        if(releaseDate == null) {
            tvReleaseDate.setTypeface(null, Typeface.ITALIC);
            releaseDate = getResources().getString(R.string.not_found);
        } else {
            tvReleaseDate.setText(releaseDate);
        }
        tvReleaseDate.setText(releaseDate);



        String overView = movieDb.getOverview();
        if (overView == null) {
            tvOverView.setTypeface(null, Typeface.ITALIC);
            overView = getResources().getString(R.string.not_found);
        }
        tvOverView.setText(overView);

        getSupportLoaderManager().restartLoader(LOADER_ID, args, this);
    }

    public void seeReviews(View v) {
        String reviewsString = Reviews.arrayToString(mReviews);
        Intent reviewsIntent = new Intent(getApplicationContext(), ReviewsActivity.class);
        reviewsIntent.putExtra(getString(R.string.reviews_intent_extra), reviewsString);
        reviewsIntent.putExtra("title",movieDb.getOriginalTitle());
        startActivity(reviewsIntent);
    }

    @Override
    public Loader<Object> onCreateLoader(final int id, final Bundle args) {

        return new AsyncTaskLoader<Object>(this) {
            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public Void loadInBackground() {

                if (args != null && args.size() != 0) {
                    boolean local = args.getBoolean("local");
                    long id = movieDb.id;

                    if (!local) {
                        String apiKey = getString(R.string.key);
                        Log.d(TAG, "Starting online query");
                        NetworkUtilsTask.OnTaskCompleted taskCompleted = new NetworkUtilsTask.OnTaskCompleted() {
                            @Override
                            public void onFetchMoviesTaskCompleted(MovieDb[] movies) {

                            }
                        };
                        NetworkUtilsTask networker = new NetworkUtilsTask(taskCompleted,apiKey);
                        URL requestTrailersUrl = networker.buildTrailersUrl(id);
                        URL requestReviewsUrl = networker.buildReviewsUrl(id);
                        try {
                            String JSONResponseTrailers = networker.getResponseFromHttpUrl(requestTrailersUrl);
                            mTrailers = fetchTrailersFromJson(JSONResponseTrailers);

                            String JSONResponseReviews = networker.getResponseFromHttpUrl(requestReviewsUrl);
                            mReviews = fetchReviewsFromJson(JSONResponseReviews);

                        } catch (IOException | JSONException e) {
                            e.printStackTrace();
                        }

                    } else {
                        Log.d(TAG, "Starting local query");
                        Cursor cursor = getContentResolver()
                                .query(MovieContract.MovieEntry.CONTENT_URI,
                                        new String[]{MovieContract.MovieEntry.MOVIE_TRAILERS, MovieContract.MovieEntry.MOVIE_REVIEWS, MovieContract.MovieEntry.MOVIE_POSTER},
                                        MovieContract.MovieEntry.MOVIE_ID + "=?",
                                        new String[]{Long.toString(id)}, null);
                        if (cursor != null && cursor.moveToFirst()) {
                            Log.d(TAG, cursor.getString(0));
                            mTrailers = Trailers.stringToArray(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_TRAILERS)));
                            mReviews = Reviews.stringToArray(cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_REVIEWS)));
                            movieDb.setPosterFromCursor(cursor);
                            cursor.close();
                        }

                    }
                }

                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(Loader loader, Object data) {
        movieDb.setTrailers(mTrailers);
        movieDb.setReviews(mReviews);

        ivPoster.setImageBitmap(movieDb.getPoster());
        if (mTrailers!=null){
            trailersAdapter.setTrailers(mTrailers);
            setListViewHeightBasedOnChildren(mTrailersListView);
        }

    }

    @Override
    public void onLoaderReset(Loader loader) {

    }

    public void setListViewHeightBasedOnChildren(ListView listView) {

        TrailersAdapter listAdapter = (TrailersAdapter) listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int elements = listAdapter.getCount();

        if (elements>0) {
            View listItem = listAdapter.getView(0, null, listView);
            listItem.measure(0,0);
            // get the height of a single item, multiply by the number of items and get the total height for the item,
            // extra space (more elements) is added
            int totalHeight = listItem.getMeasuredHeight() * (elements+2);

            ViewGroup.LayoutParams params = listView.getLayoutParams();

            //calculate the total height summing the height of the dividers too
            params.height = totalHeight
                    + (listView.getDividerHeight() * (listAdapter.getCount()-1));

            //set the height
            listView.setLayoutParams(params);
        }
    }

    private ArrayList<Trailers> fetchTrailersFromJson(String jsonString) throws JSONException {
        JSONObject json = new JSONObject(jsonString);
        JSONArray trailers = json.getJSONArray("results");
        ArrayList<Trailers> result = new ArrayList<>();

        for (int i = 0; i< trailers.length(); i++){
            JSONObject trailerObject = trailers.getJSONObject(i);
            String site = trailerObject.getString("site");
            if (site.equals("YouTube")){
                String url = "https://www.youtube.com/watch?v="+trailerObject.getString("key");
                result.add(new Trailers(trailerObject.getString("name"),url));
            }
        }
        Log.d(TAG,"Trailers Fethed ");
        return result;
    }

    private ArrayList<Reviews> fetchReviewsFromJson(String jsonString) throws JSONException{
        JSONObject json = new JSONObject(jsonString);
        JSONArray trailers = json.getJSONArray("results");
        ArrayList<Reviews> result = new ArrayList<>();

        for (int i = 0; i< trailers.length(); i++){
            JSONObject trailerObject = trailers.getJSONObject(i);
            result.add(new Reviews(trailerObject.getString("author"),trailerObject.getString("content")));
        }
        Log.d(TAG,"Reviews Fetched");
        return result;
    }


}

