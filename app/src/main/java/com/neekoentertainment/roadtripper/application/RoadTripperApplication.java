package com.neekoentertainment.roadtripper.application;

import android.app.Application;

import com.google.android.gms.common.api.GoogleApiClient;
import com.neekoentertainment.roadtripper.utils.MessagingManager;

/**
 * Created by Nicolas on 4/3/2016.
 * Clean way to pass important data between activities. Better practice than a singleton.
 */
public class RoadTripperApplication extends Application {
    private GoogleApiClient mGoogleApiClient;
    private MessagingManager mMessagingManager;
    private String mUsername;

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setGoogleApiClient(GoogleApiClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public MessagingManager getMessagingManager() {
        return mMessagingManager;
    }

    public void setMessagingManager(MessagingManager mMessagingManager) {
        this.mMessagingManager = mMessagingManager;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setUsername(String mUsername) {
        this.mUsername = mUsername;
    }
}
