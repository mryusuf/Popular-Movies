package com.permana.indra.popularmovies;

import android.app.Activity;
import android.content.Context;
import android.graphics.Movie;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;


/**
 * Created by My on 01/07/2017.
 */

class MovieDBAdapter extends BaseAdapter {
    private final Context context;
    private final MovieDb[] movieDbs;

    public MovieDBAdapter(Context context, MovieDb[] movieDbs) {
        this.context=context;
        this.movieDbs=movieDbs;
    }

    @Override
    public int getCount() {
        if(movieDbs.length==0 || movieDbs==null){
            return -1;
        }

        return movieDbs.length;
    }

    @Override
    public MovieDb getItem(int i) {
        if(movieDbs.length==0 || movieDbs==null){
            return null;
        }
        return movieDbs[i];
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ImageView imageView;

        if(convertView==null){
            imageView=new ImageView(context);
            imageView.setAdjustViewBounds(true);
        }else{
            imageView=(ImageView)convertView;
        }

        Picasso.with(context).load(movieDbs[position].getDetailPosterUri()).into(imageView);
        Log.d("posterPath= ", movieDbs[position].getDetailPosterUri());
        return imageView;
    }
}
