package com.kaltura.kalturaplayertestapp

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.kaltura.kalturaplayertestapp.tracks.TracksSelectionController
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.fbads.fbinstream.FBInstreamPlugin
import com.kaltura.playkit.utils.Consts
import com.kaltura.playkitvr.VRController
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.PlaybackControlsView

class PlaybackControlsManager(private val playerActivity: PlayerActivity, private val player: KalturaPlayer?, private val playbackControlsView: PlaybackControlsView?): PlaybackControls {

    private var tracksSelectionController: TracksSelectionController? = null

    private val videoTracksBtn: Button
    private val audioTracksBtn: Button
    private val textTracksBtn: Button

    private val loopBtn: Button
    private val shuffleBtn: Button
    private val recoverOnErrorBtn: Button

    private val prevImgBtn: ImageView
    private val nextImgBtn: ImageView

    private var liveInfoFrame: FrameLayout
    private lateinit var liveInfoText: TextView

    private val vrToggle: ImageView
    private var adPluginName: String? = null
    val lowLatencyHandler = Handler(Looper.getMainLooper())

    val lowLatencyRunnable = object : Runnable {
        override fun run() {
            player?.let {
                if (it.isPlaying) {
                    playerActivity.pkLowLatencyConfig?.let { pkLowLatencyConfig ->
                        val liveInfoBuilder = StringBuffer()
                        liveInfoBuilder.append("Live Offset: ${validateConfigParams(it.currentLiveOffset)}" + "\n")
                        liveInfoBuilder.append("TargetOffset: ${validateConfigParams(pkLowLatencyConfig.targetOffsetMs)} " + "\n")
                        liveInfoBuilder.append("MinOffset: ${validateConfigParams(pkLowLatencyConfig.minOffsetMs)} " + "\n")
                        liveInfoBuilder.append("MaxOffset: ${validateConfigParams(pkLowLatencyConfig.maxOffsetMs)} " + "\n")
                        liveInfoBuilder.append("MinPlaybackSpeed: ${pkLowLatencyConfig.minPlaybackSpeed} " + "\n")
                        liveInfoBuilder.append("MaxPlaybackSpeed: ${pkLowLatencyConfig.maxPlaybackSpeed} " + "\n")

                        liveInfoText.text = liveInfoBuilder.toString()
                    }
                }
                lowLatencyHandler.postDelayed(this, LOW_LATENCY_HANDLER_TIMER.toLong())
            }
        }
    }

    private fun validateConfigParams(value: Long) : String {
        if (value == Consts.TIME_UNSET) {
            return "TIME_UNSET"
        }
        return value.toString()
    }

    var playerState: Enum<*>? = null
        private set
    private var adPlayerState: Enum<*>? = null
    private var isAdDisplayed: Boolean = false

