package com.kaltura.playkit.samples.basicsample

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
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private val log = PKLog.get("MainActivity")
    private val START_POSITION = 0L // position for start playback in msec.
    private var player: KalturaPlayer? = null
    private val ENTRY_ID = "1_w9zx2eti"
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

    private fun addPlayerStateListener() {
        player!!.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
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

        player = KalturaOvpPlayer.create(this@MainActivity, playerInitOptions)

        player?.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        val container = player_root
        container.addView(player?.playerView)

        val ovpMediaOptions = buildOvpMediaOptions()
        player?.loadMedia(ovpMediaOptions) { mediaOptions, entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OVPMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }

        addPlayerStateListener()
    }

    private fun buildOvpMediaOptions(): OVPMediaOptions {
        val ovpMediaAsset = OVPMediaAsset()
        ovpMediaAsset.entryId = ENTRY_ID
        ovpMediaAsset.ks = null
        ovpMediaAsset.redirectFromEntryId = true
        val ovpMediaOptions = OVPMediaOptions(ovpMediaAsset)
        ovpMediaOptions.startPosition = START_POSITION

        return ovpMediaOptions
    }

    companion object {
        val PARTNER_ID = 2215841
        val SERVER_URL = "https://cdnapisec.kaltura.com"
    }
}
