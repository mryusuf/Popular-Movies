package com.permana.indra.popularmovies;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.util.ArrayList;

/**
 * Created by asus on 17/09/2017.
 */

public class MovieFavouriteAdapter extends BaseAdapter {
    Context context;
    ArrayList<MovieDb> movieDbs;
    private static LayoutInflater inflater = null;

    public MovieFavouriteAdapter(Context context, ArrayList<MovieDb> movieDbs){
        this.context = context;
        this.movieDbs=movieDbs;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return movieDbs.size();
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
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

        MovieDb movieDb=new MovieDb();
        movieDb=movieDbs.get(position);

        imageView.setImageBitmap(movieDb.getPoster());
        return imageView;
    }
}
