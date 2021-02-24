package com.kaltura.playkit.samples.dashthumbnailsample

import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.*
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.player.thumbnail.ImageRangeInfo
import com.kaltura.playkit.player.thumbnail.ThumbnailInfo
import com.kaltura.playkit.plugins.ads.AdCuePoints
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ima.IMAConfig
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.playkit.samples.dashthumbnailsample.preview.GetPreviewFromSprite
import com.kaltura.tvplayer.*
import com.kaltura.tvplayer.config.PhoenixTVPlayerParams
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    companion object {
        //Media entry configuration constants.
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        val PARTNER_ID = 3009
        val log = PKLog.get("MainActivity")
        var previewImageHashMap: HashMap<String, Bitmap> = HashMap()
        var previewImageWidth: Int? = null
        var slicesCount: Int? = null
        var currentlyPlayingMediaImageKey: String? = null
        val useOneshotSpriteDownloadPattern: Boolean = false

        fun getExtractedRectangle(thumbnailInfo: ThumbnailInfo?): RectF? {
            thumbnailInfo?.let {
                return RectF(it.x,
                        it.y,
                        it.x + it.width,
                        it.y + it.height)
            }
            return null
        }
    }

    //Basic Player Config
    //private val SOURCE_URL = "http://dash.edgesuite.net/akamai/bbb_30fps/bbb_with_tiled_thumbnails.mpd"
    //private val SOURCE_URL = "http://dash.edgesuite.net/akamai/bbb_30fps/bbb_with_tiled_thumbnails_2.mpd"
    private val SOURCE_URL = "http://dash.edgesuite.net/akamai/bbb_30fps/bbb_with_multiple_tiled_thumbnails.mpd"
    //private val SOURCE_URL = "http://dash.edgesuite.net/akamai/bbb_30fps/bbb_with_4_tiles_thumbnails.mpd"

    // Live
    // private val SOURCE_URL = "https://pf5.broadpeak-vcdn.com/bpk-tv/tvr/default/index.mpd?deviceType=32&subscriptionType=0&ip=87.71.183.190&primaryToken=b40b7407069a6e34_6c30217e28d77513f3a515d08078f5fe"

    private val MEDIA_FORMAT = PKMediaFormat.dash
    private val LICENSE_URL = null

    private val START_POSITION = 0L // position for start playback in msec.
    private val FIRST_ASSET_ID = "548576"
    private val SECOND_ASSET_ID = "548577"

    //Ad configuration constants.
    private var preMidPostSingleAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator="
    private var preMidPostAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostpodbumper&cmsid=496&vid=short_onecue&correlator="

    private var player: KalturaPlayer? = null
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null
    private var adCuePoints: AdCuePoints? = null
    private var isAdEnabled: Boolean = false
    private var buildUsingBasicPlayer = true
    private var previewImageHeight: Int? = null
    private lateinit var downloadSpriteImageCoroutine: GetPreviewFromSprite

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (useOneshotSpriteDownloadPattern) {
            downloadSpriteImageCoroutine = GetPreviewFromSprite(this)
        }

        if (buildUsingBasicPlayer) {
            val mediaEntry = createMediaEntry()
            loadPlaykitPlayer(mediaEntry)
        } else {
            loadPlaykitPlayer()
        }

        activity_main.setOnClickListener { v ->
            if (isFullScreen) {
                showSystemUI()
            } else {
                hideSystemUI()
            }
        }

        btnChangeMedia.setOnClickListener { v ->

            player?.let { it ->
                it.mediaEntry?.let {
                    if (it.id == FIRST_ASSET_ID) {
                        if (isAdEnabled) {
                            val adsConfig = getAdsConfig(preMidPostAdTagUrl)
                            player?.updatePluginConfig(IMAPlugin.factory.name, adsConfig)
                        }

                        previewImageWidth = 150
                        previewImageHeight = 84
                        slicesCount = 100

                        buildSecondOttMediaOptions()
                    } else {
                        if (isAdEnabled) {
                            val adsConfig = getAdsConfig(preMidPostSingleAdTagUrl)
                            player?.updatePluginConfig(IMAPlugin.factory.name, adsConfig)
                        }

                        previewImageWidth = 90
                        previewImageHeight = 50
                        slicesCount = 100

                        buildFirstOttMediaOptions()
                    }
                }
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

    private fun downloadPreviewImage(thumbnailInfo: ThumbnailInfo?) {
        thumbnailInfo?.let {
            downloadSpriteImageCoroutine.downloadSpriteCoroutine(thumbnailInfo, currentlyPlayingMediaImageKey!!)
        }
    }

    private fun addPlayerStateListener() {
        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }

        player?.addListener(this, PlayerEvent.error) { event ->
            log.d("player ERROR " + event.error.message)
        }

        player?.addListener(this, PlayerEvent.imageTrackChanged) { event ->
            log.d("imageTemplateUrl " + event.newTrack.imageTemplateUrl)
        }

        player?.addListener(this, PlayerEvent.tracksAvailable) { event ->
            if (!event.tracksInfo.getImageTracks().isEmpty()) {
                slicesCount = player?.vodThumbnailInfo?.imageRangeThumbnailMap?.size
                if (useOneshotSpriteDownloadPattern) {
                    player?.let {
                        extractTileImagesFromSprite(it.vodThumbnailInfo.imageRangeThumbnailMap)
                    }
                }
            }
        }

        player?.addListener(this, PlayerEvent.imageTrackChanged) { event ->
            currentlyPlayingMediaImageKey = event.newTrack.label
        }
    }

    private fun extractTileImagesFromSprite(imageRangeThumbnailMap: Map<ImageRangeInfo, ThumbnailInfo>?) {
        imageRangeThumbnailMap?.let {
            for ((imageRangeInfo, thumbnailInfo) in imageRangeThumbnailMap) {
                downloadPreviewImage(thumbnailInfo)
            }
        }
    }

    override fun onPause() {
        log.d("onPause")
        super.onPause()

        if (playerControls != null) {
            playerControls?.release()
        }

        downloadSpriteImageCoroutine.terminateService()

        player?.onApplicationPaused()
    }

    override fun onResume() {
        log.d("onResume")
        super.onResume()

        if (playerControls != null) {
            playerControls?.resume()
        }

        if (player != null && playerState != null) {
            player?.onApplicationResumed()
            player?.play()
        }
    }

    public override fun onDestroy() {
        if (player != null) {
            player?.removeListeners(this)
            player?.destroy()
            player = null
        }
        super.onDestroy()
    }

    fun loadPlaykitPlayer() {

        previewImageWidth = 90
        previewImageHeight = 50
        slicesCount = 100


        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setAllowCrossProtocolEnabled(true)

        // IMA Configuration
        val pkPluginConfigs = PKPluginConfigs()

        if (isAdEnabled) {
            val adsConfig = getAdsConfig(preMidPostSingleAdTagUrl)
            pkPluginConfigs.setPluginConfig(IMAPlugin.factory.name, adsConfig)
            playerInitOptions.setPluginConfigs(pkPluginConfigs)
        }

        if (PARTNER_ID == 198) {
            val phoenixTVPlayerParams = PhoenixTVPlayerParams()
            phoenixTVPlayerParams.analyticsUrl = "https://analytics.kaltura.com"
            phoenixTVPlayerParams.ovpPartnerId = 1774581
            phoenixTVPlayerParams.partnerId = 198
            phoenixTVPlayerParams.serviceUrl = "https://api-preprod.ott.kaltura.com/v5_2_8/"
            phoenixTVPlayerParams.ovpServiceUrl = "http://cdnapi.kaltura.com/"
            playerInitOptions?.tvPlayerParams = phoenixTVPlayerParams
        }

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)

        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        subscribeToAdEvents()

        player_root.addView(player?.playerView)

        playerControls?.setPlayer(player)

        buildFirstOttMediaOptions()

        showSystemUI()

        addPlayerStateListener()
    }

    private fun buildFirstOttMediaOptions() {
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = FIRST_ASSET_ID
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaAsset.ks = null

        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = START_POSITION

        player?.loadMedia(ottMediaOptions) { entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.id)
                //   entry?.metadata?.get("entryId")?.let { downloadPreviewImage(previewImageWidth ?: 150, previewImageHeight ?: 84, slicesCount ?: 100, it) }
            }
        }

    }

    private fun buildSecondOttMediaOptions() {
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = SECOND_ASSET_ID
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaAsset.ks = null

        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = START_POSITION

        player?.loadMedia(ottMediaOptions) { entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.id)
                // entry?.metadata?.get("entryId")?.let { downloadPreviewImage(previewImageWidth ?: 150, previewImageHeight ?: 84, slicesCount ?: 100, it) }
            }
        }
    }

    private fun getAdsConfig(adTagUrl: String): IMAConfig {
        val videoMimeTypes = ArrayList<String>()
        videoMimeTypes.add("video/mp4")
        videoMimeTypes.add("application/x-mpegURL")
        videoMimeTypes.add("application/dash+xml")
        return IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8)
    }

    private fun subscribeToAdEvents() {

        player?.addListener(this, AdEvent.started) { event ->
            //Some events holds additional data objects in them.
            //In order to get access to this object you need first cast event to
            //the object it belongs to. You can learn more about this kind of objects in
            //our documentation.

            //Then you can use the data object itself.
            val adInfo = event.adInfo

            //Print to log content type of this ad.
            log.d("ad event received: " + event.eventType().name
                    + ". Additional info: ad content type is: "
                    + adInfo.getAdContentType())
        }

        player?.addListener(this, AdEvent.contentResumeRequested) { event ->
            log.d("ADS_PLAYBACK_ENDED")
            playerControls?.setSeekBarStateForAd(false)
            playerControls?.setPlayerState(PlayerState.READY)
        }

        player?.addListener(this, AdEvent.contentPauseRequested) { event ->
            log.d("AD_CONTENT_PAUSE_REQUESTED")
            playerControls?.setSeekBarStateForAd(true)
            playerControls?.setPlayerState(PlayerState.READY)
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
            log.d("AD_CUEPOINTS_UPDATED HasPostroll = " + event.cuePoints.hasPostRoll())
            adCuePoints = event.cuePoints
            if (adCuePoints != null) {
                log.d("Has Postroll = " + adCuePoints?.hasPostRoll())
            }
        }

        player?.addListener(this, AdEvent.loaded) { event ->
            log.d("AD_LOADED " + event.adInfo.getAdIndexInPod() + "/" + event.adInfo.getTotalAdsInPod())
        }

        player?.addListener(this, AdEvent.started) { event ->
            log.d("AD_STARTED w/h - " + event.adInfo.getAdWidth() + "/" + event.adInfo.getAdHeight())
        }

        player?.addListener(this, AdEvent.resumed) { event -> log.d("AD_RESUMED") }

        player?.addListener(this, AdEvent.paused) { event -> log.d("AD_PAUSED") }

        player?.addListener(this, AdEvent.skipped) { event -> log.d("AD_SKIPPED") }

        player?.addListener(this, AdEvent.allAdsCompleted) { event ->
            log.d("AD_ALL_ADS_COMPLETED")
            if (adCuePoints != null && adCuePoints?.hasPostRoll()!!) {
                playerControls?.setPlayerState(PlayerState.IDLE)
            }
        }

        player?.addListener(this, AdEvent.completed) { event -> log.d("AD_COMPLETED") }

        player?.addListener(this, AdEvent.firstQuartile) { event -> log.d("FIRST_QUARTILE") }

        player?.addListener(this, AdEvent.midpoint) { event ->
            log.d("MIDPOINT")

            player?.let {
                val adController = it.getController(AdController::class.java)
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
            if (event != null && event.error != null) {
                playerControls?.setSeekBarStateForAd(false)
                log.e("ERROR: " + event.error.errorType + ", " + event.error.message)
            }
        }
    }

    fun loadPlaykitPlayer(pkMediaEntry: PKMediaEntry) {
        val playerInitOptions = PlayerInitOptions()

        player = KalturaBasicPlayer.create(this@MainActivity, playerInitOptions)
        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        val container = player_root
        container.addView(player?.playerView)
        playerControls?.setPlayer(player)
        player?.setMedia(pkMediaEntry, START_POSITION)

        showSystemUI()

        addPlayerStateListener()
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
        mediaEntry.id = "thumbnailEntry"

        //Set media entry type. It could be Live,Vod or Unknown.
        //In this sample we use Vod.
        //mediaEntry.mediaType = PKMediaEntry.MediaEntryType.Live
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

        //Create new PKMediaSource instance.
        val mediaSource = PKMediaSource()

        //Set the id.
        mediaSource.id = "testLiveSource"

        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.url = SOURCE_URL

        //Set the format of the source. In our case it will be hls in case of mpd/wvm formats you have to to call mediaSource.setDrmData method as well
        mediaSource.mediaFormat = MEDIA_FORMAT

        // Add DRM data if required
        if (LICENSE_URL != null) {
            mediaSource.drmData = listOf(PKDrmParams(LICENSE_URL, PKDrmParams.Scheme.WidevineCENC))
        }

        return listOf(mediaSource)
    }
}
