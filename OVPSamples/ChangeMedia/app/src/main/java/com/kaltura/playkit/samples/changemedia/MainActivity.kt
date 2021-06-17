package com.kaltura.playkit.samples.changemedia

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.providers.ovp.OVPMediaAsset
import com.kaltura.tvplayer.KalturaOvpPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OVPMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import com.kaltura.tvplayer.config.MediaEntryCacheConfig
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private val log = PKLog.get("MainActivity")
    private val FIRST_ENTRY_ID = "1_w9zx2eti"
    private val SECOND_ENTRY_ID = "1_ebs5e9cy"
    private val START_POSITION = 0L // position for start playback in msec.
    private var player: KalturaPlayer? = null
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
     * Initialize the changeMedia button. On click it will change media.
     */
    private fun initChangeMediaButton() {
        //Set click listener.
        change_media_button.setOnClickListener { v ->
            //Change media.
            changeMedia()
        }
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
    private fun changeMedia() {

        //Check if id of the media entry that is set in mediaConfig.
        if (player?.mediaEntry?.id == FIRST_ENTRY_ID) {
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
        val ovpMediaAsset = OVPMediaAsset()
        ovpMediaAsset.entryId = FIRST_ENTRY_ID
        ovpMediaAsset.ks = null
        val ovpMediaOptions = OVPMediaOptions(ovpMediaAsset)

        ovpMediaOptions.startPosition = START_POSITION

        player?.loadMedia(ovpMediaOptions) { mediaOptions, entry, error ->
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("First OVPMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }
    }

    /**
     * Prepare the second entry.
     */
    private fun prepareSecondEntry() {
        val ovpMediaAsset = OVPMediaAsset()
        ovpMediaAsset.entryId = SECOND_ENTRY_ID
        ovpMediaAsset.ks = null
        val ovpMediaOptions = OVPMediaOptions(ovpMediaAsset)
        ovpMediaOptions.startPosition = START_POSITION

        player?.loadMedia(ovpMediaOptions) { mediaOptions, entry, error ->
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("Second OVPMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }
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
                    resetPlayPauseButtonToPlayText()
                    it.pause()
                } else {
                    //If player is not playing, change text of the button and play.
                    resetPlayPauseButtonToPauseText()
                    it.play()
                }
            }
        }
    }

    /**
     * Just reset the play/pause button text to "Play".
     */
    private fun resetPlayPauseButtonToPlayText() {
        play_pause_button.setText(R.string.play_text)
    }

    private fun resetPlayPauseButtonToPauseText() {
        play_pause_button.setText(R.string.pause_text)
    }

    override fun onResume() {
        super.onResume()
        player?.let {
            resetPlayPauseButtonToPauseText()
            player?.onApplicationResumed()
            player?.play()
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
        playerInitOptions.setAllowCrossProtocolEnabled(true)
        playerInitOptions.mediaEntryCacheConfig = MediaEntryCacheConfig(true, 10, 60000)
        player = KalturaOvpPlayer.create(this@MainActivity, playerInitOptions)

        player?.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        val container = player_root
        container.addView(player?.playerView)

        //Prepare the first entry.
        prepareFirstEntry()

        addPlayerStateListener()
    }

    companion object {
        val SERVER_URL = "https://cdnapisec.kaltura.com"
        val PARTNER_ID = 2215841
    }
}