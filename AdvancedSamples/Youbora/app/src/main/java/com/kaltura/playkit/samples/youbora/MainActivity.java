package com.kaltura.playkit.samples.youbora;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.google.gson.JsonObject;
import com.kaltura.playkit.PKDrmParams;
import com.kaltura.playkit.PKEvent;
import com.kaltura.playkit.PKMediaConfig;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaFormat;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.PlayKitManager;
import com.kaltura.playkit.Player;
import com.kaltura.playkit.plugins.youbora.YouboraEvent;
import com.kaltura.playkit.plugins.youbora.YouboraPlugin;
import com.kaltura.playkit.plugins.youbora.pluginconfig.YouboraConfig;
import com.kaltura.playkit.providers.api.phoenix.APIDefines;
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OTTMediaOptions;
import com.kaltura.tvplayer.OVPMediaOptions;
import com.kaltura.tvplayer.PlayerInitOptions;
import com.kaltura.tvplayer.config.player.UiConf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    //Tag for logging.
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final Long START_POSITION = 0L; // position tp start playback in msec.

    private static final String SERVER_URL = "https://api-preprod.ott.kaltura.com/v4_7/api_v3/";
    private static final String ASSET_ID = "480989";
    private static final int PARTNER_ID = 198;
    private static final int UICONF_ID = 41188731;
    private static final int UICONF_PARTNER_ID = 2215841;

    //Youbora analytics Constants
    public static final String ACCOUNT_CODE = "your_account_code";
    public static final String UNIQUE_USER_NAME = "your_app_logged_in_user_email_or_userId";
    public static final String MEDIA_TITLE = "your_media_title";
    public static final boolean IS_LIVE = false;
    public static final boolean ENABLE_SMART_ADS = true;
    private static final String CAMPAIGN = "your_campaign_name";
    public static final String EXTRA_PARAM_1 = "playKitPlayer";
    public static final String EXTRA_PARAM_2 = "";
    public static final String GENRE = "your_genre";
    public static final String TYPE = "your_type";
    public static final String TRANSACTION_TYPE = "your_trasnsaction_type";
    public static final String YEAR = "your_year";
    public static final String CAST = "your_cast";
    public static final String DIRECTOR = "your_director";
    private static final String OWNER = "your_owner";
    public static final String PARENTAL = "your_parental";
    public static final String PRICE = "your_price";
    public static final String RATING = "your_rating";
    public static final String AUDIO_TYPE = "your_audio_type";
    public static final String AUDIO_CHANNELS = "your_audoi_channels";
    public static final String DEVICE = "your_device";
    public static final String QUALITY = "your_quality";


    private KalturaPlayer player;
    private Button playPauseButton;
    private boolean isFullScreen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        loadPlaykitPlayer();

        //Subscribe to analytics report event.
        subscribeToYouboraReportEvent();

        //Add simple play/pause button.
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
     * Subscribe to kaltura stats report event.
     * This event will be received each and every time
     * the analytics report is sent.
     */
    private void subscribeToYouboraReportEvent() {
        //Subscribe to the event.
        player.addListener(this, YouboraEvent.reportSent, event -> {
            YouboraEvent.YouboraReport reportEvent = event;

            //Get the event name from the report.
            String reportedEventName = reportEvent.reportedEventName;
            Log.i(TAG, "Youbora report sent. Reported event name: " + reportedEventName);
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

        // Youbora Configuration
        PKPluginConfigs pkPluginConfigs = new PKPluginConfigs();
        JsonObject youboraConfigJson = getYouboraConfig();
        pkPluginConfigs.setPluginConfig(YouboraPlugin.factory.getName(), youboraConfigJson);
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
                Log.i(TAG, "OTTMedia onEntryLoadComplete  entry = " + entry.getId());
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

    private JsonObject getYouboraConfig() {

        //Youbora config json. Main config goes here.
        JsonObject youboraConfigJson = new JsonObject();
        youboraConfigJson.addProperty("accountCode", ACCOUNT_CODE);
        youboraConfigJson.addProperty("username", UNIQUE_USER_NAME);
        youboraConfigJson.addProperty("haltOnError", true);
        youboraConfigJson.addProperty("enableAnalytics", true);
        youboraConfigJson.addProperty("enableSmartAds", ENABLE_SMART_ADS);


        //Media entry json.
        JsonObject mediaEntryJson = new JsonObject();
        mediaEntryJson.addProperty("isLive", IS_LIVE);
        mediaEntryJson.addProperty("title", MEDIA_TITLE);

        //Optional - Device json o/w youbora will decide by its own.
        JsonObject deviceJson = new JsonObject();
        deviceJson.addProperty("deviceCode", "AndroidTV");
        deviceJson.addProperty("brand", "Xiaomi");
        deviceJson.addProperty("model", "Mii3");
        deviceJson.addProperty("type", "TvBox");
        deviceJson.addProperty("osName", "Android/Oreo");
        deviceJson.addProperty("osVersion", "8.1");

        //Youbora ads configuration json.
        JsonObject adsJson = new JsonObject();
        adsJson.addProperty("adsExpected", true);
        adsJson.addProperty("campaign", CAMPAIGN);

        //Configure custom properties here:
        JsonObject propertiesJson = new JsonObject();
        propertiesJson.addProperty("genre", GENRE);
        propertiesJson.addProperty("type", TYPE);
        propertiesJson.addProperty("transaction_type", TRANSACTION_TYPE);
        propertiesJson.addProperty("year", YEAR);
        propertiesJson.addProperty("cast", CAST);
        propertiesJson.addProperty("director", DIRECTOR);
        propertiesJson.addProperty("owner", OWNER);
        propertiesJson.addProperty("parental", PARENTAL);
        propertiesJson.addProperty("price", PRICE);
        propertiesJson.addProperty("rating", RATING);
        propertiesJson.addProperty("audioType", AUDIO_TYPE);
        propertiesJson.addProperty("audioChannels", AUDIO_CHANNELS);
        propertiesJson.addProperty("device", DEVICE);
        propertiesJson.addProperty("quality", QUALITY);

        //You can add some extra params here:
        JsonObject extraParamJson = new JsonObject();
        extraParamJson.addProperty("param1", EXTRA_PARAM_1);
        extraParamJson.addProperty("param2", EXTRA_PARAM_2);

        //Add all the json objects created before to the pluginEntry json.
        youboraConfigJson.add("media", mediaEntryJson);
        youboraConfigJson.add("device", deviceJson);
        youboraConfigJson.add("ads", adsJson);
        youboraConfigJson.add("properties", propertiesJson);
        youboraConfigJson.add("extraParams", extraParamJson);

        return youboraConfigJson;
    }


}