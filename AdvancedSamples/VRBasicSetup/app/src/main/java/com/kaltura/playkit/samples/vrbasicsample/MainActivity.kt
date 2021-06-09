package com.kaltura.playkit.samples.vrbasicsample

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
import com.kaltura.playkit.player.vr.VRInteractionMode
import com.kaltura.playkit.player.vr.VRSettings
import com.kaltura.playkit.providers.ovp.OVPMediaAsset
import com.kaltura.playkitvr.VRController
import com.kaltura.playkitvr.VRUtil
import com.kaltura.tvplayer.KalturaOvpPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OVPMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions

class MainActivity: AppCompatActivity() {
    private var player: KalturaPlayer? = null
    private var playPauseButton: Button? = null
    private var vrButton: Button? = null

    private val ENTRY_ID = "1_afvj3z0u"
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadPlaykitPlayer()

        addPlayPauseButton()
        addVRButton()
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

    private fun addPlayerStateListener() {
        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private fun addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById(R.id.play_pause_button)
        //Add clickListener.
        playPauseButton?.setOnClickListener { v ->
            player?.let {
                if (it.isPlaying) {
                    //If player is playing, change text of the button and pause.
                    playPauseButton?.setText(R.string.play_text)
                    it.pause()
                } else {
                    //If player is not playing, change text of the button and play.
                    playPauseButton?.setText(R.string.pause_text)
                    it.play()
                }
            }
        }
    }

    /**
     * Just add a simple button which will take care or VR swtiched.
     */
    private fun addVRButton() {
        //Get reference to the play/pause button.
        vrButton = this.findViewById(R.id.vr_button)
        //Add clickListener.
        vrButton?.setOnClickListener { v ->
            player?.let {
                if (it.isPlaying) {
                    switchVRMode()
                }
            }
        }
    }

    private fun configureVRSettings(): VRSettings {
        val vrSettings = VRSettings()
        vrSettings.isFlingEnabled = true
        vrSettings.isVrModeEnabled = false
        vrSettings.interactionMode = VRInteractionMode.MotionWithTouch
        vrSettings.isZoomWithPinchEnabled = true

        val interactionMode = vrSettings.interactionMode
        if (!VRUtil.isModeSupported(this@MainActivity, interactionMode)) {
            //In case when mode is not supported we switch to supported mode.
            vrSettings.interactionMode = VRInteractionMode.Touch
        }
        return vrSettings
    }

    private fun switchVRMode() {
        if (player != null) {
            val vrController = player?.getController(VRController::class.java)
            if (vrController != null) {
                val currentState = vrController.isVRModeEnabled
                vrButton?.text = if (currentState) getString(R.string.vr_mode_on) else getString(R.string.vr_mode_off)
                vrController.enableVRMode(!currentState)
            }
        }
    }

    override fun onResume() {
        super.onResume()

        player?.let { player ->
            playPauseButton?.let {
                it.setText(R.string.pause_text)
            }
            player.onApplicationResumed()
            player.play()
        }
    }

    override fun onPause() {
        super.onPause()
        player?.let {
            player?.onApplicationPaused()
        }
    }

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)

        // Configure VR Settings
        playerInitOptions.setVRSettings(configureVRSettings())

        player = KalturaOvpPlayer.create(this@MainActivity, playerInitOptions)

        player?.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player?.playerView)

        val ovpMediaOptions = buildOvpMediaOptions()
        player?.loadMedia(ovpMediaOptions) { ovpMediaOptions, entry, loadError ->
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
        val ovpMediaOptions = OVPMediaOptions(ovpMediaAsset)
        ovpMediaOptions.startPosition = START_POSITION

        return ovpMediaOptions
    }

    companion object {

        private val log = PKLog.get("MainActivity")
        val PARTNER_ID = 2196781
        val SERVER_URL = "https://cdnapisec.kaltura.com"
        private val START_POSITION = 0L // position for start playback in msec.
    }
}
