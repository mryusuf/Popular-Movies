package com.permana.indra.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.permana.indra.popularmovies.db.MovieContract;
import com.permana.indra.popularmovies.utilities.NetworkUtilsTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends AppCompatActivity implements MovieDbAdapter.onPosterClickHandler, LoaderManager.LoaderCallbacks<ArrayList<MovieDb>> {


    final String TAG = MainActivity.class.getSimpleName();
    RecyclerView mRecyclerView;
    TextView tvError;
    MovieDbAdapter movieDbsAdapter;
    private static final int LOADER_ID = 701;
    int mPagesLoaded;
    final int MAX_PAGES = 16;
    GridLayoutManager mGridLayoutManager;
    SharedPreferences mSharedPrefs;
    SharedPreferences.OnSharedPreferenceChangeListener mOnSharedPreferenceChangeListener;
    String actualCriterion;
    private String key;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        key = getResources().getString(R.string.key);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_posters);
        tvError = (TextView) findViewById(R.id.tv_posters_error);
        final int columns = getResources().getInteger(R.integer.gallery_columns);

        mGridLayoutManager = new GridLayoutManager(this, columns);

        mRecyclerView.setLayoutManager(mGridLayoutManager);
        mRecyclerView.setHasFixedSize(true);

        movieDbsAdapter = new MovieDbAdapter(this);

        mRecyclerView.setAdapter(movieDbsAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView,
                                   int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (!actualCriterion.equals("FavouriteMovie")) {
                    int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                    int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                    int pastVisiblesItems = mGridLayoutManager.findFirstVisibleItemPosition();

                    if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                        loadPosters();
                    }
                }
            }
        });

        mPagesLoaded = 0;


        initSharedPreferences();

        if (savedInstanceState != null) {
            Log.d(TAG, "Restoring adapter");
            movieDbsAdapter.restoreInstanceState(savedInstanceState);
            Log.d(TAG, movieDbsAdapter.getItemCount() + " items recovered");
            mRecyclerView.scrollToPosition(savedInstanceState.getInt("SCROLL_POSITION"));
        } else {
            loadPosters();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "Saving instance state");
        movieDbsAdapter.saveInstanceState(outState);
        int scrollPosition = mGridLayoutManager.findFirstVisibleItemPosition();
        outState.putInt("SCROLL_POSITION", scrollPosition);

    }

    private void loadPosters() {
        Log.d(TAG, "Loading posters");
        if (mPagesLoaded < MAX_PAGES) {
            Bundle args = new Bundle();
            args.putInt("page", mPagesLoaded + 1);
            getSupportLoaderManager().restartLoader(LOADER_ID, args, this);
        }
    }

    private void initSharedPreferences() {
        mSharedPrefs = getApplicationContext().getSharedPreferences("movie_preferences", MODE_PRIVATE);
        mOnSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                actualCriterion = sharedPreferences.getString(key, getString(R.string.sort_populer));
                Log.d(TAG, "Shared preferences for " + key + "changed. Pref: " + actualCriterion);
                mPagesLoaded = 0;
                movieDbsAdapter.clear();
                loadPosters();
            }
        };
        mSharedPrefs.registerOnSharedPreferenceChangeListener(mOnSharedPreferenceChangeListener);
        Log.d(TAG, "sort");
        actualCriterion = mSharedPrefs.getString("sort", "Favourite");

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        Bundle args = new Bundle();
        args.putBoolean("local", true);
        if (id == R.id.menu_pop) {
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            editor.putString("sort", getResources().getString(R.string.url_pop));
            editor.apply();
        } else if (id == R.id.menu_top) {
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            editor.putString("sort", getResources().getString(R.string.url_top));
            editor.apply();
        } else if (id == R.id.menu_fav) {
            SharedPreferences.Editor editor = mSharedPrefs.edit();
            editor.putString("sort", "Favourite");
            editor.apply();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<ArrayList<MovieDb>> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<ArrayList<MovieDb>>(this) {
            ArrayList<MovieDb> mData;

            @Override
            protected void onStartLoading() {
                Log.d(TAG, "Start Loading");
                super.onStartLoading();
                if (actualCriterion.equals("Favourite")) {
                    //force refresh
                    movieDbsAdapter.clear();
                    forceLoad();
                } else {
                        if (mData != null) {
                            deliverResult(mData);
                        }
                        else {
                            tvError.setVisibility(View.VISIBLE);
                            forceLoad();
                        }
                }
            }

            @Override
            public ArrayList<MovieDb> loadInBackground() {
                Log.d(TAG, "Load in background");
                if (args.size() == 0) {
                    return null;
                }
                int page = args.getInt("page");
                NetworkUtilsTask networker = new NetworkUtilsTask();
                if (!(actualCriterion.equals("Favourite"))) {
                    URL request = networker.buildMoviesUrl(page, actualCriterion, key);
                    try {
                        String JSONResponse = networker.getResponseFromHttpUrl(request);
                        ArrayList<MovieDb> res = fetchMoviesFromJson(JSONResponse);
                        mPagesLoaded++;
                        return res;

                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }

                    return null;
                } else {
                    Log.d(TAG, "Local Movies");
                    Cursor cursor = getContentResolver().query(MovieContract.MovieEntry.CONTENT_URI, null, null, null, null);
                    if (cursor != null) {
                        Log.d(TAG, "Cursor is not null");
                        ArrayList<MovieDb> res = fetchMoviesFromCursor(cursor);
                        cursor.close();
                        return res;
                    }
                    return null;
                }
            }

            @Override
            public void deliverResult(ArrayList<MovieDb> data) {
                mData = data;
                super.deliverResult(data);
            }
        };
    }


    @Override
    public void onLoadFinished(Loader<ArrayList<MovieDb>> loader, ArrayList<MovieDb> movies) {
        Log.d(TAG, "Load finished");
        if (movies != null) {
            movieDbsAdapter.addMovies(movies);
            showPosters();
        }
        else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<ArrayList<MovieDb>> loader) {
        Log.d(TAG, "Restarting loader");

    }

    private ArrayList<MovieDb> fetchMoviesFromJson(String jsonStr) throws JSONException {
        final String KEY_MOVIES = "results";

        JSONObject json = new JSONObject(jsonStr);
        JSONArray movies = json.getJSONArray(KEY_MOVIES);
        ArrayList<MovieDb> result = new ArrayList<>();

        for (int i = 0; i < movies.length(); i++) {
            MovieDb resMovie = MovieDb.getMovieFromJson(movies.getJSONObject(i));
            result.add(resMovie);
        }
        return result;
    }

    private ArrayList<MovieDb> fetchMoviesFromCursor(Cursor cursor) {
        ArrayList<MovieDb> result = new ArrayList<>();
        Log.d(TAG, "Found" + cursor.getCount() + " favourite");

        if (cursor.getCount() == 0) {
            return null;
        }
        if (cursor.moveToFirst()) {
            do {
                MovieDb movie = new MovieDb(
                        cursor.getLong(cursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_ID)),
                        cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_TITLE)),
                        cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_OVERVIEW)),
                        cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_POSTER_PATH)),
                        cursor.getDouble(cursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_AVG)),
                        cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.MOVIE_RELEASE_DATE))
                );

                movie.setPosterFromCursor(cursor);

                result.add(movie);

            } while (cursor.moveToNext());

        }

        return result;
    }


    @Override
    public void onClick(MovieDb moviedb) {
        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(moviedb.EXTRA_MOVIE, moviedb.toBundle());
        startActivity(intent);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        tvError.setVisibility(View.VISIBLE);
    }
    private void showPosters() {
        mRecyclerView.setVisibility(View.VISIBLE);
        tvError.setVisibility(View.INVISIBLE);
    }
}



