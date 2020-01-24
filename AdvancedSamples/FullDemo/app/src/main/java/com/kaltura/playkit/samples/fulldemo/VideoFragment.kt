package com.kaltura.playkit.samples.fulldemo

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.kaltura.android.exoplayer2.util.MimeTypes
import com.kaltura.playkit.*
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.ads.AdEnabledPlayerController
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ima.IMAConfig
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsConfig
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsPlugin
import com.kaltura.playkit.plugins.youbora.YouboraPlugin
import com.kaltura.playkit.samples.fulldemo.Consts.AD_LOAD_TIMEOUT
import com.kaltura.playkit.samples.fulldemo.Consts.AUTO_PLAY
import com.kaltura.playkit.samples.fulldemo.Consts.COMPANION_AD_HEIGHT
import com.kaltura.playkit.samples.fulldemo.Consts.COMPANION_AD_WIDTH
import com.kaltura.playkit.samples.fulldemo.Consts.HLS_URL
import com.kaltura.playkit.samples.fulldemo.Consts.HLS_URL2
import com.kaltura.playkit.samples.fulldemo.Consts.MIME_TYPE
import com.kaltura.playkit.samples.fulldemo.Consts.MIN_AD_DURATION_FOR_SKIP_BUTTON
import com.kaltura.playkit.samples.fulldemo.Consts.PREFERRED_BITRATE
import com.kaltura.playkit.samples.fulldemo.Consts.START_FROM
import com.kaltura.tvplayer.KalturaBasicPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.PlayerInitOptions
import java.util.*

//import com.kaltura.plugins.adsmanager.AdsConfig;
//import com.kaltura.plugins.adsmanager.AdsPlugin;

class VideoFragment : Fragment() {

    private val TAG = VideoFragment::class.java.simpleName

    //id of the first entry
    private val FIRST_ENTRY_ID = "entry_id_1"
    //id of the second entry
    private val SECOND_ENTRY_ID = "entry_id_2"
    //id of the first media source.
    private val FIRST_MEDIA_SOURCE_ID = "source_id_1"
    //id of the second media source.
    private val SECOND_MEDIA_SOURCE_ID = "source_id_2"


    //Youbora analytics Constants
    val ACCOUNT_CODE = "kalturatest"
    val USER_NAME = "a@a.com"
    val MEDIA_TITLE = "your_media_title"
    val IS_LIVE = false
    val ENABLE_SMART_ADS = true
    private val CAMPAIGN = "your_campaign_name"
    val EXTRA_PARAM_1 = "playKitPlayer"
    val EXTRA_PARAM_2 = "XXX"
    val GENRE = "your_genre"
    val TYPE = "your_type"
    val TRANSACTION_TYPE = "your_trasnsaction_type"
    val YEAR = "your_year"
    val CAST = "your_cast"
    val DIRECTOR = "your_director"
    private val OWNER = "your_owner"
    val PARENTAL = "your_parental"
    val PRICE = "your_price"
    val RATING = "your_rating"
    val AUDIO_TYPE = "your_audio_type"
    val AUDIO_CHANNELS = "your_audoi_channels"
    val DEVICE = "your_device"
    val QUALITY = "your_quality"
    fun isPKMediaFormatContains(playbackFormat: String): Boolean {
        for (format in PKMediaFormat.values()) {
            if (format.name == playbackFormat) {
                return true
            }
        }
        return false
    }

    private lateinit var mVideoItem: VideoItem
    private lateinit var mVideoTitle: TextView
    private lateinit var playerLayout: FrameLayout
    private lateinit var adSkin: RelativeLayout

    private var player: KalturaPlayer? = null
    private var controlsView: PlaybackControlsView? = null
    private var nowPlaying: Boolean = false
    private var progressBar: ProgressBar? = null
    private var isFullScreen: Boolean = false
    private var fullScreenBtn: AppCompatImageView? = null
    // private PKMediaConfig mediaConfig;
    private var mLog: Logger? = null
    private var mViewCreatedCallback: OnVideoFragmentViewCreatedListener? = null

