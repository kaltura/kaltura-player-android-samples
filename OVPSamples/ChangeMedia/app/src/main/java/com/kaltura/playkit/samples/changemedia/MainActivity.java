package com.kaltura.playkit.samples.changemedia;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.kaltura.playkit.PKLog;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OVPMediaOptions;
import com.kaltura.tvplayer.PlayerInitOptions;

public class MainActivity extends AppCompatActivity {

    private static final PKLog log = PKLog.get("MainActivity");

    public static final String SERVER_URL = "https://cdnapisec.kaltura.com";
    public static final int PARTNER_ID = 2215841;

    private static final String FIRST_ENTRY_ID = "1_w9zx2eti";
    private static final String SECOND_ENTRY_ID = "1_ebs5e9cy";
    private static final Long START_POSITION = 0L; // position for start playback in msec.

    private KalturaPlayer player;
    private Button playPauseButton;
    private boolean isFullScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Add simple play/pause button.
        addPlayPauseButton();

        //Init change media button which will switch between entries.
        initChangeMediaButton();

        loadPlaykitPlayer();

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

    /**
     * Initialize the changeMedia button. On click it will change media.
     */
    private void initChangeMediaButton() {
        //Get reference to the button.
        Button changeMediaButton = this.findViewById(R.id.change_media_button);
        //Set click listener.
        changeMediaButton.setOnClickListener(v -> {
            //Change media.
            changeMedia();
        });
    }

    /**
     * Will switch between entries. If the first entry is currently active it will
     * prepare the second one. Otherwise it will prepare the first one.
     */
    private void changeMedia() {

        //Check if id of the media entry that is set in mediaConfig.
        if (player.getMediaEntry().getId().equals(FIRST_ENTRY_ID)) {
            //If first one is active, prepare second one.
            prepareSecondEntry();
        } else {
            //If the second one is active, prepare the first one.
            prepareFirstEntry();
        }

        resetPlayPauseButtonToPauseText();
    }

    /**
     * Prepare the first entry.
     */
    private void prepareFirstEntry() {
        OVPMediaOptions ovpMediaOptions = new OVPMediaOptions();
        ovpMediaOptions.entryId = FIRST_ENTRY_ID;
        ovpMediaOptions.ks = null;
        ovpMediaOptions.startPosition = START_POSITION;

        player.loadMedia(ovpMediaOptions, (entry, error) -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                log.d("First OVPMedia onEntryLoadComplete  entry = " + entry.getId());
            }
        });
    }

    /**
     * Prepare the second entry.
     */
    private void prepareSecondEntry() {
        OVPMediaOptions ovpMediaOptions = new OVPMediaOptions();
        ovpMediaOptions.entryId = SECOND_ENTRY_ID;
        ovpMediaOptions.ks = null;
        ovpMediaOptions.startPosition = START_POSITION;

        player.loadMedia(ovpMediaOptions, (entry, error) -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                log.d("Second OVPMedia onEntryLoadComplete  entry = " + entry.getId());
            }
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
            if (player != null)
                if (player.isPlaying()) {
                    //If player is playing, change text of the button and pause.
                    resetPlayPauseButtonToPlayText();
                    player.pause();
                } else {
                    //If player is not playing, change text of the button and play.
                    resetPlayPauseButtonToPauseText();
                    player.play();
                }
        });
    }

    /**
     * Just reset the play/pause button text to "Play".
     */
    private void resetPlayPauseButtonToPlayText() {
        playPauseButton.setText(R.string.play_text);
    }

    private void resetPlayPauseButtonToPauseText() {
        playPauseButton.setText(R.string.pause_text);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            if (playPauseButton != null) {
                resetPlayPauseButtonToPauseText();
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
        playerInitOptions.setAllowCrossProtocolEnabled(true);
        player = KalturaPlayer.createOVPPlayer(MainActivity.this, playerInitOptions);

        player.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        ViewGroup container = findViewById(R.id.player_root);
        container.addView(player.getPlayerView());

        //Prepare the first entry.
        prepareFirstEntry();
    }
}