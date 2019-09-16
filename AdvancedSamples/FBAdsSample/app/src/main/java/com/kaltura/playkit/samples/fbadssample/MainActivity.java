package com.kaltura.playkit.samples.fbadssample;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.PlayKitManager;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.PlayerState;
import com.kaltura.playkit.ads.AdController;
import com.kaltura.playkit.plugins.ads.AdEvent;
import com.kaltura.playkit.plugins.ads.AdInfo;
import com.kaltura.playkit.plugins.ads.AdPositionType;
import com.kaltura.playkit.plugins.fbads.fbinstream.FBInStreamAd;
import com.kaltura.playkit.plugins.fbads.fbinstream.FBInStreamAdBreak;
import com.kaltura.playkit.plugins.fbads.fbinstream.FBInstreamConfig;
import com.kaltura.playkit.plugins.fbads.fbinstream.FBInstreamPlugin;
import com.kaltura.playkit.providers.api.phoenix.APIDefines;
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider;
import com.kaltura.tvplayer.KalturaOttPlayer;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OTTMediaOptions;
import com.kaltura.tvplayer.PlayerInitOptions;

import java.util.ArrayList;
import java.util.List;





public class MainActivity extends AppCompatActivity {

    private static final PKLog log = PKLog.get("MainActivity");

    //Ad configuration constants.
    String preMidPostSingleAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=";

    private static final Long START_POSITION = 0L; // position for start playback in msec.

    //Media entry configuration constants.
    public static final String SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/";
    private static final String ASSET_ID = "548576";
    public static final int PARTNER_ID = 3009;

