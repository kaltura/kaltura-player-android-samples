package com.kaltura.playkit.samples.vrbasicsample;

import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.PlayerState;
import com.kaltura.playkit.player.vr.VRInteractionMode;
import com.kaltura.playkit.player.vr.VRSettings;
import com.kaltura.playkitvr.VRController;
import com.kaltura.playkitvr.VRUtil;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OVPMediaOptions;
import com.kaltura.tvplayer.PlayerInitOptions;

public class MainActivity extends AppCompatActivity {

    private static final PKLog log = PKLog.get("MainActivity");
    public static final int PARTNER_ID = 2196781;
    public  static final String SERVER_URL = "https://cdnapisec.kaltura.com";
    private static final Long START_POSITION = 0L; // position for start playback in msec.
    private KalturaPlayer player;
    private Button playPauseButton;
    private Button vrButton;

    private final String ENTRY_ID = "1_afvj3z0u";
    private boolean isFullScreen;
    private PlayerState playerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadPlaykitPlayer();

        addPlayPauseButton();
        addVRButton();
        showSystemUI();

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

    private void addPlayerStateListener() {
        player.addListener(this, PlayerEvent.stateChanged, event -> {
            log.d("State changed from " + event.oldState + " to " + event.newState);
            playerState = event.newState;
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
                if (player.isPlaying()) {
                    //If player is playing, change text of the button and pause.
                    playPauseButton.setText(R.string.play_text);
                    player.pause();
                } else {
                    //If player is not playing, change text of the button and play.
                    playPauseButton.setText(R.string.pause_text);
                    player.play();
                }
            }
        });
    }

    /**
     * Just add a simple button which will take care or VR swtiched.
     */
    private void addVRButton() {
        //Get reference to the play/pause button.
        vrButton = this.findViewById(R.id.vr_button);
        //Add clickListener.
        vrButton.setOnClickListener(v -> {
            if (player != null) {
                if (player.isPlaying()) {
                    switchVRMode();
                }
            }
        });
    }

    private VRSettings configureVRSettings() {
        VRSettings vrSettings = new VRSettings();
        vrSettings.setFlingEnabled(true);
        vrSettings.setVrModeEnabled(false);
        vrSettings.setInteractionMode(VRInteractionMode.Motion);
        vrSettings.setZoomWithPinchEnabled(true);

        VRInteractionMode interactionMode = vrSettings.getInteractionMode();
        if (!VRUtil.isModeSupported(MainActivity.this, interactionMode)) {
            //In case when mode is not supported we switch to supported mode.
            vrSettings.setInteractionMode(VRInteractionMode.Touch);
        }
        return vrSettings;
    }

    private void switchVRMode() {
        if (player != null) {
            VRController vrController = player.getController(VRController.class);
            if (vrController != null) {
                boolean currentState = vrController.isVRModeEnabled();
                vrButton.setText(currentState ?  getString(R.string.vr_mode_on) : getString(R.string.vr_mode_off));
                vrController.enableVRMode(!currentState);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null && playerState != null) {
            if (playPauseButton != null) {
                playPauseButton.setText(R.string.pause_text);
            }
            player.onApplicationResumed();
            player.play();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.onApplicationPaused();
        }
    }

    public void loadPlaykitPlayer() {

        PlayerInitOptions playerInitOptions = new PlayerInitOptions(PARTNER_ID);
        playerInitOptions.setAutoPlay(true);

        // Configure VR Settings
        playerInitOptions.setVRSettings(configureVRSettings());

        player = KalturaPlayer.createOVPPlayer(MainActivity.this, playerInitOptions);

        player.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        ViewGroup container = findViewById(R.id.player_root);
        container.addView(player.getPlayerView());

        OVPMediaOptions ovpMediaOptions = buildOvpMediaOptions();
        player.loadMedia(ovpMediaOptions, (entry, loadError) -> {
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                log.d("OVPMedia onEntryLoadComplete  entry = " + entry.getId());
            }
        });

        addPlayerStateListener();
    }

    private OVPMediaOptions buildOvpMediaOptions() {
        OVPMediaOptions ovpMediaOptions = new OVPMediaOptions();
        ovpMediaOptions.entryId = ENTRY_ID;
        ovpMediaOptions.ks = null;
        ovpMediaOptions.startPosition = START_POSITION;

        return ovpMediaOptions;
    }
}