    private val hideButtonsHandler = Handler(Looper.getMainLooper())
    private val hideButtonsRunnable = Runnable {
        if (playerState == null || playerState == PlayerEvent.Type.PLAYING || adPlayerState == AdEvent.Type.STARTED || adPlayerState == AdEvent.Type.RESUMED || adPlayerState == AdEvent.Type.COMPLETED) {
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

        this.loopBtn = playerActivity.findViewById(R.id.loop_btn)
        this.shuffleBtn = playerActivity.findViewById(R.id.shuffle_btn)
        this.recoverOnErrorBtn = playerActivity.findViewById(R.id.recover_btn)

        this.prevImgBtn = playerActivity.findViewById(R.id.icon_play_prev)
        this.nextImgBtn = playerActivity.findViewById(R.id.icon_play_next)

        liveInfoFrame = playerActivity.findViewById(R.id.live_info_frame)
        liveInfoText = playerActivity.findViewById(R.id.live_info_txt)

        this.vrToggle = playerActivity.findViewById(R.id.vrtoggleButton)
        vrToggle.setOnClickListener { togglVRClick() }
        showControls(View.INVISIBLE)
    }

    fun liveInfoMenuClick() {
        if (liveInfoFrame.visibility == View.GONE) {
            liveInfoFrame.visibility = View.VISIBLE
            startLiveInfoHandler()
        } else {
            liveInfoFrame.visibility = View.GONE
            removeLiveInfoHandler()
        }
    }

    private fun startLiveInfoHandler() {
        lowLatencyHandler.postDelayed(lowLatencyRunnable, LOW_LATENCY_HANDLER_TIMER.toLong())
    }

    fun removeLiveInfoHandler() {
        lowLatencyHandler.removeCallbacks(lowLatencyRunnable)
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
        if (isAdDisplayed && FBInstreamPlugin.factory.getName().equals(adPluginName)) {
            showControls(View.INVISIBLE)
        } else {
            showControls(View.VISIBLE)
        }
        playerActivity.pkLowLatencyConfig?.let {
            liveInfoMenuClick()
        }
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
            loopBtn.visibility = View.INVISIBLE
            shuffleBtn.visibility = View.INVISIBLE
            recoverOnErrorBtn.visibility = View.INVISIBLE

            nextImgBtn.visibility = View.VISIBLE
            prevImgBtn.visibility = View.VISIBLE

            videoTracksBtn.visibility = View.INVISIBLE
            audioTracksBtn.visibility = View.INVISIBLE
            textTracksBtn.visibility = View.INVISIBLE
            vrToggle.visibility = View.INVISIBLE
            return
        }

        nextImgBtn.visibility = visibility
        prevImgBtn.visibility = visibility

        loopBtn.visibility = visibility
        shuffleBtn.visibility = View.INVISIBLE
        recoverOnErrorBtn.visibility = visibility

        if (player?.playlistController != null) {
            if (player.playlistController.isLoopEnabled) {
                player.playlistController?.isLoopEnabled?.let {
                    if (it) {
                        loopBtn.setBackgroundColor(Color.rgb(66, 165, 245))
                    } else {
                        loopBtn.setBackgroundColor(ContextCompat.getColor(playerActivity, R.color.cardview_dark_background))
                    }
                }

            }

//            if (player?.playlistController.isShuffleEnabled) {
//                player?.playlistController?.isShuffleEnabled?.let {
//                    if (it) {
//                        shuffleBtn.setBackgroundColor(Color.rgb(66, 165, 245))
//                    } else {
//                        shuffleBtn.setBackgroundColor(ContextCompat.getColor(playerActivity, R.color.cardview_dark_background))
//                    }
//                }
//            }

            if (player.playlistController.isRecoverOnError) {
                player.playlistController?.isRecoverOnError?.let {
                    if (it) {
                        recoverOnErrorBtn.setBackgroundColor(Color.rgb(66, 165, 245))
                    } else {
                        recoverOnErrorBtn.setBackgroundColor(ContextCompat.getColor(playerActivity, R.color.cardview_dark_background))
                    }
                }
            }
        }

        if (tracksSelectionController == null || tracksSelectionController!!.tracks == null) {
            return
        }

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

    override fun setAdPluginName(adPluginName: String) {
        this.adPluginName = adPluginName;
    }

    override fun setAdPlayerState(playerState: Enum<*>?) {
        if (playerState == null) {
            isAdDisplayed = false
            adPlayerState = null
            setSeekBarVisibiliy(View.INVISIBLE)
            return
        }

        this.adPlayerState = playerState
        if (adPlayerState == AdEvent.Type.STARTED || adPlayerState == AdEvent.Type.CONTENT_PAUSE_REQUESTED || adPlayerState == AdEvent.Type.TAPPED) {
            isAdDisplayed = true
        } else if (adPlayerState == AdEvent.Type.CONTENT_RESUME_REQUESTED || adPlayerState == AdEvent.Type.ALL_ADS_COMPLETED) {
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

//    fun updatePrevNextBtnFunctionality(currentPlayedMediaIndex: Int, mediaListSize: Int) {
//        var isPlaylistLoopEnabled = player?.playlistController?.isLoopEnabled() ?: false
//        if (mediaListSize > 1) {
//            if (currentPlayedMediaIndex == 0) {
//                nextBtn.isClickable = true
//                nextBtn.setBackgroundColor(Color.rgb(66, 165, 245))
//                if (isPlaylistLoopEnabled) {
//                    prevBtn.isClickable = true
//                    prevBtn.setBackgroundColor(Color.rgb(66, 165, 245))
//                } else {
//                    prevBtn.isClickable = false
//                    prevBtn.setBackgroundColor(Color.RED)
//                }
//            } else if (currentPlayedMediaIndex == mediaListSize - 1 && !isPlaylistLoopEnabled) {
//                nextBtn.isClickable = false
//                nextBtn.setBackgroundColor(Color.RED)
//                prevBtn.isClickable = true
//                prevBtn.setBackgroundColor(Color.rgb(66, 165, 245))
//            } else if (currentPlayedMediaIndex > 0 && currentPlayedMediaIndex < mediaListSize - 1 || isPlaylistLoopEnabled) {
//                nextBtn.isClickable = true
//                nextBtn.setBackgroundColor(Color.rgb(66, 165, 245))
//                prevBtn.isClickable = true
//                prevBtn.setBackgroundColor(Color.rgb(66, 165, 245))
//            } else {
//                nextBtn.isClickable = false
//                nextBtn.setBackgroundColor(Color.RED)
//                prevBtn.isClickable = false
//                prevBtn.setBackgroundColor(Color.RED)
//            }
//        } else {
//            nextBtn.isClickable = false
//            nextBtn.setBackgroundColor(Color.RED)
//            prevBtn.isClickable = false
//            prevBtn.setBackgroundColor(Color.RED)
//        }
//    }

    fun updatePrevNextImgBtnFunctionality(currentPlayedMediaIndex: Int, mediaListSize: Int) {
        var isPlaylistLoopEnabled = player?.playlistController?.isLoopEnabled() ?: false
        if (mediaListSize > 1) {
            if (currentPlayedMediaIndex == 0) {
                nextImgBtn.isClickable = true
                nextImgBtn.setBackgroundColor(Color.rgb(66, 165, 245))
                if (isPlaylistLoopEnabled) {
                    prevImgBtn.isClickable = true
                    prevImgBtn.setBackgroundColor(Color.rgb(66, 165, 245))
                } else {
                    prevImgBtn.isClickable = false
                    prevImgBtn.setBackgroundColor(Color.RED)
                }
            } else if (currentPlayedMediaIndex == mediaListSize - 1 && !isPlaylistLoopEnabled) {
                nextImgBtn.isClickable = false
                nextImgBtn.setBackgroundColor(Color.RED)
                prevImgBtn.isClickable = true
                prevImgBtn.setBackgroundColor(Color.rgb(66, 165, 245))
            } else if (currentPlayedMediaIndex > 0 && currentPlayedMediaIndex < mediaListSize - 1 || isPlaylistLoopEnabled) {
                nextImgBtn.isClickable = true
                nextImgBtn.setBackgroundColor(Color.rgb(66, 165, 245))
                prevImgBtn.isClickable = true
                prevImgBtn.setBackgroundColor(Color.rgb(66, 165, 245))
            } else {
                nextImgBtn.isClickable = false
                nextImgBtn.setBackgroundColor(Color.RED)
                prevImgBtn.isClickable = false
                prevImgBtn.setBackgroundColor(Color.RED)
            }
        } else {
            nextImgBtn.isClickable = false
            nextImgBtn.setBackgroundColor(Color.RED)
            prevImgBtn.isClickable = false
            prevImgBtn.setBackgroundColor(Color.RED)
        }
    }

    fun setSeekBarVisibiliy(visibility: Int) {
        playbackControlsView?.setSeekBarVisibility(visibility)
    }

    fun addPlaylistButtonsListener() {
        if (player?.playlistController == null) {
            return
        }

        loopBtn.setOnClickListener { view ->
            player.playlistController?.let {
                if (it.isLoopEnabled) {
                    player.playlistController?.setLoop(false)
                    loopBtn.setBackgroundColor(ContextCompat.getColor(playerActivity, R.color.cardview_dark_background))
                } else {
                    player.playlistController?.setLoop(true)
                    loopBtn.setBackgroundColor(Color.rgb(66, 165, 245))
                }
            }
            updatePrevNextImgBtnFunctionality(player.playlistController.currentMediaIndex, player.playlistController.playlist?.mediaList?.size
                    ?: 0)

        }

//        shuffleBtn.setOnClickListener{ view ->
//            player?.playlistController?.let {
//                if (it.isShuffleEnabled) {
//                    player?.playlistController?.shuffle(false)
//                    shuffleBtn.setBackgroundColor(ContextCompat.getColor(playerActivity, R.color.cardview_dark_background))
//                } else {
//                    player?.playlistController?.shuffle(true)
//                    shuffleBtn.setBackgroundColor(Color.rgb(66, 165, 245))
//                }
//            }
//        }

        recoverOnErrorBtn.setOnClickListener{ view ->
            player.playlistController?.let {
                if (it.isRecoverOnError) {
                    player.playlistController?.setRecoverOnError(false)
                    recoverOnErrorBtn.setBackgroundColor(ContextCompat.getColor(playerActivity, R.color.cardview_dark_background))
                } else {
                    player.playlistController?.setRecoverOnError(true)
                    recoverOnErrorBtn.setBackgroundColor(Color.rgb(66, 165, 245))
                }
            }
        }
    }

    fun addChangeMediaImgButtonsListener(mediaListSize: Int) {
        prevImgBtn.setOnClickListener { view ->
            playbackControlsView!!.playPauseToggle.setBackgroundResource(R.drawable.play)
            playerActivity.setCurrentPlayedMediaIndex(playerActivity.getCurrentPlayedMediaIndex() - 1)
            if (mediaListSize <= 1) {
                return@setOnClickListener
            }
            updatePrevNextImgBtnFunctionality(playerActivity.getCurrentPlayedMediaIndex(), mediaListSize)
            playerActivity.clearLogView()
            player?.stop()
            if (player?.playlistController != null) {
                playerActivity.playPrev()
            } else {
                playerActivity.changeMedia()
            }
        }

        nextImgBtn.setOnClickListener{ view ->
            playbackControlsView!!.playPauseToggle.setBackgroundResource(R.drawable.play)
            playerActivity.setCurrentPlayedMediaIndex(playerActivity.getCurrentPlayedMediaIndex() + 1)

            if (mediaListSize <= 1) {
                return@setOnClickListener
            }
            updatePrevNextImgBtnFunctionality(playerActivity.getCurrentPlayedMediaIndex(), mediaListSize)
            playerActivity.clearLogView()
            player?.stop()
            if (player?.playlistController != null) {
                playerActivity.playNext()
            } else {
                playerActivity.changeMedia()
            }
        }
    }

    fun setTracksSelectionController(tracksSelectionController: TracksSelectionController?) {
        this.tracksSelectionController = tracksSelectionController
    }

    companion object {
        private val log = PKLog.get("PlaybackControlsManager")
        private val REMOVE_CONTROLS_TIMEOUT = 3000 //3250
        private val LOW_LATENCY_HANDLER_TIMER = 1000
    }
}
