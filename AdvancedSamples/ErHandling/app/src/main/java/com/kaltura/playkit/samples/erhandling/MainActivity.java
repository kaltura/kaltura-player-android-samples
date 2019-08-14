package com.kaltura.playkit.samples.erhandling;

import android.os.Build;
import android.os.Bundle;
import com.google.android.material.snackbar.Snackbar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.PlayerState;
import com.kaltura.playkit.ads.AdController;
import com.kaltura.playkit.plugins.ads.AdEvent;
import com.kaltura.playkit.plugins.ima.IMAConfig;
import com.kaltura.playkit.plugins.ima.IMAPlugin;
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
    public static final String SERVER_URL = "incorrect_source_url"; // "https://rest-us.ott.kaltura.com/v4_5/api_v3/";
    private static final String ASSET_ID = "548576";
    public static final int PARTNER_ID = 3009;

    //Ad configuration constants.
    private static final String INCORRECT_AD_TAG_URL = "incorrect_ad_tag_url";

    private KalturaPlayer player;
    private Button playPauseButton;
    private boolean isFullScreen;
    private PlayerState playerState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadPlaykitPlayer();

        addPlayPauseButton();

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
     * Will subscribe to the error events (in this example PlayerEvent.Type.ERROR and AdEvent.Type.ERROR).
     * For simplicity, in this example we will just print the errorType of the error that happened.
     * For the full list of errorTypes you can check our documentation.
     * !!!Note, we will receive only events, we subscribed to.
     */
    private void subscribeToErrorEvents() {

        player.addListener(this, PlayerEvent.error, event -> {
            PlayerEvent.Error playerErrorvEent = event;
            Log.e(TAG, "PLAYER ERROR " +  playerErrorvEent.error.errorType.name() + " " + playerErrorvEent.error.message);
        });

        player.addListener(this, AdEvent.error, event -> {
            AdEvent.Error adError = event;
            Log.e(TAG, "AD_ERROR : " +   adError.error.errorType.name() + " " + adError.error.errorType.name());
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

    private void addPlayerStateListener() {
        player.addListener(this, PlayerEvent.stateChanged, event -> {
            Log.d(TAG,"State changed from " + event.oldState + " to " + event.newState);
            playerState = event.newState;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null && playerState != null) {
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


        // IMA Configuration
        PKPluginConfigs pkPluginConfigs = new PKPluginConfigs();
        IMAConfig adsConfig = getAdsConfig(INCORRECT_AD_TAG_URL);
        pkPluginConfigs.setPluginConfig(IMAPlugin.factory.getName(), adsConfig);

        playerInitOptions.setPluginConfigs(pkPluginConfigs);

        player = KalturaPlayer.createOTTPlayer(MainActivity.this, playerInitOptions);

        player.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        ViewGroup container = findViewById(R.id.player_root);
        container.addView(player.getPlayerView());

        OTTMediaOptions ottMediaOptions = buildOttMediaOptions();
        player.loadMedia(ottMediaOptions, (entry, loadError) -> {
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                Log.d(TAG,"OTTMedia onEntryLoadComplete  entry = " + entry.getId());
            }
        });

        //Subscribe to the events.
        subscribeToErrorEvents();

        //Add simple play/pause button.
        addPlayPauseButton();

        addPlayerStateListener();
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

    private IMAConfig getAdsConfig(String adTagUrl) {
        List<String> videoMimeTypes = new ArrayList<>();
        videoMimeTypes.add("video/mp4");
        videoMimeTypes.add("application/x-mpegURL");
        videoMimeTypes.add("application/dash+xml");
        return new IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8);
    }
}
