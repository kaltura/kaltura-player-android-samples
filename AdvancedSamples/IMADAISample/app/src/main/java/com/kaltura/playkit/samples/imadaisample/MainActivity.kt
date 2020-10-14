package com.kaltura.playkit.samples.imadaisample

import android.os.Build
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import com.google.ads.interactivemedia.v3.api.FriendlyObstructionPurpose

import com.google.ads.interactivemedia.v3.api.StreamRequest
import com.kaltura.playkit.PKMediaConfig
import com.kaltura.playkit.PKPluginConfigs
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ima.PKFriendlyObstruction
import com.kaltura.playkit.plugins.imadai.IMADAIConfig
import com.kaltura.playkit.plugins.imadai.IMADAIPlugin
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions


class MainActivity: AppCompatActivity() {

    private var player: KalturaPlayer? = null
    private val mediaConfig: PKMediaConfig? = null
    private var playPauseButton: Button? = null
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null

    private val daiConfigLiveHls: IMADAIConfig
        get() {
            val assetTitle = "Live Video - Big Buck Bunny"
            val assetKey = "sN_IYUG8STe1ZzhIIE_ksA"
            val apiKey: String? = null
            val streamFormat = StreamRequest.StreamFormat.HLS
            val licenseUrl: String? = null
            return IMADAIConfig.getLiveIMADAIConfig(assetTitle,
                    assetKey,
                    apiKey,
                    streamFormat,
                    licenseUrl).setAlwaysStartWithPreroll(true).enableDebugMode(true)
        }

