package com.neekoentertainment.roadtripper.utils;

import android.util.Log;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;
import com.pubnub.api.PubnubError;
import com.pubnub.api.PubnubException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Nicolas on 4/30/2016.
 */
public class MessagingManager {

    public static final String JSON_LAT = "lat";
    public static final String JSON_LNG = "lng";
    public static final String JSON_NAME = "name";

    private static final String TAG = "Messaging Manager";
    private static final String SENT_MESSAGE = "Sent Message: ";
    private static final String SUBSCRIBING_CHANNEL = "Subscribing to channel ";
    private static final String STARTING_BROADCAST = "Starting broadcast on channel ";
    private static final String PUBNUB_PUB = "pub-c-61f34441-8781-4fe2-b6d1-5841129c122b";
    private static final String PUBNUB_SUB = "sub-c-3034f7aa-f9e0-11e5-a492-02ee2ddab7fe";
    public Callback publishCallback = new Callback() {

        @Override
        public void successCallback(String channel, Object response) {
            Log.d(TAG, SENT_MESSAGE + response.toString());
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            Log.e(TAG, error.toString());
        }
    };
    private Pubnub mPubnub;

    public void startPubnub() {
        mPubnub = new Pubnub(PUBNUB_PUB, PUBNUB_SUB);
    }

    public void subscribe(String channelName, Callback subscribeCallback) {
        try {
            mPubnub.subscribe(channelName, subscribeCallback);
            Log.d(TAG, SUBSCRIBING_CHANNEL + channelName);
        } catch (PubnubException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void broadcastLocation(String channelName, double latitude, double longitude) {
        JSONObject message = new JSONObject();
        try {
            message.put(JSON_LAT, latitude);
            message.put(JSON_LNG, longitude);
            message.put(JSON_NAME, channelName);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        Log.d(TAG, STARTING_BROADCAST + channelName);
        if (channelName != null) {
            mPubnub.publish(channelName, message, publishCallback);
        } else {
            Log.d("Test", "channel name is null");
        }
    }
}
