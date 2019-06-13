package com.kaltura.playkit.samples.imadaisample;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.ads.interactivemedia.v3.api.StreamRequest;
import com.google.gson.JsonObject;
import com.kaltura.playkit.PKEvent;
import com.kaltura.playkit.PKMediaConfig;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaFormat;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.PlayKitManager;
import com.kaltura.playkit.Player;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.ads.AdController;
import com.kaltura.playkit.plugins.ads.AdEvent;
import com.kaltura.playkit.plugins.ads.AdInfo;
import com.kaltura.playkit.plugins.ima.IMAConfig;
import com.kaltura.playkit.plugins.ima.IMAPlugin;
import com.kaltura.playkit.plugins.imadai.IMADAIConfig;
import com.kaltura.playkit.plugins.imadai.IMADAIPlugin;
import com.kaltura.playkit.providers.api.phoenix.APIDefines;
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OTTMediaOptions;
import com.kaltura.tvplayer.PlayerInitOptions;
import com.kaltura.tvplayer.config.player.UiConf;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    //Tag for logging.
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final Long START_POSITION = 0L; // position tp start playback in msec.

    //Media entry configuration constants.
    private static final String SERVER_URL = "https://api-preprod.ott.kaltura.com/v4_7/api_v3/";
    private static final String ASSET_ID = "480989";
    private static final int PARTNER_ID = 198;
    private static final int UICONF_ID = 41188731;
    private static final int UICONF_PARTNER_ID = 2215841;

    //Ad configuration constants.
    private static final String AD_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostpod&cmsid=496&vid=short_onecue&correlator=";
    //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=";
            //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";
    private static final String INCORRECT_AD_TAG_URL = "incorrect_ad_tag_url";
    private static final int PREFERRED_AD_BITRATE = 600;

    private KalturaPlayer player;
    private PKMediaConfig mediaConfig;
    private Button playPauseButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Create plugin configurations.
        PKPluginConfigs pluginConfigs = createIMADAIPlugin();

        loadPlaykitPlayer();

        //Subscribe to the ad events.
        subscribeToAdEvents();

        //Add simple play/pause button.
        addPlayPauseButton();

    }

    /**
     * Create IMAPlugin object.
     *
     * @return - {@link PKPluginConfigs} object with IMAPlugin.
     */
    private PKPluginConfigs createIMADAIPlugin() {

        //Initialize plugin configuration object.
        PKPluginConfigs pluginConfigs = new PKPluginConfigs();

        //Initialize + Configure  IMADAIConfig object.
        IMADAIConfig adsConfigVodHls1 = getDAIConfigVodHls1().enableDebugMode(true).setAlwaysStartWithPreroll(true);
        IMADAIConfig adsConfigVodLive = getDAIConfigLiveHls().enableDebugMode(true).setAlwaysStartWithPreroll(true);
        IMADAIConfig adsConfigVodDash = getDAIConfigVodDash().enableDebugMode(true).setAlwaysStartWithPreroll(true);
        IMADAIConfig adsConfigError = getDAIConfigError().enableDebugMode(true).setAlwaysStartWithPreroll(true);

        IMADAIConfig adsConfigVodHls2 = getDAIConfigVodHls2().enableDebugMode(true).setAlwaysStartWithPreroll(true);
        IMADAIConfig adsConfigVodHls3 = getDAIConfigVodHls3().enableDebugMode(true).setAlwaysStartWithPreroll(true);
        IMADAIConfig adsConfigVodHls4 = getDAIConfigVodHls4().enableDebugMode(true).setAlwaysStartWithPreroll(true);

        /* For MOAT call this API:
            List<View> overlaysList = new ArrayList<>();
            //overlaysList.add(....)
            adsConfigVodHls4.setControlsOverlayList(overlaysList);
        */

        //Set jsonObject to the main pluginConfigs object.
        pluginConfigs.setPluginConfig(IMADAIPlugin.factory.getName(), adsConfigVodHls1);

        /*
            NOTE!  for change media before player.prepare api please call:
            player.updatePluginConfig(IMADAIPlugin.factory.getName(), getDAIConfigVodHls2());
        */

        //Return created PluginConfigs object.
        return pluginConfigs;
    }

    private IMADAIConfig getDAIConfigLiveHls() {
        String assetTitle = "Live Video - Big Buck Bunny";
        String assetKey = "sN_IYUG8STe1ZzhIIE_ksA";
        String apiKey = null;
        StreamRequest.StreamFormat streamFormat = StreamRequest.StreamFormat.HLS;
        String licenseUrl = null;
        return IMADAIConfig.getLiveIMADAIConfig(assetTitle,
                assetKey,
                apiKey,
                streamFormat,
                licenseUrl).setAlwaysStartWithPreroll(true).enableDebugMode(true);
    }

    private IMADAIConfig getDAIConfigVodHls1() {
        String assetTitle = "VOD - Tears of Steel";
        String apiKey = null;
        String contentSourceId = "19463";
        String videoId = "tears-of-steel";
        StreamRequest.StreamFormat streamFormat = StreamRequest.StreamFormat.HLS;
        String licenseUrl = null;

        return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                contentSourceId,
                videoId,
                apiKey,
                streamFormat,
                licenseUrl).enableDebugMode(true).setAlwaysStartWithPreroll(true);
    }

    private IMADAIConfig getDAIConfigVodHls2() {
        String assetTitle = "VOD - Google I/O";
        String apiKey = null;
        String contentSourceId = "19463";
        String videoId = "googleio-highlights";
        StreamRequest.StreamFormat streamFormat = StreamRequest.StreamFormat.HLS;
        String licenseUrl = null;
        return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                contentSourceId,
                videoId,
                apiKey,
                streamFormat,
                licenseUrl).enableDebugMode(true);
    }

    private IMADAIConfig getDAIConfigVodHls3() {
        String assetTitle = "HLS3";
        String apiKey = null;
        String contentSourceId = "19823";
        String videoId = "ima-test";
        StreamRequest.StreamFormat streamFormat = StreamRequest.StreamFormat.HLS;
        String licenseUrl = null;
        return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                contentSourceId,
                videoId,
                apiKey,
                streamFormat,
                licenseUrl).enableDebugMode(true);
    }

    private IMADAIConfig getDAIConfigVodHls4() {
        String assetTitle3 = "HLS4";
        String assetKey3 = null;
        String apiKey3 = null;
        String contentSourceId3 = "2472176";
        String videoId3 = "2504847";
        StreamRequest.StreamFormat streamFormat3 = StreamRequest.StreamFormat.HLS;
        String licenseUrl3 = null;
        return IMADAIConfig.getVodIMADAIConfig(assetTitle3,
                contentSourceId3,
                videoId3,
                apiKey3,
                streamFormat3,
                licenseUrl3);
    }

    private IMADAIConfig getDAIConfigVodDash() {
        String assetTitle = "BBB-widevine";
        String apiKey = null;
        String contentSourceId = "2474148";
        String videoId = "bbb-widevine";
        StreamRequest.StreamFormat streamFormat = StreamRequest.StreamFormat.DASH;
        String licenseUrl = "https://proxy.uat.widevine.com/proxy";
        return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                contentSourceId,
                videoId,
                apiKey,
                streamFormat,
                licenseUrl).enableDebugMode(true);
    }


    private IMADAIConfig getDAIConfigError() {
        String assetTitle = "ERROR";
        String assetKey = null;
        String apiKey = null;
        String contentSourceId = "19823";
        String videoId = "ima-test";
        StreamRequest.StreamFormat streamFormat = StreamRequest.StreamFormat.HLS;
        String licenseUrl = null;
        return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                contentSourceId + "AAAA",
                videoId,
                apiKey,
                streamFormat,
                licenseUrl).enableDebugMode(true);
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
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    public void loadPlaykitPlayer() {

        PlayerInitOptions playerInitOptions = new PlayerInitOptions(PARTNER_ID, new UiConf(UICONF_ID, UICONF_PARTNER_ID));
        playerInitOptions.setServerUrl(SERVER_URL);
        playerInitOptions.setAutoPlay(true);

        // IMA DAI Configuration
        playerInitOptions.setPluginConfigs(createIMADAIPlugin());

        player = KalturaPlayer.createOTTPlayer(MainActivity.this, playerInitOptions);

        player.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        ViewGroup container = findViewById(R.id.player_root);
        container.addView(player.getPlayerView());

        OTTMediaOptions ottMediaOptions = buildOttMediaOptions();
        player.loadMedia(ottMediaOptions, (entry, error) -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                Log.d(TAG, "OTTMedia onEntryLoadComplete  entry = " + entry.getId());
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

}
