package com.kaltura.kalturaplayertestapp

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ImageView
import com.kaltura.kalturaplayertestapp.tracks.TracksSelectionController
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.utils.Consts
import com.kaltura.playkitvr.VRController
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.PlaybackControlsView

class PlaybackControlsManager(private val playerActivity: PlayerActivity, private val player: KalturaPlayer?, private val playbackControlsView: PlaybackControlsView?) : PlaybackControls {
    private var tracksSelectionController: TracksSelectionController? = null

    private val videoTracksBtn: Button
    private val audioTracksBtn: Button
    private val textTracksBtn: Button
    private val prevBtn: Button
    private val nextBtn: Button
    private val vrToggle: ImageView


    var playerState: Enum<*>? = null
        private set
    private var adPlayerState: Enum<*>? = null
    private var isAdDisplayed: Boolean = false

    private val hideButtonsHandler = Handler(Looper.getMainLooper())
    private val hideButtonsRunnable = Runnable {
        if (playerState === PlayerEvent.Type.PLAYING || adPlayerState === AdEvent.Type.STARTED || adPlayerState === AdEvent.Type.RESUMED || adPlayerState === AdEvent.Type.COMPLETED) {
            showControls(View.INVISIBLE)
        }
    }

    fun getAdPlayerState(): Enum<*>? {
        return adPlayerState
    }

    init {
        this.videoTracksBtn = playerActivity.findViewById(R.id.video_tracks)
        this.textTracksBtn = playerActivity.findViewById(R.id.text_tracks)
        this.audioTracksBtn = playerActivity.findViewById(R.id.audio_tracks)
        addTracksButtonsListener()
        this.prevBtn = playerActivity.findViewById(R.id.prev_btn)
        this.nextBtn = playerActivity.findViewById(R.id.next_btn)
        this.vrToggle = playerActivity.findViewById(R.id.vrtoggleButton)
        vrToggle.setOnClickListener { togglVRClick() }
        showControls(View.INVISIBLE)
    }

    fun togglVRClick() {
        if (player == null) {
            return
        }

        val vrController = player.getController(VRController::class.java)
        if (vrController != null) {
            val currentState = vrController.isVRModeEnabled
            vrController.enableVRMode(!currentState)
            if (currentState) {
                vrToggle.setBackgroundResource(R.drawable.ic_vr_active)
            } else {
                vrToggle.setBackgroundResource(R.drawable.ic_vr)
            }
        }

    }

    override fun handleContainerClick() {
        log.d("CLICK handleContainerClick playerState = $playerState adPlayerState $adPlayerState")
        if (playerState == null && adPlayerState == null) {
            return
        }
        showControls(View.VISIBLE)
        hideButtonsHandler.removeCallbacks(hideButtonsRunnable)
        hideButtonsHandler.postDelayed(hideButtonsRunnable, REMOVE_CONTROLS_TIMEOUT.toLong())
    }

    override fun showControls(visibility: Int) {
        if (playbackControlsView != null) {
            if (player != null) {
                val adController = player.getController(AdController::class.java)
                if (adController != null && adController.isAdDisplayed) {
                    playbackControlsView.setSeekbarDisabled()
                } else {
                    playbackControlsView.setSeekbarEnabled()
                }
            }
            playbackControlsView.visibility = visibility
        }

        if (isAdDisplayed) {
            nextBtn.visibility = View.INVISIBLE
            prevBtn.visibility = View.INVISIBLE
            videoTracksBtn.visibility = View.INVISIBLE
            audioTracksBtn.visibility = View.INVISIBLE
            textTracksBtn.visibility = View.INVISIBLE
            vrToggle.visibility = View.INVISIBLE
            return
        }

        if (tracksSelectionController == null || tracksSelectionController!!.tracks == null) {
            return
        }

        nextBtn.visibility = visibility
        prevBtn.visibility = visibility

        if (tracksSelectionController!!.tracks!!.videoTracks.size > 1) {
            videoTracksBtn.visibility = visibility
            if (player != null) {
                val vrController = player.getController(VRController::class.java)
                if (vrController != null) {
                    vrToggle.visibility = visibility
                }
            }
        } else {
            videoTracksBtn.visibility = View.INVISIBLE
        }

        if (tracksSelectionController!!.tracks?.audioTracks!!.size > 1) {
            audioTracksBtn.visibility = visibility
        } else {
            audioTracksBtn.visibility = View.INVISIBLE
        }

        if (tracksSelectionController!!.tracks?.textTracks!!.size > 1) {
            textTracksBtn.visibility = visibility
        } else {
            textTracksBtn.visibility = View.INVISIBLE
        }
    }

