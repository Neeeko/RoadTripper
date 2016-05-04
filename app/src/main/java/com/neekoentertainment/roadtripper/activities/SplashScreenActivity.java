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
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
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

    private static final String TAG = "SplashScreenActivity";

    private GoogleApiClient mGoogleApiClient;
    private DeezerConnect mDeezerConnect;
    private ArrayList<Playlist> mPlaylistList;
    private ListView mListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen);
        initPubnub();
        initGoogleApi();
        RelativeLayout deezerConnect = (RelativeLayout) findViewById(R.id.deezer_connect);
        mListView = (ListView) findViewById(R.id.playlist_listview);
        if (deezerConnect != null) {
            deezerConnect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    connectToDeezer((RelativeLayout) v);
                }
            });
        }
    }

    private void connectToDeezer(final RelativeLayout connectButton) {
        final LinearLayout container = (LinearLayout) findViewById(R.id.container);
        mPlaylistList = new ArrayList<>();
        ServicesAuthentication.DeezerConnection mCallback = new ServicesAuthentication.DeezerConnection() {
            @Override
            public void onDeezerConnected(DeezerConnect deezerConnect) {
                mDeezerConnect = deezerConnect;
                ((RoadTripperApplication) getApplicationContext()).setDeezerConnect(mDeezerConnect);
                if (connectButton != null) {
                    connectButton.setVisibility(View.GONE);
                    mListView.setVisibility(View.VISIBLE);
                }
                getCurrentUserPlaylists(DEEZER_PLAYLISTS_URL, new OnDataRetrieved() {
                    @Override
                    public void onDataRetrieved() {
                        final PlaylistsAdapter playlistsAdapter = new PlaylistsAdapter(getApplicationContext(), mPlaylistList);
                        mListView.setAdapter(playlistsAdapter);
                        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Log.d("Test", playlistsAdapter.getItem(position).getTitle());
                            }
                        });
                    }
                });
            }

            @Override
            public void onDeezerDisconnected() {
            }

            @Override
            public void onDeezerFailed(String e) {
                if (connectButton != null) {
                    mListView.setVisibility(View.GONE);
                    connectButton.setVisibility(View.VISIBLE);
                    connectButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            connectToDeezer(connectButton);
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
                            intent.putExtra(getString(R.string.username), editText.getText().toString());
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

    public class PlaylistsAdapter extends BaseAdapter {

        private ArrayList<Playlist> mPlaylist;
        private Context mContext;

        public PlaylistsAdapter(Context context, ArrayList<Playlist> playlists) {
            mPlaylist = playlists;
            mContext = context;
        }

        @Override
        public int getCount() {
            return mPlaylist.size();
        }

        @Override
        public Playlist getItem(int position) {
            return mPlaylist.get(position);
        }

        @Override
        public long getItemId(int position) {
            return mPlaylist.get(position).hashCode();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) mContext
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.playlist_view, parent, false);

                viewHolder = new ViewHolder();
                viewHolder.cover = (ImageView) convertView.findViewById(R.id.playlist_cover);
                viewHolder.playlistTitle = (TextView) convertView.findViewById(R.id.playlist_title);
                viewHolder.creator = (TextView) convertView.findViewById(R.id.playlist_creator);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.playlistTitle.setText(getItem(position).getTitle());
            viewHolder.playlistTitle.setSelected(true);
            viewHolder.creator.setText(getItem(position).getCreator().getName());
            viewHolder.creator.setSelected(true);
            Picasso.with(mContext).load(getItem(position).getSmallImageUrl()).fit().centerCrop().into(viewHolder.cover);
            return convertView;
        }

        private class ViewHolder {
            ImageView cover;
            TextView playlistTitle;
            TextView creator;
        }
    }
}
