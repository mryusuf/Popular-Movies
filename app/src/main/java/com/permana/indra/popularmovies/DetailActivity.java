package com.permana.indra.popularmovies;

import android.content.Intent;
import android.graphics.Movie;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.ParseException;

public class DetailActivity extends AppCompatActivity {
    private final String LOG_TAG = DetailActivity.class.getSimpleName();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        TextView tvOriginalTitle = (TextView) findViewById(R.id.tv_title);
        ImageView ivPoster = (ImageView) findViewById(R.id.poster_detail);
        TextView tvOverView = (TextView) findViewById(R.id.tv_overview);
        TextView tvVoteAverage = (TextView) findViewById(R.id.tv_vote_average);
        TextView tvReleaseDate = (TextView) findViewById(R.id.tv_release_date);

        Intent intent = getIntent();
        MovieDb movieDb = intent.getParcelableExtra(getString(R.string.parcel_moviedb));

        tvOriginalTitle.setText(movieDb.getOriginalTitle());

        Picasso.with(this)
                .load(movieDb.getPosterPath())
                .into(ivPoster);


        tvVoteAverage.setText(movieDb.getVoteAverage());


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
    }

    }

