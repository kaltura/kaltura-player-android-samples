package com.kaltura.playkit.samples.basicsample

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKRequestConfig
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val log = PKLog.get("MainActivity")

    private val ASSET_ID = "548576"
    val MEDIA_FORMAT = "Mobile_Main"
    private val START_POSITION = 0L // position for start playback in msec.
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

        activity_main.setOnClickListener { v ->
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
        //Add clickListener.
        play_pause_button.setOnClickListener { v ->
            player?.let {
                if (it.isPlaying) {
                    //If player is playing, change text of the button and pause.
                    play_pause_button.setText(R.string.play_text)
                    it.pause()
                } else {
                    //If player is not playing, change text of the button and play.
                    play_pause_button.setText(R.string.pause_text)
                    it.play()
                }
            }
        }
    }

    private fun addPlayerStateListener() {
        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    override fun onResume() {
        super.onResume()
        player?.let {
            play_pause_button.setText(R.string.pause_text)
            it.onApplicationResumed()
            it.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.destroy();
    }

    override fun onPause() {
        super.onPause()
        player?.onApplicationPaused()
    }

    fun loadPlaykitPlayer() {
        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setPKRequestConfig(PKRequestConfig(true))

        /*
            NOTE:
             In case of OTT Partner where DMS is not configured
             playerInitOptions.tvPlayerParams
             should be set manually Q/W playback will be blocked.

             if you are OTT client with media-prep
                 please verify what is your OVP PartnerId! and set the ovpPartnerId
             if you are OTT client without media-prep
                 please do not set ovpPartnerd
           Example:

        val phoenixTVPlayerParams = PhoenixTVPlayerParams()
        phoenixTVPlayerParams.analyticsUrl = ANALYTICS_URL
        phoenixTVPlayerParams.ovpPartnerId = OVP_PARTER_ID
        phoenixTVPlayerParams.partnerId = PARTNER_ID
        phoenixTVPlayerParams.serviceUrl = SERVER_URL
        phoenixTVPlayerParams.ovpServiceUrl = OVP_SERVICE_URL
        playerInitOptions.tvPlayerParams = phoenixTVPlayerParams
         */

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)
        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        val container = player_root
        container.addView(player?.playerView)

        val ottMediaOptions = buildOttMediaOptions()
        player?.loadMedia(ottMediaOptions) { mediaOptions, entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }

        addPlayerStateListener()
    }

    private fun buildOttMediaOptions(): OTTMediaOptions {
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = ASSET_ID
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaAsset.formats = listOf(MEDIA_FORMAT)
        ottMediaAsset.ks = null
        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = START_POSITION


        return ottMediaOptions
    }

    companion object {
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        val PARTNER_ID = 3009
    }
}
