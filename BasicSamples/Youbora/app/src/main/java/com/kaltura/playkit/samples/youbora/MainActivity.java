package com.kaltura.playkit.samples.youbora;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
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
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.PlayerInitOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    //Tag for logging.
    private static final String TAG = MainActivity.class.getSimpleName();

    private static final Long START_POSITION = 0L; // position tp start playback in msec.

    private static final PKMediaFormat MEDIA_FORMAT = PKMediaFormat.hls;
    private static final String SOURCE_URL = "https://cdnapisec.kaltura.com/p/2215841/sp/221584100/playManifest/entryId/1_w9zx2eti/protocol/https/format/applehttp/falvorIds/1_1obpcggb,1_yyuvftfz,1_1xdbzoa6,1_k16ccgto,1_djdf6bk8/a.m3u8";
    private static final String LICENSE_URL = null;
    private static final int PLAYER_HEIGHT = 600;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PKMediaEntry mediaEntry = createMediaEntry();

        loadPlaykitPlayer(mediaEntry);

        //Subscribe to analytics report event.
        subscribeToYouboraReportEvent();

        //Add simple play/pause button.
        addPlayPauseButton();
    }


    /**
     * Create {@link PKMediaEntry} with minimum necessary data.
     *
     * @return - the {@link PKMediaEntry} object.
     */
    private PKMediaEntry createMediaEntry() {
        //Create media entry.
        PKMediaEntry mediaEntry = new PKMediaEntry();

        //Set id for the entry.
        mediaEntry.setId("testEntry");

        //Set media entry type. It could be Live,Vod or Unknown.
        //In this sample we use Vod.
        mediaEntry.setMediaType(PKMediaEntry.MediaEntryType.Vod);

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        List<PKMediaSource> mediaSources = createMediaSources();

        //Set media sources to the entry.
        mediaEntry.setSources(mediaSources);

        return mediaEntry;
    }

    /**
     * Create list of {@link PKMediaSource}.
     *
     * @return - the list of sources.
     */
    private List<PKMediaSource> createMediaSources() {

        //Create new PKMediaSource instance.
        PKMediaSource mediaSource = new PKMediaSource();

        //Set the id.
        mediaSource.setId("testSource");

        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.setUrl(SOURCE_URL);

        //Set the format of the source. In our case it will be hls in case of mpd/wvm formats you have to to call mediaSource.setDrmData method as well
        mediaSource.setMediaFormat(MEDIA_FORMAT);

        // Add DRM data if required
        if (LICENSE_URL != null) {
            mediaSource.setDrmData(Collections.singletonList(
                    new PKDrmParams(LICENSE_URL, PKDrmParams.Scheme.WidevineCENC)
            ));
        }

        return Collections.singletonList(mediaSource);
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

    public void loadPlaykitPlayer(PKMediaEntry pkMediaEntry) {
        PlayerInitOptions playerInitOptions = new PlayerInitOptions();

        // Youbora Configuration
        PKPluginConfigs pkPluginConfigs = new PKPluginConfigs();
        JsonObject youboraConfigJson = getYouboraConfig();
        pkPluginConfigs.setPluginConfig(YouboraPlugin.factory.getName(), youboraConfigJson);
        playerInitOptions.setPluginConfigs(pkPluginConfigs);

        player = KalturaPlayer.createBasicPlayer(MainActivity.this, playerInitOptions);
        player.setMedia(pkMediaEntry, START_POSITION);
        player.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, PLAYER_HEIGHT);

        ViewGroup container = findViewById(R.id.player_root);
        container.addView(player.getPlayerView());
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
