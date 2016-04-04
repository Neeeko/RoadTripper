package com.neekoentertainment.roadtripper.application;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Nicolas on 4/3/2016.
 * Clean way to pass important data between activities. Better practice than a singleton.
 */
public class RoadTripperApplication extends Application {
    private GoogleApiClient mGoogleApiClient;

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }
}
