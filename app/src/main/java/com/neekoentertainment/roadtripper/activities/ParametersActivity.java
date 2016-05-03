package com.neekoentertainment.roadtripper.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.neekoentertainment.roadtripper.R;
import com.neekoentertainment.roadtripper.utils.SharedPreferencesUtils;

public class ParametersActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_parameters);

        EditText spotifyURI = (EditText) findViewById(R.id.spotifyURI);
        if (spotifyURI != null)
            spotifyURI.setText(SharedPreferencesUtils.SPOTIFY_URI);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
            toolbar.setTitle(R.string.parameters);
        setSupportActionBar(toolbar);
    }

    public void onSaveClicked(View view) {

        EditText spotify = (EditText) findViewById(R.id.spotifyURI);

        if (spotify.getText().toString() != null && !spotify.getText().toString().equals(""))
            SharedPreferencesUtils.putSpotifyURIToSharedPreferences(spotify.getText().toString(), getApplicationContext());

        Toast.makeText(ParametersActivity.this, "Successfully saved", Toast.LENGTH_SHORT).show();
   }


}