    private var isAutoPlay: Boolean = false
    private var startPosition: Long? = null
    private var adLoadTimeOut: Int = 0
    private var videoMimeType: String? = null
    private var videoBitrate: Int = 0
    private var companionAdWidth: Int = 0
    private var companionAdHeight: Int = 0
    private var minAdDurationForSkipButton: Int = 0
    private var firstLaunch = true

    private var rootView: View? = null

    private val phoenixAnalyticsConfig: PhoenixAnalyticsConfig
        get() {
            val ks = "djJ8MTk4fHFftqeAPxdlLVzZBk0Et03Vb8on1wLsKp7cbOwzNwfOvpgmOGnEI_KZDhRWTS-76jEY7pDONjKTvbWyIJb5RsP4NL4Ng5xuw6L__BeMfLGAktkVliaGNZq9SXF5n2cMYX-sqsXLSmWXF9XN89io7-k="
            return PhoenixAnalyticsConfig(198, "https://rest-as.ott.kaltura.com/v4_4/api_v3/", ks, 30)
        }

    /**
     * Listener called when the fragment's onCreateView is fired.
     */
    interface OnVideoFragmentViewCreatedListener {
        fun onVideoFragmentViewCreated()
    }

    override fun onAttach(activity: Activity?) {
        firstLaunch = true
        try {
            mViewCreatedCallback = activity as OnVideoFragmentViewCreatedListener?

        } catch (e: ClassCastException) {
            throw ClassCastException(activity!!.toString()
                    + " must implement " + OnVideoFragmentViewCreatedListener::class.java.name)
        }

        super.onAttach(activity)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        isAutoPlay = arguments!!.getBoolean(AUTO_PLAY)
        startPosition = arguments!!.getLong(START_FROM)
        adLoadTimeOut = arguments!!.getInt(AD_LOAD_TIMEOUT)
        videoMimeType = arguments!!.getString(MIME_TYPE)
        videoBitrate = arguments!!.getInt(PREFERRED_BITRATE)
        companionAdWidth = arguments!!.getInt(COMPANION_AD_WIDTH)
        companionAdHeight = arguments!!.getInt(COMPANION_AD_HEIGHT)
        minAdDurationForSkipButton = arguments!!.getInt(MIN_AD_DURATION_FOR_SKIP_BUTTON)

        rootView = inflater.inflate(R.layout.fragment_video, container, false)
        initUi(rootView!!)
        if (mViewCreatedCallback != null) {
            mViewCreatedCallback!!.onVideoFragmentViewCreated()
        }

        return rootView
    }

