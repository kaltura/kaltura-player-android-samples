package com.kaltura.playkit.samples.broadpeak

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PKPluginConfigs
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.plugins.broadpeak.BroadpeakConfig
import com.kaltura.playkit.plugins.broadpeak.BroadpeakEvent
import com.kaltura.playkit.plugins.broadpeak.BroadpeakPlugin
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.*

class MainActivity : AppCompatActivity() {

    //Tag for logging.
    private val TAG = MainActivity::class.java.simpleName

    private val START_POSITION = 0L // position for start playback in msec.

    companion object {
        const val SERVER_URL = "phoenixUrl"
        const val ASSET_ID = "assetId"
        const val PARTNER_ID = 111111111
        const val KS = ""
        const val MEDIA_FORMAT = "FORMAT"
    }

    private var player: KalturaPlayer? = null
    private var playPauseButton: Button? = null
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
        playPauseButton = this.findViewById<View>(R.id.play_pause_button) as Button
        //Add clickListener.
        playPauseButton?.setOnClickListener {
            if (player!!.isPlaying) {
                //If player is playing, change text of the button and pause.
                playPauseButton?.setText(R.string.play_text)
                player?.pause()
            } else {
                //If player is not playing, change text of the button and play.
                playPauseButton?.setText(R.string.pause_text)
                player?.play()
            }
        }
    }

    private fun addPlayerStateListener() {
        player?.addListener<PlayerEvent.StateChanged>(this, PlayerEvent.stateChanged) { event ->
            Log.d(TAG, "State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        if (player != null) {
            if (playPauseButton != null) {
                playPauseButton?.setText(R.string.pause_text)
            }
            player?.onApplicationPaused()
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()

        if (player != null && playerState != null) {
            player?.onApplicationResumed()
            player?.play()
        }
    }

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setAllowCrossProtocolEnabled(true)

        // Broadpeak Configuration
        val pkPluginConfigs = PKPluginConfigs()
        val broadpeakConfig = BroadpeakConfig().apply {
            analyticsAddress = "https://analytics.kaltura.com/api_v3/index.php"
            nanoCDNHost = "cdnapisec.kaltura.com"
            broadpeakDomainNames = "*"
        }

        pkPluginConfigs.setPluginConfig(BroadpeakPlugin.factory.name, broadpeakConfig)
        playerInitOptions.setPluginConfigs(pkPluginConfigs)

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)

        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        player?.addListener(this, PlayerEvent.error) { event ->
            Log.i(TAG, "PLAYER ERROR " + event.error.message!!)
        }
        player?.addListener(this, BroadpeakEvent.error) { event ->
            Log.i(TAG, "BROADPEAK ERROR " + event.errorMessage)
        }

        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player?.playerView)
        val ottMediaOptions = buildOttMediaOptions()
        player?.loadMedia(ottMediaOptions) { entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_SHORT).show()
            } else {
                Log.i(TAG, "OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }

        //Add simple play/pause button.
        addPlayPauseButton()

        showSystemUI()

        addPlayerStateListener()
    }

    private fun buildOttMediaOptions(): OTTMediaOptions {
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = ASSET_ID
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Https
        ottMediaAsset.urlType = APIDefines.KalturaUrlType.Direct
        ottMediaAsset.streamerType = APIDefines.KalturaStreamerType.Mpegdash
        ottMediaAsset.ks = KS
        ottMediaAsset.formats = listOf(MEDIA_FORMAT)
        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)

        ottMediaOptions.startPosition = START_POSITION


        return ottMediaOptions
    }
}
