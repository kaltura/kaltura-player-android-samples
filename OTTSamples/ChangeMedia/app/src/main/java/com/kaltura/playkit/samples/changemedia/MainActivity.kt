package com.kaltura.playkit.samples.changemedia

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


class MainActivity : AppCompatActivity() {

    private val log = PKLog.get("MainActivity")

    private val FIRST_ASSET_ID = "548576"
    private val SECOND_ASSET_ID = "548577"
    private val START_POSITION = 0L // position for start playback in msec.
    private var player: KalturaPlayer? = null
    private var playPauseButton: Button? = null
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Add simple play/pause button.
        addPlayPauseButton()

        //Init change media button which will switch between entries.
        initChangeMediaButton()

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
     * Initialize the changeMedia button. On click it will change media.
     */
    private fun initChangeMediaButton() {
        //Get reference to the button.
        val changeMediaButton = this.findViewById<View>(R.id.change_media_button) as Button
        //Set click listener.
        changeMediaButton.setOnClickListener { v ->
            //Change media.
            changeMedia()
        }
    }

    private fun addPlayerStateListener() {
        player!!.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    /**
     * Will switch between entries. If the first entry is currently active it will
     * prepare the second one. Otherwise it will prepare the first one.
     */
    private fun changeMedia() {

        //Check if id of the media entry that is set in mediaConfig.
        if (player!!.mediaEntry.id == FIRST_ASSET_ID) {
            //If first one is active, prepare second one.
            prepareSecondEntry()
        } else {
            //If the second one is active, prepare the first one.
            prepareFirstEntry()
        }

        resetPlayPauseButtonToPauseText()
    }

    /**
     * Prepare the first entry.
     */
    private fun prepareFirstEntry() {
        val ottMediaOptions = OTTMediaOptions()
        ottMediaOptions.assetId = FIRST_ASSET_ID
        ottMediaOptions.assetType = APIDefines.KalturaAssetType.Media
        ottMediaOptions.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaOptions.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaOptions.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaOptions.ks = null
        ottMediaOptions.startPosition = START_POSITION


        player!!.loadMedia(ottMediaOptions) { entry, error ->
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }
    }

    /**
     * Prepare the second entry.
     */
    private fun prepareSecondEntry() {
        val ottMediaOptions = OTTMediaOptions()
        ottMediaOptions.assetId = SECOND_ASSET_ID
        ottMediaOptions.assetType = APIDefines.KalturaAssetType.Media
        ottMediaOptions.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaOptions.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaOptions.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaOptions.ks = null
        ottMediaOptions.startPosition = START_POSITION

        player!!.loadMedia(ottMediaOptions) { entry, error ->
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private fun addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById<View>(R.id.play_pause_button) as Button
        //Add clickListener.
        playPauseButton!!.setOnClickListener { v ->
            if (player != null) {
                if (player!!.isPlaying) {
                    //If player is playing, change text of the button and pause.
                    resetPlayPauseButtonToPlayText()
                    player!!.pause()
                } else {
                    //If player is not playing, change text of the button and play.
                    resetPlayPauseButtonToPauseText()
                    player!!.play()
                }
            }
        }
    }

    /**
     * Just reset the play/pause button text to "Play".
     */
    private fun resetPlayPauseButtonToPlayText() {
        playPauseButton!!.setText(R.string.play_text)
    }

    private fun resetPlayPauseButtonToPauseText() {
        playPauseButton!!.setText(R.string.pause_text)
    }

    override fun onResume() {
        super.onResume()
        if (player != null && playerState != null) {
            if (playPauseButton != null) {
                resetPlayPauseButtonToPauseText()
            }
            player!!.onApplicationResumed()
            player!!.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.destroy();
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

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)
        player!!.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player!!.playerView)

        //Prepare the first entry.
        prepareFirstEntry()

        addPlayerStateListener()

    }

    companion object {
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        val PARTNER_ID = 3009
    }
}