    private fun changeMedia() {
        if (player == null) {
            return
        }

        clearLog()

        //Check if id of the media entry that is set in mediaConfig.
        val AD_HONDA2 = "http://pubads.g.doubleclick.net/gampad/ads?sz=640x360&iu=/6062/iab_vast_samples/skippable&ciu_szs=300x250,728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&correlator=" + System.currentTimeMillis()//"http://pubads.g.doubleclick.net/gampad/ads?sz=400x300&iu=%2F6062%2Fhanna_MA_group%2Fvideo_comp_app&ciu_szs=&impl=s&gdfp_req=1&env=vp&output=xml_vast3&unviewed_position_start=1&m_ast=vast&url=";
        val videoMimeTypes = ArrayList<String>()
        videoMimeTypes.add("video/mp4")
        videoMimeTypes.add("application/x-mpegURL")
        if (player?.mediaEntry?.id == FIRST_ENTRY_ID) {
            val AD_HOND = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator="//"http://externaltests.dev.kaltura.com/player/Vast_xml/alexs.qacore-vast3-rol_02.xml";

            val adsConfig = IMAConfig().setAdTagUrl(AD_HOND).enableDebugMode(true).setVideoMimeTypes(videoMimeTypes)
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

            val referrer = "app://NonDefaultReferrer1/" + activity!!.packageCodePath
            //player.updatePluginConfig(AdsPlugin.factory.getName(), adsConfig);
            player?.updatePluginConfig(PhoenixAnalyticsPlugin.factory.name, phoenixAnalyticsConfig)
            player?.updatePluginConfig(IMAPlugin.factory.name, adsConfig)
            player?.updatePluginConfig(YouboraPlugin.factory.name, getConverterYoubora(MEDIA_TITLE + "_changeMedia1", false).toJson())
            //If first one is active, prepare second one.
            prepareFirstEntry()
        } else {
            val adsConfig = IMAConfig().setAdTagUrl(AD_HONDA2).enableDebugMode(true).setVideoMimeTypes(videoMimeTypes)

            //            AdsConfig adsConfig = new AdsConfig().setAdTagURL(AD_HONDA2).
            //                    setPlayerViewContainer(playerLayout).
            //                    setAdSkinContainer(adSkin).
            //                    setAdLoadTimeOut(adLoadTimeOut).
            //                    setVideoMimeTypes(PKMediaFormat.valueOf(videoMimeType)).
            //                    setVideoBitrate(videoBitrate).
            //                    setMinAdDurationForSkipButton(minAdDurationForSkipButton).
            //                    setCompanionAdWidth(companionAdWidth).
            //                    setCompanionAdHeight(companionAdHeight);

            val referrer = "app://NonDefaultReferrer2/" + activity!!.packageName
            player?.updatePluginConfig(PhoenixAnalyticsPlugin.factory.name, phoenixAnalyticsConfig)
            //player?.updatePluginConfig(AdsPlugin.factory.getName(), adsConfig);
            player?.updatePluginConfig(IMAPlugin.factory.name, adsConfig)
            player?.updatePluginConfig(YouboraPlugin.factory.name, getConverterYoubora(MEDIA_TITLE + "_changeMedia2", false).toJson())

            //If the second one is active, prepare the first one.
            prepareSecondEntry()
        }
    }

    /**
     * Prepare the first entry.
     */
    private fun prepareFirstEntry() {
        //First. Create PKMediaEntry object.
        val mediaEntry = createFirstMediaEntry()

        //Prepare player with media configuration.
        player?.setMedia(mediaEntry, 0L)
    }

    /**
     * Prepare the second entry.
     */
    private fun prepareSecondEntry() {
        //Second. Create PKMediaEntry object.
        val mediaEntry = createSecondMediaEntry()

        //Prepare player with media configuration.
        player?.setMedia(mediaEntry, 0L)
    }

    private fun createFirstMediaEntry(): PKMediaEntry {
        //Create media entry.
        val mediaEntry = PKMediaEntry()

        //Set id for the entry.
        mediaEntry.id = FIRST_ENTRY_ID
        mediaEntry.duration = (300 * 1000).toLong()
        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
        mediaEntry.mediaType = PKMediaEntry.MediaEntryType.Vod

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        val mediaSources = createFirstMediaSources()

        //Set media sources to the entry.
        mediaEntry.sources = mediaSources

        return mediaEntry
    }

    /**
     * Create [PKMediaEntry] with minimum necessary data.
     *
     * @return - the [PKMediaEntry] object.
     */
    private fun createSecondMediaEntry(): PKMediaEntry {
        //Create media entry.
        val mediaEntry = PKMediaEntry()

        //Set id for the entry.
        mediaEntry.id = SECOND_ENTRY_ID
        mediaEntry.duration = (450 * 1000).toLong()
        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
        mediaEntry.mediaType = PKMediaEntry.MediaEntryType.Vod

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        val mediaSources = createSecondMediaSources()

        //Set media sources to the entry.
        mediaEntry.sources = mediaSources

        return mediaEntry
    }

    private fun createFirstMediaSources(): List<PKMediaSource> {
        //Init list which will hold the PKMediaSources.
        val mediaSources = ArrayList<PKMediaSource>()

        //Create new PKMediaSource instance.
        val mediaSource = PKMediaSource()

        //Set the id.
        mediaSource.id = FIRST_MEDIA_SOURCE_ID

        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.url = HLS_URL2

        //Set the format of the source. In our case it will be hls.
        mediaSource.mediaFormat = PKMediaFormat.valueOfUrl(HLS_URL2)

        //Add media source to the list.
        mediaSources.add(mediaSource)

        return mediaSources
    }

