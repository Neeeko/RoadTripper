package com.neekoentertainment.roadtripper.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Baptiste on 02/05/2016.
 */
public class SharedPreferencesUtils {
        public static final String SPOTIFY_URI = "spotify:user:tommikohn:playlist:7qfB2KGVfhUpFhjK5Lnxr0";

        public static void putSpotifyURIToSharedPreferences(String spotifyURI, Context context) {

            SharedPreferences sharedPreferences = context.getSharedPreferences(SPOTIFY_URI, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(SPOTIFY_URI, spotifyURI);
            editor.apply();
        }

}