    override fun setContentPlayerState(playerState: Enum<*>?) {
        this.playerState = playerState

    }

    override fun setAdPlayerState(adPlayerState: Enum<*>?) {
        this.adPlayerState = adPlayerState
        if (adPlayerState === AdEvent.Type.STARTED || adPlayerState === AdEvent.Type.CONTENT_PAUSE_REQUESTED || adPlayerState === AdEvent.Type.TAPPED) {
            isAdDisplayed = true
        } else if (adPlayerState === AdEvent.Type.CONTENT_RESUME_REQUESTED || adPlayerState === AdEvent.Type.ALL_ADS_COMPLETED) {
            isAdDisplayed = false
        }
    }

    private fun addTracksButtonsListener() {
        videoTracksBtn.setOnClickListener{ view ->
            if (tracksSelectionController != null && !isAdDisplayed) {
                tracksSelectionController!!.showTracksSelectionDialog(Consts.TRACK_TYPE_VIDEO)
            }
        }

        textTracksBtn.setOnClickListener { view ->
            if (tracksSelectionController != null && !isAdDisplayed) {
                tracksSelectionController!!.showTracksSelectionDialog(Consts.TRACK_TYPE_TEXT)
            }
        }

        audioTracksBtn.setOnClickListener { view ->
            if (tracksSelectionController != null && !isAdDisplayed) {
                tracksSelectionController!!.showTracksSelectionDialog(Consts.TRACK_TYPE_AUDIO)
            }
        }
    }

    fun updatePrevNextBtnFunctionality(currentPlayedMediaIndex: Int, mediaListSize: Int) {
        if (mediaListSize > 1) {
            if (currentPlayedMediaIndex == 0) {
                nextBtn.isClickable = true
                nextBtn.setBackgroundColor(Color.rgb(66, 165, 245))
                prevBtn.isClickable = false
                prevBtn.setBackgroundColor(Color.RED)
            } else if (currentPlayedMediaIndex == mediaListSize - 1) {
                nextBtn.isClickable = false
                nextBtn.setBackgroundColor(Color.RED)
                prevBtn.isClickable = true
                prevBtn.setBackgroundColor(Color.rgb(66, 165, 245))
            } else if (currentPlayedMediaIndex > 0 && currentPlayedMediaIndex < mediaListSize - 1) {
                nextBtn.isClickable = true
                nextBtn.setBackgroundColor(Color.rgb(66, 165, 245))
                prevBtn.isClickable = true
                prevBtn.setBackgroundColor(Color.rgb(66, 165, 245))
            } else {
                nextBtn.isClickable = false
                nextBtn.setBackgroundColor(Color.RED)
                prevBtn.isClickable = false
                prevBtn.setBackgroundColor(Color.RED)
            }
        } else {
            nextBtn.isClickable = false
            nextBtn.setBackgroundColor(Color.RED)
            prevBtn.isClickable = false
            prevBtn.setBackgroundColor(Color.RED)
        }
    }

    fun addChangeMediaButtonsListener(mediaListSize: Int) {
        prevBtn.setOnClickListener { view ->
            playbackControlsView!!.playPauseToggle.setBackgroundResource(R.drawable.play)
            playerActivity.setCurrentPlayedMediaIndex(playerActivity.getCurrentPlayedMediaIndex() - 1)
            if (mediaListSize <= 1) {
                return@setOnClickListener
            }
            updatePrevNextBtnFunctionality(playerActivity.getCurrentPlayedMediaIndex(), mediaListSize)
            playerActivity.clearLogView()
            player?.stop()
            playerActivity.changeMedia()
        }

        nextBtn.setOnClickListener{ view ->
            playbackControlsView!!.playPauseToggle.setBackgroundResource(R.drawable.play)
            playerActivity.setCurrentPlayedMediaIndex(playerActivity.getCurrentPlayedMediaIndex() + 1)

            if (mediaListSize <= 1) {
                return@setOnClickListener
            }
            updatePrevNextBtnFunctionality(playerActivity.getCurrentPlayedMediaIndex(), mediaListSize)
            playerActivity.clearLogView()
            player?.stop()
            playerActivity.changeMedia()
        }
    }

    fun setTracksSelectionController(tracksSelectionController: TracksSelectionController?) {
        this.tracksSelectionController = tracksSelectionController
    }

    companion object {
        private val log = PKLog.get("PlaybackControlsManager")
        private val REMOVE_CONTROLS_TIMEOUT = 3000 //3250
    }



}
