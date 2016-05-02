package com.neekoentertainment.roadtripper.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.neekoentertainment.roadtripper.R;
import com.neekoentertainment.roadtripper.application.RoadTripperApplication;
import com.neekoentertainment.roadtripper.utils.MessagingManager;

public class SplashScreenActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;

    private static final String TAG = "SplashScreenActivity";

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        initPubnub();
        initGoogleApi();
    }

    private void initGoogleApi() {
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        ((RoadTripperApplication) getApplicationContext()).setGoogleApiClient(mGoogleApiClient);
    }

    private void initPubnub() {
        MessagingManager messagingManager = new MessagingManager();
        messagingManager.startPubnub();
        ((RoadTripperApplication) getApplicationContext()).setMessagingManager(messagingManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    /* GOOGLE API CALLBACKS */

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestLocationPermission();
        } else {
            startHomeActivity();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "Google Api Connection Suspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Google Api Connection Failed");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startHomeActivity();
                } else {
                    finish();
                }
                break;
            }
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    SplashScreenActivity.MY_PERMISSION_ACCESS_FINE_LOCATION);
        } else {
            startHomeActivity();
        }
    }

    private void startHomeActivity() {
        final EditText editText = (EditText) findViewById(R.id.editText);
        Button button = (Button) findViewById(R.id.button);
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editText != null) {
                        if (!editText.getText().toString().isEmpty() && !editText.getText().toString().trim().equals("")) {
                            Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                            intent.putExtra(getString(R.string.username), editText.getText().toString());
                            startActivity(intent);
                            finish();
                        } else {
                            editText.setError(getString(R.string.empty_user_name));
                        }
                    }
                }
            });
        }
    }
}
