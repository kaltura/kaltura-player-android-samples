package com.kaltura.playkit.samples.fulldemo

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import com.kaltura.android.exoplayer2.Player
import com.kaltura.android.exoplayer2.Timeline
import com.kaltura.android.exoplayer2.ui.DefaultTimeBar
import com.kaltura.android.exoplayer2.ui.TimeBar
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.utils.Consts
import com.kaltura.tvplayer.KalturaPlayer
import java.util.*

class PlaybackControlsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private val log = PKLog.get("PlaybackControlsView")
    private val PROGRESS_BAR_MAX = 100
    private val formatter: Formatter
    private val formatBuilder: StringBuilder
    private val componentListener: ComponentListener
    private val updateProgressAction = Runnable { this.updateProgress() }

    private var player: KalturaPlayer? = null
    private var playerState: PlayerState? = null
    private var dragging = false

    private lateinit var seekBar: DefaultTimeBar
    private lateinit var tvCurTime: TextView
    private lateinit var tvTime: TextView
    private lateinit var btnPlay: ImageButton
    private lateinit var btnPause: ImageButton
    private lateinit var btnFastForward: ImageButton
    private lateinit var btnRewind: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrevious: ImageButton
    private lateinit var btnShuffle: ImageButton
    private lateinit var btnRepeatToggle: ImageButton
    private lateinit var btnVr: ImageButton

    init {
        LayoutInflater.from(context).inflate(R.layout.exo_playback_control_view_old, this)
        formatBuilder = StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
        componentListener = ComponentListener()
        initPlaybackControls()
    }

    private fun initPlaybackControls() {

        btnPlay = this.findViewById(R.id.kexo_play)
        btnPause = this.findViewById(R.id.kexo_pause)
        btnFastForward = this.findViewById(R.id.kexo_ffwd)
        btnFastForward.visibility = View.GONE
        btnRewind = this.findViewById(R.id.kexo_rew)
        btnRewind.visibility = View.GONE
        btnNext = this.findViewById(R.id.kexo_next)
        btnPrevious = this.findViewById(R.id.kexo_prev)
        btnRepeatToggle = this.findViewById(R.id.kexo_repeat_toggle)
        btnRepeatToggle.visibility = View.GONE
        btnShuffle = this.findViewById(R.id.kexo_shuffle)
        btnShuffle.visibility = View.GONE
        btnVr = this.findViewById(R.id.kexo_vr)
        btnVr.visibility = View.GONE

        btnPlay.setOnClickListener(this)
        btnPause.setOnClickListener(this)
        btnFastForward.setOnClickListener(this)
        btnRewind.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnPrevious.setOnClickListener(this)

        seekBar = this.findViewById(R.id.kexo_progress)
        seekBar.setPlayedColor(resources.getColor(R.color.colorAccent))
        seekBar.setBufferedColor(resources.getColor(R.color.grey))
        seekBar.setUnplayedColor(resources.getColor(R.color.black))
        seekBar.setScrubberColor(resources.getColor(R.color.colorAccent))
        seekBar.addListener(componentListener)

        tvCurTime = this.findViewById(R.id.kexo_position)
        tvTime = this.findViewById(R.id.kexo_duration)
    }


    private fun updateProgress() {
        var duration: Long? = Consts.TIME_UNSET
        var position: Long? = Consts.POSITION_UNSET.toLong()
        var bufferedPosition: Long? = 0
        if (player != null) {
            val adController = player?.getController(AdController::class.java)
            if (adController != null && adController.isAdDisplayed) {
                duration = adController.adDuration
                position = adController.adCurrentPosition

                //log.d("adController Duration:" + duration);
                //log.d("adController Position:" + position);
            } else {
                duration = player?.duration
                position = player?.currentPosition
                //log.d("Duration:" + duration);
                //log.d("Position:" + position);
                bufferedPosition = player?.bufferedPosition
            }
        }

        if (duration != Consts.TIME_UNSET) {
            //log.d("updateProgress Set Duration:" + duration);
            tvTime.text = stringForTime(duration!!)
        }

        if (!dragging && position != Consts.POSITION_UNSET.toLong() && duration != Consts.TIME_UNSET) {
            //log.d("updateProgress Set Position:" + position);
            tvCurTime.text = stringForTime(position!!)
            seekBar.setPosition(position)
            seekBar.setDuration(duration)
        }

        seekBar.setBufferedPosition(bufferedPosition!!)
        // Remove scheduled updates.
        removeCallbacks(updateProgressAction)
        // Schedule an update if necessary.
        if (playerState != PlayerState.IDLE) {
            val delayMs: Long = 1000
            postDelayed(updateProgressAction, delayMs)
        }
    }

    /**
     * Component Listener for Default time bar from ExoPlayer UI
     */
    private inner class ComponentListener : Player.Listener, TimeBar.OnScrubListener, View.OnClickListener {

        override fun onScrubStart(timeBar: TimeBar, position: Long) {
            dragging = true
        }

        override fun onScrubMove(timeBar: TimeBar, position: Long) {
            tvCurTime.text = stringForTime(position)
        }

        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
            dragging = false
            player?.seekTo(position)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            updateProgress()
        }

        override fun onPositionDiscontinuity(
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            updateProgress()
        }

        override fun onTimelineChanged(timeline: Timeline, reason: Int) {
            updateProgress()
        }

        override fun onClick(view: View) {}
    }

    private fun progressBarValue(position: Long): Int {
        var progressValue = 0
        player?.let{
            var duration: Long? = it.duration
            //log.d("position = "  + position);
            //log.d("duration = "  + duration);
            val adController = it.getController(AdController::class.java)
            if (adController != null && adController.isAdDisplayed) {
                duration = adController.adDuration
            }
            if (duration!! > 0) {
                //log.d("position = "  + position);
                progressValue = Math.round((position * PROGRESS_BAR_MAX / duration).toFloat())
            }
            //log.d("progressValue = "  + progressValue);
        }

        return progressValue
    }

    private fun positionValue(progress: Long): Long {
        var positionValue: Long = 0
        player?.let {
            var duration: Long? = it.duration
            val adController = it.getController(AdController::class.java)
            if (adController != null && adController.isAdDisplayed) {
                duration = adController.adDuration
            }
            positionValue = Math.round((duration?.times(progress)?.div(PROGRESS_BAR_MAX))!!.toFloat()).toLong()
        }

        return positionValue
    }

    private fun stringForTime(timeMs: Long): String {

        val totalSeconds = (timeMs + 500) / 1000
        val seconds = totalSeconds % 60
        val minutes = totalSeconds / 60 % 60
        val hours = totalSeconds / 3600
        formatBuilder.setLength(0)
        return if (hours > 0)
            formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
        else
            formatter.format("%02d:%02d", minutes, seconds).toString()
    }

    fun setPlayer(player: KalturaPlayer?) {
        this.player = player
    }

    fun setPlayerState(playerState: PlayerState) {
        this.playerState = playerState
        updateProgress()
    }

    fun setSeekBarStateForAd(isAdPlaying: Boolean) {
        seekBar.isEnabled = !isAdPlaying
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.kexo_play -> player?.play()

            R.id.kexo_pause -> player?.pause()

            R.id.kexo_ffwd -> {
                //Do nothing for now
            }
            R.id.kexo_rew -> {
                //Do nothing for now
            }
            R.id.kexo_next -> {
                //Do nothing for now
            }
            R.id.kexo_prev -> {
                //Do nothing for now
            }
        }
    }

    fun release() {
        removeCallbacks(updateProgressAction)
    }

    fun resume() {
        updateProgress()
    }
}
