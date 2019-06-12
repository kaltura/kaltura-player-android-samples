package com.kaltura.playkit.samples.imasample;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.ads.AdController;
import com.kaltura.playkit.plugins.ads.AdEvent;
import com.kaltura.playkit.plugins.ads.AdInfo;
import com.kaltura.playkit.plugins.ima.IMAConfig;
import com.kaltura.playkit.plugins.ima.IMAPlugin;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OVPMediaOptions;
import com.kaltura.tvplayer.PlayerInitOptions;
import com.kaltura.tvplayer.config.player.UiConf;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    //Ad configuration constants.
    String preMidPostSingleAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=";

    private static final int PLAYER_HEIGHT = 600;
    private static final Long START_POSITION = 0L; // position tp start playback in msec.

    private static final String SERVER_URL = "https://cdnapisec.kaltura.com";
    private static final String ENTRY_ID = "1_w9zx2eti";
    private static final int PARTNER_ID = 2215841;
    private static final int UICONF_ID = 43795681;
    private static final int UICONF_PARTNER_ID = 2427592;

    private KalturaPlayer player;
    private Button playPauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Add simple play/pause button.
        addPlayPauseButton();

        loadPlaykitPlayer();

        //Subscribe to the ad events.
        subscribeToAdEvents();
    }

    /**
     * Will subscribe to ad events.
     * For simplicity, in this example we will show subscription to the couple of events.
     * For the full list of ad events you can check our documentation.
     * !!!Note, we will receive only ad events, we subscribed to.
     */
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
            Log.d(TAG, "ad event received: " + event.eventType().name()
                    + ". Additional info: ad content type is: "
                    + adInfo.getAdContentType());
        });

        player.addListener(this, AdEvent.contentResumeRequested, event -> {
            Log.d(TAG, "ADS_PLAYBACK_ENDED");
        });

        player.addListener(this, AdEvent.adPlaybackInfoUpdated, event -> {
            AdEvent.AdPlaybackInfoUpdated playbackInfoUpdated = event;
            Log.d(TAG, "AD_PLAYBACK_INFO_UPDATED  = " + playbackInfoUpdated.width + "/" + playbackInfoUpdated.height + "/" + playbackInfoUpdated.bitrate);
        });

        player.addListener(this, AdEvent.skippableStateChanged, event -> {
            Log.d(TAG, "SKIPPABLE_STATE_CHANGED");
        });

        player.addListener(this, AdEvent.adRequested, event -> {
            AdEvent.AdRequestedEvent adRequestEvent = event;
            Log.d(TAG, "AD_REQUESTED adtag = " + adRequestEvent.adTagUrl);
        });

        player.addListener(this, AdEvent.playHeadChanged, event -> {
            AdEvent.AdPlayHeadEvent adEventProress = event;
            //Log.d(TAG, "received AD PLAY_HEAD_CHANGED " + adEventProress.adPlayHead);
        });


        player.addListener(this, AdEvent.adBreakStarted, event -> {
            Log.d(TAG, "AD_BREAK_STARTED");
        });

        player.addListener(this, AdEvent.cuepointsChanged, event -> {
            AdEvent.AdCuePointsUpdateEvent cuePointsList = event;
            Log.d(TAG, "AD_CUEPOINTS_UPDATED HasPostroll = " + cuePointsList.cuePoints.hasPostRoll());
        });

        player.addListener(this, AdEvent.loaded, event -> {
            AdEvent.AdLoadedEvent adLoadedEvent = event;
            Log.d(TAG, "AD_LOADED " + adLoadedEvent.adInfo.getAdIndexInPod() + "/" + adLoadedEvent.adInfo.getTotalAdsInPod());
        });

        player.addListener(this, AdEvent.started, event -> {
            AdEvent.AdStartedEvent adStartedEvent = event;
            Log.d(TAG, "AD_STARTED w/h - " + adStartedEvent.adInfo.getAdWidth() + "/" + adStartedEvent.adInfo.getAdHeight());
        });

        player.addListener(this, AdEvent.resumed, event -> {
            Log.d(TAG, "AD_RESUMED");
        });

        player.addListener(this, AdEvent.paused, event -> {
            Log.d(TAG, "AD_PAUSED");
        });

        player.addListener(this, AdEvent.skipped, event -> {
            Log.d(TAG, "AD_SKIPPED");
        });

        player.addListener(this, AdEvent.allAdsCompleted, event -> {
            Log.d(TAG, "AD_ALL_ADS_COMPLETED");
        });

        player.addListener(this, AdEvent.completed, event -> {
            Log.d(TAG, "AD_COMPLETED");
        });

        player.addListener(this, AdEvent.firstQuartile, event -> {
            Log.d(TAG, "FIRST_QUARTILE");
        });

        player.addListener(this, AdEvent.midpoint, event -> {
            Log.d(TAG, "MIDPOINT");
            if (player != null) {
                AdController adController = player.getController(AdController.class);
                if (adController != null) {
                    if (adController.isAdDisplayed()) {
                        Log.d(TAG, "AD CONTROLLER API: " + adController.getAdCurrentPosition() + "/" + adController.getAdDuration());
                    }
                    //Log.d(TAG, "adController.getCuePoints().getAdCuePoints().size());
                    //Log.d(TAG, adController.getAdInfo().toString());
                    //adController.skip();
                }
            }
        });

        player.addListener(this, AdEvent.thirdQuartile, event -> {
            Log.d(TAG, "THIRD_QUARTILE");
        });

        player.addListener(this, AdEvent.adBreakEnded, event -> {
            Log.d(TAG, "AD_BREAK_ENDED");
        });

        player.addListener(this, AdEvent.adClickedEvent, event -> {
            AdEvent.AdClickedEvent advtClickEvent = event;
            Log.d(TAG, "AD_CLICKED url = " + advtClickEvent.clickThruUrl);
        });

        player.addListener(this, AdEvent.error, event -> {
            AdEvent.Error adError = event;
            Log.e(TAG, "AD_ERROR : " + adError.error.errorType.name());
        });

        player.addListener(this, PlayerEvent.error, event -> {
            Log.e(TAG, "PLAYER ERROR " + event.error.message);
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

        player = KalturaPlayer.createOVPPlayer(MainActivity.this, playerInitOptions);
        player.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, PLAYER_HEIGHT);
        ViewGroup container = findViewById(R.id.player_root);
        container.addView(player.getPlayerView());

        OVPMediaOptions ovpMediaOptions = buildOvpMediaOptions();
        player.loadMedia(ovpMediaOptions, (entry, error) -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "OVPMedia onEntryLoadComplete  entry = " + entry.getId());
            }
        });
    }

    private OVPMediaOptions buildOvpMediaOptions() {
        OVPMediaOptions ovpMediaOptions = new OVPMediaOptions();
        ovpMediaOptions.entryId = ENTRY_ID;
        ovpMediaOptions.ks = null;
        ovpMediaOptions.startPosition = START_POSITION;
        return ovpMediaOptions;
    }

    private IMAConfig getAdsConfig(String adTagUrl) {
        List<String> videoMimeTypes = new ArrayList<>();
        videoMimeTypes.add("video/mp4");
        videoMimeTypes.add("application/x-mpegURL");
        videoMimeTypes.add("application/dash+xml");
        return new IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8);
    }

}