    /**
     * Create list of [PKMediaSource].
     *
     * @return - the list of sources.
     */
    private fun createSecondMediaSources(): List<PKMediaSource> {
        //Init list which will hold the PKMediaSources.
        val mediaSources = ArrayList<PKMediaSource>()

        //Create new PKMediaSource instance.
        val mediaSource = PKMediaSource()

        //Set the id.
        mediaSource.id = SECOND_MEDIA_SOURCE_ID

        mediaSource.url = HLS_URL

        mediaSource.mediaFormat = PKMediaFormat.valueOfUrl(SECOND_MEDIA_SOURCE_ID)

        //Add media source to the list.
        mediaSources.add(mediaSource)

        return mediaSources
    }

    fun loadVideo(videoItem: VideoItem) {
        mVideoItem = videoItem
    }

    // KalturaPlayer

    fun loadPlaykitPlayer() {
        clearLog()

        val playerInitOptions = PlayerInitOptions()

        // IMA Configuration
        val pkPluginConfigs = PKPluginConfigs()
        val adsConfig = getAdsConfig(mVideoItem!!.adTagUrl)
        pkPluginConfigs.setPluginConfig(IMAPlugin.factory.name, adsConfig)

        playerInitOptions.setPluginConfigs(pkPluginConfigs)


        player = KalturaBasicPlayer.create(activity, playerInitOptions)
        player?.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT)

        val container = rootView!!.findViewById<ViewGroup>(R.id.player_root)
        container.addView(player?.playerView)
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

    private fun addPhoenixAnalyticsPluginConfig(config: PKPluginConfigs) {
        val phoenixAnalyticsConfig = phoenixAnalyticsConfig
        config.setPluginConfig(PhoenixAnalyticsPlugin.factory.name, phoenixAnalyticsConfig)
    }

    private fun getAdsConfig(adTagUrl: String): IMAConfig {

        //List<String> videoMimeTypes = new ArrayList<>();
        //videoMimeTypes.add(MimeTypes.APPLICATION_MP4);
        //videoMimeTypes.add(MimeTypes.APPLICATION_M3U8);
        //Map<Double, String> tagTimesMap = new HashMap<>();
        //tagTimesMap.put(2.0,"ADTAG");
        val videoMimeTypes = ArrayList<String>()
        videoMimeTypes.add("video/mp4")
        videoMimeTypes.add(MimeTypes.APPLICATION_M3U8)
        return IMAConfig().setAdTagUrl(adTagUrl).enableDebugMode(true).setVideoMimeTypes(videoMimeTypes)
    }

    private fun addYouboraPlugin(pluginConfigs: PKPluginConfigs) {
        val converterYoubora = getConverterYoubora(MEDIA_TITLE, false)

        pluginConfigs.setPluginConfig(YouboraPlugin.factory.name, converterYoubora.toJson())
    }

    private fun getConverterYoubora(mediaTitle: String, isLive: Boolean): ConverterYoubora {
        val accountCode = JsonPrimitive(ACCOUNT_CODE)
        val username = JsonPrimitive(USER_NAME)
        val haltOnError = JsonPrimitive(true)
        val enableAnalytics = JsonPrimitive(true)
        val enableSmartAds = JsonPrimitive(ENABLE_SMART_ADS)

        val mediaEntry = JsonObject()
        mediaEntry.addProperty("isLive", isLive)
        mediaEntry.addProperty("title", mediaTitle)

        val adsEntry = JsonObject()
        adsEntry.addProperty("campaign", CAMPAIGN)

        val extraParamEntry = JsonObject()
        extraParamEntry.addProperty("param1", "mobile")
        extraParamEntry.addProperty("param2", EXTRA_PARAM_2)
        extraParamEntry.addProperty("param3", "CCC")

        val propertiesEntry = JsonObject()
        propertiesEntry.addProperty("genre", GENRE)
        propertiesEntry.addProperty("type", TYPE)
        propertiesEntry.addProperty("transaction_type", TRANSACTION_TYPE)
        propertiesEntry.addProperty("year", YEAR)
        propertiesEntry.addProperty("cast", CAST)
        propertiesEntry.addProperty("director", DIRECTOR)
        propertiesEntry.addProperty("owner", OWNER)
        propertiesEntry.addProperty("parental", PARENTAL)
        propertiesEntry.addProperty("price", PRICE)
        propertiesEntry.addProperty("rating", RATING)
        propertiesEntry.addProperty("audioType", AUDIO_TYPE)
        propertiesEntry.addProperty("audioChannels", AUDIO_CHANNELS)
        propertiesEntry.addProperty("device", DEVICE)
        propertiesEntry.addProperty("quality", QUALITY)


        return ConverterYoubora(accountCode, username, haltOnError, enableAnalytics, enableSmartAds,
                mediaEntry,
                adsEntry, extraParamEntry, propertiesEntry)
    }

