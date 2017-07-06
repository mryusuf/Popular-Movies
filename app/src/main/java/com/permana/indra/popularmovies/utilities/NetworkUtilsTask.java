package com.permana.indra.popularmovies.utilities;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.permana.indra.popularmovies.MovieDb;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by My on 06/07/2017.
 */

public class NetworkUtilsTask extends AsyncTask<String, Void, MovieDb[]> {
    /**
     * For logging purposes
     */
    private final String LOG_TAG = NetworkUtilsTask.class.getSimpleName();

    /**
     * TMDb API key
     */
    private final String mApiKey;

    /**
     * Interface / listener
     */
    private final OnTaskCompleted mListener;

    /**
     * Constructor
     *
     * @param listener UI listener
     * @param apiKey TMDb API key
     */
    public NetworkUtilsTask(OnTaskCompleted listener, String apiKey) {
        super();

        mListener = listener;
        mApiKey = apiKey;
    }

    @Override
    protected MovieDb[] doInBackground(String... params) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        // Holds data returned from the API
        String moviesJsonStr = null;

        try {
            URL url = getApiUrl(params);

            // Start connecting to get JSON
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder builder = new StringBuilder();

            if (inputStream == null) {
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Adds '\n' at last line if not already there.
                // This supposedly makes it easier to debug.
                builder.append(line).append("\n");
            }

            if (builder.length() == 0) {
                // No data found. Nothing more to do here.
                return null;
            }

            moviesJsonStr = builder.toString();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
            return null;
        } finally {
            // Tidy up: release url connection and buffered reader
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        try {
            // Make sense of the JSON data
            return getMoviesDataFromJson(moviesJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Extracts data from the JSON object and returns an Array of movie objects.
     *
     * @param moviesJsonStr JSON string to be traversed
     * @return Array of Movie objects
     * @throws JSONException
     */
    private MovieDb[] getMoviesDataFromJson(String moviesJsonStr) throws JSONException {
        // JSON tags
        final String TAG_RESULTS = "results";
        final String TAG_ORIGINAL_TITLE = "original_title";
        final String TAG_POSTER_PATH = "poster_path";
        final String TAG_OVERVIEW = "overview";
        final String TAG_VOTE_AVERAGE = "vote_average";
        final String TAG_RELEASE_DATE = "release_date";

        // Get the array containing hte movieDbs found
        JSONObject moviesJson = new JSONObject(moviesJsonStr);
        JSONArray resultsArray = moviesJson.getJSONArray(TAG_RESULTS);

        // Create array of Movie objects that stores data from the JSON string
        MovieDb[] movieDbs = new MovieDb[resultsArray.length()];

        // Traverse through movieDbs one by one and get data
        for (int i = 0; i < resultsArray.length(); i++) {
            // Initialize each object before it can be used
            movieDbs [i] = new MovieDb();

            // Object contains all tags we're looking for
            JSONObject movieInfo = resultsArray.getJSONObject(i);

            // Store data in movie object
            movieDbs [i].setOriginalTitle(movieInfo.getString(TAG_ORIGINAL_TITLE));
            movieDbs [i].setPosterPath(movieInfo.getString(TAG_POSTER_PATH));
            movieDbs [i].setOverview(movieInfo.getString(TAG_OVERVIEW));
            movieDbs [i].setVoteAverage(movieInfo.getString(TAG_VOTE_AVERAGE));
            movieDbs [i].setReleaseDate(movieInfo.getString(TAG_RELEASE_DATE));
        }

        return movieDbs ;
    }

    /**
     * Creates and returns an URL.
     *
     * @param parameters Parameters to be used in the API call
     * @return URL formatted with parameters for the API
     * @throws MalformedURLException
     */
    private URL getApiUrl(String[] parameters) throws MalformedURLException {
        final String TMDB_BASE_URL = "https://api.themoviedb.org/3/discover/movie?";
        final String SORT_BY_PARAM = "sort_by";
        final String API_KEY_PARAM = "api_key";

        Uri builtUri = Uri.parse(TMDB_BASE_URL).buildUpon()
                .appendQueryParameter(SORT_BY_PARAM, parameters[0])
                .appendQueryParameter(API_KEY_PARAM, mApiKey)
                .build();

        return new URL(builtUri.toString());
    }

    @Override
    protected void onPostExecute(MovieDb[] movieDbs ) {
        super.onPostExecute(movieDbs );

        // Notify UI
        mListener.onFetchMoviesTaskCompleted(movieDbs );
    }

    public interface OnTaskCompleted {
        void onFetchMoviesTaskCompleted(MovieDb[] movieDbs );
    }

}
