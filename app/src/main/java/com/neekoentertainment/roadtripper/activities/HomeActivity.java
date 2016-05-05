package com.neekoentertainment.roadtripper.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.deezer.sdk.model.PlayableEntity;
import com.deezer.sdk.model.Track;
import com.deezer.sdk.network.request.DeezerRequest;
import com.deezer.sdk.network.request.DeezerRequestFactory;
import com.deezer.sdk.network.request.event.DeezerError;
import com.deezer.sdk.network.request.event.JsonRequestListener;
import com.deezer.sdk.player.PlaylistPlayer;
import com.deezer.sdk.player.event.PlayerState;
import com.deezer.sdk.player.event.PlayerWrapperListener;
import com.deezer.sdk.player.exception.TooManyPlayersExceptions;
import com.deezer.sdk.player.networkcheck.WifiAndMobileNetworkStateChecker;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.neekoentertainment.roadtripper.R;
import com.neekoentertainment.roadtripper.application.RoadTripperApplication;
import com.neekoentertainment.roadtripper.utils.MessagingManager;
import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by Nicolas on 4/3/2016.
 */
public class HomeActivity extends AppCompatActivity implements LocationListener, PlayerWrapperListener {

    private static final int INTERVAL = 10000;
    private static final int FASTEST_INTERVAL = 5000;
    private static final String TAG = "HomeActivity";

    private Marker myLastPos;
    private Marker myFriendLastPos;
    private boolean isFirstLaunch = true;
    private String mUsername;
    private GoogleApiClient mGoogleApiClient;
    private MessagingManager mMessagingManager;
    private GoogleMap mGoogleMap;
    private Boolean mCameraLock = true;

    private BroadcastAsyncTask mBroadcastAsyncTask;
    private SubscribeAsyncTask mSubscribeAsyncTask;

    private long mPlaylistId;
    private PlaylistPlayer mPlaylistPlayer;

