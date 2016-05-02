package com.neekoentertainment.roadtripper.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by Nicolas on 4/3/2016.
 */
public class HomeActivity extends AppCompatActivity {

    private static final int INTERVAL = 10000;
    private static final int FASTEST_INTERVAL = 5000;
    private static final String TAG = "HomeActivity";

    private ActionBarDrawerToggle mDrawerToggle;
    private Marker myLastPos;
    private Marker myFriendLastPos;
    private boolean isFirstLaunch = true;
    private String mUsername;
    private GoogleApiClient mGoogleApiClient;
    private MessagingManager mMessagingManager;
    private GoogleMap mGoogleMap;
    private Boolean mCameraLock = true;

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
                            .create()
                            .show();
                }
            });
        }
        setNavigationDrawerAndToolbars();
        setMap();
        if (getIntent() != null && getIntent().getStringExtra(getString(R.string.username)) != null) {
            mUsername = getIntent().getStringExtra(getString(R.string.username));
        }
    }

    private void subscribeToFriend(String name) {
        new SubscribeAsyncTask().execute(name);
    }

    private Location createLocation(double lat, double lng, String name) {
        Location location = new Location(name + getString(R.string.name_position));
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setTime(new Date().getTime());
        return location;
    }

    private void setNavigationDrawerAndToolbars() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {

                    if (item.getTitle().toString().equals(getString(R.string.spotify))) {
                        Intent intent = new Intent(getApplicationContext(), SpotifyActivity.class);
                        startActivity(intent);
                    }

                    return true;
                }
            });
        }
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                invalidateOptionsMenu();
            }
        };

        if (mDrawerLayout != null) {
            mDrawerLayout.addDrawerListener(mDrawerToggle);
        }
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        mDrawerToggle.syncState();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }


    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(new com.google.android.gms.maps.OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                mGoogleMap = googleMap;
                mGoogleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                LocationRequest locationRequest = getLocationRequest();
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest,
                        new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                new BroadcastAsyncTask().execute(location);
                            }
                        });
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
