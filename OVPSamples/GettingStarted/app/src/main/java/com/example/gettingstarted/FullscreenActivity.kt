package com.example.gettingstarted


import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.providers.ovp.OVPMediaAsset
import com.kaltura.tvplayer.KalturaOvpPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OVPMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import kotlinx.android.synthetic.main.activity_fullscreen.*

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class FullscreenActivity : AppCompatActivity() {

    private var player: KalturaPlayer? = null

    private val entryId = "1_w9zx2eti"
    private var playerState: PlayerState? = null

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LOW_PROFILE or
                        View.SYSTEM_UI_FLAG_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
    private val mShowPart2Runnable = Runnable {
        // Delayed display of UI elements
        supportActionBar?.show()
        fullscreen_content_controls.visibility = View.VISIBLE
    }
    private var mVisible: Boolean = false
    private val mHideRunnable = Runnable { hide() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_fullscreen)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mVisible = true

        loadPlaykitPlayer()
        addPlayPauseButton()

        fullscreen_content.setOnClickListener { toggle() }
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private fun addPlayPauseButton() {
        //Add clickListener.
        playPauseButton.setOnClickListener { v ->
            player?.let {
                if (it.isPlaying) {
                    //If player is playing, change text of the button and pause.
                    it.pause()
                    playPauseButton.setImageResource(R.drawable.exo_controls_play)
                } else {
                    //If player is not playing, change text of the button and play.
                    it.play()
                    playPauseButton.setImageResource(R.drawable.exo_controls_pause)
                }
            }
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100)
    }

    private fun toggle() {
        if (mVisible) {
            hide()
        } else {
            show()
        }
    }

    private fun hide() {
        // Hide UI first
        supportActionBar?.hide()
        fullscreen_content_controls.visibility = View.GONE
        mVisible = false

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable)
        mHideHandler.postDelayed(mHidePart2Runnable, 200)
    }

    private fun show() {
        // Show the system bar
        fullscreen_content.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
                        View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        mVisible = true

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable)
        mHideHandler.postDelayed(mShowPart2Runnable, 200)
    }

    /**
     * Schedules a call to hide() in [delayMillis], canceling any
     * previously scheduled calls.
     */
    private fun delayedHide(delayMillis: Int) {
        mHideHandler.removeCallbacks(mHideRunnable)
        mHideHandler.postDelayed(mHideRunnable, delayMillis.toLong())
    }

    private fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID) //player config/behavior
        playerInitOptions.setAutoPlay(true)

        val player = KalturaOvpPlayer.create(this@FullscreenActivity, playerInitOptions) ?: return

        //create A VIEW that is determined by the size of the player
        player.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)

        fullscreen_content.addView(player.playerView)

        val ovpMediaOptions = buildOvpMediaOptions()
        player.loadMedia(ovpMediaOptions) { ovpMediaOptions, entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                Log.d(TAG, "OVPMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }
        this.player = player
        addPlayerEventsListener()
        addPlayerStateListener()
    }

    private fun addPlayerStateListener() {
        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            Log.d(TAG, "State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    private fun addPlayerEventsListener() {
        player?.addListener(this, PlayerEvent.tracksAvailable) { event ->
            Log.d(TAG, "TracksAvailable event")
            val trackInfo = event.tracksInfo
            for(videoTrack in trackInfo.videoTracks) {
                Log.d(TAG, "video id = " + videoTrack.uniqueId + " track bitrate = " + videoTrack.bitrate)
            }
        }

        player?.addListener(this,  PlayerEvent.canPlay) { event ->
            Log.d(TAG, "PlayerEvent " + event.eventType())
        }

        player?.addListener(this,  PlayerEvent.playing) { event ->
            Log.d(TAG, "PlayerEvent " + event.eventType())
        }

        player?.addListener(this,  PlayerEvent.error) { event ->
            Log.d(TAG, "Error PlayerEvent " + event.error.message + " isFatal = " + event.error.isFatal)
        }
    }


    private fun buildOvpMediaOptions(): OVPMediaOptions {
        val ovpMediaAsset = OVPMediaAsset()
        ovpMediaAsset.entryId = entryId
        ovpMediaAsset.ks = null
        val ovpMediaOptions = OVPMediaOptions(ovpMediaAsset)
        return ovpMediaOptions
    }


    companion object {

        private const val TAG = "FullscreenActivity"
        public const val OVP_SERVER_URL = "https://cdnapisec.kaltura.com"
        public const val PARTNER_ID = 2215841

    }

    override fun onResume() {
        super.onResume()
        player?.let {
            playPauseButton.setImageResource(R.drawable.exo_controls_pause)
            it.onApplicationResumed()
            it.play()
        }
    }

    override fun onPause() {
        super.onPause()
        player?.onApplicationPaused()
    }

    public override fun onDestroy() {
        player?.let {
            it.removeListeners(this)
            it.destroy()
            player = null
        }
        super.onDestroy()
    }
}
