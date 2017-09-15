package com.permana.indra.popularmovies;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by asus on 28/08/2017.
 */

public class Trailers {
    String title;
    String url;

    Trailers(String title, String url) {
        this.title = title;
        this.url = url;
    }

    static String arrayToString(ArrayList<Trailers> trailers){
        String res = "";
        try {
            for (int i = 0; i < trailers.size(); i++) {
                res += trailers.get(i).title + "," + trailers.get(i).url;
                if (i < trailers.size() - 1) {
                    res += " -trailerSeparator- ";
                }
            }
        }catch (NullPointerException e){
            return "";
        }
        return res;
    }

    static ArrayList<Trailers> stringToArray(String string){
        String[] elements = string.split(" -trailerSeparator- ");

        ArrayList<Trailers> res = new ArrayList<>();

        for (String element : elements) {
            try {
                String[] item = element.split(",");
                res.add(new Trailers(item[0], item[1]));
            } catch (IndexOutOfBoundsException e) {
                Log.d("TRAILERS", e.toString());
            }
        }

        return res;
    }

}
