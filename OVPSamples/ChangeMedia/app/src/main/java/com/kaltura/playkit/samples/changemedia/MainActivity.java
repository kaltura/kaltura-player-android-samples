package com.kaltura.playkit.samples.changemedia;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;

import com.google.gson.JsonObject;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.PlayerState;
import com.kaltura.playkit.plugins.ima.IMAConfig;
import com.kaltura.playkit.plugins.ima.IMAPlugin;
import com.kaltura.playkit.plugins.youbora.YouboraPlugin;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OVPMediaOptions;
import com.kaltura.tvplayer.PlayerInitOptions;

import java.util.ArrayList;
import java.util.List;

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
    private PlayerState playerState;

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

    private void addPlayerStateListener() {
        player.addListener(this, PlayerEvent.stateChanged, event -> {
            log.d("State changed from " + event.oldState + " to " + event.newState);
            playerState = event.newState;
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
            updatePluginsConfig("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=", "TITLE2");
            prepareSecondEntry();
        } else {
            updatePluginsConfig("https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=", "TITLE3");
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

    private IMAConfig getAdsConfig(String adTagUrl) {
        List<String> videoMimeTypes = new ArrayList<>();
        videoMimeTypes.add("video/mp4");
        videoMimeTypes.add("application/x-mpegURL");
        videoMimeTypes.add("application/dash+xml");
        return new IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8);
    }

    @NonNull
    private JsonObject getYouboraJsonObject(String title) {
        JsonObject pluginEntry = new JsonObject();

        pluginEntry.addProperty("accountCode", "kalturatest");
        pluginEntry.addProperty("username", "a@a.com");
        pluginEntry.addProperty("haltOnError", true);
        pluginEntry.addProperty("enableAnalytics", true);
        pluginEntry.addProperty("enableSmartAds", true);


        //Optional - Device json o/w youbora will decide by its own.
        JsonObject deviceJson = new JsonObject();
        deviceJson.addProperty("deviceCode", "AndroidTV");
        deviceJson.addProperty("brand", "Xiaomi");
        deviceJson.addProperty("model", "Mii3");
        deviceJson.addProperty("type", "TvBox");
        deviceJson.addProperty("osName", "Android/Oreo");
        deviceJson.addProperty("osVersion", "8.1");


        //Media entry json.
        JsonObject mediaEntryJson = new JsonObject();
        //mediaEntryJson.addProperty("isLive", isLive);
        mediaEntryJson.addProperty("title", title);

        //Youbora ads configuration json.
        JsonObject adsJson = new JsonObject();
        adsJson.addProperty("adsExpected", true);
        adsJson.addProperty("campaign", "zzz");

        //Configure custom properties here:
        JsonObject propertiesJson = new JsonObject();
        propertiesJson.addProperty("genre", "");
        propertiesJson.addProperty("type", "");
        propertiesJson.addProperty("transaction_type", "");
        propertiesJson.addProperty("year", "");
        propertiesJson.addProperty("cast", "");
        propertiesJson.addProperty("director", "");
        propertiesJson.addProperty("owner", "");
        propertiesJson.addProperty("parental", "");
        propertiesJson.addProperty("price", "");
        propertiesJson.addProperty("rating", "");
        propertiesJson.addProperty("audioType", "");
        propertiesJson.addProperty("audioChannels", "");
        propertiesJson.addProperty("device", "");
        propertiesJson.addProperty("quality", "");

        //You can add some extra params here:
        JsonObject extraParamJson = new JsonObject();
        extraParamJson.addProperty("param1", "param1");
        extraParamJson.addProperty("param2", "param2");

        //Add all the json objects created before to the pluginEntry json.
        pluginEntry.add("device", deviceJson);
        pluginEntry.add("media", mediaEntryJson);
        pluginEntry.add("ads", adsJson);
        pluginEntry.add("properties", propertiesJson);
        pluginEntry.add("extraParams", extraParamJson);
        return pluginEntry;
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
        if (player != null && playerState != null) {
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

        PKPluginConfigs pluginConfig = new PKPluginConfigs();
        addPluginsConfig(pluginConfig, "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=", "title1");

        PlayerInitOptions playerInitOptions = new PlayerInitOptions(PARTNER_ID);
        playerInitOptions.setAutoPlay(true);
        playerInitOptions.setAllowCrossProtocolEnabled(true);
        playerInitOptions.setPluginConfigs(pluginConfig);
        player = KalturaPlayer.createOVPPlayer(MainActivity.this, playerInitOptions);

        player.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        ViewGroup container = findViewById(R.id.player_root);
        container.addView(player.getPlayerView());

        //Prepare the first entry.
        prepareFirstEntry();

        addPlayerStateListener();
    }

    private void addPluginsConfig(PKPluginConfigs config, String adTag, String title) {
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/3274935/preroll&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]";
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostpod&cmsid=496&vid=short_onecue&correlator=";
        IMAConfig adsConfig = getAdsConfig(adTag);
        config.setPluginConfig(IMAPlugin.factory.getName(), adsConfig);
        JsonObject pluginEntry = getYouboraJsonObject(title);

        //Set plugin entry to the plugin configs.
        config.setPluginConfig(YouboraPlugin.factory.getName(), pluginEntry);

    }

    private void updatePluginsConfig(String adTag, String title) {
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/3274935/preroll&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]";
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostpod&cmsid=496&vid=short_onecue&correlator=";
        if (player != null) {
            IMAConfig adsConfig = getAdsConfig(adTag);
            player.updatePluginConfig(IMAPlugin.factory.getName(), adsConfig);
            JsonObject pluginEntry = getYouboraJsonObject(title);
            //Set plugin entry to the plugin configs.
            player.updatePluginConfig(YouboraPlugin.factory.getName(), pluginEntry);
        }
    }
}