package com.kaltura.playkit.samples.erhandling

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

import com.kaltura.playkit.PKPluginConfigs
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ima.IMAConfig
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOvpPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions

import java.util.ArrayList

class MainActivity: AppCompatActivity() {

    //Tag for logging.
    private val TAG = MainActivity::class.java.simpleName
    private val START_POSITION = 0L // position for start playback in msec.
    private val ASSET_ID = "548576"

    //Ad configuration constants.
    private val INCORRECT_AD_TAG_URL = "incorrect_ad_tag_url"

    private var player: KalturaPlayer? = null
    private var playPauseButton: Button? = null
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadPlaykitPlayer()

        addPlayPauseButton()

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
     * Will subscribe to the error events (in this example PlayerEvent.Type.ERROR and AdEvent.Type.ERROR).
     * For simplicity, in this example we will just print the errorType of the error that happened.
     * For the full list of errorTypes you can check our documentation.
     * !!!Note, we will receive only events, we subscribed to.
     */
    private fun subscribeToErrorEvents() {

        player!!.addListener(this, PlayerEvent.error) { event ->
            Log.e(TAG, "PLAYER ERROR " + event.error.errorType.name + " " + event.error.message)
        }

        player!!.addListener(this, AdEvent.error) { event ->
            Log.e(TAG, "AD_ERROR: " + event.error.errorType.name + " " + event.error.errorType.name)
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

    private fun addPlayerStateListener() {
        player!!.addListener(this, PlayerEvent.stateChanged) { event ->
            Log.d(TAG, "State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    override fun onResume() {
        super.onResume()
        if (player != null && playerState != null) {
            player!!.onApplicationResumed()
            player!!.play()
        }
    }

    override fun onPause() {
        super.onPause()
        if (player != null) {
            player!!.onApplicationPaused()
        }
    }

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setAllowCrossProtocolEnabled(true)


        // IMA Configuration
        val pkPluginConfigs = PKPluginConfigs()
        val adsConfig = getAdsConfig(INCORRECT_AD_TAG_URL)
        pkPluginConfigs.setPluginConfig(IMAPlugin.factory.name, adsConfig)

        playerInitOptions.setPluginConfigs(pkPluginConfigs)

        player = KalturaOvpPlayer.create(this@MainActivity, playerInitOptions)

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

        //Subscribe to the events.
        subscribeToErrorEvents()

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

    private fun getAdsConfig(adTagUrl: String): IMAConfig {
        val videoMimeTypes = ArrayList<String>()
        videoMimeTypes.add("video/mp4")
        videoMimeTypes.add("application/x-mpegURL")
        videoMimeTypes.add("application/dash+xml")
        return IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8)
    }

    companion object {
        //The url of the source to play
        val SERVER_URL = "incorrect_source_url" // "https://rest-us.ott.kaltura.com/v4_5/api_v3/";
        val PARTNER_ID = 3009
    }
}
