package com.permana.indra.popularmovies;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by asus on 26/09/2017.
 */

public class MovieDbAdapter extends RecyclerView.Adapter<MovieDbAdapter.PosterViewHolder> {
    private static final String TAG = MovieDbAdapter.class.getSimpleName();
    private ArrayList<MovieDb> mMovies;
    private final onPosterClickHandler mClickHandler;

    interface onPosterClickHandler {
        void onClick(MovieDb movie);

    }

    MovieDbAdapter(onPosterClickHandler clickHandler) {
        mMovies = new ArrayList<>();
        mClickHandler = clickHandler;
    }

    void addMovies(ArrayList<MovieDb> movies) {
        mMovies.addAll(movies);
        notifyDataSetChanged();
    }

    void clear() {
        mMovies.clear();
        notifyDataSetChanged();
    }

    void saveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("ADAPTER_MOVIEDB", mMovies);
    }

    void restoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "Restore instance state");
        if (savedInstanceState.containsKey("ADAPTER_MOVIEDB")) {
            ArrayList<MovieDb> savedMovies = savedInstanceState.getParcelableArrayList("ADAPTER_MOVIEDB");
            mMovies.clear();
            mMovies.addAll(savedMovies);
            notifyDataSetChanged();
        }
    }

    @Override
    public PosterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final Context context = parent.getContext();
        int layoutItemId = R.layout.movies_item;
        LayoutInflater inflater = LayoutInflater.from(context);

        View view = inflater.inflate(layoutItemId, parent, false);

        return new PosterViewHolder(view);

    }

    @Override
    public void onBindViewHolder(PosterViewHolder holder, int position) {
        holder.setImage(mMovies.get(position));
    }

    @Override
    public int getItemCount() {
        return mMovies.size();
    }

    class PosterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView mImageView;
        Context mContext;

        PosterViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.image_poster);
            mContext = itemView.getContext();
            itemView.setOnClickListener(this);
        }

        void setImage(MovieDb movie) {
            Uri posterUri = movie.getDetailPosterUri();
            Picasso.with(mContext).load(posterUri).into(mImageView);
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            MovieDb selectedMovie = mMovies.get(position);
            mClickHandler.onClick(selectedMovie);
        }
    }
}