    private String mFirstSongTitle;
    private String mFirstSongArtistName;
    private String mFirstSongAlbumUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        mGoogleApiClient = ((RoadTripperApplication) getApplicationContext()).getGoogleApiClient();
        mMessagingManager = ((RoadTripperApplication) getApplicationContext()).getMessagingManager();
        FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.fab);
        final Context context = this;
        if (mFab != null) {
            mFab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                    LayoutInflater layoutInflater = LayoutInflater.from(context);
                    final View dialogView = layoutInflater.inflate(R.layout.add_friend_dialog, null);
                    final EditText editText = (EditText) dialogView.findViewById(R.id.friendName);
                    alertDialogBuilder.setView(dialogView)
                            .setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!editText.getText().toString().isEmpty()
                                            && !editText.getText().toString().trim().equals("")) {
                                        subscribeToFriend(editText.getText().toString());
                                    } else {
                                        Toast.makeText(context, getString(R.string.empty_friend_name), Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setTitle(getString(R.string.dialog_title))
                            .create()
                            .show();
                }
            });
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        setMap();
        if (getIntent() != null) {
            if (getIntent().getStringExtra(SplashScreenActivity.INTENT_EXTRA_USERNAME) != null) {
                mUsername = getIntent().getStringExtra(SplashScreenActivity.INTENT_EXTRA_USERNAME);
            }
            mPlaylistId = getIntent().getLongExtra(SplashScreenActivity.INTENT_EXTRA_PLAYLIST_ID, -1);
        }

        initializeUiElements();

    }

    @Override
    public void onBackPressed() {
        if (mPlaylistPlayer != null && mPlaylistId != -1) {
            mPlaylistPlayer.stop();
            mPlaylistPlayer.release();
        }
        super.onBackPressed();
    }

    protected void initializeUiElements() {
        changeUIButtonState(false);

        TextView titleTxt = (TextView) findViewById(R.id.title_text);
        TextView artistTxt = (TextView) findViewById(R.id.artist_text);

        if (titleTxt != null)
            titleTxt.setSelected(true);
        if (artistTxt != null)
            artistTxt.setSelected(true);


        if (mPlaylistId != -1) {
            try {
                mPlaylistPlayer = new PlaylistPlayer(getApplication(),
                        ((RoadTripperApplication) getApplicationContext()).getDeezerConnect(),
                        new WifiAndMobileNetworkStateChecker());
                mPlaylistPlayer.addPlayerListener(this);

                DeezerRequest playlistTracksRequest = DeezerRequestFactory.requestPlaylistTracks(mPlaylistId);
                ((RoadTripperApplication) getApplicationContext()).getDeezerConnect().requestAsync(playlistTracksRequest, new JsonRequestListener() {
                    @Override
                    public void onResult(Object result, Object requestId) {
                        List<Track> trackList = (List<Track>) result;

                        if (result == null || ((List<Track>) result).size() == 0) {
                            RelativeLayout playerLayout = (RelativeLayout) findViewById(R.id.player_layout);
                            FloatingActionButton addFab = (FloatingActionButton) findViewById(R.id.fab);

                            if (addFab != null) {
                                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) addFab.getLayoutParams();

                                params.setMargins(0, 0, 20, 20);

                                params.addRule(RelativeLayout.ABOVE, 0);
                                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                                addFab.setLayoutParams(params);
                            }

                            if (playerLayout != null)
                                playerLayout.setVisibility(View.GONE);

                            Toast.makeText(HomeActivity.this, getString(R.string.empty_playlist), Toast.LENGTH_SHORT).show();
                        } else {
                            mFirstSongTitle = trackList.get(0).getTitle();
                            mFirstSongArtistName = trackList.get(0).getArtist().getName();
                            mFirstSongAlbumUrl = trackList.get(0).getAlbum().getCoverUrl();

                            TextView titleTxt = (TextView) findViewById(R.id.title_text);
                            TextView artistTxt = (TextView) findViewById(R.id.artist_text);

                            if (titleTxt != null)
                                titleTxt.setText(trackList.get(0).getTitle());
                            if (artistTxt != null)
                                artistTxt.setText(trackList.get(0).getArtist().getName());

                            ImageView albumImg = (ImageView) findViewById(R.id.album_img);
                            if (albumImg != null)
                                Picasso.with(getApplicationContext()).load(trackList.get(0).getAlbum().getCoverUrl()).fit().centerCrop().into(albumImg);
                        }
                    }

                    @Override
                    public void onUnparsedResult(String s, Object o) {

                    }

                    @Override
                    public void onException(Exception e, Object o) {
                        e.printStackTrace();
                        Log.e(TAG, e.getMessage());
                    }
                });

            } catch (TooManyPlayersExceptions | DeezerError e) {
                e.printStackTrace();
                Log.e(TAG, e.getMessage());
            }
        } else {
            RelativeLayout playerLayout = (RelativeLayout) findViewById(R.id.player_layout);
            FloatingActionButton addFab = (FloatingActionButton) findViewById(R.id.fab);

            if (addFab != null) {
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) addFab.getLayoutParams();

                params.setMargins(0, 0, 20, 20);

                params.addRule(RelativeLayout.ABOVE, 0);
                params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                addFab.setLayoutParams(params);
            }

            if (playerLayout != null)
                playerLayout.setVisibility(View.GONE);
        }
        changeUIButtonState(true);
    }

    protected void changeUIButtonState(boolean enabled) {
        ImageButton prevButton = (ImageButton) findViewById(R.id.prev_button);
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.play_pause_button);
        ImageButton nextButton = (ImageButton) findViewById(R.id.next_button);

        if (prevButton != null) {
            prevButton.setEnabled(enabled);
        }
        if (playPauseButton != null) {
            playPauseButton.setEnabled(enabled);
        }
        if (nextButton != null) {
            nextButton.setEnabled(enabled);
        }
    }

    public void onPrevClicked(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click_animation));

        if (mPlaylistPlayer != null) {
            mPlaylistPlayer.skipToPreviousTrack();
        }
    }

    public void onPlayPauseClicked(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click_animation));

        if (mPlaylistPlayer != null && mPlaylistPlayer.getPlayerState() == PlayerState.PLAYING)
        {
            ImageButton playPauseButton = (ImageButton) findViewById(R.id.play_pause_button);
            if (playPauseButton != null)
                playPauseButton.setBackgroundResource(R.drawable.playbutton);
            mPlaylistPlayer.pause();
        } else if (mPlaylistPlayer != null && mPlaylistPlayer.getPlayerState() == PlayerState.PAUSED) {
            ImageButton playPauseButton = (ImageButton) findViewById(R.id.play_pause_button);
            if (playPauseButton != null)
                playPauseButton.setBackgroundResource(R.drawable.pausebutton);
            mPlaylistPlayer.play();
        } else if (mPlaylistPlayer != null && (mPlaylistPlayer.getPlayerState() == PlayerState.STARTED ||
                mPlaylistPlayer.getPlayerState() == PlayerState.STOPPED ||
                mPlaylistPlayer.getPlayerState() == PlayerState.PLAYBACK_COMPLETED)) {
            mPlaylistPlayer.playPlaylist(mPlaylistId);
            ImageButton playPauseButton = (ImageButton) findViewById(R.id.play_pause_button);
            if (playPauseButton != null)
                playPauseButton.setBackgroundResource(R.drawable.pausebutton);
        }
    }

    public void onNextClicked(View v) {
        v.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.click_animation));

        if (mPlaylistPlayer != null) {
            mPlaylistPlayer.skipToNextTrack();
        }
    }

    @Override
    protected void onPause() {
        if (mSubscribeAsyncTask != null && mSubscribeAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            mSubscribeAsyncTask.cancel(true);
        }

        if (mBroadcastAsyncTask != null && mBroadcastAsyncTask.getStatus() == AsyncTask.Status.RUNNING) {
            mBroadcastAsyncTask.cancel(true);
        }
        super.onPause();
    }

    private void subscribeToFriend(String name) {
        mSubscribeAsyncTask = new SubscribeAsyncTask();
        mSubscribeAsyncTask.execute(name);
    }

    private Location createLocation(double lat, double lng, String name) {
        Location location = new Location(name + getString(R.string.name_position));
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setTime(new Date().getTime());
        return location;
    }

    private void setMap() {
        SupportMapFragment mMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mMapFragment.getMapAsync(new com.google.android.gms.maps.OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                LocationRequest locationRequest = getLocationRequest();
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, HomeActivity.this);
                mGoogleMap.getUiSettings().setMapToolbarEnabled(false);
                mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
            }
        });
    }

    private void setPosition(final Location location, final String username, final boolean isMyPosition) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCameraLock && isFirstLaunch) {
                    isFirstLaunch = false;
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(location.getLatitude(), location.getLongitude()))
                            .zoom(17.0f)
                            .build();
                    mGoogleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
                if (isMyPosition) {
                    // First position of user
                    if (myLastPos == null) {
                        myLastPos = mGoogleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                .title(username + getString(R.string.name_position)));
                    } else {
                        myLastPos.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                } else {
                    // First position of friend
                    if (myFriendLastPos == null) {
                        myFriendLastPos = mGoogleMap.addMarker(new MarkerOptions()
                                .position(new LatLng(location.getLatitude(), location.getLongitude()))
                                .title(username + getString(R.string.name_position))
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    } else {
                        myFriendLastPos.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                }
            }
        });
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onLocationChanged(Location location) {

        mBroadcastAsyncTask = new BroadcastAsyncTask();
        mBroadcastAsyncTask.execute(location);
    }

    @Override
    public void onAllTracksEnded() {
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.play_pause_button);
        if (playPauseButton != null)
            playPauseButton.setBackgroundResource(R.drawable.playbutton);

        TextView titleTxt = (TextView) findViewById(R.id.title_text);
        TextView artistTxt = (TextView) findViewById(R.id.artist_text);

        if (titleTxt != null)
            titleTxt.setText(mFirstSongTitle);
        if (artistTxt != null)
            artistTxt.setText(mFirstSongArtistName);

        ImageView albumImg = (ImageView) findViewById(R.id.album_img);
        if (albumImg != null)
            Picasso.with(getApplicationContext()).load(mFirstSongAlbumUrl).fit().centerCrop().into(albumImg);

    }

    @Override
    public void onPlayTrack(PlayableEntity playableEntity) {

        ImageButton playPauseButton = (ImageButton) findViewById(R.id.play_pause_button);
        if (playPauseButton != null)
            playPauseButton.setBackgroundResource(R.drawable.pausebutton);

        TextView titleTxt = (TextView) findViewById(R.id.title_text);
        TextView artistTxt = (TextView) findViewById(R.id.artist_text);

        if (titleTxt != null)
            titleTxt.setText(mPlaylistPlayer.getCurrentTrack().getTitle());
        if (artistTxt != null)
            artistTxt.setText(mPlaylistPlayer.getCurrentTrack().getArtist().getName());

        ImageView albumImg = (ImageView) findViewById(R.id.album_img);
        if (albumImg != null)
            Picasso.with(getApplicationContext()).load(mPlaylistPlayer.getCurrentTrack().getAlbum().getCoverUrl()).fit().centerCrop().into(albumImg);
    }

    @Override
    public void onTrackEnded(PlayableEntity playableEntity) {



    }

    @Override
    public void onRequestException(Exception e, Object o) {

    }

    private class SubscribeAsyncTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            mMessagingManager.subscribe(params[0], new Callback() {
                @Override
                public void successCallback(String channel, Object message) {
                    JSONObject jsonObject = (JSONObject) message;
                    try {
                        double mLat = jsonObject.getDouble(MessagingManager.JSON_LAT);
                        double mLng = jsonObject.getDouble(MessagingManager.JSON_LNG);
                        String name = jsonObject.getString(MessagingManager.JSON_NAME);
                        setPosition(createLocation(mLat, mLng, name), name, false);
                    } catch (JSONException e) {
                        Log.e(TAG, e.getMessage());
                    }
                }

                @Override
                public void errorCallback(String channel, PubnubError error) {
                    Log.e(TAG, error.getErrorString());
                }
            });
            return null;
        }
    }

    private class BroadcastAsyncTask extends AsyncTask<Location, Void, Void> {

        @Override
        protected Void doInBackground(Location... params) {
            Location location = params[0];
            setPosition(location, mUsername, true);
            mMessagingManager.broadcastLocation(mUsername,
                    location.getLatitude(),
                    location.getLongitude());
            return null;
        }
    }
}
