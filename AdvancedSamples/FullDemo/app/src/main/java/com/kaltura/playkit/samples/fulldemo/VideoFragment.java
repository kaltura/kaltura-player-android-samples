package com.kaltura.playkit.samples.fulldemo;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kaltura.android.exoplayer2.util.MimeTypes;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.kaltura.playkit.PKDrmParams;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaFormat;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.PKPluginConfigs;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.PlayerState;
import com.kaltura.playkit.ads.AdController;
import com.kaltura.playkit.ads.AdEnabledPlayerController;
import com.kaltura.playkit.plugins.ads.AdEvent;
import com.kaltura.playkit.plugins.ima.IMAConfig;
import com.kaltura.playkit.plugins.ima.IMAPlugin;
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsConfig;
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsPlugin;
import com.kaltura.playkit.plugins.youbora.YouboraPlugin;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.PlayerInitOptions;

import java.util.ArrayList;
import java.util.List;

import static com.kaltura.playkit.samples.fulldemo.Consts.AD_LOAD_TIMEOUT;
import static com.kaltura.playkit.samples.fulldemo.Consts.AUTO_PLAY;
import static com.kaltura.playkit.samples.fulldemo.Consts.COMPANION_AD_HEIGHT;
import static com.kaltura.playkit.samples.fulldemo.Consts.COMPANION_AD_WIDTH;
import static com.kaltura.playkit.samples.fulldemo.Consts.HLS_URL;
import static com.kaltura.playkit.samples.fulldemo.Consts.HLS_URL2;
import static com.kaltura.playkit.samples.fulldemo.Consts.MIME_TYPE;
import static com.kaltura.playkit.samples.fulldemo.Consts.MIN_AD_DURATION_FOR_SKIP_BUTTON;
import static com.kaltura.playkit.samples.fulldemo.Consts.PREFERRED_BITRATE;
import static com.kaltura.playkit.samples.fulldemo.Consts.START_FROM;

//import com.kaltura.plugins.adsmanager.AdsConfig;
//import com.kaltura.plugins.adsmanager.AdsPlugin;

public class VideoFragment extends Fragment {
    private static final String TAG = VideoFragment.class.getSimpleName();

    //id of the first entry
    private static final String FIRST_ENTRY_ID = "entry_id_1";
    //id of the second entry
    private static final String SECOND_ENTRY_ID = "entry_id_2";
    //id of the first media source.
    private static final String FIRST_MEDIA_SOURCE_ID = "source_id_1";
    //id of the second media source.
    private static final String SECOND_MEDIA_SOURCE_ID = "source_id_2";


    //Youbora analytics Constants
    public static final String ACCOUNT_CODE = "kalturatest";
    public static final String USER_NAME = "a@a.com";
    public static final String MEDIA_TITLE = "your_media_title";
    public static final boolean IS_LIVE = false;
    public static final boolean ENABLE_SMART_ADS = true;
    private static final String CAMPAIGN = "your_campaign_name";
    public static final String EXTRA_PARAM_1 = "playKitPlayer";
    public static final String EXTRA_PARAM_2 = "XXX";
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

    private VideoItem mVideoItem;
    private TextView mVideoTitle;
    private FrameLayout playerLayout;
    private RelativeLayout adSkin;
    private KalturaPlayer player;
    private PlaybackControlsView controlsView;
    private boolean nowPlaying;
    private ProgressBar progressBar;
    private boolean isFullScreen;
    private AppCompatImageView fullScreenBtn;
   // private PKMediaConfig mediaConfig;
    private Logger mLog;
    private OnVideoFragmentViewCreatedListener mViewCreatedCallback;

    private boolean isAutoPlay;
    private Long startPosition;
    private int adLoadTimeOut;
    private String videoMimeType;
    private int videoBitrate;
    private int companionAdWidth;
    private int companionAdHeight;
    private int minAdDurationForSkipButton;
    private boolean firstLaunch = true;

    private View rootView;

    /**
     * Listener called when the fragment's onCreateView is fired.
     */
    public interface OnVideoFragmentViewCreatedListener {
        void onVideoFragmentViewCreated();
    }

    @Override
    public void onAttach(Activity activity) {
        firstLaunch = true;
        try {
            mViewCreatedCallback = (OnVideoFragmentViewCreatedListener) activity;

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement " + OnVideoFragmentViewCreatedListener.class.getName());
        }

        super.onAttach(activity);
    }

