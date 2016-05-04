package com.neekoentertainment.roadtripper.activities;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.neekoentertainment.roadtripper.R;
import com.neekoentertainment.roadtripper.utils.SharedPreferencesUtils;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import com.spotify.sdk.android.player.Config;
import com.spotify.sdk.android.player.ConnectionStateCallback;
import com.spotify.sdk.android.player.Player;
import com.spotify.sdk.android.player.PlayerNotificationCallback;
import com.spotify.sdk.android.player.PlayerState;
import com.spotify.sdk.android.player.Spotify;

/**
 * Created by Nicolas on 4/4/2016.
 */
public class SpotifyActivity extends AppCompatActivity implements PlayerNotificationCallback, ConnectionStateCallback {

    private static final int REQUEST_CODE = 1;
    private Player mPlayer;
    private boolean mIsPaused;
    private ActionBarDrawerToggle mDrawerToggle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spotify_activity);

        initializeButtons();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            toolbar.setTitle(R.string.spotify);
        }
        setSupportActionBar(toolbar);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(getString(R.string.spotify_client_id),
                AuthenticationResponse.Type.TOKEN,
                getString(R.string.spotify_redirect_uri));
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
        setNavigationDrawerAndToolbars();
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

    private void setNavigationDrawerAndToolbars() {
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        if (navigationView != null) {
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    if (item.getTitle().toString().equals(getString(R.string.app_name))) {
                        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
                        startActivity(intent);
                    } else if (item.getTitle().toString().equals(getString(R.string.spotify))) {
                        Intent intent = new Intent(getApplicationContext(), SpotifyActivity.class);
                        startActivity(intent);
                    } else if (item.getTitle().toString().equals(getString(R.string.parameters))) {
                        Intent intent = new Intent(getApplicationContext(), ParametersActivity.class);
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

    protected void initializeButtons() {
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        ImageButton nextButton = (ImageButton) findViewById(R.id.nextButton);
        ImageButton prevButton = (ImageButton) findViewById(R.id.prevButton);

        if (playPauseButton != null)
            playPauseButton.setEnabled(false);
        if (prevButton != null)
            prevButton.setEnabled(false);
        if (nextButton != null)
            nextButton.setEnabled(false);

        mIsPaused = false;
    }


    @Override
    protected void onDestroy() {
        Spotify.destroyPlayer(this);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == REQUEST_CODE) {
            AuthenticationResponse authenticationResponse = AuthenticationClient.getResponse(resultCode, intent);
            switch (authenticationResponse.getType()) {
                case ERROR:
                    Toast.makeText(this, authenticationResponse.getError(), Toast.LENGTH_LONG).show();
                    finish();
                    break;
                case EMPTY:
                    finish();
                    break;
                case TOKEN:
                    Config playerConfig = new Config(this, authenticationResponse.getAccessToken(), getString(R.string.spotify_client_id));
                    mPlayer = Spotify.getPlayer(playerConfig, this, new Player.InitializationObserver() {
                        @Override
                        public void onInitialized(Player player) {
                            mPlayer.addConnectionStateCallback(SpotifyActivity.this);
                            mPlayer.addPlayerNotificationCallback(SpotifyActivity.this);
                            activateButtons();
                        }

                        @Override
                        public void onError(Throwable throwable) {
                            Log.e("MainActivity", "Could not initialize player: " + throwable.getMessage());
                        }
                    });
                    break;
                default:
                    break;
            }
        }
    }

    public void activateButtons() {
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        ImageButton nextButton = (ImageButton) findViewById(R.id.nextButton);
        ImageButton prevButton = (ImageButton) findViewById(R.id.prevButton);

        if (playPauseButton != null)
            playPauseButton.setEnabled(true);
        if (prevButton != null)
            prevButton.setEnabled(true);
        if (nextButton != null)
            nextButton.setEnabled(true);
    }

    public void switchPausePlayButton() {
        ImageButton playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);

        if (playPauseButton != null) {
            if (!mIsPaused) {
                playPauseButton.setBackgroundResource(R.drawable.pausebutton);
            } else {
                playPauseButton.setBackgroundResource(R.drawable.playbutton);
            }
        }
    }

    public void onPlayPauseClicked(View view) {
        if (!mIsPaused) {
            String spotifyURI = SharedPreferencesUtils.SPOTIFY_URI;
            mPlayer.play(spotifyURI);
            switchPausePlayButton();
        } else {
            switchPausePlayButton();
            mPlayer.resume();
        }
    }

    public void onNextClicked(View view) {
        mPlayer.skipToNext();
    }

    public void onPrevClicked(View view) {
        mPlayer.skipToPrevious();
    }


    @Override
    public void onLoggedIn() {
        Log.d("SpotifyActivity", "User logged in");
    }

    @Override
    public void onLoggedOut() {
        Log.d("SpotifyActivity", "User logged out");
    }

    @Override
    public void onLoginFailed(Throwable throwable) {
        Log.d("SpotifyActivity", "Login failed");
        Toast.makeText(this, throwable.getLocalizedMessage(), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onTemporaryError() {
        Log.d("SpotifyActivity", "Temporary error occurred");
    }

    @Override
    public void onConnectionMessage(String s) {
        Log.d("SpotifyActivity", "Received connection message: " + s);
    }

    @Override
    public void onPlaybackEvent(EventType eventType, PlayerState playerState) {
        Log.d("SpotifyActivity", "Playback event received: " + eventType.name());
    }

    @Override
    public void onPlaybackError(ErrorType errorType, String s) {
        Log.d("SpotifyActivity", "Playback error received: " + errorType.name());
    }
}
