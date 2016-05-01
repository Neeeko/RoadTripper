package com.neekoentertainment.roadtripper.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.constants.Style;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.neekoentertainment.roadtripper.R;
import com.neekoentertainment.roadtripper.application.RoadTripperApplication;
import com.neekoentertainment.roadtripper.utils.MessagingManager;
import com.pubnub.api.Callback;
import com.pubnub.api.PubnubError;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

/**
 * Created by Nicolas on 4/3/2016.
 */
public class HomeActivity extends AppCompatActivity {

    private Bundle mSavedInstanceState;
    private MapView mMapView;
    private ActionBarDrawerToggle mDrawerToggle;
    private String mUsername;
    private GoogleApiClient mGoogleApiClient;
    private MessagingManager mMessagingManager;
    private MapboxMap mMapboxMap;
    private Boolean mCameraLock = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);
        mGoogleApiClient = ((RoadTripperApplication) getApplicationContext()).getGoogleApiClient();
        mMessagingManager = ((RoadTripperApplication) getApplicationContext()).getMessagingManager();
        mMapView = (MapView) findViewById(R.id.mapview);
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
                            .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (!editText.getText().toString().isEmpty()
                                            && !editText.getText().toString().equals("")) {
                                        subscribeToFriend(editText.getText().toString());
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
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
        mSavedInstanceState = savedInstanceState;
        setMap();
        setNavigationDrawerAndToolbars();
        if (getIntent() != null && getIntent().getStringExtra("username") != null) {
            mUsername = getIntent().getStringExtra("username");
        }
    }

    private void subscribeToFriend(String name) {
        mMessagingManager.subscribe(name, new Callback() {
            @Override
            public void successCallback(String channel, Object message) {
                JSONObject jsonObject = (JSONObject) message;
                try {
                    double mLat = jsonObject.getDouble("lat");
                    double mLng = jsonObject.getDouble("lng");
                    String name = jsonObject.getString("name");
                    setPosition(mMapboxMap, createLocation(mLat, mLng, name), name, false);
                    Log.d("Test", "Message received = " + message.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void errorCallback(String channel, PubnubError error) {
                Log.e("HomeActivity", error.getErrorString());
            }
        });
    }

    private Location createLocation(double lat, double lng, String name) {
        Location location = new Location(name + "'s position");
        location.setLatitude(lat);
        location.setLongitude(lng);
        location.setTime(new Date().getTime());
        return location;
    }

    private void setNavigationDrawerAndToolbars() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //final Toolbar toolbar_drawer = (Toolbar) findViewById(R.id.toolbar_drawer);
        /*tmp toolbar name in drawer drawer_menu*/
        //toolbar_drawer.setTitle("Menu");
        setSupportActionBar(toolbar);


        mMapView = (MapView) findViewById(R.id.mapview);
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                //setSupportActionBar(toolbar_drawer);

                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);

                setSupportActionBar(toolbar);

                invalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mMapView.onSaveInstanceState(outState);
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
        if (mMapView != null) {
            mMapView.onCreate(mSavedInstanceState);
            mMapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                    mMapboxMap = mapboxMap;
                    mapboxMap.setStyleUrl(Style.MAPBOX_STREETS);
                    LocationRequest locationRequest = getLocationRequest();
                    LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest,
                            new LocationListener() {
                                @Override
                                public void onLocationChanged(Location location) {
                                    setPosition(mapboxMap, location, mUsername, true);
                                    mMessagingManager.broadcastLocation(mUsername,
                                            location.getLatitude(),
                                            location.getLongitude());
                                }
                            });
                }
            });
        }
    }

    private void setPosition(MapboxMap mapboxMap, Location location, String username, boolean isMyPosition) {
        if (mCameraLock && isMyPosition) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location))
                    .build();
            mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
        List<com.mapbox.mapboxsdk.annotations.Marker> markerList = mapboxMap.getMarkers();

        // First position of user
        if (markerList.isEmpty()) {
            mapboxMap.addMarker(new MarkerOptions()
                    .position(new LatLng(location))
                    .title(username + "'s position"));
        }
        for (com.mapbox.mapboxsdk.annotations.Marker marker : markerList) {
            // Position updated
            if (marker.getTitle().contains(username)) {
                marker.setPosition(new LatLng(location));
                mapboxMap.updateMarker(marker);
            } else {
                // First position of a friend
                mapboxMap.addMarker(new MarkerOptions()
                        .position(new LatLng(location))
                        .title(username + "'s position"));
            }
        }
        Log.d("Test", username + "'s position changed");
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }
}
