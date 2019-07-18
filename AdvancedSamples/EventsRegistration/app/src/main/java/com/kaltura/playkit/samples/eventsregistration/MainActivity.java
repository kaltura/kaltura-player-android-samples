package com.kaltura.playkit.samples.eventsregistration;

import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.PlayerState;
import com.kaltura.playkit.ads.AdController;
import com.kaltura.playkit.player.PKTracks;
import com.kaltura.playkit.providers.api.phoenix.APIDefines;
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OTTMediaOptions;
import com.kaltura.tvplayer.PlayerInitOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //Tag for logging.
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final Long START_POSITION = 0L; // position for start playback in msec.

    //The url of the source to play
    public static final String SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/";
    private static final String ASSET_ID = "548576";
    public static final int PARTNER_ID = 3009;

    private KalturaPlayer player;
    private Button playPauseButton;
    private Spinner speedSpinner;
    private boolean userIsInteracting;
    private boolean isFullScreen;
    private PlayerState playerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadPlaykitPlayer();

        (findViewById(R.id.activity_main)).setOnClickListener(v -> {
            if (isFullScreen) {
                showSystemUI();
            } else {
                hideSystemUI();
            }
        });
    }

    private void hideSystemUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                            | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                            | View.SYSTEM_UI_FLAG_IMMERSIVE);
        }
        isFullScreen = true;
    }

    private void showSystemUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
        isFullScreen = false;
    }

    public void addItemsOnSpeedSpinner() {

        speedSpinner = findViewById(R.id.sppedSpinner);
        List<Float> list = new ArrayList();
        list.add(0.5f);
        list.add(1.0f);
        list.add(1.5f);
        list.add(2.0f);
        ArrayAdapter<Float> dataAdapter = new ArrayAdapter(this,
                android.R.layout.simple_spinner_item, list);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        speedSpinner.setAdapter(dataAdapter);
        speedSpinner.setOnItemSelectedListener(new CustomOnItemSelectedListener());
        speedSpinner.setSelection(1);

    }

    /**
     * Will subscribe to the changes in the player states.
     */
    private void subscribeToPlayerStateChanges() {

        player.addListener(this, PlayerEvent.stateChanged, event -> {
            playerState = event.newState;
            PlayerEvent.StateChanged stateChanged = event;
            //Switch on the new state that is received.
            switch (stateChanged.newState) {

                //Player went to the Idle state.
                case IDLE:
                    //Print to log.
                    Log.d(TAG, "StateChanged: IDLE.");
                    break;
                //The player is in Loading state.
                case LOADING:
                    //Print to log.
                    Log.d(TAG, "StateChanged: LOADING.");
                    break;
                //The player is ready for playback.
                case READY:
                    //Print to log.
                    Log.d(TAG, "StateChanged: READY.");
                    break;
                //Player is buffering now.
                case BUFFERING:
                    //Print to log.
                    Log.d(TAG, "StateChanged: BUFFERING.");
                    break;
            }
        });
    }

    /**
     * Will subscribe to the player events. The main difference between
     * player state changes and player events, is that events are notify us
     * about playback events like PLAY, PAUSE, TRACKS_AVAILABLE, SEEKING etc.
     * The player state changed events, notify us about more major changes in
     * his states. Like IDLE, LOADING, READY and BUFFERING.
     * For simplicity, in this example we will show subscription to the couple of events.
     * For the full list of events you can check our documentation.
     * !!!Note, we will receive only events, we subscribed to.
     */
    private void subscribeToPlayerEvents() {

        player.addListener(this, PlayerEvent.play, event -> {
            Log.d(TAG, "event received: " + event.eventType().name());

        });

        player.addListener(this, PlayerEvent.pause, event -> {
            Log.d(TAG, "event received: " + event.eventType().name());
        });

        player.addListener(this, PlayerEvent.playbackRateChanged, event -> {
            PlayerEvent.PlaybackRateChanged playbackRateChanged = event;
            Log.d(TAG, "event received: " + event.eventType().name() + " Rate = " + playbackRateChanged.rate);

        });

        player.addListener(this, PlayerEvent.tracksAvailable, event -> {
            Log.d(TAG, "Event TRACKS_AVAILABLE");

            PlayerEvent.TracksAvailable tracksAvailable = event;

            //Then you can use the data object itself.
            PKTracks tracks = tracksAvailable.tracksInfo;

            //Print to log amount of video tracks that are available for this entry.
            Log.d(TAG, "event received: " + event.eventType().name()
                    + ". Additional info: Available video tracks number: "
                    + tracks.getVideoTracks().size());
        });

        player.addListener(this, PlayerEvent.error, event -> {
            PlayerEvent.Error errorEvent = event;
            Log.e(TAG, "Error Event: " + errorEvent.error.errorType  + " " + event.error.message);
        });
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private void addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById(R.id.play_pause_button);
        //Add clickListener.
        playPauseButton.setOnClickListener(v -> {
            if (player != null) {
                AdController adController = player.getController(AdController.class);
                if (player.isPlaying() || (adController != null && adController.isAdDisplayed() && adController.isAdPlaying())) {
                    if (adController != null && adController.isAdDisplayed()) {
                        adController.pause();
                    } else {
                        player.pause();
                    }
                    //If player is playing, change text of the button and pause.
                    playPauseButton.setText(R.string.play_text);
                } else {
                    if (adController != null && adController.isAdDisplayed()) {
                        adController.play();
                    } else {
                        player.play();
                    }
                    //If player is not playing, change text of the button and play.
                    playPauseButton.setText(R.string.pause_text);
                }
            }
        });
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
        if (player != null) {
            if (playPauseButton != null) {
                playPauseButton.setText(R.string.pause_text);
            }
            player.onApplicationPaused();
        }
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        if (player != null && playerState != null) {
            player.onApplicationResumed();
            player.play();
        }
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.removeListeners(this);
            player.destroy();
            player = null;
        }
        super.onDestroy();
    }


    class CustomOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

        public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
            if (userIsInteracting) {
                Toast.makeText(parent.getContext(),
                        "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString() + "X",
                        Toast.LENGTH_SHORT).show();
                if (player != null) {
                    player.setPlaybackRate((float) parent.getItemAtPosition(pos));
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {

        }
    }

    public void loadPlaykitPlayer() {

        PlayerInitOptions playerInitOptions = new PlayerInitOptions(PARTNER_ID);
        playerInitOptions.setAutoPlay(true);
        playerInitOptions.setAllowCrossProtocolEnabled(true);


        player = KalturaPlayer.createOTTPlayer(MainActivity.this, playerInitOptions);
        //Subscribe to events, which will notify about changes in player states.
        subscribeToPlayerStateChanges();

        //Subscribe to the player events.
        subscribeToPlayerEvents();
        player.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        ViewGroup container = findViewById(R.id.player_root);
        container.addView(player.getPlayerView());

        OTTMediaOptions ottMediaOptions = buildOttMediaOptions();
        player.loadMedia(ottMediaOptions, (entry, loadError) -> {
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "OTTMedia onEntryLoadComplete  entry = " + entry.getId());
            }
        });

        addItemsOnSpeedSpinner();

        showSystemUI();

        //Add simple play/pause button.
        addPlayPauseButton();
    }

    private OTTMediaOptions buildOttMediaOptions() {
        OTTMediaOptions ottMediaOptions = new OTTMediaOptions();
        ottMediaOptions.assetId = ASSET_ID;
        ottMediaOptions.assetType = APIDefines.KalturaAssetType.Media;
        ottMediaOptions.contextType = APIDefines.PlaybackContextType.Playback;
        ottMediaOptions.assetReferenceType = APIDefines.AssetReferenceType.Media;
        ottMediaOptions.protocol = PhoenixMediaProvider.HttpProtocol.Http;
        ottMediaOptions.ks = null;
        ottMediaOptions.startPosition = START_POSITION;
        ottMediaOptions.formats = new String []{"Mobile_Main"};

        return ottMediaOptions;
    }
}