    @Override
    public void onActivityCreated(Bundle bundle) {
        super.onActivityCreated(bundle);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        isAutoPlay    = getArguments().getBoolean(AUTO_PLAY);
        startPosition = getArguments().getLong(START_FROM);
        adLoadTimeOut = getArguments().getInt(AD_LOAD_TIMEOUT);
        videoMimeType = getArguments().getString(MIME_TYPE);
        videoBitrate  = getArguments().getInt(PREFERRED_BITRATE);
        companionAdWidth  = getArguments().getInt(COMPANION_AD_WIDTH);
        companionAdHeight = getArguments().getInt(COMPANION_AD_HEIGHT);
        minAdDurationForSkipButton = getArguments().getInt(MIN_AD_DURATION_FOR_SKIP_BUTTON);

        rootView = inflater.inflate(R.layout.fragment_video, container, false);
        initUi(rootView);
        if (mViewCreatedCallback != null) {
            mViewCreatedCallback.onVideoFragmentViewCreated();
        }

        return rootView;
    }

    private void changeMedia() {
        if (player == null) {
            return;
        }

        clearLog();

        //Check if id of the media entry that is set in mediaConfig.
        String AD_HONDA2 = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x360&iu=/6062/iab_vast_samples/skippable&ciu_szs=300x250,728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&correlator=" + System.currentTimeMillis();//"http://pubads.g.doubleclick.net/gampad/ads?sz=400x300&iu=%2F6062%2Fhanna_MA_group%2Fvideo_comp_app&ciu_szs=&impl=s&gdfp_req=1&env=vp&output=xml_vast3&unviewed_position_start=1&m_ast=vast&url=";
        List<String> videoMimeTypes = new ArrayList<>();
        videoMimeTypes.add("video/mp4");
        videoMimeTypes.add("application/x-mpegURL");
        if (player.getMediaEntry().getId().equals(FIRST_ENTRY_ID)) {
            String AD_HOND = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=";//"http://externaltests.dev.kaltura.com/player/Vast_xml/alexs.qacore-vast3-rol_02.xml";

            IMAConfig adsConfig = new IMAConfig().setAdTagUrl(AD_HOND).enableDebugMode(true).setVideoMimeTypes(videoMimeTypes);
            //"http://pubads.g.doubleclick.net/gampad/ads?sz=400x300&iu=%2F6062%2Fhanna_MA_group%2Fvideo_comp_app&ciu_szs=&impl=s&gdfp_req=1&env=vp&output=xml_vast3&unviewed_position_start=1&m_ast=vast&url=";
//            AdsConfig adsConfig = new AdsConfig().
//                    setAdTagURL(AD_HOND).
//                    setPlayerViewContainer(playerLayout).
//                    setAdSkinContainer(adSkin).
//                    setAdLoadTimeOut(adLoadTimeOut).
//                    setVideoMimeTypes(PKMediaFormat.valueOf(videoMimeType)).
//                    setVideoBitrate(videoBitrate).
//                    setMinAdDurationForSkipButton(minAdDurationForSkipButton).
//                    setCompanionAdWidth(companionAdWidth).
//                    setCompanionAdHeight(companionAdHeight);

            String referrer = "app://NonDefaultReferrer1/"  + getActivity().getPackageCodePath();
            //player.updatePluginConfig(AdsPlugin.factory.getName(), adsConfig);
            player.updatePluginConfig(PhoenixAnalyticsPlugin.factory.getName(), getPhoenixAnalyticsConfig());
            player.updatePluginConfig(IMAPlugin.factory.getName(), adsConfig);
            player.updatePluginConfig(YouboraPlugin.factory.getName(), getConverterYoubora(MEDIA_TITLE + "_changeMedia1", false).toJson());
            //If first one is active, prepare second one.
            prepareFirstEntry();
        } else {
            IMAConfig adsConfig = new IMAConfig().setAdTagUrl(AD_HONDA2).enableDebugMode(true).setVideoMimeTypes(videoMimeTypes);

//            AdsConfig adsConfig = new AdsConfig().setAdTagURL(AD_HONDA2).
//                    setPlayerViewContainer(playerLayout).
//                    setAdSkinContainer(adSkin).
//                    setAdLoadTimeOut(adLoadTimeOut).
//                    setVideoMimeTypes(PKMediaFormat.valueOf(videoMimeType)).
//                    setVideoBitrate(videoBitrate).
//                    setMinAdDurationForSkipButton(minAdDurationForSkipButton).
//                    setCompanionAdWidth(companionAdWidth).
//                    setCompanionAdHeight(companionAdHeight);

            String referrer = "app://NonDefaultReferrer2/"  + getActivity().getPackageName();
            player.updatePluginConfig(PhoenixAnalyticsPlugin.factory.getName(), getPhoenixAnalyticsConfig());
            //player.updatePluginConfig(AdsPlugin.factory.getName(), adsConfig);
            player.updatePluginConfig(IMAPlugin.factory.getName(), adsConfig);
            player.updatePluginConfig(YouboraPlugin.factory.getName(), getConverterYoubora(MEDIA_TITLE + "_changeMedia2", false).toJson());

            //If the second one is active, prepare the first one.
            prepareSecondEntry();
        }
    }

