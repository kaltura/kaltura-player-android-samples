package com.kaltura.playkit.samples.broadpeak

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //Tag for logging.
    private val TAG = MainActivity::class.java.simpleName

    private val START_POSITION = -1L // position for start playback in seconds.

    companion object {
        const val SERVER_URL = "phoenixUrl"
        const val FIRST_ASSET_ID = "assetId-1"
        const val SECOND_ASSET_ID = "assetId-2"
        const val PARTNER_ID = 11111111
        const val KS = "KS"
        const val MEDIA_FORMAT = "FORMAT"
    }

    private var player: KalturaPlayer? = null
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null
    private var currentlyPlayingAsset: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadPlaykitPlayer()

        activity_main.setOnClickListener {
            if (isFullScreen) {
                showSystemUI()
            } else {
                hideSystemUI()
            }
        }

        change_media_button.setOnClickListener {
            currentlyPlayingAsset?.let {
                if (it == FIRST_ASSET_ID) {
                    loadSecondOttMedia()
                } else {
                    loadFirstOttMedia()
                }
            }
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar

                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar

                or View.SYSTEM_UI_FLAG_IMMERSIVE)
        isFullScreen = true
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        isFullScreen = false
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private fun addPlayPauseButton() {
        play_pause_button.setOnClickListener {
            if (player!!.isPlaying) {
                //If player is playing, change text of the button and pause.
                play_pause_button.setText(R.string.play_text)
                player?.pause()
            } else {
                //If player is not playing, change text of the button and play.
                play_pause_button.setText(R.string.pause_text)
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
        player?.let {
            play_pause_button.setText(R.string.pause_text)
            it.onApplicationPaused()
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

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        player?.destroy()
    }

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setAllowCrossProtocolEnabled(true)

        // Broadpeak Configuration
        val pkPluginConfigs = PKPluginConfigs()
        val broadpeakConfig = BroadpeakConfig().apply {
            analyticsAddress = "https://analytics.kaltura.com/api_v3/index.php"
            nanoCDNHost = ""
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

        loadFirstOttMedia()

        //Add simple play/pause button.
        addPlayPauseButton()

        showSystemUI()

        addPlayerStateListener()
    }

    private fun buildOttMediaOptions(assetId: String): OTTMediaOptions {
        currentlyPlayingAsset = assetId
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = assetId
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Https
        ottMediaAsset.urlType = APIDefines.KalturaUrlType.Direct
        ottMediaAsset.streamerType = APIDefines.KalturaStreamerType.Mpegdash
        ottMediaAsset.ks = KS
        ottMediaAsset.formats = listOf(MEDIA_FORMAT)
        return OTTMediaOptions(ottMediaAsset)
    }

    private fun loadFirstOttMedia() {
        val ottMediaOptions = buildOttMediaOptions(FIRST_ASSET_ID)
        ottMediaOptions.startPosition = START_POSITION
        player?.loadMedia(ottMediaOptions) { ottMediaOptions, entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_SHORT).show()
            } else {
                Log.i(TAG, "OTTMedia onEntryLoadComplete entry = " + entry.id)
            }
        }
    }

    private fun loadSecondOttMedia() {
        val ottMediaOptions = buildOttMediaOptions(SECOND_ASSET_ID)
        ottMediaOptions.startPosition = START_POSITION
        player?.loadMedia(ottMediaOptions) { ottMediaOptions, entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_SHORT).show()
            } else {
                Log.i(TAG, "OTTMedia onEntryLoadComplete entry = " + entry.id)
            }
        }
    }
}