    private KalturaPlayer player;
    private Button playPauseButton;
    private Button seekButton;
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
                if (player.isPlaying() || (adController != null && adController.isAdPlaying())) {
                    player.pause();
                    //If player is playing, change text of the button and pause.
                    playPauseButton.setText(R.string.play_text);
                } else {
                    player.play();
                    //If player is not playing, change text of the button and play.
                    playPauseButton.setText(R.string.pause_text);
                }
            }
        });
    }

        private void addSeekButton() {

        seekButton = this.findViewById(R.id.seekTo0);
        seekButton.setOnClickListener(v -> {
            if (player == null) {
                return;
            }
            AdController adController = player.getController(AdController.class);
            if (adController != null && adController.isAdPlaying()) {
               return;
            } else {
                player.seekTo(0L);
            }
        });
    }

    private PKPluginConfigs createFBInStreamPlugin(PKPluginConfigs pluginConfigs) {

        //First register your IMAPlugin.
        PlayKitManager.registerPlugins(this, FBInstreamPlugin.factory);

        addFBInStreamPluginConfig(pluginConfigs);

        //Return created PluginConfigs object.
        return pluginConfigs;
    }

    private void addFBInStreamPluginConfig(PKPluginConfigs config) {
        List<FBInStreamAd> preRollFBInStreamAdList = new ArrayList<>();
        FBInStreamAd preRoll1 = new FBInStreamAd("156903085045437_239184776817267",0, 0);
        FBInStreamAd preRoll2 = new FBInStreamAd("156903085045437_239184776817267",0, 1);
        preRollFBInStreamAdList.add(preRoll1);
        //preRollFBInStreamAdList.add(preRoll2);
        FBInStreamAdBreak preRollAdBreak = new FBInStreamAdBreak(AdPositionType.PRE_ROLL, 0, preRollFBInStreamAdList);

        List<FBInStreamAd> midRoll1FBInStreamAdList = new ArrayList<>();
        FBInStreamAd midRoll1 = new FBInStreamAd("156903085045437_239184776817267",15000, 0);
        midRoll1FBInStreamAdList.add(midRoll1);
        FBInStreamAdBreak midRoll1AdBreak = new FBInStreamAdBreak(AdPositionType.MID_ROLL, 15000, midRoll1FBInStreamAdList);


        List<FBInStreamAd> midRoll2FBInStreamAdList = new ArrayList<>();
        FBInStreamAd midRoll2 = new FBInStreamAd("156903085045437_239184776817267", 30000, 0);
        midRoll2FBInStreamAdList.add(midRoll2);
        FBInStreamAdBreak midRoll2AdBreak = new FBInStreamAdBreak(AdPositionType.MID_ROLL, 30000, midRoll2FBInStreamAdList);


        List<FBInStreamAd> postRollFBInStreamAdList = new ArrayList<>();
        FBInStreamAd postRoll1 = new FBInStreamAd("156903085045437_239184776817267", Long.MAX_VALUE, 0);
        postRollFBInStreamAdList.add(postRoll1);
        FBInStreamAdBreak postRollAdBreak = new FBInStreamAdBreak(AdPositionType.POST_ROLL, Long.MAX_VALUE, postRollFBInStreamAdList);

        List<FBInStreamAdBreak> fbInStreamAdBreakList = new ArrayList<>();
        fbInStreamAdBreakList.add(preRollAdBreak);
        fbInStreamAdBreakList.add(midRoll1AdBreak);
        fbInStreamAdBreakList.add(midRoll2AdBreak);
        fbInStreamAdBreakList.add(postRollAdBreak);

        //"294d7470-4781-4795-9493-36602bf29231");//("7450a453-4ba6-464b-85b6-6f319c7f7326");
        FBInstreamConfig fbInstreamConfig = new FBInstreamConfig(fbInStreamAdBreakList).enableDebugMode(true).setTestDevice("294d7470-4781-4795-9493-36602bf29231").setAlwaysStartWithPreroll(false);
        config.setPluginConfig(FBInstreamPlugin.factory.getName(), fbInstreamConfig);
    }


    private void addPlayerStateListener() {
        player.addListener(this, PlayerEvent.stateChanged, event -> {
            log.d("State changed from " + event.oldState + " to " + event.newState);
            playerState = event.newState;
        });
    }

    @Override
    protected void onPause() {
        log.d("onPause");
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
        log.d("onResume");
        super.onResume();

        if (player != null && playerState != null) {
            player.onApplicationResumed();
            player.play();
        }
    }

    public void loadPlaykitPlayer() {

        PlayerInitOptions playerInitOptions = new PlayerInitOptions(PARTNER_ID);
        playerInitOptions.setAutoPlay(true);
        playerInitOptions.setAllowCrossProtocolEnabled(true);


        // IMA Configuration
        PKPluginConfigs pkPluginConfigs = new PKPluginConfigs();
        PKPluginConfigs setPlugins = createFBInStreamPlugin(pkPluginConfigs);
        playerInitOptions.setPluginConfigs(pkPluginConfigs);

        player = KalturaOttPlayer.create(MainActivity.this, playerInitOptions);

        player.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        ViewGroup container = findViewById(R.id.player_root);
        container.addView(player.getPlayerView());

        OTTMediaOptions ottMediaOptions = buildOttMediaOptions();
        player.loadMedia(ottMediaOptions, (entry, loadError) -> {
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.getId());
            }
        });

        addPlayPauseButton();
        addSeekButton();
        showSystemUI();

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

    private void subscribeToAdEvents() {

        player.addListener(this, AdEvent.started, event -> {
            //Some events holds additional data objects in them.
            //In order to get access to this object you need first cast event to
            //the object it belongs to. You can learn more about this kind of objects in
            //our documentation.
            AdEvent.AdStartedEvent adStartedEvent = event;

            //Then you can use the data object itself.
            AdInfo adInfo = adStartedEvent.adInfo;

            //Print to log content type of this ad.
            log.d("ad event received: " + event.eventType().name()
                    + ". Additional info: ad content type is: "
                    + adInfo.getAdContentType());
        });

        player.addListener(this, AdEvent.contentResumeRequested, event -> {
            log.d("ADS_PLAYBACK_ENDED");
        });

        player.addListener(this, AdEvent.adPlaybackInfoUpdated, event -> {
            AdEvent.AdPlaybackInfoUpdated playbackInfoUpdated = event;
            log.d("AD_PLAYBACK_INFO_UPDATED  = " + playbackInfoUpdated.width + "/" + playbackInfoUpdated.height + "/" + playbackInfoUpdated.bitrate);
        });

        player.addListener(this, AdEvent.skippableStateChanged, event -> {
            log.d("SKIPPABLE_STATE_CHANGED");
        });

        player.addListener(this, AdEvent.adRequested, event -> {
            AdEvent.AdRequestedEvent adRequestEvent = event;
            log.d("AD_REQUESTED adtag = " + adRequestEvent.adTagUrl);
        });

        player.addListener(this, AdEvent.playHeadChanged, event -> {
            AdEvent.AdPlayHeadEvent adEventProress = event;
            //Log.d(TAG, "received AD PLAY_HEAD_CHANGED " + adEventProress.adPlayHead);
        });


        player.addListener(this, AdEvent.adBreakStarted, event -> {
            log.d("AD_BREAK_STARTED");
        });

        player.addListener(this, AdEvent.cuepointsChanged, event -> {
            AdEvent.AdCuePointsUpdateEvent cuePointsList = event;
            log.d("AD_CUEPOINTS_UPDATED HasPostroll = " + cuePointsList.cuePoints.hasPostRoll());
        });

        player.addListener(this, AdEvent.loaded, event -> {
            AdEvent.AdLoadedEvent adLoadedEvent = event;
            log.d("AD_LOADED " + adLoadedEvent.adInfo.getAdIndexInPod() + "/" + adLoadedEvent.adInfo.getTotalAdsInPod());
        });

        player.addListener(this, AdEvent.started, event -> {
            AdEvent.AdStartedEvent adStartedEvent = event;
            log.d("AD_STARTED w/h - " + adStartedEvent.adInfo.getAdWidth() + "/" + adStartedEvent.adInfo.getAdHeight());
        });

        player.addListener(this, AdEvent.resumed, event -> {
            log.d("AD_RESUMED");
        });

        player.addListener(this, AdEvent.paused, event -> {
            log.d("AD_PAUSED");
        });

        player.addListener(this, AdEvent.skipped, event -> {
            log.d("AD_SKIPPED");
        });

        player.addListener(this, AdEvent.allAdsCompleted, event -> {
            log.d("AD_ALL_ADS_COMPLETED");
        });

        player.addListener(this, AdEvent.completed, event -> {
            log.d("AD_COMPLETED");
        });

        player.addListener(this, AdEvent.firstQuartile, event -> {
            log.d("FIRST_QUARTILE");
        });

        player.addListener(this, AdEvent.midpoint, event -> {
            log.d("MIDPOINT");
            if (player != null) {
                AdController adController = player.getController(AdController.class);
                if (adController != null) {
                    if (adController.isAdDisplayed()) {
                        log.d("AD CONTROLLER API: " + adController.getAdCurrentPosition() + "/" + adController.getAdDuration());
                    }
                    //Log.d(TAG, "adController.getCuePoints().getAdCuePoints().size());
                    //Log.d(TAG, adController.getAdInfo().toString());
                    //adController.skip();
                }
            }
        });

        player.addListener(this, AdEvent.thirdQuartile, event -> {
            log.d("THIRD_QUARTILE");
        });

        player.addListener(this, AdEvent.adBreakEnded, event -> {
            log.d("AD_BREAK_ENDED");
        });

        player.addListener(this, AdEvent.adClickedEvent, event -> {
            AdEvent.AdClickedEvent advtClickEvent = event;
            log.d("AD_CLICKED url = " + advtClickEvent.clickThruUrl);
        });

        player.addListener(this, AdEvent.error, event -> {
            AdEvent.Error adError = event;
            log.d("AD_ERROR : " + adError.error.errorType.name());
        });

        player.addListener(this, PlayerEvent.error, event -> {
            log.d("PLAYER ERROR " + event.error.message);
        });
    }
}
