package com.neekoentertainment.roadtripper.application;

import android.app.Application;

import com.deezer.sdk.network.connect.DeezerConnect;
import com.google.android.gms.common.api.GoogleApiClient;
import com.neekoentertainment.roadtripper.utils.MessagingManager;

/**
 * Created by Nicolas on 4/3/2016.
 * Clean way to pass important data between activities. Better practice than a singleton.
 */
public class RoadTripperApplication extends Application {
    private GoogleApiClient mGoogleApiClient;
    private MessagingManager mMessagingManager;
    private DeezerConnect mDeezerConnect;

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

    public DeezerConnect getDeezerConnect() {
        return mDeezerConnect;
    }

    public void setDeezerConnect(DeezerConnect mDeezerConnect) {
        this.mDeezerConnect = mDeezerConnect;
    }
}
