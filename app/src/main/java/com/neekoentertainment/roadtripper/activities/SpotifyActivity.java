package com.neekoentertainment.roadtripper.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.spotify_activity);

        initializeButtons();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.spotify);
        setSupportActionBar(toolbar);

        AuthenticationRequest.Builder builder = new AuthenticationRequest.Builder(getString(R.string.spotify_client_id),
                AuthenticationResponse.Type.TOKEN,
                getString(R.string.spotify_redirect_uri));
        builder.setScopes(new String[]{"user-read-private", "streaming"});
        AuthenticationRequest request = builder.build();

        AuthenticationClient.openLoginActivity(this, REQUEST_CODE, request);
    }


    protected void initializeButtons() {
        ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
        ImageButton nextButton = (ImageButton) findViewById(R.id.nextButton);
        ImageButton prevButton = (ImageButton) findViewById(R.id.prevButton);
        ImageButton pauseButton = (ImageButton) findViewById(R.id.pauseButton);

        if (playButton != null)
            playButton.setEnabled(false);
        if (prevButton != null)
            prevButton.setEnabled(false);
        if (nextButton != null)
            nextButton.setEnabled(false);
        if (pauseButton != null)
            pauseButton.setEnabled(false);

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
        ImageButton playButton = (ImageButton) findViewById(R.id.playButton);
        ImageButton nextButton = (ImageButton) findViewById(R.id.nextButton);
        ImageButton prevButton = (ImageButton) findViewById(R.id.prevButton);
        ImageButton pauseButton = (ImageButton) findViewById(R.id.pauseButton);

        if (playButton != null)
            playButton.setEnabled(true);
        if (prevButton != null)
            prevButton.setEnabled(true);
        if (nextButton != null)
            nextButton.setEnabled(true);
        if (pauseButton != null)
            pauseButton.setEnabled(true);
    }

    public void onPlayClicked(View view) {
        if (!mIsPaused) {
            String spotifyURI = SharedPreferencesUtils.SPOTIFY_URI;
            mPlayer.play(spotifyURI);
        } else {
            mPlayer.resume();
        }
    }

    public void onPauseClicked(View view) {
        mIsPaused = true;
        mPlayer.pause();
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
