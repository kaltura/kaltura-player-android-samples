package com.kaltura.playkitdemo;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.interactivemedia.v3.api.StreamRequest;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;
import com.google.gson.JsonObject;
import com.kaltura.playkit.PKDrmParams;
import com.kaltura.playkit.PKEvent;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.PKRequestParams;
import com.kaltura.playkit.PlayKitManager;
import com.kaltura.playkit.Player;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.PlayerState;
import com.kaltura.playkit.player.AudioTrack;
import com.kaltura.playkit.player.BaseTrack;
import com.kaltura.playkit.player.MediaSupport;
import com.kaltura.playkit.player.PKTracks;
import com.kaltura.playkit.player.TextTrack;
import com.kaltura.playkit.player.VideoTrack;
import com.kaltura.playkit.plugins.ads.AdCuePoints;
import com.kaltura.playkit.plugins.ads.AdEvent;
import com.kaltura.playkit.plugins.ima.IMAConfig;
import com.kaltura.playkit.plugins.ima.IMAPlugin;
import com.kaltura.playkit.plugins.imadai.IMADAIConfig;
import com.kaltura.playkit.plugins.imadai.IMADAIPlugin;
import com.kaltura.playkit.plugins.ott.OttEvent;
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsConfig;
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsEvent;
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsPlugin;
import com.kaltura.playkit.plugins.youbora.YouboraPlugin;
import com.kaltura.playkit.providers.MediaEntryProvider;
import com.kaltura.playkit.providers.api.phoenix.APIDefines;
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider;
import com.kaltura.playkit.utils.Consts;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OTTMediaOptions;
import com.kaltura.tvplayer.OVPMediaOptions;
import com.kaltura.tvplayer.PlayerInitOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

//import com.kaltura.playkitvr.VRUtil;


public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener,
        OrientationManager.OrientationListener {

    private static final PKLog log = PKLog.get("MainActivity");
    public static final String IMA_PLUGIN = "IMA";
    public static final String DAI_PLUGIN = "DAI";
    public static int READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST = 123;
    public static int changeMediaIndex = -1;
    public static Long START_POSITION = 0L;//65L;

    String preMidPostAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostpodbumper&cmsid=496&vid=short_onecue&correlator=";
    String preSkipAdTagUrl    = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";
    String inLinePreAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dlinear&correlator=";
    String preMidPostSingleAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=";

    private KalturaPlayer player;
    private MediaEntryProvider mediaProvider;
    private PlaybackControlsView controlsView;
    private boolean nowPlaying;
    private boolean isFullScreen;
    ProgressBar progressBar;
    private RelativeLayout playerContainer;
    private RelativeLayout spinerContainer;
    private AppCompatImageView fullScreenBtn;
    private AdCuePoints adCuePoints;
    private Spinner videoSpinner, audioSpinner, textSpinner;

    private OrientationManager mOrientationManager;
    private boolean userIsInteracting;
    private PKTracks tracksInfo;
    private boolean isAdsEnabled = true;
    private boolean isDAIMode = false;

    PlayerInitOptions playerInitOptions;

    // OVP startSimpleOvpMediaLoadingHls

    private static final String OVP_SERVER_URL_HLS = "https://cdnapisec.kaltura.com";
    private static final String OVP_ENTRY_ID_HLS = "1_3o1seqnv";
    private static final int OVP_PARTNER_ID_HLS = 1734751;


    // OVP startSimpleOvpMediaLoadingDRM

    private static final String OVP_SERVER_URL_DRM = "https://cdnapisec.kaltura.com";
    private static final String OVP_ENTRY_ID_DRM = "1_f93tepsn"; //("1_asoyc5ef") //("1_uzea2uje")
    private static final int OVP_PARTNER_ID_DRM = 2222401;

    // OVP startSimpleOvpMediaLoadingVR

    private static final String OVP_SERVER_URL_VR = "https://cdnapisec.kaltura.com";
    private static final String OVP_ENTRY_ID_VR = "1_afvj3z0u";
    private static final int OVP_PARTNER_ID_VR = 2196781;

    // OVP startSimpleOvpMediaLoadingClear

    private static final String OVP_SERVER_URL_CLEAR = "http://qa-apache-php7.dev.kaltura.com/";
    private static final String OVP_ENTRY_ID_CLEAR = "0_wu32qrt3";
    private static final int OVP_PARTNER_ID_CLEAR = 1091;

    // OVP startSimpleOvpMediaLoadingLive

    private static final String OVP_SERVER_URL_LIVE = "http://qa-apache-php7.dev.kaltura.com/";
    private static final String OVP_ENTRY_ID_LIVE = "0_nwkp7jtx";
    private static final int OVP_PARTNER_ID_LIVE = 1091;

    // OVP startSimpleOvpMediaLoadingLive_1

    private static final String OVP_SERVER_URL_LIVE_1 = "https://cdnapisec.kaltura.com/";
    private static final String OVP_ENTRY_ID_LIVE_1 = "1_fdv46dba";
    private static final int OVP_PARTNER_ID_LIVE_1 = 1740481;

    // OTT startOttMediaLoading
    private static final String OTT_SERVER_URL = MockParams.OTT_BASE_URL;
    private static final String OTT_ASSET_ID =MockParams.OTT_ASSET_ID; //bunny no horses id "485380"
    private static final int OTT_PARTNER_ID = MockParams.OTT_PARTNER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //getPermissionToReadExternalStorage();
        initDrm();

        try {
            ProviderInstaller.installIfNeeded(this);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }

        mOrientationManager = new OrientationManager(this, SensorManager.SENSOR_DELAY_NORMAL, this);
        mOrientationManager.enable();
        setContentView(R.layout.activity_main);

        log.i("PlayKitManager: " + PlayKitManager.CLIENT_TAG);

        Button button = findViewById(R.id.changeMedia);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (player != null) {
                    changeMediaIndex++;
                    changeMedia();
                    if (changeMediaIndex % 4 == 0) {
                        startSimpleOvpMediaLoadingHls();
                       // startSimpleOvpMediaLoadingDRM();
                        //startSimpleOvpMediaLoadingVR(playLoadedEntry);
                        //startMockMediaLoading(playLoadedEntry);
                    } else if (changeMediaIndex % 4 == 1) {
                        startSimpleOvpMediaLoadingHls();
                    } if (changeMediaIndex % 4 == 2) {
                        startSimpleOvpMediaLoadingHls();
                    } if (changeMediaIndex % 4 == 3) {
                        startSimpleOvpMediaLoadingHls();
                    }
                }
            }
        });

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        PKPluginConfigs pkPluginConfigs = configurePlugins();
        loadPlaykitPlayer(OTT_PARTNER_ID, OTT_SERVER_URL, PLAYER_TYPE.OTT, pkPluginConfigs);

        //startSimpleOvpMediaLoadingVR(playLoadedEntry);
        startSimpleOvpMediaLoadingHls();

        addPlayerListeners(progressBar);
        initSpinners();
        controlsView = findViewById(R.id.playerControls);
        controlsView.setPlayer(player);