    private fun prepareMediaEntry() {
        val pkMediaEntry = createMediaEntry()
        player?.setMedia(pkMediaEntry)
    }

    /**
     * Create [PKMediaEntry] with minimum necessary data.
     *
     * @return - the [PKMediaEntry] object.
     */
    private fun createMediaEntry(): PKMediaEntry {
        //Create media entry.
        val mediaEntry = PKMediaEntry()

        //Set id for the entry.
        mediaEntry.id = "1_w9zx2eti"
        mediaEntry.duration = (883 * 1000).toLong()
        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
        mediaEntry.mediaType = PKMediaEntry.MediaEntryType.Vod

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        val mediaSources = createMediaSources()

        //Set media sources to the entry.
        mediaEntry.sources = mediaSources

        return mediaEntry
    }

    /**
     * Create list of [PKMediaSource].
     *
     * @return - the list of sources.
     */
    private fun createMediaSources(): List<PKMediaSource> {
        //Init list which will hold the PKMediaSources.
        val mediaSources = ArrayList<PKMediaSource>()

        //Create new PKMediaSource instance.
        val mediaSource = PKMediaSource()

        //Set the id.
        mediaSource.id = "11111"

        if (!TextUtils.isEmpty(mVideoItem!!.videoLic)) {
            val pkDrmDataList = ArrayList<PKDrmParams>()
            var pkDrmParams: PKDrmParams? = null
            if (mVideoItem!!.videoUrl.endsWith("mpd")) {
                pkDrmParams = PKDrmParams(mVideoItem!!.videoLic, PKDrmParams.Scheme.WidevineCENC)
            } else {
                pkDrmParams = PKDrmParams(mVideoItem!!.videoLic, PKDrmParams.Scheme.WidevineClassic)
            }
            pkDrmDataList.add(pkDrmParams)
            mediaSource.drmData = pkDrmDataList

        }
        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.url = mVideoItem!!.videoUrl
        mediaSource.mediaFormat = PKMediaFormat.valueOfUrl(mVideoItem!!.videoUrl)


        //Add media source to the list.
        mediaSources.add(mediaSource)

        return mediaSources
    }

    private fun initUi(rootView: View) {

        val changeMediaButton = rootView.findViewById<Button>(R.id.changeMedia)
        //Set click listener.
        changeMediaButton.setOnClickListener {
            //Change media.
            changeMedia()
        }
        mVideoTitle = rootView.findViewById(R.id.video_title)
        playerLayout = rootView.findViewById(R.id.player_root)
        progressBar = rootView.findViewById(R.id.progressBarSpinner)
        controlsView = rootView.findViewById(R.id.playerControls)
        progressBar!!.visibility = View.INVISIBLE
        adSkin = rootView.findViewById(R.id.ad_skin)
        fullScreenBtn = rootView.findViewById(R.id.full_screen_switcher)
        fullScreenBtn!!.setOnClickListener {
            val orient: Int
            if (isFullScreen) {
                orient = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                setFullScreen(false)
            } else {
                orient = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                setFullScreen(true)
            }
            activity!!.requestedOrientation = orient
        }

        val logText = rootView.findViewById<TextView>(R.id.logText)
        val logScroll = rootView.findViewById<ScrollView>(R.id.logScroll)

        val logger = object : Logger {
            override fun log(message: String) {
                Log.i(TAG, message)
                logText?.append(message)
                logScroll?.post { logScroll.fullScroll(View.FOCUS_DOWN) }
            }

            override fun clearLog() {
                if (logText != null) {
                    logText.text = ""
                }
            }
        }

        // If we've already selected a video, load it now.
        mLog = logger
        if (mVideoItem != null) {
            loadPlaykitPlayer()
            controlsView!!.setPlayer(player)
            addPlayerListeners(progressBar!!)
            prepareMediaEntry()
        }
    }


