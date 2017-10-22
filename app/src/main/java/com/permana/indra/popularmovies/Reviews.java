package com.permana.indra.popularmovies;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by asus on 28/08/2017.
 */

public class Reviews {
    String author;
    String content;

    Reviews(String author, String content) {
        this.author = author;
        this.content = content;
    }

    static String arrayToString(ArrayList<Reviews> reviews) {
        String res = "";
        try {
            for (int i = 0; i < reviews.size(); i++) {
                res += reviews.get(i).author + ",reviewSeparator," + reviews.get(i).content;
                if (i < reviews.size() - 1) {
                    res += " -reviewSeparator- ";
                }
            }
        } catch (NullPointerException e) {
            return "";
        }
        return res;
    }

    static String singleArrayToStringReview(ArrayList<Reviews> reviews) {
        String res = "";
        try {
            for (int i = 0; i < 1; i++) {
                res += reviews.get(i).author + "\n" + reviews.get(i).content;
            }
        } catch (NullPointerException e) {
            return "";
        }
        return res;
    }

    static ArrayList<Reviews> stringToArray(String string) {
        String[] elements = string.split(" -reviewSeparator- ");
        ArrayList<Reviews> res = new ArrayList<>();

        for (String element : elements) {
            String[] item = element.split(",reviewSeparator,");
            try {
                res.add(new Reviews(item[0], item[1]));
            } catch (IndexOutOfBoundsException e) {
                Log.d("REVIEWS", e.toString());
            }
        }
        return res;
    }
}