//      startSimpleOvpMediaLoadingLive1(playLoadedEntry);
//      startMockMediaLoading(playLoadedEntry);
//      startOvpMediaLoading(playLoadedEntry);
        startOttMediaLoading();
//      startSimpleOvpMediaLoadingDRM(playLoadedEntry);
//      LocalAssets.start(this, playLoadedEntry);
        playerContainer = (RelativeLayout)findViewById(R.id.player_container);
        spinerContainer = (RelativeLayout)findViewById(R.id.spiner_container);
        fullScreenBtn = (AppCompatImageView)findViewById(R.id.full_screen_switcher);
        fullScreenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int orient;
                if (isFullScreen) {
                    orient = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                }
                else {
                    orient = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                }
                setRequestedOrientation(orient);
            }
        });
    }

    private void startOttMediaLoading() {
        buildOttMediaOptions(OTT_ASSET_ID, null);
    }

    private void startSimpleOvpMediaLoadingHls() {
        buildOvpMediaOptions(OVP_ENTRY_ID_HLS, null);
    }

    private void startSimpleOvpMediaLoadingDRM() {
        buildOvpMediaOptions(OVP_ENTRY_ID_DRM, null);
    }

    private void startSimpleOvpMediaLoadingVR() {
        buildOvpMediaOptions(OVP_ENTRY_ID_VR, null);
    }

    private void startSimpleOvpMediaLoadingClear() {
        buildOvpMediaOptions(OVP_ENTRY_ID_CLEAR, null);
    }

    private void startSimpleOvpMediaLoadingLive() {
        buildOvpMediaOptions(OVP_ENTRY_ID_LIVE, null);
    }

    private void startSimpleOvpMediaLoadingLive1() {
        buildOvpMediaOptions(OVP_ENTRY_ID_LIVE_1, null);
    }


    private void getPermissionToReadExternalStorage() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            }
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST);
        }
    }

    // Callback with the request from calling requestPermissions(...)
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST) {
            if (grantResults.length == 1 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read Storage permission granted", Toast.LENGTH_SHORT).show();
            } else {
                boolean showRationale = ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (showRationale) {
                    // do something here to handle degraded mode
                } else {
                    Toast.makeText(this, "Read Storage permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initDrm() {
        MediaSupport.initializeDrm(this, new MediaSupport.DrmInitCallback() {
            @Override
            public void onDrmInitComplete(Set<PKDrmParams.Scheme> supportedDrmSchemes, boolean provisionPerformed, Exception provisionError) {
                if (provisionPerformed) {
                    if (provisionError != null) {
                        log.e("DRM Provisioning failed", provisionError);
                    } else {
                        log.d("DRM Provisioning succeeded");
                    }
                }
                log.d("DRM initialized; supported: " + supportedDrmSchemes);

                // Now it's safe to look at `supportedDrmSchemes`
            }
        });
    }

    private PKMediaEntry simpleMediaEntry(String id, String contentUrl, String licenseUrl, PKDrmParams.Scheme scheme) {
        return new PKMediaEntry()
                .setSources(Collections.singletonList(new PKMediaSource()
                        .setUrl(contentUrl)
                        .setDrmData(Collections.singletonList(
                                new PKDrmParams(licenseUrl, scheme)
                                )
                        )))
                .setId(id);
    }

    private PKMediaEntry simpleMediaEntry(String id, String contentUrl) {
        return new PKMediaEntry()
                .setSources(Collections.singletonList(new PKMediaSource()
                        .setUrl(contentUrl)
                ))
                .setId(id);
    }

    private void changeMedia() {
            if (changeMediaIndex % 4 == 0) {
                if (isAdsEnabled) {
                    if (isDAIMode) {
                        promptMessage(DAI_PLUGIN, getDAIConfig2().getAssetTitle());
                        player.updatePluginConfig(IMADAIPlugin.factory.getName(), getDAIConfig2());
                    } else {
                        log.d("Play Ad preMidPostAdTagUrl");
                        promptMessage(IMA_PLUGIN, "preMidPostAdTagUrl");
                        player.updatePluginConfig(IMAPlugin.factory.getName(), getAdsConfig(preMidPostAdTagUrl));
                    }
                }
                player.updatePluginConfig(YouboraPlugin.factory.getName(), getYouboraJsonObject(false, "preMidPostAdTagUrl media2"));
            } else if (changeMediaIndex % 4 == 1) {
                if (isAdsEnabled) {
                    if (isDAIMode) {
                        promptMessage(DAI_PLUGIN, getDAIConfig3().getAssetTitle());
                        player.updatePluginConfig(IMADAIPlugin.factory.getName(), getDAIConfig3());
                    } else {
                        log.d("Play Ad inLinePreAdTagUrl");
                        promptMessage(IMA_PLUGIN, "inLinePreAdTagUrl");
                        player.updatePluginConfig(IMAPlugin.factory.getName(), getAdsConfig(inLinePreAdTagUrl));
                    }
                }
                player.updatePluginConfig(YouboraPlugin.factory.getName(), getYouboraJsonObject(true, "inLinePreAdTagUrl media3"));
            } if (changeMediaIndex % 4 == 2) {
                if (isAdsEnabled) {
                    if (isDAIMode) {
                        promptMessage(DAI_PLUGIN, getDAIConfig4().getAssetTitle());
                        player.updatePluginConfig(IMADAIPlugin.factory.getName(), getDAIConfig4());
                    } else {
                        log.d("Play NO Ad");
                        promptMessage(IMA_PLUGIN, "Enpty AdTag");
                        player.updatePluginConfig(IMAPlugin.factory.getName(), getAdsConfig(""));
                    }
                }
                player.updatePluginConfig(YouboraPlugin.factory.getName(), getYouboraJsonObject(false, "NO AD media4"));
            } if (changeMediaIndex % 4 == 3) {
                if (isAdsEnabled) {
                    if (isDAIMode) {
                        promptMessage(DAI_PLUGIN, getDAIConfig5().getAssetTitle());
                        player.updatePluginConfig(IMADAIPlugin.factory.getName(), getDAIConfig5());
                    } else {
                        log.d("Play Ad preSkipAdTagUrl");
                        promptMessage(IMA_PLUGIN, "preSkipAdTagUrl");
                        player.updatePluginConfig(IMAPlugin.factory.getName(), getAdsConfig(preSkipAdTagUrl));
                    }
                }

//                player.setPlayerBuffers(new LoadControlBuffers().
//                        setMinPlayerBufferMs(2500).
//                        setMaxPlayerBufferMs(50000).setAllowedVideoJoiningTimeMs(4000));

                player.updatePluginConfig(YouboraPlugin.factory.getName(), getYouboraJsonObject(false, "preSkipAdTagUrl media1"));
            }
    }

    private void initSpinners() {
        videoSpinner = (Spinner) this.findViewById(R.id.videoSpinner);
        audioSpinner = (Spinner) this.findViewById(R.id.audioSpinner);
        textSpinner = (Spinner) this.findViewById(R.id.subtitleSpinner);

        textSpinner.setOnItemSelectedListener(this);
        audioSpinner.setOnItemSelectedListener(this);
        videoSpinner.setOnItemSelectedListener(this);
    }

    private PKPluginConfigs configurePlugins() {

        PKPluginConfigs pluginConfig = new PKPluginConfigs();
        if (isAdsEnabled) {
            if (isDAIMode) {
                addIMADAIPluginConfig(pluginConfig, 1);
            } else {
                addIMAPluginConfig(pluginConfig);
            }
        }
        //addKaluraStatsPluginConfig(pluginConfigs, 1734751, "1_3o1seqnv");
        addYouboraPluginConfig(pluginConfig, false, "preMidPostSingleAdTagUrl Title1");
        //addKavaPluginConfig(pluginConfigs, 1734751, "1_3o1seqnv");
        //addPhoenixAnalyticsPluginConfig(pluginConfigs);
        //addTVPAPIAnalyticsPluginConfig(pluginConfigs);

        return pluginConfig;
    }

    private void addYouboraPluginConfig(PKPluginConfigs pluginConfigs, boolean isLive, String title) {
        JsonObject pluginEntry = getYouboraJsonObject(isLive, title);

        //Set plugin entry to the plugin configs.
        pluginConfigs.setPluginConfig(YouboraPlugin.factory.getName(), pluginEntry);
    }

    @NonNull
    private JsonObject getYouboraJsonObject(boolean isLive, String title) {
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
        mediaEntryJson.addProperty("isLive", isLive);
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

    private void addPhoenixAnalyticsPluginConfig(PKPluginConfigs config) {
        String ks = "djJ8MTk4fHFftqeAPxdlLVzZBk0Et03Vb8on1wLsKp7cbOwzNwfOvpgmOGnEI_KZDhRWTS-76jEY7pDONjKTvbWyIJb5RsP4NL4Ng5xuw6L__BeMfLGAktkVliaGNZq9SXF5n2cMYX-sqsXLSmWXF9XN89io7-k=";
        PhoenixAnalyticsConfig phoenixAnalyticsConfig = new PhoenixAnalyticsConfig(198, "http://api-preprod.ott.kaltura.com/v4_2/api_v3/", ks, 30);
        config.setPluginConfig(PhoenixAnalyticsPlugin.factory.getName(), phoenixAnalyticsConfig);
    }

    private void addIMAPluginConfig(PKPluginConfigs config) {
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/3274935/preroll&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]";
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostpod&cmsid=496&vid=short_onecue&correlator=";

        log.d("Play Ad preSkipAdTagUrl");
        promptMessage(IMA_PLUGIN, "preSkipAdTagUrl");
        IMAConfig adsConfig = getAdsConfig(preMidPostSingleAdTagUrl);
        config.setPluginConfig(IMAPlugin.factory.getName(), adsConfig);
    }

    private IMAConfig getAdsConfig(String adTagUrl) {
        List<String> videoMimeTypes = new ArrayList<>();
        videoMimeTypes.add("video/mp4");
        videoMimeTypes.add("application/x-mpegURL");
        videoMimeTypes.add("application/dash+xml");
        return new IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8);
    }


    private IMAConfig getAdsConfigResponse(String adResponse) {
        List<String> videoMimeTypes = new ArrayList<>();
        videoMimeTypes.add("video/mp4");
        videoMimeTypes.add("application/x-mpegURL");
        // videoMimeTypes.add("application/dash+xml");
        return new IMAConfig().setAdTagResponse(adResponse).setVideoMimeTypes(videoMimeTypes).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8);
    }

    //IMA DAI CONFIG
    private void addIMADAIPluginConfig(PKPluginConfigs config, int daiType) {
        switch (daiType) {
            case 1: {
                promptMessage(DAI_PLUGIN, getDAIConfig1().getAssetTitle());
                IMADAIConfig adsConfig = getDAIConfig1();
                config.setPluginConfig(IMADAIPlugin.factory.getName(), adsConfig);
            }
            break;
            case 2: {
                promptMessage(DAI_PLUGIN, getDAIConfig2().getAssetTitle());
                IMADAIConfig adsConfigLive = getDAIConfig2();
                config.setPluginConfig(IMADAIPlugin.factory.getName(), adsConfigLive);
            }
            break;
            case 3: {
                promptMessage(DAI_PLUGIN, getDAIConfig3().getAssetTitle());
                IMADAIConfig adsConfigDash = getDAIConfig3();
                config.setPluginConfig(IMADAIPlugin.factory.getName(), adsConfigDash);
            }
            break;
            case 4: {
                promptMessage(DAI_PLUGIN, getDAIConfig4().getAssetTitle());
                IMADAIConfig adsConfigVod2 = getDAIConfig4();
                config.setPluginConfig(IMADAIPlugin.factory.getName(), adsConfigVod2);
            }
            break;
            case 5: {
                promptMessage(DAI_PLUGIN, getDAIConfig5().getAssetTitle());
                IMADAIConfig adsConfig5 = getDAIConfig5();
                config.setPluginConfig(IMADAIPlugin.factory.getName(), adsConfig5);
            }
            break;
            case 6: {
                promptMessage(DAI_PLUGIN, getDAIConfig6().getAssetTitle());
                IMADAIConfig adsConfigError = getDAIConfig6();
                config.setPluginConfig(IMADAIPlugin.factory.getName(), adsConfigError);
            }
            break;
            default:
                break;
        }
    }

    private void promptMessage(String type, String title) {
        Toast.makeText(this, type + " " + title, Toast.LENGTH_SHORT).show();
    }

    private IMADAIConfig getDAIConfig6() {
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

    private IMADAIConfig getDAIConfig5() {
        String assetTitle = "VOD - Google I/O";
        String assetKey = null;
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

    private IMADAIConfig getDAIConfig5_1() {
        String assetTitle = "AD5_1";
        String assetKey = null;
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


    @NonNull
    private IMADAIConfig getDAIConfig4() {
        String assetTitle = "AD4";
        String apiKey = null;
        String contentSourceId = "2472176";
        String videoId = "2504847";
        StreamRequest.StreamFormat streamFormat = StreamRequest.StreamFormat.HLS;
        String licenseUrl = null;
        return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                contentSourceId,
                videoId,
                apiKey,
                streamFormat,
                licenseUrl);
    }

    private IMADAIConfig getDAIConfig3() {
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

    private IMADAIConfig getDAIConfig2() {
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

    private IMADAIConfig getDAIConfig1() {
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

    @Override
    protected void onPause() {
        super.onPause();

        if (controlsView != null) {
            controlsView.release();
        }
        if (player != null) {
            player.onApplicationPaused();
        }
    }

    @Override
    public void onDestroy() {
        if (player != null) {
            player.removeListeners(this);
            player.destroy();
            player = null;
        }
        super.onDestroy();
    }

    private void addPlayerListeners(final ProgressBar appProgressBar) {


        player.addListener(this, AdEvent.contentResumeRequested, event -> {
            log.d("CONTENT_RESUME_REQUESTED");
            appProgressBar.setVisibility(View.INVISIBLE);
            controlsView.setSeekBarStateForAd(false);
            controlsView.setPlayerState(PlayerState.READY);
        });

        player.addListener(this, AdEvent.daiSourceSelected, event -> {
            log.d("DAI_SOURCE_SELECTED: " + event.sourceURL);

        });

        player.addListener(this, AdEvent.contentPauseRequested, event -> {
            log.d("AD_CONTENT_PAUSE_REQUESTED");
            appProgressBar.setVisibility(View.VISIBLE);
            controlsView.setSeekBarStateForAd(true);
            controlsView.setPlayerState(PlayerState.READY);
        });

        player.addListener(this, AdEvent.adPlaybackInfoUpdated, event -> {
            log.d("AD_PLAYBACK_INFO_UPDATED");
            log.d("playbackInfoUpdated  = " + event.width + "/" + event.height + "/" + event.bitrate);
        });

        player.addListener(this, AdEvent.cuepointsChanged, event -> {
            adCuePoints = event.cuePoints;

            if (adCuePoints != null) {
                log.d("Has Postroll = " + adCuePoints.hasPostRoll());
            }
        });

        player.addListener(this, AdEvent.adBufferStart, event -> {
            log.d("AD_BUFFER_START pos = " + event.adPosition);
            appProgressBar.setVisibility(View.VISIBLE);
        });

        player.addListener(this, AdEvent.adBufferEnd, event -> {
            log.d("AD_BUFFER_END pos = " + event.adPosition);
            appProgressBar.setVisibility(View.INVISIBLE);
        });

        player.addListener(this, AdEvent.adFirstPlay, event -> {
            log.d("AD_FIRST_PLAY");
            appProgressBar.setVisibility(View.INVISIBLE);
        });

        player.addListener(this, AdEvent.started, event -> {
            log.d("AD_STARTED w/h - " + event.adInfo.getAdWidth() + "/" + event.adInfo.getAdHeight());
            appProgressBar.setVisibility(View.INVISIBLE);
        });

        player.addListener(this, AdEvent.resumed, event -> {
            log.d("Ad Event AD_RESUMED");
            nowPlaying = true;
            appProgressBar.setVisibility(View.INVISIBLE);
        });

        player.addListener(this, AdEvent.playHeadChanged, event -> {
            appProgressBar.setVisibility(View.INVISIBLE);
            //log.d("received AD PLAY_HEAD_CHANGED " + event.adPlayHead);
        });

        player.addListener(this, AdEvent.allAdsCompleted, event -> {
            log.d("Ad Event AD_ALL_ADS_COMPLETED");
            appProgressBar.setVisibility(View.INVISIBLE);
            if (adCuePoints != null && adCuePoints.hasPostRoll()) {
                controlsView.setPlayerState(PlayerState.IDLE);
            }
        });

        player.addListener(this, AdEvent.error, event -> {
            if (event != null && event.error != null) {
                controlsView.setSeekBarStateForAd(false);
                log.e("ERROR: " + event.error.errorType + ", " + event.error.message);
            }
        });

        player.addListener(this, AdEvent.skipped, event -> {
            log.d("Ad Event SKIPPED");
            nowPlaying = true;
        });

        player.addListener(this, PlayerEvent.surfaceAspectRationSizeModeChanged, event -> {
            log.d("resizeMode updated" + event.resizeMode);
        });


        /////// PLAYER EVENTS

        player.addListener(this, PlayerEvent.play, event -> {
            log.d("Player Event PLAY");
            nowPlaying = true;
        });

        player.addListener(this, PlayerEvent.playing, event -> {
            log.d("Player Event PLAYING");
            appProgressBar.setVisibility(View.INVISIBLE);
            nowPlaying = true;
        });

        player.addListener(this, PlayerEvent.pause, event -> {
            log.d("Player Event PAUSE");
            nowPlaying = false;
        });

        player.addListener(this, PlayerEvent.playbackRateChanged, event -> {
            log.d("playbackRateChanged event  rate = " + event.rate);
        });

        player.addListener(this, PlayerEvent.tracksAvailable, event -> {
            //When the track data available, this event occurs. It brings the info object with it.
            tracksInfo = event.tracksInfo;
            populateSpinnersWithTrackInfo(event.tracksInfo);
        });

        player.addListener(this, PlayerEvent.playbackRateChanged, event -> {
            log.d("playbackRateChanged event  rate = " + event.rate);
        });

        player.addListener(this, PlayerEvent.error, event -> {
            //When the track data available, this event occurs. It brings the info object with it.
            if (event != null && event.error != null) {
                log.d("PlayerEvent.Error event  position = " + event.error.errorType + " errorMessage = " + event.error.message);
            }
        });

        player.addListener(this, PlayerEvent.ended, event -> {
            appProgressBar.setVisibility(View.INVISIBLE);
        });

        player.addListener(this, PlayerEvent.playheadUpdated, event -> {
            //When the track data available, this event occurs. It brings the info object with it.
            //log.d("playheadUpdated event  position = " + event.position + " duration = " + event.duration);
        });

        player.addListener(this, PlayerEvent.videoFramesDropped, event -> {
            //log.d("VIDEO_FRAMES_DROPPED " + event.droppedVideoFrames);
        });

        player.addListener(this, PlayerEvent.bytesLoaded, event -> {
            //log.d("BYTES_LOADED " + event.bytesLoaded);
        });

        player.addListener(this, PlayerEvent.stateChanged, new PKEvent.Listener<PlayerEvent.StateChanged>() {
            @Override
            public void onEvent(PlayerEvent.StateChanged event) {
                log.d("State changed from " + event.oldState + " to " + event.newState);
                if (event.newState == PlayerState.BUFFERING) {
                    appProgressBar.setVisibility(View.VISIBLE);
                }
                if ((event.oldState == PlayerState.LOADING || event.oldState == PlayerState.BUFFERING) && event.newState == PlayerState.READY) {
                    appProgressBar.setVisibility(View.INVISIBLE);

                }
                if(controlsView != null){
                    controlsView.setPlayerState(event.newState);
                }
            }
        });

        /////Phoenix events

        player.addListener(this, PhoenixAnalyticsEvent.bookmarkError, event -> {
            log.d("bookmarkErrorEvent errorCode = " + event.errorCode + " message = " + event.errorMessage);
        });

        player.addListener(this, PhoenixAnalyticsEvent.concurrencyError, event -> {
            log.d("ConcurrencyErrorEvent errorCode = " + event.errorCode + " message = " + event.errorMessage);
        });

        player.addListener(this, PhoenixAnalyticsEvent.error, event -> {
            log.d("Phoenox Analytics errorEvent errorCode = " + event.errorCode + " message = " + event.errorMessage);
        });

        player.addListener(this, PhoenixAnalyticsEvent.error, event -> {
            log.d("Phoenox Analytics errorEvent errorCode = " + event.errorCode + " message = " + event.errorMessage);
        });

        player.addListener(this, OttEvent.ottEvent, event -> {
            log.d("Concurrency event = " + event.type);
        });
    }

    @Override
    protected void onResume() {
        log.d("Application onResume");
        super.onResume();
        if (player != null) {
            player.onApplicationResumed();
        }
        if (controlsView != null) {
            controlsView.resume();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        setFullScreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE);
        super.onConfigurationChanged(newConfig);
        Log.v("orientation", "state = "+newConfig.orientation);
    }


    private void setFullScreen(boolean isFullScreen) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)playerContainer.getLayoutParams();
        // Checks the orientation of the screen
        this.isFullScreen = isFullScreen;
        if (isFullScreen) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            fullScreenBtn.setImageResource(R.drawable.ic_no_fullscreen);
            spinerContainer.setVisibility(View.GONE);
            params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT;

        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            fullScreenBtn.setImageResource(R.drawable.ic_fullscreen);
            spinerContainer.setVisibility(View.VISIBLE);
            params.height = (int)getResources().getDimension(R.dimen.player_height);
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        }
        playerContainer.requestLayout();
    }

    /**
     * populating spinners with track info.
     *
     * @param tracksInfo - the track info.
     */
    private void populateSpinnersWithTrackInfo(PKTracks tracksInfo) {

        //Retrieve info that describes available tracks.(video/audio/subtitle).
        TrackItem[] videoTrackItems = obtainRelevantTrackInfo(Consts.TRACK_TYPE_VIDEO, tracksInfo.getVideoTracks());
        //populate spinner with this info.

        applyAdapterOnSpinner(videoSpinner, videoTrackItems, tracksInfo.getDefaultVideoTrackIndex());

        TrackItem[] audioTrackItems = obtainRelevantTrackInfo(Consts.TRACK_TYPE_AUDIO, tracksInfo.getAudioTracks());
        applyAdapterOnSpinner(audioSpinner, audioTrackItems, tracksInfo.getDefaultAudioTrackIndex());

        TrackItem[] subtitlesTrackItems = obtainRelevantTrackInfo(Consts.TRACK_TYPE_TEXT, tracksInfo.getTextTracks());
        applyAdapterOnSpinner(textSpinner, subtitlesTrackItems, tracksInfo.getDefaultTextTrackIndex());
    }

    /**
     * Obtain info that user is interested in.
     * For example if user want to display in UI bitrate of the available tracks,
     * he can do it, by obtaining the tackType of video, and getting the getBitrate() from videoTrackInfo.
     *
     * @param trackType  - tyoe of the track you are interested in.
     * @param trackInfos - all availables tracks.
     * @return
     */
    private TrackItem[] obtainRelevantTrackInfo(int trackType, List<? extends BaseTrack> trackInfos) {
        TrackItem[] trackItems = new TrackItem[trackInfos.size()];
        switch (trackType) {
            case Consts.TRACK_TYPE_VIDEO:
                TextView tvVideo = (TextView) this.findViewById(R.id.tvVideo);
                changeSpinnerVisibility(videoSpinner, tvVideo, trackInfos);

                for (int i = 0; i < trackInfos.size(); i++) {
                    VideoTrack videoTrackInfo = (VideoTrack) trackInfos.get(i);
                    if(videoTrackInfo.isAdaptive()){
                        trackItems[i] = new TrackItem("Auto", videoTrackInfo.getUniqueId());
                    }else{
                        trackItems[i] = new TrackItem(String.valueOf(videoTrackInfo.getBitrate()), videoTrackInfo.getUniqueId());
                    }
                }

                break;
            case Consts.TRACK_TYPE_AUDIO:
                TextView tvAudio = (TextView) this.findViewById(R.id.tvAudio);
                changeSpinnerVisibility(audioSpinner, tvAudio, trackInfos);
                //Map<Integer, AtomicInteger> channelMap = new HashMap<>();
                SparseArray<AtomicInteger> channelSparseIntArray = new SparseArray<>();

                for (int i = 0; i < trackInfos.size(); i++) {
                    if (channelSparseIntArray.get(((AudioTrack) trackInfos.get(i)).getChannelCount()) != null) {
                        channelSparseIntArray.get(((AudioTrack) trackInfos.get(i)).getChannelCount()).incrementAndGet();
                    } else {
                        channelSparseIntArray.put(((AudioTrack) trackInfos.get(i)).getChannelCount(), new AtomicInteger(1));
                    }
                }
                boolean addChannel = false;
                if (channelSparseIntArray.size() > 0 && !(new AtomicInteger(trackInfos.size()).toString().equals(channelSparseIntArray.get(((AudioTrack) trackInfos.get(0)).getChannelCount()).toString()))) {
                    addChannel = true;
                }
                for (int i = 0; i < trackInfos.size(); i++) {
                    AudioTrack audioTrackInfo = (AudioTrack) trackInfos.get(i);
                    String label = audioTrackInfo.getLabel() != null ? audioTrackInfo.getLabel() : audioTrackInfo.getLanguage();
                    String bitrate = (audioTrackInfo.getBitrate() >  0) ? "" + audioTrackInfo.getBitrate() : "";
                    if (TextUtils.isEmpty(bitrate) && addChannel) {
                        bitrate = buildAudioChannelString(audioTrackInfo.getChannelCount());
                    }
                    if (audioTrackInfo.isAdaptive()) {
                        if (!TextUtils.isEmpty(bitrate)) {
                            bitrate += " Adaptive";
                        } else {
                            bitrate = "Adaptive";
                        }
                        if (label == null) {
                            label = "";
                        }
                    }
                    trackItems[i] = new TrackItem(label + " " + bitrate, audioTrackInfo.getUniqueId());
                }
                break;
            case Consts.TRACK_TYPE_TEXT:
                TextView tvSubtitle = (TextView) this.findViewById(R.id.tvText);
                changeSpinnerVisibility(textSpinner, tvSubtitle, trackInfos);

                for (int i = 0; i < trackInfos.size(); i++) {

                    TextTrack textTrackInfo = (TextTrack) trackInfos.get(i);
                    String lang = (textTrackInfo.getLabel() != null) ? textTrackInfo.getLabel() : "unknown";
                    trackItems[i] = new TrackItem(lang, textTrackInfo.getUniqueId());
                }
                break;
        }
        return trackItems;
    }

    private void changeSpinnerVisibility(Spinner spinner, TextView textView, List<? extends BaseTrack> trackInfos) {
        //hide spinner if no data available.
        if (trackInfos.isEmpty()) {
            textView.setVisibility(View.GONE);
            spinner.setVisibility(View.GONE);
        } else {
            textView.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.VISIBLE);
        }
    }

    private void applyAdapterOnSpinner(Spinner spinner, TrackItem[] trackInfo, int defaultSelectedIndex) {
        TrackItemAdapter trackItemAdapter = new TrackItemAdapter(this, R.layout.track_items_list_row, trackInfo);
        spinner.setAdapter(trackItemAdapter);
        if (defaultSelectedIndex > 0) {
            spinner.setSelection(defaultSelectedIndex);
        }
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        userIsInteracting = true;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (!userIsInteracting) {
            return;
        }
        TrackItem trackItem = (TrackItem) parent.getItemAtPosition(position);
        //tell to the player, to switch track based on the user selection.

        player.changeTrack(trackItem.getUniqueId());

        //String selectedIndex = getQualityIndex(BitRateRange.QualityType.Auto, currentTracks.getVideoTracks());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onOrientationChange(OrientationManager.ScreenOrientation screenOrientation) {
        switch(screenOrientation){
            case PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case REVERSED_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            case LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case REVERSED_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
            default:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
        }
    }

    protected String getQualityIndex(BitRateRange.QualityType videoQuality, List<VideoTrack> videoTrackInfo) {
        String uniqueTrackId = null;
        long bitRateValue = 0;
        BitRateRange bitRateRange = null;

        switch (videoQuality) {
            case Low:
                bitRateRange = BitRateRange.getLowQuality();
                List<VideoTrack> lowBitrateMatchedTracks = getVideoTracksInRange(videoTrackInfo, bitRateRange);
                Collections.sort(lowBitrateMatchedTracks, bitratesComperator());

                for (VideoTrack track : lowBitrateMatchedTracks) {
                    bitRateValue = track.getBitrate();
                    if (isBitrateInRange(bitRateValue, bitRateRange.getLow(), bitRateRange.getHigh())) {
                        uniqueTrackId = track.getUniqueId();
                        break;
                    }
                }
                break;
            case Mediun:
                bitRateRange = BitRateRange.getMedQuality();
                List<VideoTrack> medBitratesMatchedTracks = getVideoTracksInRange(videoTrackInfo, bitRateRange);
                Collections.sort(medBitratesMatchedTracks, bitratesComperator());

                for (VideoTrack track : medBitratesMatchedTracks) {
                    bitRateValue = track.getBitrate();
                    if (isBitrateInRange(bitRateValue, bitRateRange.getLow(), bitRateRange.getHigh())) {
                        uniqueTrackId = track.getUniqueId();
                        break;
                    }
                }
                break;
            case High:
                bitRateRange = BitRateRange.getHighQuality();
                Collections.sort(videoTrackInfo, bitratesComperator());
                for (BaseTrack entry : videoTrackInfo) {
                    bitRateValue = ((VideoTrack) entry).getBitrate();
                    if (bitRateValue >= bitRateRange.getLow()) {
                        uniqueTrackId = entry.getUniqueId();
                        break;
                    }
                }
                break;
            case Auto:
            default:
                for (VideoTrack track : videoTrackInfo) {
                    if (track.isAdaptive()) {
                        uniqueTrackId = track.getUniqueId();
                        break;
                    }
                }
                break;
        }

        //null protection
        if (uniqueTrackId == null && tracksInfo != null) {
            tracksInfo.getDefaultVideoTrackIndex();
        }
        return uniqueTrackId;
    }

    private static List<VideoTrack> getVideoTracksInRange(List<VideoTrack> videoTracks, BitRateRange bitRateRange) {
        List<VideoTrack> videoTrackInfo = new ArrayList<>() ;
        long bitRate;
        for (VideoTrack track : videoTracks) {
            bitRate = track.getBitrate();
            if (bitRate >= bitRateRange.getLow() && bitRate <= bitRateRange.getHigh()) {
                videoTrackInfo.add(track);
            }
        }
        return videoTrackInfo;
    }

    private boolean isBitrateInRange(long bitRate, long low, long high) {
        return low <= bitRate && bitRate <= high;
    }

    @NonNull
    private Comparator<VideoTrack> bitratesComperator() {
        return new Comparator<VideoTrack>() {
            @Override
            public int compare(VideoTrack track1, VideoTrack track2) {
                return Long.valueOf(track1.getBitrate()).compareTo(track2.getBitrate());
            }
        };
    }

    private String buildAudioChannelString(int channelCount) {
        switch (channelCount) {
            case 1:
                return "Mono";
            case 2:
                return "Stereo";
            case 6:
            case 7:
                return "Surround_5.1";
            case 8:
                return "Surround_7.1";
            default:
                return "Surround";
        }
    }

    //Example for Custom Licens Adapter
    static class DRMAdapter implements PKRequestParams.Adapter {

        public static String customData;
        @Override
        public PKRequestParams adapt(PKRequestParams requestParams) {
            requestParams.headers.put("customData", customData);
            return requestParams;
        }

        @Override
        public void updateParams(Player player) {
            // TODO?
        }

        @Override
        public String getApplicationName() {
            return null;
        }
    }

    public void loadPlaykitPlayer(Integer partnerId, String serverUrl, PLAYER_TYPE player_type, PKPluginConfigs pkPluginConfigs) {

        playerInitOptions = new PlayerInitOptions(partnerId);
        playerInitOptions.setServerUrl(serverUrl);
        playerInitOptions.setAutoPlay(true);
        playerInitOptions.setSecureSurface(false);
        playerInitOptions.setAdAutoPlayOnResume(true);
        playerInitOptions.setAllowCrossProtocolEnabled(true);
       // playerInitOptions.setLoadControlBuffers(new LoadControlBuffers());

        playerInitOptions.setPluginConfigs(pkPluginConfigs);

        switch (player_type) {
            case OTT:
                player = KalturaPlayer.createOTTPlayer(MainActivity.this, playerInitOptions);
                break;

            case OVP:
                player = KalturaPlayer.createOVPPlayer(MainActivity.this, playerInitOptions);
                break;

            case BASIC:
                player = KalturaPlayer.createBasicPlayer(MainActivity.this, playerInitOptions);
                break;

            default:
                log.d("There is no player to create.");
                break;
        }

        player.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        ViewGroup container = findViewById(R.id.player_view);
        container.addView(player.getPlayerView());
    }

    private void buildOttMediaOptions(String assetId, String ks) {

        if(!isPlayerLoaded()) {
            return;
        }

        OTTMediaOptions ottMediaOptions = new OTTMediaOptions();
        ottMediaOptions.assetId = assetId;
        ottMediaOptions.assetType = APIDefines.KalturaAssetType.Media;
        ottMediaOptions.contextType = APIDefines.PlaybackContextType.Playback;
        ottMediaOptions.assetReferenceType = APIDefines.AssetReferenceType.Media;
        ottMediaOptions.protocol = PhoenixMediaProvider.HttpProtocol.Http;
        ottMediaOptions.ks = ks;
        ottMediaOptions.startPosition = START_POSITION;
        ottMediaOptions.formats = new String []{"Mobile_Main"};

        player.loadMedia(ottMediaOptions, (entry, error) -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.getId());
            }
        });
    }

    private void buildOvpMediaOptions(String entryId, String ks) {

        if(!isPlayerLoaded()) {
            return;
        }

        OVPMediaOptions ovpMediaOptions = new OVPMediaOptions();
        ovpMediaOptions.entryId = entryId;
        ovpMediaOptions.ks = ks;
        ovpMediaOptions.startPosition = START_POSITION;

        player.loadMedia(ovpMediaOptions, (entry, error) -> {
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG).show();
            } else {
                log.d("OVPMedia onEntryLoadComplete  entry = " + entry.getId());
            }
        });

    }

    private void buildBasicMediaOptions(PKMediaEntry pkMediaEntry){

        if(!isPlayerLoaded()) {
            return;
        }

        if (pkMediaEntry != null) {
            player.setMedia(pkMediaEntry, START_POSITION);
        } else {
            log.d("PKMediaEntry is null");
        }
    }

    private enum PLAYER_TYPE {
        OVP(1),
        OTT(2),
        BASIC(3);

        private final int value;

        PLAYER_TYPE(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private boolean isPlayerLoaded() {
        if (player == null) {
            log.d("Kaltura Player is null");
            return false;
        }

        return true;
    }

}
