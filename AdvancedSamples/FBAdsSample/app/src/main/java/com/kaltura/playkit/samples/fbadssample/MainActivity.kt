package com.kaltura.playkit.samples.fbadssample

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.*
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ads.AdPositionType
import com.kaltura.playkit.plugins.fbads.fbinstream.FBInStreamAd
import com.kaltura.playkit.plugins.fbads.fbinstream.FBInStreamAdBreak
import com.kaltura.playkit.plugins.fbads.fbinstream.FBInstreamConfig
import com.kaltura.playkit.plugins.fbads.fbinstream.FBInstreamPlugin
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import java.util.*

class MainActivity : AppCompatActivity() {

    //Ad configuration constants.
    internal var preMidPostSingleAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator="

    private var player: KalturaPlayer? = null
    private var playPauseButton: Button? = null
    private var seekButton: Button? = null
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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


    /**
     * Just add a simple button which will start/pause playback.
     */
    private fun addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById(R.id.play_pause_button)
        //Add clickListener.
        playPauseButton!!.setOnClickListener { v ->
            player?.let {
                val adController =  it.getController(AdController::class.java)
                if (it.isPlaying || adController != null && adController.isAdPlaying) {
                    it.pause()
                    //If player is playing, change text of the button and pause.
                    playPauseButton!!.setText(R.string.play_text)
                } else {
                    it.play()
                    //If player is not playing, change text of the button and play.
                    playPauseButton!!.setText(R.string.pause_text)
                }
            }
        }
    }

    private fun addSeekButton() {

        seekButton = this.findViewById(R.id.seekTo0)
        seekButton!!.setOnClickListener { v ->
            if (player == null) {
                return@setOnClickListener
            }
            val adController = player?.getController(AdController::class.java)
            if (adController != null && adController.isAdPlaying) {
                return@setOnClickListener
            } else {
                player?.seekTo(0L)
            }
        }
    }

    private fun createFBInStreamPlugin(pluginConfigs: PKPluginConfigs): PKPluginConfigs {

        //First register your IMAPlugin.
        PlayKitManager.registerPlugins(this, FBInstreamPlugin.factory)

        addFBInStreamPluginConfig(pluginConfigs)

        //Return created PluginConfigs object.
        return pluginConfigs
    }

    private fun addFBInStreamPluginConfig(config: PKPluginConfigs) {
        val preRollFBInStreamAdList = ArrayList<FBInStreamAd>()
        val preRoll1 = FBInStreamAd("156903085045437_239184776817267", 0, 0)
        val preRoll2 = FBInStreamAd("156903085045437_239184776817267", 0, 1)
        preRollFBInStreamAdList.add(preRoll1)
        //preRollFBInStreamAdList.add(preRoll2);
        val preRollAdBreak = FBInStreamAdBreak(AdPositionType.PRE_ROLL, 0, preRollFBInStreamAdList)

        val midRoll1FBInStreamAdList = ArrayList<FBInStreamAd>()
        val midRoll1 = FBInStreamAd("156903085045437_239184776817267", 15000, 0)
        midRoll1FBInStreamAdList.add(midRoll1)
        val midRoll1AdBreak = FBInStreamAdBreak(AdPositionType.MID_ROLL, 15000, midRoll1FBInStreamAdList)


        val midRoll2FBInStreamAdList = ArrayList<FBInStreamAd>()
        val midRoll2 = FBInStreamAd("156903085045437_239184776817267", 30000, 0)
        midRoll2FBInStreamAdList.add(midRoll2)
        val midRoll2AdBreak = FBInStreamAdBreak(AdPositionType.MID_ROLL, 30000, midRoll2FBInStreamAdList)


        val postRollFBInStreamAdList = ArrayList<FBInStreamAd>()
        val postRoll1 = FBInStreamAd("156903085045437_239184776817267", java.lang.Long.MAX_VALUE, 0)
        postRollFBInStreamAdList.add(postRoll1)
        val postRollAdBreak = FBInStreamAdBreak(AdPositionType.POST_ROLL, java.lang.Long.MAX_VALUE, postRollFBInStreamAdList)

        val fbInStreamAdBreakList = ArrayList<FBInStreamAdBreak>()
        fbInStreamAdBreakList.add(preRollAdBreak)
        fbInStreamAdBreakList.add(midRoll1AdBreak)
        fbInStreamAdBreakList.add(midRoll2AdBreak)
        fbInStreamAdBreakList.add(postRollAdBreak)

        //"294d7470-4781-4795-9493-36602bf29231");//("7450a453-4ba6-464b-85b6-6f319c7f7326");
        val fbInstreamConfig = FBInstreamConfig(fbInStreamAdBreakList).enableDebugMode(true).setTestDevice("294d7470-4781-4795-9493-36602bf29231").setAlwaysStartWithPreroll(false)
        config.setPluginConfig(FBInstreamPlugin.factory.name, fbInstreamConfig)
    }


    private fun addPlayerStateListener() {
        player?.addListener<PlayerEvent.StateChanged>(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    override fun onPause() {
        log.d("onPause")
        super.onPause()
        player?.let { player->
            playPauseButton?.setText(R.string.pause_text)
            player.onApplicationPaused()
        }
    }

    override fun onResume() {
        log.d("onResume")
        super.onResume()

        player?.let { player ->
            playerState?.let {
                player.onApplicationResumed()
                player.play()
            }
        }
    }

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setAllowCrossProtocolEnabled(true)


        // FBAds Configuration
        val pkPluginConfigs = PKPluginConfigs()
        val setPlugins = createFBInStreamPlugin(pkPluginConfigs)
        playerInitOptions.setPluginConfigs(pkPluginConfigs)

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)

        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player?.playerView)

        val ottMediaOptions = buildOttMediaOptions()
        player?.loadMedia(ottMediaOptions) { entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }

        addPlayPauseButton()
        addSeekButton()
        showSystemUI()

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

    private fun subscribeToAdEvents() {

        player?.addListener<AdEvent.AdStartedEvent>(this, AdEvent.started) { event ->
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

        player?.addListener(this, AdEvent.contentResumeRequested) { event -> log.d("ADS_PLAYBACK_ENDED") }

        player?.addListener<AdEvent.AdPlaybackInfoUpdated>(this, AdEvent.adPlaybackInfoUpdated) { event ->
            log.d("AD_PLAYBACK_INFO_UPDATED  = " + event.width + "/" + event.height + "/" + event.bitrate)
        }

        player?.addListener(this, AdEvent.skippableStateChanged) { event -> log.d("SKIPPABLE_STATE_CHANGED") }

        player?.addListener<AdEvent.AdRequestedEvent>(this, AdEvent.adRequested) { event ->
            log.d("AD_REQUESTED adtag = " + event.adTagUrl)
        }

        player?.addListener<AdEvent.AdPlayHeadEvent>(this, AdEvent.playHeadChanged) { event ->
            val adEventProress = event
            //Log.d(TAG, "received AD PLAY_HEAD_CHANGED " + adEventProress.adPlayHead);
        }


        player?.addListener(this, AdEvent.adBreakStarted) { event -> log.d("AD_BREAK_STARTED") }

        player?.addListener<AdEvent.AdCuePointsUpdateEvent>(this, AdEvent.cuepointsChanged) { event ->
            log.d("AD_CUEPOINTS_UPDATED HasPostroll = " + event.cuePoints.hasPostRoll())
        }

        player?.addListener<AdEvent.AdLoadedEvent>(this, AdEvent.loaded) { event ->
            log.d("AD_LOADED " + event.adInfo.getAdIndexInPod() + "/" + event.adInfo.getTotalAdsInPod())
        }

        player?.addListener<AdEvent.AdStartedEvent>(this, AdEvent.started) { event ->
            log.d("AD_STARTED w/h - " + event.adInfo.getAdWidth() + "/" + event.adInfo.getAdHeight())
        }

        player?.addListener<AdEvent.AdResumedEvent>(this, AdEvent.resumed) { event -> log.d("AD_RESUMED") }

        player?.addListener<AdEvent.AdPausedEvent>(this, AdEvent.paused) { event -> log.d("AD_PAUSED") }

        player?.addListener<AdEvent.AdSkippedEvent>(this, AdEvent.skipped) { event -> log.d("AD_SKIPPED") }

        player?.addListener(this, AdEvent.allAdsCompleted) { event -> log.d("AD_ALL_ADS_COMPLETED") }

        player?.addListener(this, AdEvent.completed) { event -> log.d("AD_COMPLETED") }

        player?.addListener(this, AdEvent.firstQuartile) { event -> log.d("FIRST_QUARTILE") }

        player?.addListener(this, AdEvent.midpoint) { event ->
            log.d("MIDPOINT")
            if (player != null) {
                val adController = player?.getController(AdController::class.java)
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

        player?.addListener<AdEvent.AdClickedEvent>(this, AdEvent.adClickedEvent) { event ->
            log.d("AD_CLICKED url = " + event.clickThruUrl)
        }

        player?.addListener(this, AdEvent.error) { event ->
            log.d("AD_ERROR : " + event.error.errorType.name)
        }

        player?.addListener(this, PlayerEvent.error) { event -> log.d("PLAYER ERROR " + event.error.message!!) }
    }

    companion object {

        private val log = PKLog.get("MainActivity")

        private val START_POSITION = 0L // position for start playback in msec.

        //Media entry configuration constants.
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        private val ASSET_ID = "548576"
        val PARTNER_ID = 3009
    }
}