    /**
     * Prepare the first entry.
     */
    private void prepareFirstEntry() {
        //First. Create PKMediaEntry object.
        PKMediaEntry mediaEntry = createFirstMediaEntry();

        //Prepare player with media configuration.
        player.setMedia(mediaEntry, 0L);
    }

    /**
     * Prepare the second entry.
     */
    private void prepareSecondEntry() {
        //Second. Create PKMediaEntry object.
        PKMediaEntry mediaEntry = createSecondMediaEntry();

        //Prepare player with media configuration.
        player.setMedia(mediaEntry, 0L);
    }

    private PKMediaEntry createFirstMediaEntry() {
        //Create media entry.
        PKMediaEntry mediaEntry = new PKMediaEntry();

        //Set id for the entry.
        mediaEntry.setId(FIRST_ENTRY_ID);
        mediaEntry.setDuration(300 * 1000);
        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
        mediaEntry.setMediaType(PKMediaEntry.MediaEntryType.Vod);

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        List<PKMediaSource> mediaSources = createFirstMediaSources();

        //Set media sources to the entry.
        mediaEntry.setSources(mediaSources);

        return mediaEntry;
    }

    /**
     * Create {@link PKMediaEntry} with minimum necessary data.
     *
     * @return - the {@link PKMediaEntry} object.
     */
    private PKMediaEntry createSecondMediaEntry() {
        //Create media entry.
        PKMediaEntry mediaEntry = new PKMediaEntry();

        //Set id for the entry.
        mediaEntry.setId(SECOND_ENTRY_ID);
        mediaEntry.setDuration(450 * 1000);
        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
        mediaEntry.setMediaType(PKMediaEntry.MediaEntryType.Vod);

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        List<PKMediaSource> mediaSources = createSecondMediaSources();

        //Set media sources to the entry.
        mediaEntry.setSources(mediaSources);

        return mediaEntry;
    }

    private List<PKMediaSource> createFirstMediaSources() {
        //Init list which will hold the PKMediaSources.
        List<PKMediaSource> mediaSources = new ArrayList<>();

        //Create new PKMediaSource instance.
        PKMediaSource mediaSource = new PKMediaSource();

        //Set the id.
        mediaSource.setId(FIRST_MEDIA_SOURCE_ID);

        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.setUrl(HLS_URL2);

        //Set the format of the source. In our case it will be hls.
        mediaSource.setMediaFormat(PKMediaFormat.valueOfUrl(HLS_URL2));

        //Add media source to the list.
        mediaSources.add(mediaSource);

        return mediaSources;
    }

    /**
     * Create list of {@link PKMediaSource}.
     *
     * @return - the list of sources.
     */
    private List<PKMediaSource> createSecondMediaSources() {
        //Init list which will hold the PKMediaSources.
        List<PKMediaSource> mediaSources = new ArrayList<>();

        //Create new PKMediaSource instance.
        PKMediaSource mediaSource = new PKMediaSource();

        //Set the id.
        mediaSource.setId(SECOND_MEDIA_SOURCE_ID);

        mediaSource.setUrl(HLS_URL);

        mediaSource.setMediaFormat(PKMediaFormat.valueOfUrl(SECOND_MEDIA_SOURCE_ID));

        //Add media source to the list.
        mediaSources.add(mediaSource);

        return mediaSources;
    }

   public void loadVideo(VideoItem videoItem) {
       mVideoItem = videoItem;
   }

    // KalturaPlayer

