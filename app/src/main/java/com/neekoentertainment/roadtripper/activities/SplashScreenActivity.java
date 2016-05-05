package com.neekoentertainment.roadtripper.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.deezer.sdk.model.PaginatedList;
import com.deezer.sdk.model.Playlist;
import com.deezer.sdk.network.connect.DeezerConnect;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.JsonUtils;
import com.deezer.sdk.network.request.event.RequestListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.neekoentertainment.roadtripper.R;
import com.neekoentertainment.roadtripper.application.RoadTripperApplication;
import com.neekoentertainment.roadtripper.utils.MessagingManager;
import com.neekoentertainment.roadtripper.utils.ServicesAuthentication;
import com.squareup.picasso.Picasso;

import org.json.JSONException;

import java.util.ArrayList;

public class SplashScreenActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final int MY_PERMISSION_ACCESS_FINE_LOCATION = 1;

    public static final String DEEZER_PLAYLISTS_URL = "user/me/playlists";
    public static final String INTENT_EXTRA_USERNAME = "intent_extra_username";
    public static final String INTENT_EXTRA_PLAYLIST_ID = "intent_extra_playlist_id";
    public static final String INTENT_EXTRA_PLAYLISTS_LIST = "intent_extra_playlists_list";
    public static final String RETURN_INTENT_EXTRA_SELECTED_PLAYLIST = "return_intent_extra_selected_playlist";

    private static final int GET_PLAYLIST = 1;
    private static final String TAG = "SplashScreenActivity";

    private GoogleApiClient mGoogleApiClient;
    private DeezerConnect mDeezerConnect;
    private ArrayList<Playlist> mPlaylistList;
    private ListView mListView;
    private RelativeLayout mConnectButton;
    private ProgressBar mProgressBar;
    private RelativeLayout mSelectedPlaylistHolder;
    private long mIdPlaylist = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        initPubnub();
        initGoogleApi();
        RelativeLayout deezerConnect = (RelativeLayout) findViewById(R.id.deezer_connect);
        mListView = (ListView) findViewById(R.id.playlist_listview);
        mProgressBar = (ProgressBar) findViewById(R.id.progress);
        mSelectedPlaylistHolder = (RelativeLayout) findViewById(R.id.selected_playlist_holder);
        if (deezerConnect != null) {
            deezerConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mConnectButton = (RelativeLayout) v;
                    connectToDeezer();
                }
            });
        }
    }

    private void connectToDeezer() {
        final LinearLayout container = (LinearLayout) findViewById(R.id.container);
        final Context context = this;
        mPlaylistList = new ArrayList<>();
        ServicesAuthentication.DeezerConnection mCallback = new ServicesAuthentication.DeezerConnection() {
            @Override
            public void onDeezerConnected(DeezerConnect deezerConnect) {
                mDeezerConnect = deezerConnect;
                ((RoadTripperApplication) getApplicationContext()).setDeezerConnect(mDeezerConnect);
                if (mConnectButton != null) {
                    mConnectButton.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.VISIBLE);
                }
                getCurrentUserPlaylists(DEEZER_PLAYLISTS_URL, new OnDataRetrieved() {
                    @Override
                    public void onDataRetrieved() {
                        Intent intent = new Intent(context, PlaylistPickerActivity.class);
                        intent.putParcelableArrayListExtra(INTENT_EXTRA_PLAYLISTS_LIST, mPlaylistList);
                        startActivityForResult(intent, GET_PLAYLIST);
                    }
                });
            }

            @Override
            public void onDeezerDisconnected() {
            }

            @Override
            public void onDeezerFailed(String e) {
                if (mConnectButton != null) {
                    mListView.setVisibility(View.GONE);
                    mConnectButton.setVisibility(View.VISIBLE);
                    mConnectButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            connectToDeezer();
                        }
                    });
                }
                if (container != null) {
                    Snackbar.make(container, "Connection to Deezer Failed.", Snackbar.LENGTH_SHORT);
                }
            }
        };
        ServicesAuthentication.getDeezerConnect(this, mCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == GET_PLAYLIST) {
            mProgressBar.setVisibility(View.GONE);
            if (resultCode == AppCompatActivity.RESULT_OK) {
                Playlist selectedPlaylist = data.getParcelableExtra(RETURN_INTENT_EXTRA_SELECTED_PLAYLIST);
                if (mSelectedPlaylistHolder != null) {
                    final Context context = this;
                    mIdPlaylist = selectedPlaylist.getId();
                    LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    View selectedPlaylistView = inflater.inflate(R.layout.playlist_view, mSelectedPlaylistHolder, false);
                    ImageView coverImage = (ImageView) selectedPlaylistView.findViewById(R.id.playlist_cover);
                    TextView creator = (TextView) selectedPlaylistView.findViewById(R.id.playlist_creator);
                    creator.setSelected(true);
                    TextView title = (TextView) selectedPlaylistView.findViewById(R.id.playlist_title);
                    title.setSelected(true);
                    creator.setText(selectedPlaylist.getCreator().getName());
                    title.setText(selectedPlaylist.getTitle());
                    Picasso.with(this).load(selectedPlaylist.getSmallImageUrl()).fit().centerCrop().into(coverImage);
                    mSelectedPlaylistHolder.setVisibility(View.VISIBLE);
                    mSelectedPlaylistHolder.addView(selectedPlaylistView);
                    mSelectedPlaylistHolder.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(context, PlaylistPickerActivity.class);
                            intent.putParcelableArrayListExtra(INTENT_EXTRA_PLAYLISTS_LIST, mPlaylistList);
                            startActivityForResult(intent, GET_PLAYLIST);
                        }
                    });
                }
            }
            if (resultCode == AppCompatActivity.RESULT_CANCELED) {
                if (mSelectedPlaylistHolder.getVisibility() != View.VISIBLE)
                    mConnectButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void getCurrentUserPlaylists(String url, final OnDataRetrieved onDataRetrieved) {
        RequestListener requestListener = new RequestListener() {
            @SuppressWarnings("unchecked")
            @Override
            public void onComplete(String result, Object o) {
                try {
                    PaginatedList<Playlist> userPlaylists;
                    userPlaylists = (PaginatedList<Playlist>) JsonUtils.deserializeJson(result);
                    for (Playlist album : userPlaylists) {
                        mPlaylistList.add(album);
                    }
                    if (userPlaylists.getNextUrl() != null && !userPlaylists.getNextUrl().isEmpty()) {
                        getCurrentUserPlaylists(userPlaylists.getNextUrl(), onDataRetrieved);
                    } else {
                        onDataRetrieved.onDataRetrieved();
                    }
                } catch (JSONException e) {
                    Log.e("DeezerConnect", e.getMessage());
                }
            }

            public void onException(Exception e, Object requestId) {
            }
        };
        DeezerRequest request = new DeezerRequest(url);
        request.setId("getPlaylists");
        mDeezerConnect.requestAsync(request, requestListener);
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

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestLocationPermission();
        } else {
            startHomeActivity();
        }
    }

    /* GOOGLE API CALLBACKS */

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
                            intent.putExtra(INTENT_EXTRA_USERNAME, editText.getText().toString());
                            if (mIdPlaylist != -1) {
                                intent.putExtra(INTENT_EXTRA_PLAYLIST_ID, mIdPlaylist);
                            }
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

    public interface OnDataRetrieved {
        void onDataRetrieved();
    }
}
