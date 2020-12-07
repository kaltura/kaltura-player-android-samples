package com.kaltura.playkit.samples.eventsregistration

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import java.util.*

class MainActivity : AppCompatActivity() {

    private var player: KalturaPlayer? = null
    private var playPauseButton: Button? = null
    private var speedSpinner: Spinner? = null
    private var userIsInteracting: Boolean = false
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

    fun addItemsOnSpeedSpinner() {

        speedSpinner = findViewById(R.id.sppedSpinner)
        val list = ArrayList<Float>()
        list.add(0.5f)
        list.add(1.0f)
        list.add(1.5f)
        list.add(2.0f)
        val dataAdapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, list)
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        speedSpinner!!.adapter = dataAdapter
        speedSpinner!!.onItemSelectedListener = CustomOnItemSelectedListener()
        speedSpinner!!.setSelection(1)

    }

    /**
     * Will subscribe to the changes in the player states.
     */
    private fun subscribeToPlayerStateChanges() {

        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            playerState = event.newState
            //Switch on the new state that is received.
            when (event.newState) {

                //Player went to the Idle state.
                PlayerState.IDLE ->
                    //Print to log.
                    Log.d(TAG, "StateChanged: IDLE.")
                //The player is in Loading state.
                PlayerState.LOADING ->
                    //Print to log.
                    Log.d(TAG, "StateChanged: LOADING.")
                //The player is ready for playback.
                PlayerState.READY ->
                    //Print to log.
                    Log.d(TAG, "StateChanged: READY.")
                //Player is buffering now.
                PlayerState.BUFFERING ->
                    //Print to log.
                    Log.d(TAG, "StateChanged: BUFFERING.")
            }
        }
    }

    /**
     * Will subscribe to the player events. The main difference between
     * player state changes and player events, is that events are notify us
     * about playback events like PLAY, PAUSE, TRACKS_AVAILABLE, SEEKING etc.
     * The player state changed events, notify us about more major changes in
     * his states. Like IDLE, LOADING, READY and BUFFERING.
     * For simplicity, in this example we will show subscription to the couple of events.
     * For the full list of events you can check our documentation.
     * !!!Note, we will receive only events, we subscribed to.
     */
    private fun subscribeToPlayerEvents() {

        player?.addListener(this, PlayerEvent.play) { event ->
            Log.d(TAG, "event received: " + event.eventType().name)
        }

        player?.addListener(this, PlayerEvent.pause) { event -> Log.d(TAG, "event received: " + event.eventType().name) }

        player?.addListener(this, PlayerEvent.playbackRateChanged) { event ->
            Log.d(TAG, "event received: " + event.eventType().name + " Rate = " + event.rate)

        }

        player?.addListener(this, PlayerEvent.tracksAvailable) { event ->
            Log.d(TAG, "Event TRACKS_AVAILABLE")

            //Then you can use the data object itself.
            val tracks = event.tracksInfo

            //Print to log amount of video tracks that are available for this entry.
            Log.d(TAG, "event received: " + event.eventType().name
                    + ". Additional info: Available video tracks number: "
                    + tracks.getVideoTracks().size)
        }

        player?.addListener(this, PlayerEvent.error) { event ->
            Log.e(TAG, "Error Event: " + event.error.errorType + " " + event.error.message)
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
            player?.let {
                val adController = it.getController(AdController::class.java)
                if (it.isPlaying || adController != null && adController.isAdDisplayed && adController.isAdPlaying) {
                    if (adController != null && adController.isAdDisplayed) {
                        adController.pause()
                    } else {
                        it.pause()
                    }
                    //If player is playing, change text of the button and pause.
                    playPauseButton!!.setText(R.string.play_text)
                } else {
                    if (adController != null && adController.isAdDisplayed) {
                        adController.play()
                    } else {
                        it.play()
                    }
                    //If player is not playing, change text of the button and play.
                    playPauseButton!!.setText(R.string.pause_text)
                }
            }
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        userIsInteracting = true
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        player?.let { player ->
            playPauseButton?.setText(R.string.pause_text)
            player.onApplicationPaused()
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()

        player?.let { player ->
            playerState?.let {
                player.onApplicationResumed()
                player.play()
            }
        }
    }

    public override fun onDestroy() {
        player?.let {
            it.removeListeners(this)
            it.destroy()
            player = null
        }
        super.onDestroy()
    }


    internal inner class CustomOnItemSelectedListener : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>, view: View, pos: Int, id: Long) {
            if (userIsInteracting) {
                Toast.makeText(parent.context,
                        "OnItemSelectedListener : " + parent.getItemAtPosition(pos).toString() + "X",
                        Toast.LENGTH_SHORT).show()
                player?.let {
                    it.playbackRate = parent.getItemAtPosition(pos) as Float
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>) {

        }
    }

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setAllowCrossProtocolEnabled(true)


        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)
        //Subscribe to events, which will notify about changes in player states.
        subscribeToPlayerStateChanges()

        //Subscribe to the player events.
        subscribeToPlayerEvents()
        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player?.playerView)

        val ottMediaOptions = buildOttMediaOptions()
        player?.loadMedia(ottMediaOptions) { entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                Log.d(TAG, "OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }

        addItemsOnSpeedSpinner()

        showSystemUI()

        //Add simple play/pause button.
        addPlayPauseButton()
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

    companion object {

        //Tag for logging.
        private val TAG = MainActivity::class.java.simpleName

        private val START_POSITION = 0L // position for start playback in msec.

        //The url of the source to play
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        private val ASSET_ID = "548576"
        val PARTNER_ID = 3009
    }
}