    fun makeFullscreen(isFullscreen: Boolean) {
        setFullScreen(isFullscreen)
    }

    override fun onPause() {
        super.onPause()
        player?.onApplicationPaused()
    }

    override fun onDestroy() {
        if (player != null) {
            player?.removeListeners(this)
            player?.destroy()
            player = null
        }
        super.onDestroy()
    }

    override fun onResume() {
        super.onResume()
        if (firstLaunch) {
            firstLaunch = false
            return
        }
        if (player != null) {
            player?.onApplicationResumed()
            //player.play();
        }
    }

    private fun addPlayerListeners(appProgressBar: ProgressBar) {

        player?.addListener(this, AdEvent.contentResumeRequested) { event ->
            log("ADS_PLAYBACK_ENDED")
            controlsView?.setSeekBarStateForAd(false)
        }

        player?.addListener(this, AdEvent.contentPauseRequested) { event ->
            log("AD_CONTENT_PAUSE_REQUESTED")
            controlsView?.setSeekBarStateForAd(true)
        }

        player?.addListener(this, AdEvent.adPlaybackInfoUpdated) { event ->
            log("AD_PLAYBACK_INFO_UPDATED")
            //log.d("XXX playbackInfoUpdated  = " + playbackInfoUpdated.width + "/" + playbackInfoUpdated.height + "/" + playbackInfoUpdated.bitrate);
            log("AD_PLAYBACK_INFO_UPDATED bitrate = " + event.bitrate)
        }

        player?.addListener(this, AdEvent.skippableStateChanged) { event -> log("SKIPPABLE_STATE_CHANGED") }

        player?.addListener(this, AdEvent.adRequested) { event ->
            val adRequestEvent = event
            log("AD_REQUESTED")// adtag = " + adRequestEvent.adTagUrl);
        }

        player?.addListener(this, AdEvent.playHeadChanged) { event ->
            appProgressBar.visibility = View.INVISIBLE
            val adEventProress = event
            //log.d("received AD PLAY_HEAD_CHANGED " + adEventProress.adPlayHead);
        }

        player?.addListener(this, AdEvent.error) { event ->
            Log.d(TAG, "AD_ERROR " + event.type + " " + event.error.message)
            appProgressBar.visibility = View.INVISIBLE
            controlsView?.setSeekBarStateForAd(false)
            log("AD_ERROR")
        }

        player?.addListener(this, AdEvent.adBreakStarted) { event ->
            log("AD_BREAK_STARTED")
            appProgressBar.visibility = View.VISIBLE
        }

        player?.addListener(this, AdEvent.cuepointsChanged) { event ->
            Log.d(TAG, "Has Postroll = " + event.cuePoints.hasPostRoll())
            log("AD_CUEPOINTS_UPDATED")
            onCuePointChanged()
        }

        player?.addListener(this, AdEvent.loaded) { event ->
            log("AD_LOADED " + event.adInfo.getAdIndexInPod() + "/" + event.adInfo.getTotalAdsInPod())
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.started) { event ->
            log("AD_STARTED w/h - " + event.adInfo.getAdWidth() + "/" + event.adInfo.getAdHeight())
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.resumed) { event ->
            log("AD_RESUMED")
            nowPlaying = true
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.paused) { event ->
            log("AD_PAUSED")
            nowPlaying = true
            player?.let {
                val adController = it.getController(AdController::class.java)
                if (adController != null && adController.isAdDisplayed) {
                    log("Ad " + adController.adCurrentPosition + "/" + adController.adDuration)
                } else {
                    log("Player " + it.currentPosition + "/" + it.duration)
                }
            }
        }

        player?.addListener(this, AdEvent.skipped) { event -> log("AD_SKIPPED") }

        player?.addListener(this, AdEvent.allAdsCompleted) { event ->
            log("AD_ALL_ADS_COMPLETED")
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.completed) { event ->
            log("AD_COMPLETED")
            //                AdEvent.AdEndedEvent adEndedEvent = (AdEvent.AdEndedEvent) event;
            //                if (adEndedEvent.adEndedReason == PKAdEndedReason.COMPLETED) {
            //                    log("AD_ENDED-" + adEndedEvent.adEndedReason);
            //                } else if (adEndedEvent.adEndedReason == PKAdEndedReason.SKIPPED) {
            //                    log("AD_ENDED-" + adEndedEvent.adEndedReason);
            //                    nowPlaying = false;
            //                }
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.firstQuartile) { event -> log("FIRST_QUARTILE") }

        player?.addListener(this, AdEvent.midpoint) { event ->
            log("MIDPOINT")
            player?.let {
                val adController = it.getController(AdController::class.java)
                if (adController != null) {
                    if (adController.isAdDisplayed) {
                        log(adController.adCurrentPosition.toString() + "/" + adController.adDuration)
                    }
                    //log("" + adController.getCuePoints().getAdCuePoints().size());
                    //log(adController.getAdInfo().toString());
                    //adController.skip();
                }
            }
        }

        player?.addListener(this, AdEvent.thirdQuartile) { event -> log("THIRD_QUARTILE") }

        player?.addListener(this, AdEvent.adBreakEnded) { event -> log("AD_BREAK_ENDED") }

        player?.addListener(this, AdEvent.adClickedEvent) { event ->
            log("AD_CLICKED")
            Log.d(TAG, "AD_CLICKED url = " + event.clickThruUrl)
            //                nowPlaying = false;
        }


        //        player.addEventListener(new PKEvent.Listener() {
        //            @Override
        //            public void onEvent(PKEvent event) {
        //                log("COMPANION_AD_CLICKED");
        //                AdEvent.CompanionAdClickEvent advtCompanionClickEvent = (AdEvent.CompanionAdClickEvent) event;
        //                Log.d(TAG, "COMPANION_AD_CLICKED url = " + advtCompanionClickEvent.advtCompanionLink);
        //                nowPlaying = false;
        //            }
        //        }, AdEvent.Type.COMPANION_AD_CLICKED);

        player?.addListener(this, AdEvent.adBufferStart) { event ->
            log("AD_STARTED_BUFFERING")
            appProgressBar.visibility = View.VISIBLE
        }

        player?.addListener(this, AdEvent.adBufferEnd) { event ->
            log("AD_BUFFER_END")
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.adBreakEnded) { event ->
            log("AD_BREAK_ENDED")
            appProgressBar.visibility = View.INVISIBLE
        }


        ////PLAYER Events

        player?.addListener(this, PlayerEvent.videoFramesDropped) { event ->
            val videoFramesDropped = event
            //log("VIDEO_FRAMES_DROPPED " + videoFramesDropped.droppedVideoFrames);
        }

        player?.addListener(this, PlayerEvent.bytesLoaded) { event ->
            val bytesLoaded = event
            //log("BYTES_LOADED " + bytesLoaded.bytesLoaded);
        }

        player?.addListener(this, PlayerEvent.play) { event ->
            log("PLAYER PLAY")
            nowPlaying = true
        }

        player?.addListener(this, PlayerEvent.pause) { event ->
            log("PLAYER PAUSE")
            nowPlaying = false
            player?.let {
                val adController = it.getController(AdController::class.java)
                if (adController != null && adController.isAdDisplayed) {
                    log("Ad " + adController.adCurrentPosition + "/" + adController.adDuration)
                } else {
                    log("Player " + it.currentPosition + "/" + it.duration)
                }
            }
        }

        player?.addListener(this, PlayerEvent.error) { event ->
            log("PLAYER ERROR " + event.error.message!!)
            appProgressBar.visibility = View.INVISIBLE
            nowPlaying = false
        }

        player?.addListener(this, PlayerEvent.ended) { event ->
            log("PLAYER ENDED")
            appProgressBar.visibility = View.INVISIBLE
            nowPlaying = false
        }

        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            //log("State changed from " + stateChanged.oldState + " to " + stateChanged.newState);
            if (event.newState == PlayerState.BUFFERING) {
                appProgressBar.visibility = View.VISIBLE

            } else if (event.newState == PlayerState.READY) {
                appProgressBar.visibility = View.INVISIBLE
            }
            if (controlsView != null) {
                controlsView!!.setPlayerState(event.newState)
            }
        }

