package com.kaltura.playkit.samples.basicsample

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout

import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import com.kaltura.tvplayer.config.PhoenixTVPlayerParams

class MainActivity : AppCompatActivity() {
    private val log = PKLog.get("MainActivity")

    private val ASSET_ID = "480989"
    val MEDIA_FORMAT = "Mobile_Devices_Main_HD_Dash"
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
            if (player != null) {
                if (player!!.isPlaying) {
                    //If player is playing, change text of the button and pause.
                    playPauseButton!!.setText(R.string.play_text)
                    player!!.pause()
                } else {
                    //If player is not playing, change text of the button and play.
                    playPauseButton!!.setText(R.string.pause_text)
                    player!!.play()
                }
            }
        }
    }

    private fun addPlayerStateListener() {
        player!!.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    override fun onResume() {
        super.onResume()
        if (player != null && playerState != null) {
            if (playPauseButton != null) {
                playPauseButton!!.setText(R.string.pause_text)
            }
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

        /*
            NOTE:
             In case of OTT Partner where DMS is not configured
             playerInitOptions.tvPlayerParams
             should be set manually Q/W playback will be blocked.

             if you are OTT client with media-prep
                 please verify what is your OVP PartnerId! and set the ovpPartnerId
             if you are OTT client without media-prep
                 please do not set ovpPartnerd
         */

        val phoenixTVPlayerParams = PhoenixTVPlayerParams()
        phoenixTVPlayerParams.analyticsUrl = "https://analytics.kaltura.com"
        phoenixTVPlayerParams.ovpPartnerId = 1774581
        phoenixTVPlayerParams.partnerId = PARTNER_ID
        phoenixTVPlayerParams.serviceUrl = SERVER_URL
        phoenixTVPlayerParams.ovpServiceUrl = "http://cdnapi.kaltura.com/"
        playerInitOptions.tvPlayerParams = phoenixTVPlayerParams

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)
        player!!.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
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

        addPlayerStateListener()
    }

    private fun buildOttMediaOptions(): OTTMediaOptions {
        val ottMediaOptions = OTTMediaOptions()
        ottMediaOptions.assetId = ASSET_ID
        ottMediaOptions.assetType = APIDefines.KalturaAssetType.Media
        ottMediaOptions.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaOptions.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaOptions.protocol = PhoenixMediaProvider.HttpProtocol.Https
        ottMediaOptions.ks = null
        ottMediaOptions.startPosition = START_POSITION
        ottMediaOptions.formats = arrayOf(MEDIA_FORMAT)

        return ottMediaOptions
    }

    companion object {
        val SERVER_URL = "https://api-preprod.ott.kaltura.com/v5_1_0/"
        val PARTNER_ID = 198
    }
}
