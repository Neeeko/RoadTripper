package com.neekoentertainment.roadtripper.activities;

import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
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

/**
 * Created by Nicolas on 4/3/2016.
 */
public class HomeActivity extends AppCompatActivity {

    private Bundle mSavedInstanceState;
    private MapView mapView;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final Toolbar toolbar_drawer = (Toolbar) findViewById(R.id.toolbar_drawer);
        /*tmp toolbar name in drawer menu*/
        toolbar_drawer.setTitle("Menu");
        setSupportActionBar(toolbar);


        mapView = (MapView) findViewById(R.id.mapview);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawer_open, R.string.drawer_close) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                setSupportActionBar(toolbar_drawer);

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

        mSavedInstanceState = savedInstanceState;
        setMap();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
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
        GoogleApiClient googleApiClient = ((RoadTripperApplication) getApplicationContext()).getGoogleApiClient();
        final Location mLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (mapView != null) {
            mapView.onCreate(mSavedInstanceState);
            mapView.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(@NonNull MapboxMap mapboxMap) {

                    // Set map style
                    mapboxMap.setStyleUrl(Style.MAPBOX_STREETS);

                    // Set the camera's starting position
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(mLocation)) // set the camera's center position
                            .zoom(12)  // set the camera's zoom level
                            .tilt(20)  // set the camera's tilt
                            .build();

                    // Move the camera to that position
                    mapboxMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    mapboxMap.addMarker(new MarkerOptions()
                            .position(new LatLng(mLocation))
                            .title("Your position"));
                }
            });
        }
    }
}