    private val daiConfigVodHls1: IMADAIConfig
        get() {
            val assetTitle = "VOD - Tears of Steel"
            val apiKey: String? = null
            val contentSourceId = "2528370"
            val videoId = "tears-of-steel"
            val streamFormat = StreamRequest.StreamFormat.HLS
            val licenseUrl: String? = null

            return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                    contentSourceId,
                    videoId,
                    apiKey,
                    streamFormat,
                    licenseUrl).enableDebugMode(true).setAlwaysStartWithPreroll(true)
        }

    private val daiConfigVodHls2: IMADAIConfig
        get() {
            val assetTitle = "VOD - Google I/O"
            val apiKey: String? = null
            val contentSourceId = "2477953"
            val videoId = "googleio-highlights"
            val streamFormat = StreamRequest.StreamFormat.HLS
            val licenseUrl: String? = null
            return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                    contentSourceId,
                    videoId,
                    apiKey,
                    streamFormat,
                    licenseUrl).enableDebugMode(true)
        }

    private val daiConfigVodHls3: IMADAIConfig
        get() {
            val assetTitle = "HLS3"
            val apiKey: String? = null
            val contentSourceId = "19823"
            val videoId = "ima-test"
            val streamFormat = StreamRequest.StreamFormat.HLS
            val licenseUrl: String? = null
            return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                    contentSourceId,
                    videoId,
                    apiKey,
                    streamFormat,
                    licenseUrl).enableDebugMode(true)
        }

    private val daiConfigVodHls4: IMADAIConfig
        get() {
            val assetTitle3 = "HLS4"
            val assetKey3: String? = null
            val apiKey3: String? = null
            val contentSourceId3 = "2472176"
            val videoId3 = "2504847"
            val streamFormat3 = StreamRequest.StreamFormat.HLS
            val licenseUrl3: String? = null
            return IMADAIConfig.getVodIMADAIConfig(assetTitle3,
                    contentSourceId3,
                    videoId3,
                    apiKey3,
                    streamFormat3,
                    licenseUrl3)
        }

    private val daiConfigVodDash: IMADAIConfig
        get() {
            val assetTitle = "BBB-widevine"
            val apiKey: String? = null
            val contentSourceId = "2474148"
            val videoId = "bbb-widevine"
            val streamFormat = StreamRequest.StreamFormat.DASH
            val licenseUrl = "https://proxy.uat.widevine.com/proxy"
            return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                    contentSourceId,
                    videoId,
                    apiKey,
                    streamFormat,
                    licenseUrl).enableDebugMode(true)
        }


    private val daiConfigError: IMADAIConfig
        get() {
            val assetTitle = "ERROR"
            val assetKey: String? = null
            val apiKey: String? = null
            val contentSourceId = "19823"
            val videoId = "ima-test"
            val streamFormat = StreamRequest.StreamFormat.HLS
            val licenseUrl: String? = null
            return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                    contentSourceId + "AAAA",
                    videoId,
                    apiKey,
                    streamFormat,
                    licenseUrl).enableDebugMode(true)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadPlaykitPlayer()

        showSystemUI()

        findViewById<View>(R.id.activity_main).setOnClickListener { v ->
            if (isFullScreen) {
                showSystemUI()
            } else {
                hideSystemUI()
            }
        }
    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar

                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar

                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
        }
        isFullScreen = true
    }

    private fun showSystemUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        isFullScreen = false
    }

    /**
     * Create IMAPlugin object.
     *
     * @return - [PKPluginConfigs] object with IMAPlugin.
     */
    private fun createIMADAIPlugin(): PKPluginConfigs {

        //Initialize plugin configuration object.
        val pluginConfigs = PKPluginConfigs()

        //Initialize + Configure  IMADAIConfig object.
        val adsConfigVodHls1 = daiConfigVodHls1.enableDebugMode(true).setAlwaysStartWithPreroll(true); //.setFriendlyObstructions(getFriendlyViews())
        val adsConfigVodLive = daiConfigLiveHls.enableDebugMode(true).setAlwaysStartWithPreroll(true)
        val adsConfigVodDash = daiConfigVodDash.enableDebugMode(true).setAlwaysStartWithPreroll(true)
        val adsConfigError = daiConfigError.enableDebugMode(true).setAlwaysStartWithPreroll(true)

        val adsConfigVodHls2 = daiConfigVodHls2.enableDebugMode(true).setAlwaysStartWithPreroll(true)
        val adsConfigVodHls3 = daiConfigVodHls3.enableDebugMode(true).setAlwaysStartWithPreroll(true)
        val adsConfigVodHls4 = daiConfigVodHls4.enableDebugMode(true).setAlwaysStartWithPreroll(true)

        /* For MOAT call this API:
            List<View> overlaysList = new ArrayList<>();
            //overlaysList.add(....)
            adsConfigVodHls4.setControlsOverlayList(overlaysList);
        */

        //Set jsonObject to the main pluginConfigs object.
        pluginConfigs.setPluginConfig(IMADAIPlugin.factory.name, adsConfigVodHls1)

        /*
            NOTE!  for change media before player.prepare api please call:
            player.updatePluginConfig(IMADAIPlugin.factory.getName(), getDAIConfigVodHls2());
        */

        //Return created PluginConfigs object.
        return pluginConfigs
    }

    private fun getFriendlyViews() : MutableList<PKFriendlyObstruction> {
        var view1 = View(applicationContext);
        var view2 = View(applicationContext);
        var view3 = View(applicationContext);

        val friendlyObstructionView1 = PKFriendlyObstruction(view1, FriendlyObstructionPurpose.VIDEO_CONTROLS, "description1")
        val friendlyObstructionView2 = PKFriendlyObstruction(view2, FriendlyObstructionPurpose.NOT_VISIBLE, "description2")
        val friendlyObstructionView3 = PKFriendlyObstruction(view3, FriendlyObstructionPurpose.OTHER, "description3")
        val pkFriendlyObstructionList: MutableList<PKFriendlyObstruction> = mutableListOf<PKFriendlyObstruction>()

        pkFriendlyObstructionList.add(friendlyObstructionView1)
        pkFriendlyObstructionList.add(friendlyObstructionView2)
        pkFriendlyObstructionList.add(friendlyObstructionView3)

        return pkFriendlyObstructionList;
    }


    /**
     * Will subscribe to ad events.
     * For simplicity, in this example we will show subscription to the couple of events.
     * For the full list of ad events you can check our documentation.
     * !!!Note, we will receive only ad events, we subscribed to.
     */
    private fun subscribeToAdEvents() {

        player!!.addListener<AdEvent.AdStartedEvent>(this, AdEvent.started) { event ->
            //Some events holds additional data objects in them.
            //In order to get access to this object you need first cast event to
            //the object it belongs to. You can learn more about this kind of objects in
            //our documentation.

            //Then you can use the data object itself.
            val adInfo = event.adInfo

            //Print to log content type of this ad.
            Log.d(TAG, "ad event received: " + event.eventType().name
                    + ". Additional info: ad content type is: "
                    + adInfo.getAdContentType())
        }

        player!!.addListener(this, AdEvent.contentResumeRequested) { event -> Log.d(TAG, "ADS_PLAYBACK_ENDED") }

        player!!.addListener<AdEvent.AdPlaybackInfoUpdated>(this, AdEvent.adPlaybackInfoUpdated) { event ->
            Log.d(TAG, "AD_PLAYBACK_INFO_UPDATED  = " + event.width + "/" + event.height + "/" + event.bitrate)
        }

        player!!.addListener(this, AdEvent.skippableStateChanged) { event -> Log.d(TAG, "SKIPPABLE_STATE_CHANGED") }

        player!!.addListener<AdEvent.AdRequestedEvent>(this, AdEvent.adRequested) { event ->
            Log.d(TAG, "AD_REQUESTED adtag = " + event.adTagUrl)
        }

        player!!.addListener<AdEvent.AdPlayHeadEvent>(this, AdEvent.playHeadChanged) { event ->
            val adEventProress = event
            //Log.d(TAG, "received AD PLAY_HEAD_CHANGED " + adEventProress.adPlayHead);
        }


        player!!.addListener(this, AdEvent.adBreakStarted) { event -> Log.d(TAG, "AD_BREAK_STARTED") }

        player!!.addListener<AdEvent.AdCuePointsUpdateEvent>(this, AdEvent.cuepointsChanged) { event ->
            Log.d(TAG, "AD_CUEPOINTS_UPDATED HasPostroll = " + event.cuePoints.hasPostRoll())
        }

        player!!.addListener<AdEvent.AdLoadedEvent>(this, AdEvent.loaded) { event ->
            Log.d(TAG, "AD_LOADED " + event.adInfo.getAdIndexInPod() + "/" + event.adInfo.getTotalAdsInPod())
        }

        player!!.addListener<AdEvent.AdStartedEvent>(this, AdEvent.started) { event ->
            Log.d(TAG, "AD_STARTED w/h - " + event.adInfo.getAdWidth() + "/" + event.adInfo.getAdHeight())
        }

        player!!.addListener<AdEvent.AdResumedEvent>(this, AdEvent.resumed) { event -> Log.d(TAG, "AD_RESUMED") }

        player!!.addListener<AdEvent.AdPausedEvent>(this, AdEvent.paused) { event -> Log.d(TAG, "AD_PAUSED") }

        player!!.addListener<AdEvent.AdSkippedEvent>(this, AdEvent.skipped) { event -> Log.d(TAG, "AD_SKIPPED") }

        player!!.addListener(this, AdEvent.allAdsCompleted) { event -> Log.d(TAG, "AD_ALL_ADS_COMPLETED") }

        player!!.addListener(this, AdEvent.completed) { event -> Log.d(TAG, "AD_COMPLETED") }

        player!!.addListener(this, AdEvent.firstQuartile) { event -> Log.d(TAG, "FIRST_QUARTILE") }

        player!!.addListener(this, AdEvent.midpoint) { event ->
            Log.d(TAG, "MIDPOINT")
            if (player != null) {
                val adController = player!!.getController(AdController::class.java)
                if (adController != null) {
                    if (adController.isAdDisplayed) {
                        Log.d(TAG, "AD CONTROLLER API: " + adController.adCurrentPosition + "/" + adController.adDuration)
                    }
                    //Log.d(TAG, "adController.getCuePoints().getAdCuePoints().size());
                    //Log.d(TAG, adController.getAdInfo().toString());
                    //adController.skip();
                }
            }
        }

        player!!.addListener(this, AdEvent.thirdQuartile) { event -> Log.d(TAG, "THIRD_QUARTILE") }

        player!!.addListener(this, AdEvent.adBreakEnded) { event -> Log.d(TAG, "AD_BREAK_ENDED") }

        player!!.addListener<AdEvent.AdClickedEvent>(this, AdEvent.adClickedEvent) { event ->
            Log.d(TAG, "AD_CLICKED url = " + event.clickThruUrl)
        }

        player!!.addListener(this, AdEvent.error) { event ->
            Log.e(TAG, "AD_ERROR: " + event.error.errorType.name)
        }
    }

    private fun addPlayerStateListener() {
        player!!.addListener(this, PlayerEvent.error) { event -> Log.e(TAG, "PLAYER ERROR " + event.error.message!!) }

        player!!.addListener<PlayerEvent.StateChanged>(this, PlayerEvent.stateChanged) { event ->
            Log.d(TAG, "State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private fun addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById(R.id.play_pause_button)
        //Add clickListener.
        playPauseButton!!.setOnClickListener { v ->
            if (player != null) {
                val adController = player!!.getController(AdController::class.java)
                if (player!!.isPlaying || adController != null && adController.isAdDisplayed && adController.isAdPlaying) {
                    if (adController != null && adController.isAdDisplayed) {
                        adController.pause()
                    } else {
                        player!!.pause()
                    }
                    //If player is playing, change text of the button and pause.
                    playPauseButton!!.setText(R.string.play_text)
                } else {
                    if (adController != null && adController.isAdDisplayed) {
                        adController.play()
                    } else {
                        player!!.play()
                    }
                    //If player is not playing, change text of the button and play.
                    playPauseButton!!.setText(R.string.pause_text)
                }
            }
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        if (player != null) {
            if (playPauseButton != null) {
                playPauseButton!!.setText(R.string.pause_text)
            }
            player!!.onApplicationPaused()
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()

        if (player != null && playerState != null) {
            player!!.onApplicationResumed()
            player!!.play()
        }
    }

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setAllowCrossProtocolEnabled(true)

        // IMA DAI Configuration
        playerInitOptions.setPluginConfigs(createIMADAIPlugin())

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)
        //Subscribe to the ad events.
        subscribeToAdEvents()
        player!!.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player!!.playerView)

        val ottMediaOptions = buildOttMediaOptions()
        player!!.loadMedia(ottMediaOptions) { entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                Log.d(TAG, "OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }

        //Create plugin configurations.
        createIMADAIPlugin()

        //Add simple play/pause button.
        addPlayPauseButton()

        addPlayerStateListener()
    }

    private fun buildOttMediaOptions(): OTTMediaOptions {
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = ASSET_ID
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaAsset.ks = null
        ottMediaAsset.formats = listOf("Mobile_Main")

        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = START_POSITION


        return ottMediaOptions
    }

    companion object {

        //Tag for logging.
        private val TAG = MainActivity::class.java.simpleName

        private val START_POSITION = 0L // position for start playback in msec.

        //Media entry configuration constants.
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        private val ASSET_ID = "548576"
        val PARTNER_ID = 3009

        //Ad configuration constants.
        private val AD_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostpod&cmsid=496&vid=short_onecue&correlator="
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator=";
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";
        private val INCORRECT_AD_TAG_URL = "incorrect_ad_tag_url"
        private val PREFERRED_AD_BITRATE = 600
    }

}
