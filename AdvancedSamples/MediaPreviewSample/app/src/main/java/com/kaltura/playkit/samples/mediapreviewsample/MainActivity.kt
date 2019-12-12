package com.kaltura.playkit.samples.mediapreviewsample

import android.os.Build
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity

import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout

import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKPluginConfigs
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.plugins.ads.AdCuePoints
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ima.IMAConfig
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions

import java.util.ArrayList


class MainActivity : AppCompatActivity() {

    private val log = PKLog.get("MainActivity")

    private val START_POSITION = 0L // position for start playback in msec.
    private val ASSET_ID = "548576"

    //Ad configuration constants.
    internal var preMidPostSingleAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator="

    private var player: KalturaPlayer? = null
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null
    private var controlsView: PlaybackControlsView? = null
    private var adCuePoints: AdCuePoints? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        controlsView = findViewById(R.id.playerControls);

        loadPlaykitPlayer()

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

    private fun addPlayerStateListener() {
        player!!.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }

        player!!.addListener(this, PlayerEvent.error) { event -> log.d("PLAYER ERROR " + event.error.message!!) }
    }

    override fun onPause() {
        log.d("onPause")
        super.onPause()

        if (controlsView != null) {
            controlsView!!.release()
        }

        if (player != null) {
            player!!.onApplicationPaused()
        }
    }

    override fun onResume() {
        log.d("onResume")
        super.onResume()

        if (controlsView != null) {
            controlsView!!.resume()
        }

        if (player != null && playerState != null) {
            player!!.onApplicationResumed()
            player!!.play()
        }
    }

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setAllowCrossProtocolEnabled(true)


        // IMA Configuration
        val pkPluginConfigs = PKPluginConfigs()
        val adsConfig = getAdsConfig(preMidPostSingleAdTagUrl)
        pkPluginConfigs.setPluginConfig(IMAPlugin.factory.name, adsConfig)

        playerInitOptions.setPluginConfigs(pkPluginConfigs)

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)

        player!!.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        subscribeToAdEvents()
        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player!!.playerView)

        val ottMediaOptions = buildOttMediaOptions()
        player!!.loadMedia(ottMediaOptions) { entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }

        controlsView!!.setPlayer(player!!)

        showSystemUI()

        addPlayerStateListener()
    }

    private fun buildOttMediaOptions(): OTTMediaOptions {
        val ottMediaOptions = OTTMediaOptions()
        ottMediaOptions.assetId = ASSET_ID
        ottMediaOptions.assetType = APIDefines.KalturaAssetType.Media
        ottMediaOptions.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaOptions.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaOptions.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaOptions.ks = null
        ottMediaOptions.startPosition = START_POSITION
        ottMediaOptions.formats = arrayOf("Mobile_Main")

        return ottMediaOptions
    }

    private fun getAdsConfig(adTagUrl: String): IMAConfig {
        val videoMimeTypes = ArrayList<String>()
        videoMimeTypes.add("video/mp4")
        videoMimeTypes.add("application/x-mpegURL")
        videoMimeTypes.add("application/dash+xml")
        return IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8)
    }

    private fun subscribeToAdEvents() {

        player!!.addListener(this, AdEvent.started) { event ->
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

        player!!.addListener(this, AdEvent.contentResumeRequested) {
            event -> log.d("ADS_PLAYBACK_ENDED")
            controlsView!!.setSeekBarStateForAd(false)
            controlsView!!.setPlayerState(PlayerState.READY)
        }

        player!!.addListener(this, AdEvent.contentPauseRequested) { event ->
            log.d("AD_CONTENT_PAUSE_REQUESTED")
            controlsView!!.setSeekBarStateForAd(true)
            controlsView!!.setPlayerState(PlayerState.READY)
        }

        player!!.addListener(this, AdEvent.adPlaybackInfoUpdated) { event ->
            log.d("AD_PLAYBACK_INFO_UPDATED  = " + event.width + "/" + event.height + "/" + event.bitrate)
        }

        player!!.addListener(this, AdEvent.skippableStateChanged) { event -> log.d("SKIPPABLE_STATE_CHANGED") }

        player!!.addListener(this, AdEvent.adRequested) { event ->
            log.d("AD_REQUESTED adtag = " + event.adTagUrl)
        }

        player!!.addListener(this, AdEvent.playHeadChanged) { event ->
            val adEventProress = event
            //Log.d(TAG, "received AD PLAY_HEAD_CHANGED " + adEventProress.adPlayHead);
        }


        player!!.addListener(this, AdEvent.adBreakStarted) { event -> log.d("AD_BREAK_STARTED") }

        player!!.addListener(this, AdEvent.cuepointsChanged) { event ->
            log.d("AD_CUEPOINTS_UPDATED HasPostroll = " + event.cuePoints.hasPostRoll())
            adCuePoints = event.cuePoints
            if (adCuePoints != null) {
                log.d("Has Postroll = " + adCuePoints!!.hasPostRoll())
            }
        }

        player!!.addListener(this, AdEvent.loaded) { event ->
            log.d("AD_LOADED " + event.adInfo.getAdIndexInPod() + "/" + event.adInfo.getTotalAdsInPod())
        }

        player!!.addListener(this, AdEvent.started) { event ->
            log.d("AD_STARTED w/h - " + event.adInfo.getAdWidth() + "/" + event.adInfo.getAdHeight())
        }

        player!!.addListener(this, AdEvent.resumed) { event -> log.d("AD_RESUMED") }

        player!!.addListener(this, AdEvent.paused) { event -> log.d("AD_PAUSED") }

        player!!.addListener(this, AdEvent.skipped) { event -> log.d("AD_SKIPPED") }

        player!!.addListener(this, AdEvent.allAdsCompleted) {
            event -> log.d("AD_ALL_ADS_COMPLETED")
            if (adCuePoints != null && adCuePoints!!.hasPostRoll()) {
                controlsView!!.setPlayerState(PlayerState.IDLE)
            }
        }

        player!!.addListener(this, AdEvent.completed) { event -> log.d("AD_COMPLETED") }

        player!!.addListener(this, AdEvent.firstQuartile) { event -> log.d("FIRST_QUARTILE") }

        player!!.addListener(this, AdEvent.midpoint) { event ->
            log.d("MIDPOINT")
            if (player != null) {
                val adController = player!!.getController(AdController::class.java)
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

        player!!.addListener(this, AdEvent.thirdQuartile) { event -> log.d("THIRD_QUARTILE") }

        player!!.addListener(this, AdEvent.adBreakEnded) { event -> log.d("AD_BREAK_ENDED") }

        player!!.addListener(this, AdEvent.adClickedEvent) { event ->
            log.d("AD_CLICKED url = " + event.clickThruUrl)
        }

        player!!.addListener(this, AdEvent.error) { event ->
            log.d("AD_ERROR : " + event.error.errorType.name)
            if (event != null && event.error != null) {
                controlsView!!.setSeekBarStateForAd(false)
                log.e("ERROR: " + event.error.errorType + ", " + event.error.message)
            }
        }
    }

    companion object {
        //Media entry configuration constants.
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        val PARTNER_ID = 3009
    }
}
