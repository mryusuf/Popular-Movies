package com.permana.indra.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.permana.indra.popularmovies.utilities.NetworkUtilsTask;

public class MainActivity extends AppCompatActivity {
    GridView gridView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        gridView = (GridView) findViewById(R.id.poster_grid);
        gridView.setOnItemClickListener(moviePosterClickListener);

        if (savedInstanceState == null) {
            // Get data from the Internet
            getMovie(getResources().getString(R.string.url_pop));
        } else {
            // Get data from local resources
            // Get Movie objects
            Parcelable[] parcelable = savedInstanceState.
                    getParcelableArray(getString(R.string.parcel_moviedb));

            if (parcelable != null) {
                int numMovieObjects = parcelable.length;
                MovieDb[] movies = new MovieDb[numMovieObjects];
                for (int i = 0; i < numMovieObjects; i++) {
                    movies[i] = (MovieDb) parcelable[i];
                }

                // Load movie objects into view
                gridView.setAdapter(new MovieDBAdapter(this, movies));
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        //Log.v(LOG_TAG, "onSaveInstanceState");

        int numMovieObjects = gridView.getCount();
        if (numMovieObjects > 0) {
            // Get Movie objects from gridview
            MovieDb[] movies = new MovieDb[numMovieObjects];
            for (int i = 0; i < numMovieObjects; i++) {
                movies[i] = (MovieDb) gridView.getItemAtPosition(i);
            }

            // Save Movie objects to bundle
            outState.putParcelableArray(getString(R.string.parcel_moviedb), movies);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id =item.getItemId();

        if(id==R.id.menu_pop){
            getMovie(getResources().getString(R.string.url_pop));
        }
        else if (id==R.id.menu_top){

            getMovie(getResources().getString(R.string.url_top));
        }


        return super.onOptionsItemSelected(item);
    }


    private final GridView.OnItemClickListener moviePosterClickListener = new GridView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MovieDb movie = (MovieDb) parent.getItemAtPosition(position);

            Intent intent = new Intent(getApplicationContext(), DetailActivity.class);
            intent.putExtra(getResources().getString(R.string.parcel_moviedb), movie);

            startActivity(intent);
        }
    };



    private void getMovie(String sortMethod) {
        if (isNetworkAvailable()) {
            // Key needed to get data from TMDb
            String apiKey = getString(R.string.key);

            // Listener for when AsyncTask is ready to update UI
            NetworkUtilsTask.OnTaskCompleted taskCompleted = new NetworkUtilsTask.OnTaskCompleted() {
                @Override
                public void onFetchMoviesTaskCompleted(MovieDb[] movies) {
                    gridView.setAdapter(new MovieDBAdapter(getApplicationContext(), movies));
                }
            };

            // Execute task
            NetworkUtilsTask movieTask = new NetworkUtilsTask(taskCompleted, apiKey);
            movieTask.execute(sortMethod);
        } else {
            Toast.makeText(this,"Check Internet Connection", Toast.LENGTH_LONG).show();
        }
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}



