package com.neekoentertainment.roadtripper.utils;

import android.app.Activity;
import android.os.Bundle;

import com.deezer.sdk.model.Permissions;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.connect.SessionStore;
import com.deezer.sdk.network.connect.event.DialogListener;

/**
 * Created by Nicolas on 5/4/2016.
 */
public class ServicesAuthentication {

    private static final String APP_ID = "178502";

    private static final String[] permissions = new String[]{
            Permissions.BASIC_ACCESS,
            Permissions.MANAGE_LIBRARY,
            Permissions.LISTENING_HISTORY};

    public static void getDeezerConnect(final Activity mContext, final DeezerConnection mCallback) {
        final DeezerConnect deezerConnect = new DeezerConnect(mContext.getApplicationContext(), APP_ID);
        SessionStore sessionStore = new SessionStore();
        if (!sessionStore.restore(deezerConnect, mContext.getApplicationContext())) {
            // No value on the Session Store, or invalid token
            mCallback.onDeezerDisconnected();
            DialogListener listener = new DialogListener() {
                public void onComplete(Bundle values) {
                    SessionStore sessionStore = new SessionStore();
                    sessionStore.save(deezerConnect, mContext.getApplicationContext());
                    mCallback.onDeezerConnected(deezerConnect);
                }

                public void onCancel() {
                    mCallback.onDeezerFailed("Connection process canceled");
                }

                public void onException(Exception e) {
                    mCallback.onDeezerFailed(e.getMessage());
                }
            };
            deezerConnect.authorize(mContext, permissions, listener);
        }
        // Retrieved from the Session Store
        if (deezerConnect.isSessionValid())
            mCallback.onDeezerConnected(deezerConnect);
    }

    public interface DeezerConnection {
        void onDeezerConnected(DeezerConnect deezerConnect);

        void onDeezerDisconnected();

        void onDeezerFailed(String e);
    }
}