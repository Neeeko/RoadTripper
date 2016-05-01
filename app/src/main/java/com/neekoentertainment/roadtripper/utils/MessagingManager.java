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

    public Callback publishCallback = new Callback() {

        @Override
        public void successCallback(String channel, Object response) {
            Log.d("Messaging Manager", "Sent Message: " + response.toString());
        }

        @Override
        public void errorCallback(String channel, PubnubError error) {
            Log.d("Messaging Manager", error.toString());
        }
    };
    private Pubnub mPubnub;
    private String TAG = "Messaging Manager";

    public void startPubnub() {
        mPubnub = new Pubnub("pub-c-61f34441-8781-4fe2-b6d1-5841129c122b", "sub-c-3034f7aa-f9e0-11e5-a492-02ee2ddab7fe");
    }

    public void subscribe(String channelName, Callback subscribeCallback) {
        try {
            mPubnub.subscribe(channelName, subscribeCallback);
            Log.d(TAG, "Subscribing to channel " + channelName);
        } catch (PubnubException e) {
            Log.e(TAG, e.getMessage());
        }
    }

    public void broadcastLocation(String channelName, double latitude, double longitude) {
        JSONObject message = new JSONObject();
        try {
            message.put("lat", latitude);
            message.put("lng", longitude);
            message.put("name", channelName);
        } catch (JSONException e) {
            Log.e(TAG, e.getMessage());
        }
        Log.d(TAG, "Starting broadcast on channel " + channelName);
        mPubnub.publish(channelName, message, publishCallback);
    }
}