    public void loadPlaykitPlayer() {
        clearLog();

        PlayerInitOptions playerInitOptions = new PlayerInitOptions();

        // IMA Configuration
        PKPluginConfigs pkPluginConfigs = new PKPluginConfigs();
        IMAConfig adsConfig = getAdsConfig(mVideoItem.getAdTagUrl());
        pkPluginConfigs.setPluginConfig(IMAPlugin.factory.getName(), adsConfig);

        playerInitOptions.setPluginConfigs(pkPluginConfigs);


        player = KalturaPlayer.createBasicPlayer(getActivity(), playerInitOptions);
        player.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT);

        ViewGroup container = rootView.findViewById(R.id.player_root);
        container.addView(player.getPlayerView());
    }


//    private void addAdPluginConfig(PKPluginConfigs config, FrameLayout layout, RelativeLayout adSkin) {
//
//        String adtag = mVideoItem.getAdTagUrl();
//        if (adtag == null) {
//            return;
//        }
//        if (adtag.endsWith("correlator=")) {
//            adtag += System.currentTimeMillis() + 100000;
//        }
//
//        videoMimeType = (isPKMediaFormatContains(videoMimeType)) ? videoMimeType : "mp4";
//        AdsConfig adsConfig = new AdsConfig().
//                setPlayerViewContainer(layout).
//                setAdSkinContainer(adSkin).
//                setAdLoadTimeOut(adLoadTimeOut).
//                setVideoMimeTypes(PKMediaFormat.valueOf(videoMimeType)).
//                setVideoBitrate(videoBitrate).
//                setMinAdDurationForSkipButton(minAdDurationForSkipButton).
//                setCompanionAdWidth(companionAdWidth).
//                setCompanionAdHeight(companionAdHeight);
//        if (adtag.startsWith("http")) {
//            adsConfig.setAdTagURL(adtag);
//        } else {
//            adsConfig.setAdTagXML(adtag);
//        }
//        config.setPluginConfig(AdsPlugin.factory.getName(), adsConfig);
//    }

    private void addPhoenixAnalyticsPluginConfig(PKPluginConfigs config) {
        PhoenixAnalyticsConfig phoenixAnalyticsConfig = getPhoenixAnalyticsConfig();
        config.setPluginConfig(PhoenixAnalyticsPlugin.factory.getName(),phoenixAnalyticsConfig);
    }

    @NonNull
    private PhoenixAnalyticsConfig getPhoenixAnalyticsConfig() {
        String ks = "djJ8MTk4fHFftqeAPxdlLVzZBk0Et03Vb8on1wLsKp7cbOwzNwfOvpgmOGnEI_KZDhRWTS-76jEY7pDONjKTvbWyIJb5RsP4NL4Ng5xuw6L__BeMfLGAktkVliaGNZq9SXF5n2cMYX-sqsXLSmWXF9XN89io7-k=";
        return new PhoenixAnalyticsConfig(198, "https://rest-as.ott.kaltura.com/v4_4/api_v3/", ks, 30);
    }

    private IMAConfig getAdsConfig(String adTagUrl) {

        //List<String> videoMimeTypes = new ArrayList<>();
        //videoMimeTypes.add(MimeTypes.APPLICATION_MP4);
        //videoMimeTypes.add(MimeTypes.APPLICATION_M3U8);
        //Map<Double, String> tagTimesMap = new HashMap<>();
        //tagTimesMap.put(2.0,"ADTAG");
        List<String> videoMimeTypes = new ArrayList<>();
        videoMimeTypes.add("video/mp4");
        videoMimeTypes.add(MimeTypes.APPLICATION_M3U8);
        return new IMAConfig().setAdTagUrl(adTagUrl).enableDebugMode(true).setVideoMimeTypes(videoMimeTypes);
    }

    private void addYouboraPlugin(PKPluginConfigs pluginConfigs) {
        ConverterYoubora converterYoubora = getConverterYoubora(MEDIA_TITLE, false);

        pluginConfigs.setPluginConfig(YouboraPlugin.factory.getName(), converterYoubora.toJson());
    }

    private ConverterYoubora getConverterYoubora(String mediaTitle, boolean isLive) {
        JsonPrimitive accountCode = new JsonPrimitive(ACCOUNT_CODE);
        JsonPrimitive username = new JsonPrimitive(USER_NAME);
        JsonPrimitive haltOnError = new JsonPrimitive(true);
        JsonPrimitive enableAnalytics = new JsonPrimitive(true);
        JsonPrimitive enableSmartAds = new JsonPrimitive(ENABLE_SMART_ADS);

        JsonObject mediaEntry = new JsonObject();
        mediaEntry.addProperty("isLive", isLive);
        mediaEntry.addProperty("title", mediaTitle);

        JsonObject adsEntry = new JsonObject();
        adsEntry.addProperty("campaign", CAMPAIGN);

        JsonObject extraParamEntry = new JsonObject();
        extraParamEntry.addProperty("param1", "mobile");
        extraParamEntry.addProperty("param2", EXTRA_PARAM_2);
        extraParamEntry.addProperty("param3", "CCC");

        JsonObject propertiesEntry = new JsonObject();
        propertiesEntry.addProperty("genre", GENRE);
        propertiesEntry.addProperty("type", TYPE);
        propertiesEntry.addProperty("transaction_type", TRANSACTION_TYPE);
        propertiesEntry.addProperty("year", YEAR);
        propertiesEntry.addProperty("cast", CAST);
        propertiesEntry.addProperty("director", DIRECTOR);
        propertiesEntry.addProperty("owner", OWNER);
        propertiesEntry.addProperty("parental", PARENTAL);
        propertiesEntry.addProperty("price", PRICE);
        propertiesEntry.addProperty("rating", RATING);
        propertiesEntry.addProperty("audioType", AUDIO_TYPE);
        propertiesEntry.addProperty("audioChannels", AUDIO_CHANNELS);
        propertiesEntry.addProperty("device", DEVICE);
        propertiesEntry.addProperty("quality", QUALITY);


        ConverterYoubora converterYoubora = new ConverterYoubora(accountCode, username, haltOnError, enableAnalytics, enableSmartAds,
                mediaEntry,
                adsEntry, extraParamEntry, propertiesEntry);
        return  converterYoubora;
    }
    public static boolean isPKMediaFormatContains(String playbackFormat) {
        for (PKMediaFormat format : PKMediaFormat.values()) {
            if (format.name().equals(playbackFormat)) {
                return true;
            }
        }
        return false;
    }

    private void prepareMediaEntry() {
        PKMediaEntry pkMediaEntry = createMediaEntry();
        player.setMedia(pkMediaEntry);
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
        mediaEntry.setId("1_w9zx2eti");
        mediaEntry.setDuration(883 * 1000);
        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
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
        //Init list which will hold the PKMediaSources.
        List<PKMediaSource> mediaSources = new ArrayList<>();

        //Create new PKMediaSource instance.
        PKMediaSource mediaSource = new PKMediaSource();

        //Set the id.
        mediaSource.setId("11111");

        if (!TextUtils.isEmpty(mVideoItem.getVideoLic())) {
            List<PKDrmParams> pkDrmDataList = new ArrayList<>();
            PKDrmParams pkDrmParams = null;
            if (mVideoItem.getVideoUrl().endsWith("mpd")) {
                pkDrmParams = new PKDrmParams(mVideoItem.getVideoLic(), PKDrmParams.Scheme.WidevineCENC);
            } else {
                pkDrmParams = new PKDrmParams(mVideoItem.getVideoLic(), PKDrmParams.Scheme.WidevineClassic);
            }
            pkDrmDataList.add(pkDrmParams);
            mediaSource.setDrmData(pkDrmDataList);

        }
        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.setUrl(mVideoItem.getVideoUrl());
        mediaSource.setMediaFormat(PKMediaFormat.valueOfUrl(mVideoItem.getVideoUrl()));


        //Add media source to the list.
        mediaSources.add(mediaSource);

        return mediaSources;
    }

    private void initUi(View rootView) {

        Button changeMediaButton = rootView.findViewById(R.id.changeMedia);
        //Set click listener.
        changeMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Change media.
                changeMedia();
            }
        });
        mVideoTitle = rootView.findViewById(R.id.video_title);
        playerLayout = rootView.findViewById(R.id.player_root);
        progressBar = rootView.findViewById(R.id.progressBarSpinner);
        controlsView = rootView.findViewById(R.id.playerControls);
        progressBar.setVisibility(View.INVISIBLE);
        adSkin = rootView.findViewById(R.id.ad_skin);
        fullScreenBtn = rootView.findViewById(R.id.full_screen_switcher);
        fullScreenBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int orient;
                if (isFullScreen) {
                    orient = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
                    setFullScreen(false);
                }
                else {
                    orient = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
                    setFullScreen(true);
                }
                getActivity().setRequestedOrientation(orient);
            }
        });

        final TextView logText = rootView.findViewById(R.id.logText);
        final ScrollView logScroll = rootView.findViewById(R.id.logScroll);

        Logger logger = new Logger() {
            @Override
            public void log(String message) {
                Log.i(TAG, message);
                if (logText != null) {
                    logText.append(message);
                }
                if (logScroll != null) {
                    logScroll.post(new Runnable() {
                        @Override
                        public void run() {
                            logScroll.fullScroll(View.FOCUS_DOWN);
                        }
                    });
                }
            }

            @Override
            public void clearLog() {
                if (logText != null) {
                    logText.setText("");
                }
            }
        };

        // If we've already selected a video, load it now.
        mLog = logger;
        if (mVideoItem != null) {
            loadPlaykitPlayer();
            controlsView.setPlayer(player);
            addPlayerListeners(progressBar);
            prepareMediaEntry();
        }
    }


    public void makeFullscreen(boolean isFullscreen) {
        setFullScreen(isFullscreen);
    }

    @Override
    public void onPause() {

        if (player != null) {
            player.onApplicationPaused();
        }
        super.onPause();
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

    @Override
    public void onResume() {
        super.onResume();
        if (firstLaunch) {
            firstLaunch = false;
            return;
        }
        if (player != null) {
            player.onApplicationResumed();
            //player.play();
        }
    }

    private void addPlayerListeners(final ProgressBar appProgressBar) {

        player.addListener(this, AdEvent.contentResumeRequested, event -> {
            log("ADS_PLAYBACK_ENDED");
        });

        player.addListener(this, AdEvent.adPlaybackInfoUpdated, event -> {
            log("AD_PLAYBACK_INFO_UPDATED");
            AdEvent.AdPlaybackInfoUpdated playbackInfoUpdated = event;
            //log.d("XXX playbackInfoUpdated  = " + playbackInfoUpdated.width + "/" + playbackInfoUpdated.height + "/" + playbackInfoUpdated.bitrate);
            log("AD_PLAYBACK_INFO_UPDATED bitrate = " + playbackInfoUpdated.bitrate);
        });

        player.addListener(this, AdEvent.skippableStateChanged, event -> {
            log("SKIPPABLE_STATE_CHANGED");
        });

        player.addListener(this, AdEvent.adRequested, event -> {
            AdEvent.AdRequestedEvent adRequestEvent = event;
            log("AD_REQUESTED");// adtag = " + adRequestEvent.adTagUrl);
        });

        player.addListener(this, AdEvent.playHeadChanged, event -> {
            appProgressBar.setVisibility(View.INVISIBLE);
            AdEvent.AdPlayHeadEvent adEventProress = event;
            //log.d("received AD PLAY_HEAD_CHANGED " + adEventProress.adPlayHead);
        });

        player.addListener(this, AdEvent.error, event -> {
            AdEvent.Error adError = event;
            Log.d(TAG, "AD_ERROR " + adError.type + " "  + adError.error.message);
            appProgressBar.setVisibility(View.INVISIBLE);
            log("AD_ERROR");
        });

        player.addListener(this, AdEvent.adBreakStarted, event -> {
            log("AD_BREAK_STARTED");
            appProgressBar.setVisibility(View.VISIBLE);
        });

        player.addListener(this, AdEvent.cuepointsChanged, event -> {
            AdEvent.AdCuePointsUpdateEvent cuePointsList = event;
            Log.d(TAG, "Has Postroll = " + cuePointsList.cuePoints.hasPostRoll());
            log("AD_CUEPOINTS_UPDATED");
            onCuePointChanged();
        });

        player.addListener(this, AdEvent.loaded, event -> {
            AdEvent.AdLoadedEvent adLoadedEvent = event;
            log("AD_LOADED " + adLoadedEvent.adInfo.getAdIndexInPod() + "/" + adLoadedEvent.adInfo.getTotalAdsInPod());
            appProgressBar.setVisibility(View.INVISIBLE);
        });

        player.addListener(this, AdEvent.started, event -> {
            AdEvent.AdStartedEvent adStartedEvent = event;
            log("AD_STARTED w/h - " + adStartedEvent.adInfo.getAdWidth() + "/" + adStartedEvent.adInfo.getAdHeight());
            appProgressBar.setVisibility(View.INVISIBLE);
        });

        player.addListener(this, AdEvent.resumed, event -> {
            log("AD_RESUMED");
            nowPlaying = true;
            appProgressBar.setVisibility(View.INVISIBLE);
        });

        player.addListener(this, AdEvent.paused, event -> {
            log("AD_PAUSED");
            nowPlaying = true;
            if (player != null) {
                AdController adController = player.getController(AdController.class);
                if (adController != null && adController.isAdDisplayed()) {
                    log("Ad " + adController.getAdCurrentPosition() + "/" + adController.getAdDuration());
                } else {
                    log("Player " + player.getCurrentPosition() + "/" + player.getDuration());
                }
            }
        });

        player.addListener(this, AdEvent.skipped, event -> {
            log("AD_SKIPPED");
        });

        player.addListener(this, AdEvent.allAdsCompleted, event -> {
            log("AD_ALL_ADS_COMPLETED");
            appProgressBar.setVisibility(View.INVISIBLE);
        });

        player.addListener(this, AdEvent.completed, event -> {
            log("AD_COMPLETED");
//                AdEvent.AdEndedEvent adEndedEvent = (AdEvent.AdEndedEvent) event;
//                if (adEndedEvent.adEndedReason == PKAdEndedReason.COMPLETED) {
//                    log("AD_ENDED-" + adEndedEvent.adEndedReason);
//                } else if (adEndedEvent.adEndedReason == PKAdEndedReason.SKIPPED) {
//                    log("AD_ENDED-" + adEndedEvent.adEndedReason);
//                    nowPlaying = false;
//                }
            appProgressBar.setVisibility(View.INVISIBLE);
        });

        player.addListener(this, AdEvent.firstQuartile, event -> {
            log("FIRST_QUARTILE");
        });

        player.addListener(this, AdEvent.midpoint, event -> {
            log("MIDPOINT");
            if (player != null) {
                AdController adController = player.getController(AdController.class);
                if (adController != null) {
                    if (adController.isAdDisplayed()) {
                        log(adController.getAdCurrentPosition() + "/" + adController.getAdDuration());
                    }
                    //log("" + adController.getCuePoints().getAdCuePoints().size());
                    //log(adController.getAdInfo().toString());
                    //adController.skip();
                }
            }
        });

        player.addListener(this, AdEvent.thirdQuartile, event -> {
            log("THIRD_QUARTILE");
        });

        player.addListener(this, AdEvent.adBreakEnded, event -> {
            log("AD_BREAK_ENDED");
        });

        player.addListener(this, AdEvent.adClickedEvent, event -> {
            log("AD_CLICKED");
                AdEvent.AdClickedEvent advtClickEvent = event;
                Log.d(TAG, "AD_CLICKED url = " + advtClickEvent.clickThruUrl);
//                nowPlaying = false;
        });


//        player.addEventListener(new PKEvent.Listener() {
//            @Override
//            public void onEvent(PKEvent event) {
//                log("COMPANION_AD_CLICKED");
//                AdEvent.CompanionAdClickEvent advtCompanionClickEvent = (AdEvent.CompanionAdClickEvent) event;
//                Log.d(TAG, "COMPANION_AD_CLICKED url = " + advtCompanionClickEvent.advtCompanionLink);
//                nowPlaying = false;
//            }
//        }, AdEvent.Type.COMPANION_AD_CLICKED);

        player.addListener(this, AdEvent.adBufferStart, event -> {
            log("AD_STARTED_BUFFERING");
            appProgressBar.setVisibility(View.VISIBLE);
        });

        player.addListener(this, AdEvent.adBufferEnd, event -> {
            log("AD_BUFFER_END");
            appProgressBar.setVisibility(View.INVISIBLE);
        });

        player.addListener(this, AdEvent.adBreakEnded, event -> {
            log("AD_BREAK_ENDED");
            appProgressBar.setVisibility(View.INVISIBLE);
        });


        ////PLAYER Events

        player.addListener(this, PlayerEvent.videoFramesDropped, event -> {
            PlayerEvent.VideoFramesDropped videoFramesDropped = event;
            //log("VIDEO_FRAMES_DROPPED " + videoFramesDropped.droppedVideoFrames);
        });

        player.addListener(this, PlayerEvent.bytesLoaded, event -> {
            PlayerEvent.BytesLoaded bytesLoaded = event;
            //log("BYTES_LOADED " + bytesLoaded.bytesLoaded);
        });

        player.addListener(this, PlayerEvent.play, event -> {
            log("PLAYER PLAY");
            nowPlaying = true;
        });

        player.addListener(this, PlayerEvent.pause, event -> {
            log("PLAYER PAUSE");
            nowPlaying = false;
            if (player != null) {
                AdController adController = player.getController(AdController.class);
                if (adController != null && adController.isAdDisplayed()) {
                    log("Ad " + adController.getAdCurrentPosition() + "/" + adController.getAdDuration());
                } else {
                    log("Player " + player.getCurrentPosition() + "/" + player.getDuration());
                }
            }
        });

        player.addListener(this, PlayerEvent.error, event -> {
            log("PLAYER ERROR " + event.error.message);
            appProgressBar.setVisibility(View.INVISIBLE);
            nowPlaying = false;
        });

        player.addListener(this, PlayerEvent.ended, event -> {
            log("PLAYER ENDED");
            appProgressBar.setVisibility(View.INVISIBLE);
            nowPlaying = false;
        });

        player.addListener(this, PlayerEvent.stateChanged, event -> {
            PlayerEvent.StateChanged stateChanged = event;
            //log("State changed from " + stateChanged.oldState + " to " + stateChanged.newState);
            if (stateChanged.newState == PlayerState.BUFFERING) {
                appProgressBar.setVisibility(View.VISIBLE);

            } else if (stateChanged.newState == PlayerState.READY) {
                appProgressBar.setVisibility(View.INVISIBLE);
            }
            if(controlsView != null){
                controlsView.setPlayerState(stateChanged.newState);
            }
        });

        player.addListener(this, PlayerEvent.tracksAvailable, event -> {
            log("TRACKS_AVAILABLE");
            //When the track data available, this event occurs. It brings the info object with it.
            //PlayerEvent.TracksAvailable tracksAvailable = (PlayerEvent.TracksAvailable) event;
            //populateSpinnersWithTrackInfo(tracksAvailable.tracksInfo);
            //log("PLAYER TRACKS_AVAILABLE");

            if (player != null) {
                AdController adController = player.getController(AdController.class);
                if (adController != null) {
                    if (adController.isAdDisplayed()) {
                        //log(adController.getCurrentPosition() + "/" + adController.getDuration());
                    }
                }
            }
        });

        player.addListener(this, PlayerEvent.playheadUpdated, event -> {
            //When the track data available, this event occurs. It brings the info object with it.
            PlayerEvent.PlayheadUpdated playheadUpdated = event;
            //log.d("playheadUpdated event  position = " + playheadUpdated.position + " duration = " + playheadUpdated.duration);
        });
    }

    private void onCuePointChanged() {

        (adSkin).findViewById(R.id.skip_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player != null) {
                    log("Controller skipAd");
                    AdController adController = player.getController(AdEnabledPlayerController.class);
                    if (adController != null) {
                        adController.skip();
                    }
                }
            }
        });

        (adSkin).findViewById(R.id.learn_more_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player != null && player.getController(AdController.class) != null) {
                    log("Controller openLearnMore");
                    //player.getController(AdEnabledPlayerController.class).openLearnMore();
                }
            }
        });

        LinearLayout companionAdPlaceHolder = (LinearLayout) adSkin.findViewById(R.id.companionAdSlot);
        (companionAdPlaceHolder).findViewById(R.id.imageViewCompanion).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (player != null && player.getController(AdController.class) != null) {
                    log("Controller openCompanionAdLearnMore");
                    //player.getController(AdEnabledPlayerController.class).openCompanionAdLearnMore();
                }
            }
        });
    }

    private void setFullScreen(boolean isFullScreen) {
        if (isFullScreen == this.isFullScreen) {
            return;
        }

        if (player != null && player.getController(AdController.class) != null) {
            //log("Controller screenOrientationChanged");
            //player.getController(AdEnabledPlayerController.class).screenOrientationChanged(isFullScreen);
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)playerLayout.getLayoutParams();
        // Checks the orientation of the screen
        this.isFullScreen = isFullScreen;
        if (isFullScreen) {
            ((AppCompatActivity)getActivity()).getSupportActionBar().hide();
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            fullScreenBtn.setImageResource(R.drawable.ic_no_fullscreen);
            params.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT;

        } else {
            ((AppCompatActivity)getActivity()).getSupportActionBar().show();
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            fullScreenBtn.setImageResource(R.drawable.ic_fullscreen);

            params.height = (int)getResources().getDimension(R.dimen.player_height);
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT;
        }
        playerLayout.requestLayout();
    }

    public interface Logger {
        void log(String logMessage);
        void clearLog();
    }

    private void log(String message) {
        if (mLog != null) {
            mLog.log(message + "\n");
        }
    }

    private void clearLog() {
        if (mLog != null) {
            mLog.clearLog();
        }
    }
}
