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
import com.kaltura.playkit.providers.api.phoenix.APIDefines;
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OTTMediaOptions;
import com.kaltura.tvplayer.PlayerInitOptions;
import com.kaltura.tvplayer.config.player.UiConf;


public class MainActivity extends AppCompatActivity {

    private static final PKLog log = PKLog.get("MainActivity");

    private static final int PLAYER_HEIGHT = 600;

    private static final Long START_POSITION = 0L; // position tp start playback in msec.

    private static final String SERVER_URL = "https://api-preprod.ott.kaltura.com/v4_7/api_v3/";
    private static final int PARTNER_ID = 198;
    private static final int UICONF_ID = 41188731;
    private static final int UICONF_PARTNER_ID = 2215841;

    private static final String FIRST_ASSET_ID = "480989";
    private static final String SECOND_ASSET_ID = "259153";

    private KalturaPlayer player;
    private Button playPauseButton;
    private boolean shouldExecuteOnResume;
    private boolean isFullScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        shouldExecuteOnResume = false;

        //Add simple play/pause button.
        addPlayPauseButton();

        //Init change media button which will switch between entries.
        initChangeMediaButton();

        loadPlaykitPlayer();

        //Prepare the first entry.
        prepareFirstEntry();

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
        Button changeMediaButton = (Button) this.findViewById(R.id.change_media_button);
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
        if (player.getMediaEntry().getId().equals(FIRST_ASSET_ID)) {
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
        OTTMediaOptions ottMediaOptions = new OTTMediaOptions();
        ottMediaOptions.assetId = FIRST_ASSET_ID;
        ottMediaOptions.assetType = APIDefines.KalturaAssetType.Media;
        ottMediaOptions.contextType = APIDefines.PlaybackContextType.Playback;
        ottMediaOptions.assetReferenceType = APIDefines.AssetReferenceType.Media;
        ottMediaOptions.protocol = PhoenixMediaProvider.HttpProtocol.Https;
        ottMediaOptions.ks = null;
        ottMediaOptions.startPosition = START_POSITION;
        //  ottMediaOptions.formats = new String []{"Tablet Main"};

        player.loadMedia(ottMediaOptions, (entry, error) -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.getId());
            }
        });
    }

    /**
     * Prepare the second entry.
     */
    private void prepareSecondEntry() {
        OTTMediaOptions ottMediaOptions = new OTTMediaOptions();
        ottMediaOptions.assetId = SECOND_ASSET_ID;
        ottMediaOptions.assetType = APIDefines.KalturaAssetType.Media;
        ottMediaOptions.contextType = APIDefines.PlaybackContextType.Playback;
        ottMediaOptions.assetReferenceType = APIDefines.AssetReferenceType.Media;
        ottMediaOptions.protocol = PhoenixMediaProvider.HttpProtocol.Http;
        ottMediaOptions.formats = new String[]{ "Mobile_Devices_Main_SD" };
        ottMediaOptions.ks = null;
        ottMediaOptions.startPosition = START_POSITION;
        //  ottMediaOptions.formats = new String []{"Tablet Main"};

        player.loadMedia(ottMediaOptions, (entry, error) -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.getId());
            }
        });
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private void addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = (Button) this.findViewById(R.id.play_pause_button);
        //Add clickListener.
        playPauseButton.setOnClickListener(v -> {
            if (player.isPlaying()) {
                //If player is playing, change text of the button and pause.
                playPauseButton.setText(R.string.play_text);
                player.pause();
            } else {
                //If player is not playing, change text of the button and play.
                playPauseButton.setText(R.string.pause_text);
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
        if (shouldExecuteOnResume) {
            if (player != null) {
                player.onApplicationResumed();
            }
        } else {
            shouldExecuteOnResume = true;
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
        PlayerInitOptions playerInitOptions = new PlayerInitOptions(PARTNER_ID, new UiConf(UICONF_ID, UICONF_PARTNER_ID));
        playerInitOptions.setServerUrl(SERVER_URL);
        playerInitOptions.setAutoPlay(true);
        playerInitOptions.setAllowCrossProtocolEnabled(true);

        player = KalturaPlayer.createOTTPlayer(MainActivity.this, playerInitOptions);
        player.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, PLAYER_HEIGHT);
        ViewGroup container = findViewById(R.id.player_root);
        container.addView(player.getPlayerView());
    }

}