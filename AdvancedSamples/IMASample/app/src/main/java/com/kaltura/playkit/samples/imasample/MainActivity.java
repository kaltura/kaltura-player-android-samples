package com.kaltura.playkit.samples.imasample;

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
import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.plugins.ima.IMAConfig;
import com.kaltura.playkit.plugins.ima.IMAPlugin;
import com.kaltura.playkit.providers.api.phoenix.APIDefines;
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OTTMediaOptions;
import com.kaltura.tvplayer.PlayerInitOptions;
import com.kaltura.tvplayer.config.player.UiConf;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final PKLog log = PKLog.get("MainActivity");

    //Ad configuration constants.
    String preMidPostSingleAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=";

    private static final Long START_POSITION = 0L; // position tp start playback in msec.

    private static final String SERVER_URL = "https://api-preprod.ott.kaltura.com/v4_7/api_v3/";
    private static final String ASSET_ID = "480989";
    private static final int PARTNER_ID = 198;
    private static final int UICONF_ID = 41188731;
    private static final int UICONF_PARTNER_ID = 2215841;

    private KalturaPlayer player;
    private Button playPauseButton;
    private boolean isFullScreen;

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
     * Just add a simple button which will start/pause playback.
     */
    private void addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById(R.id.play_pause_button);
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

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
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

        PlayerInitOptions playerInitOptions = new PlayerInitOptions(PARTNER_ID, new UiConf(UICONF_ID, UICONF_PARTNER_ID));
        playerInitOptions.setServerUrl(SERVER_URL);
        playerInitOptions.setAutoPlay(true);

        // IMA Configuration
        PKPluginConfigs pkPluginConfigs = new PKPluginConfigs();
        IMAConfig adsConfig = getAdsConfig(preMidPostSingleAdTagUrl);
        pkPluginConfigs.setPluginConfig(IMAPlugin.factory.getName(), adsConfig);

        playerInitOptions.setPluginConfigs(pkPluginConfigs);

        player = KalturaPlayer.createOTTPlayer(MainActivity.this, playerInitOptions);

        player.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        ViewGroup container = findViewById(R.id.player_root);
        container.addView(player.getPlayerView());

        OTTMediaOptions ottMediaOptions = buildOttMediaOptions();
        player.loadMedia(ottMediaOptions, (entry, error) -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.getId());
            }
        });
    }

    private OTTMediaOptions buildOttMediaOptions() {
        OTTMediaOptions ottMediaOptions = new OTTMediaOptions();
        ottMediaOptions.assetId = ASSET_ID;
        ottMediaOptions.assetType = APIDefines.KalturaAssetType.Media;
        ottMediaOptions.contextType = APIDefines.PlaybackContextType.Playback;
        ottMediaOptions.assetReferenceType = APIDefines.AssetReferenceType.Media;
        ottMediaOptions.protocol = PhoenixMediaProvider.HttpProtocol.Https;
        ottMediaOptions.ks = null;
        ottMediaOptions.startPosition = START_POSITION;
        //  ottMediaOptions.formats = new String []{"Tablet Main"};

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
