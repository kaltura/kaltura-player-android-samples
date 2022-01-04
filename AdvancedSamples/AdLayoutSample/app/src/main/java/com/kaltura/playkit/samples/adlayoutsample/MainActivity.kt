package com.kaltura.playkit.samples.adlayoutsample

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.*
import com.kaltura.playkit.ads.*
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ima.IMAConfig
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), PlaybackControlsView.ChangeMediaListener {

    private val log = PKLog.get("MainActivity")

    private val FIRST_ASSET_ID = "548576"
    private val SECOND_ASSET_ID = "548577"
    private val START_POSITION = 0L // position for start playback in msec.
    private var player: KalturaPlayer? = null
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null
    private var preMidPostSingleAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator="

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadPlaykitPlayer()

        hideSystemUI()

        activity_main.setOnClickListener { v ->
            if (isFullScreen) {
                //   showSystemUI()
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

    private fun addPlayerStateListener() {
        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    /**
     * Will switch between entries. If the first entry is currently active it will
     * prepare the second one. Otherwise it will prepare the first one.
     */
    override fun changeMediaOnClick() {

        //Check if id of the media entry that is set in mediaConfig.
        if (player?.mediaEntry?.id == FIRST_ASSET_ID) {
            //If first one is active, prepare second one.
            prepareSecondEntry()
        } else {
            //If the second one is active, prepare the first one.
            prepareFirstEntry()
        }
    }

    /**
     * Prepare the first entry.
     */
    private fun prepareFirstEntry() {
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = FIRST_ASSET_ID
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaAsset.ks = null
        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = START_POSITION

        player?.setAdvertisingConfig(getAdvertisingConfigForFirstEntry())

        player?.loadMedia(ottMediaOptions) { mediaOptions, entry, error ->
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }
    }

    /**
     * Example for AdvertisingConfig as an object
     */
    private fun getAdvertisingConfigForFirstEntry(): AdvertisingConfig {

        // Preroll Ad, here list of ads denotes that it has waterfalling ads
        // Means if first ad fails to play then next ad will be picked from the list.
        // In case, if App adds only one VAST ad url in list, it means that
        // there is no Waterfalling ad available
        val prerollVastUrlListWithWaterfallingAd1 = listOf("https://kaltura.github.io/playkit-admanager-samplasdasdasdases/vast/pod-inline-someskip.xml",
            "https://pubaasdasdasdasdasdasdasdasdsg.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
            "https://pubaasndjknasjdnasds.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
            "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")

        // Preroll AdPod Configuration
        val prerollAdPod = listOf(prerollVastUrlListWithWaterfallingAd1)
        // Preroll AdBreak,For preroll position should be 0
        val prerollAdBreak = AdBreak(AdBreakPositionType.POSITION, 0, prerollAdPod)

        val midrollVastUrlList = listOf("https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml",
            "https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml",
            "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&cue=15000&vad_type=linear&vpos=midroll&pod=2&mridx=1&rmridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")

        // Midroll Configuration. For testing we are playing the same ad on 15, 45, 60, 90 and 120 seconds
        // For millisecond, pass AdTimeUnit.MILISECONDS while creating AdvertisingConfig object but then position values
        // should be 15000, 45000, 60000, 90000, 120000 respectively.

        // Order is not important for any ad break.
        val midrollAdBreak1 = AdBreak(AdBreakPositionType.POSITION, 15, listOf(midrollVastUrlList))
        val midrollAdBreak2 = AdBreak(AdBreakPositionType.POSITION,45, listOf(midrollVastUrlList))
        val midrollAdBreak3 = AdBreak(AdBreakPositionType.POSITION, 60, listOf(midrollVastUrlList))
        val midrollAdBreak4 = AdBreak(AdBreakPositionType.POSITION, 90, listOf(midrollVastUrlList))
        val midrollAdBreak5 = AdBreak(AdBreakPositionType.POSITION, 120, listOf(midrollVastUrlList))

        // Postroll ad configuration, For postroll position should be -1
        val postrollVastUrlList = listOf("https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=postroll&pod=3&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")
        val postrollAdBreak = AdBreak(AdBreakPositionType.POSITION, -1, listOf(postrollVastUrlList))

        return AdvertisingConfig(listOf(
            prerollAdBreak,
            midrollAdBreak1,
            midrollAdBreak2,
            midrollAdBreak3,
            midrollAdBreak4,
            midrollAdBreak5,
            postrollAdBreak),
            AdTimeUnit.SECONDS) // App can pass AdTimeUnit in Second or Millisecond.
                                // This time should be mention in the AdBreak
    }

    /**
     * Prepare the second entry.
     */
    private fun prepareSecondEntry() {
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = SECOND_ASSET_ID
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaAsset.ks = null
        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)

        ottMediaOptions.startPosition = START_POSITION

        player?.setAdvertisingConfig(getAdvertisingConfigForSecondEntry())

        player?.loadMedia(ottMediaOptions) { mediaOptions, entry, error ->
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }
    }

    /**
     * Example for AdvertisingConfig as JSON object
     */
    private fun getAdvertisingConfigForSecondEntry(): String {
        return "{\n" +
                "  \"advertising\": [\n" +
                "    {\n" +
                "      \"adBreakPositionType\": \"POSITION\",\n" +
                "      \"position\": 0,\n" +
                "      \"ads\": [\n" +
                "        [\n" +
                "          \"https://externaltests.dev.kaltura.com/standalonePlayer/Ads/adManager/customAdTags/vast/single_preroll_skip_wildlife.xml\"\n" +
                "        ],\n" +
                "        [\n" +
                "          \"https://externaltests.dev.kaltura.com/standalonePlayer/Ads/adManager/customAdTags/vast/single_preroll_skip_terminator.xml\",\n" +
                "          \"https://externaltests.dev.kaltura.com/standalonePlayer/Ads/adManager/customAdTags/vast/single_preroll_skip_transformers.xml\"\n" +
                "        ],\n" +
                "        [\n" +
                "          \"https://externaltests.dev.kaltura.com/standalonePlayer/Ads/adManager/customAdTags/vast/single_preroll_skip_hdVideo.xml\"\n" +
                "        ]\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"adBreakPositionType\": \"POSITION\",\n" +
                "      \"position\": 30,\n" +
                "      \"ads\": [\n" +
                "        [\n" +
                "          \"https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml\",\n" +
                "          \"https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml\",\n" +
                "          \"https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&cue=15000&vad_type=linear&vpos=midroll&pod=2&mridx=1&rmridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0\"\n" +
                "        ]\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"adBreakPositionType\": \"POSITION\",\n" +
                "      \"position\": 15,\n" +
                "      \"ads\": [\n" +
                "        [\n" +
                "          \"https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml\",\n" +
                "          \"https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml\",\n" +
                "          \"https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&cue=15000&vad_type=linear&vpos=midroll&pod=2&mridx=1&rmridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0\"\n" +
                "        ]\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"adBreakPositionType\": \"POSITION\",\n" +
                "      \"position\": -1,\n" +
                "      \"ads\": [\n" +
                "        [\n" +
                "          \"https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=postroll&pod=3&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0\"\n" +
                "        ]\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"adTimeUnit\": \"SECONDS\"\n" +
                "}"
    }

    override fun onResume() {
        super.onResume()
        playerControls?.resume()
        player?.let {
            if (it.mediaEntry != null) {
                it.onApplicationResumed()
                it.play()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.destroy()
    }

    override fun onPause() {
        super.onPause()
        playerControls?.release()
        player?.onApplicationPaused()
    }

    fun loadPlaykitPlayer() {
        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setPKRequestConfig(PKRequestConfig(true))

        val pkPluginConfigs = PKPluginConfigs()
        val adsConfig = getAdsConfig("")
        pkPluginConfigs.setPluginConfig(IMAPlugin.factory.name, adsConfig)

        playerInitOptions.setPluginConfigs(pkPluginConfigs)
        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)
        player?.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)

        subscribeToAdEvents()

        val container = player_root
        container.addView(player?.playerView)

        playerControls?.setPlayer(player)
        playerControls.setChangeMediaListener(this)

        //Prepare the first entry.
        prepareFirstEntry()

        addPlayerStateListener()

        btn_play_ad_now?.setOnClickListener {
            playAdNowApiTest()
        }

    }

    /**
     * Play Ad Now API
     */
    fun playAdNowApiTest() {
        player?.advertisingController?.playAdNow(getPlayAdNowJSON())
    }

    /**
     * Application can pass either AdBreak Object or JSON
     * Here we are sending JSON AdBreak
     */
    fun getPlayAdNowJSON(): String {
        return "{\n" +
                "  \"adBreakPositionType\": \"POSITION\",\n" +
                "  \"position\": 15,\n" +
                "  \"ads\": [\n" +
                "    [\n" +
                "      \"https://kaltura.github.io/playkit-admanager-samples/vast/pod-inline-someskip.xml\"\n" +
                "    ]\n" +
                "  ]\n" +
                "}"
    }

    private fun getAdsConfig(adTagUrl: String): IMAConfig {
        val videoMimeTypes = ArrayList<String>()
        videoMimeTypes.add("video/mp4")
        videoMimeTypes.add("application/x-mpegURL")
        videoMimeTypes.add("application/dash+xml")
        return IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8)
    }

    private fun subscribeToAdEvents() {

        player?.addListener(this, AdEvent.adWaterFalling) { event ->
            log.d("adInfo callback from IMAPlugin: AdEvent.adWaterFalling \n " +
                    "${event.adBreakConfig}")
        }

        player?.addListener(this, AdEvent.adWaterFallingFailed) { event ->
            log.w("adInfo callback from IMAPlugin: AdEvent.adWaterFallingFailed \n" +
                    "${event.adBreakConfig}")
        }

        player?.addListener(this, AdEvent.error) { event ->
            log.e("adInfo callback from IMAPlugin: AdEvent.error \n" +
                    "${event.error}")
        }

        player?.addListener(this, AdEvent.adBreakFetchError) { event ->
            log.e("adInfo callback from IMAPlugin: AdEvent.adBreakFetchError \n" +
                    "${event.eventType()}")
        }

        player?.addListener(this, AdEvent.started) { event ->
            //Some events holds additional data objects in them.
            //In order to get access to this object you need first cast event to
            //the object it belongs to. You can learn more about this kind of objects in
            //our documentation.

            //Then you can use the data object itself.
            val adInfo = event.adInfo
            //Print to log content type of this ad.
            log.d("adInfo callback from IMAPlugin adInfo: $adInfo")

            log.d("ad event received: " + event.eventType().name
                    + ". Additional info: ad content type is: "
                    + adInfo.getAdContentType())

        }

        player?.addListener(this, AdEvent.contentResumeRequested) {
            log.d("ADS_PLAYBACK_ENDED")
            playerControls?.visibility = View.VISIBLE
            playerControls?.setSeekBarStateForAd(false)
            playerControls?.setPlayerState(PlayerState.READY)
        }

        player?.addListener(this, AdEvent.adBufferStart) {
            log.d("AdEvent.adBufferStart")
            progressBar.visibility = View.VISIBLE
        }

        player?.addListener(this, AdEvent.adBufferEnd) {
            log.d("AdEvent.adBreakEnded")
            progressBar.visibility = View.GONE
        }

        player?.addListener(this, AdEvent.contentPauseRequested) {
            log.d("AD_CONTENT_PAUSE_REQUESTED")
            playerControls?.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.adPlaybackInfoUpdated) { event ->
            log.d("AD_PLAYBACK_INFO_UPDATED  = " + event.width + "/" + event.height + "/" + event.bitrate)
        }

        player?.addListener(this, AdEvent.skippableStateChanged) { event -> log.d("SKIPPABLE_STATE_CHANGED") }

        player?.addListener(this, AdEvent.adRequested) { event ->
            log.d("AD_REQUESTED adtag = " + event.adTagUrl)
        }

        player?.addListener(this, AdEvent.playHeadChanged) { event ->
            val adEventProress = event
            //Log.d(TAG, "received AD PLAY_HEAD_CHANGED " + adEventProress.adPlayHead);
        }

        player?.addListener(this, AdEvent.adBreakStarted) { event -> log.d("AD_BREAK_STARTED") }

        player?.addListener(this, AdEvent.cuepointsChanged) { event ->
            log.d("AD_CUEPOINTS_UPDATED")
        }

        player?.addListener(this, AdEvent.loaded) { event ->
            log.d("AD_LOADED " + event.adInfo.getAdIndexInPod() + "/" + event.adInfo.getTotalAdsInPod())
            //   player?.advertisingController?.playAdNow(getPlayAdNowConfigAdBreak())
        }

        player?.addListener(this, AdEvent.resumed) { event -> log.d("AD_RESUMED") }

        player?.addListener(this, AdEvent.paused) { event -> log.d("AD_PAUSED") }

        player?.addListener(this, AdEvent.skipped) { event -> log.d("AD_SKIPPED") }

        player?.addListener(this, AdEvent.allAdsCompleted) {
                event -> log.d("AD_ALL_ADS_COMPLETED")
        }

        player?.addListener(this, AdEvent.completed) { event -> log.d("AD_COMPLETED") }

        player?.addListener(this, AdEvent.firstQuartile) { event -> log.d("FIRST_QUARTILE") }

        player?.addListener(this, AdEvent.midpoint) { event ->
            log.d("MIDPOINT")
            if (player != null) {
                val adController =  player?.getController(AdController::class.java)
                if (adController != null) {
                    if (adController.isAdDisplayed) {
                        log.d("AD CONTROLLER API: " + adController.adCurrentPosition + "/" + adController.adDuration)
                    }
                    //Log.d(TAG, "adController.getCuePoints().getAdCuePoints().size());
                    //Log.d(TAG, adController.getAdInfo().toString());
                    //adController.skip();
                }
            }
        }

        player?.addListener(this, AdEvent.thirdQuartile) { event -> log.d("THIRD_QUARTILE") }

        player?.addListener(this, AdEvent.adBreakEnded) { event -> log.d("AD_BREAK_ENDED") }

        player?.addListener(this, AdEvent.adClickedEvent) { event ->
            log.d("AD_CLICKED url = " + event.clickThruUrl)
        }

        player?.addListener(this, AdEvent.error) { event ->
            log.d("AD_ERROR : " + event.error.errorType.name)
            if (event?.error != null) {
                playerControls?.setSeekBarStateForAd(false)
                log.e("ERROR: " + event.error.errorType + ", " + event.error.message)
            }
        }

        player?.addListener(this, PlayerEvent.error) { event -> log.d("PLAYER ERROR " + event.error.message!!) }

        player?.addListener(this, PlayerEvent.loadedMetadata) {
            log.d("PLAYER loadedMetadata ")
        }

        //player?.addListener(this, PlayerEvent.me)
    }

    companion object {
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        val PARTNER_ID = 3009
    }
}