        player?.addListener(this, PlayerEvent.tracksAvailable) { event ->
            log("TRACKS_AVAILABLE")
            //When the track data available, this event occurs. It brings the info object with it.
            //PlayerEvent.TracksAvailable tracksAvailable = (PlayerEvent.TracksAvailable) event;
            //populateSpinnersWithTrackInfo(tracksAvailable.tracksInfo);
            //log("PLAYER TRACKS_AVAILABLE");

            player?.let {
                val adController = it.getController(AdController::class.java)
                if (adController != null) {
                    if (adController.isAdDisplayed) {
                        //log(adController.getCurrentPosition() + "/" + adController.getDuration());
                    }
                }
            }
        }

        player?.addListener(this, PlayerEvent.playheadUpdated) { event ->
            //When the track data available, this event occurs. It brings the info object with it.
            val playheadUpdated = event
            //log.d("playheadUpdated event  position = " + playheadUpdated.position + " duration = " + playheadUpdated.duration);
        }
    }

    private fun onCuePointChanged() {

        adSkin?.findViewById<View>(R.id.skip_btn)?.setOnClickListener {
            if (player != null) {
                log("Controller skipAd")
                val adController = player!!.getController(AdEnabledPlayerController::class.java)
                adController?.skip()
            }
        }

        adSkin?.findViewById<View>(R.id.learn_more_btn)?.setOnClickListener {
            if (player != null && player!!.getController(AdController::class.java) != null) {
                log("Controller openLearnMore")
                //player.getController(AdEnabledPlayerController.class).openLearnMore();
            }
        }

        val companionAdPlaceHolder = adSkin!!.findViewById<View>(R.id.companionAdSlot) as LinearLayout
        companionAdPlaceHolder.findViewById<View>(R.id.imageViewCompanion).setOnClickListener {
            if (player != null && player?.getController(AdController::class.java) != null) {
                log("Controller openCompanionAdLearnMore")
                //player.getController(AdEnabledPlayerController.class).openCompanionAdLearnMore();
            }
        }
    }

    private fun setFullScreen(isFullScreen: Boolean) {
        if (isFullScreen == this.isFullScreen) {
            return
        }

        if (player != null && player?.getController(AdController::class.java) != null) {
            //log("Controller screenOrientationChanged");
            //player.getController(AdEnabledPlayerController.class).screenOrientationChanged(isFullScreen);
        }

        val params = playerLayout?.layoutParams as RelativeLayout.LayoutParams
        // Checks the orientation of the screen
        this.isFullScreen = isFullScreen
        if (isFullScreen) {
            (activity as AppCompatActivity).supportActionBar?.hide()
            activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            fullScreenBtn?.setImageResource(R.drawable.ic_no_fullscreen)
            params.height = RelativeLayout.LayoutParams.MATCH_PARENT
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT

        } else {
            (activity as AppCompatActivity).supportActionBar?.show()
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            fullScreenBtn?.setImageResource(R.drawable.ic_fullscreen)

            params.height = resources.getDimension(R.dimen.player_height).toInt()
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT
        }
        playerLayout?.requestLayout()
    }

    interface Logger {
        fun log(logMessage: String)
        fun clearLog()
    }

    private fun log(message: String) {
        mLog?.log(message + "\n")

    }

    private fun clearLog() {
        mLog?.clearLog()
    }
}
