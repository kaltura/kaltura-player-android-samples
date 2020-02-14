package com.kaltura.playlist

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
import com.kaltura.playkit.utils.Consts
import com.kaltura.tvplayer.KalturaPlayer
import java.util.*

class PlaybackControlsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener {

    private val log = PKLog.get("PlaybackControlsView")
    private val PROGRESS_BAR_MAX = 100
    private var player: KalturaPlayer? = null
    private var playerState: PlayerState? = null

    private val formatter: Formatter
    private val formatBuilder: StringBuilder

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

    private var dragging = false

    private val componentListener: ComponentListener

    private val updateProgressAction = Runnable { updateProgress() }

    init {
        LayoutInflater.from(context).inflate(R.layout.exo_playback_control_view_old, this)
        formatBuilder = StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
        componentListener = ComponentListener()
        initPlaybackControls()
    }

    private fun initPlaybackControls() {

        btnPlay = this.findViewById(R.id.exo_play)
        btnPause = this.findViewById(R.id.exo_pause)
        btnFastForward = this.findViewById(R.id.exo_ffwd)
        btnFastForward.visibility = View.GONE
        btnRewind = this.findViewById(R.id.exo_rew)
        btnRewind.visibility = View.GONE
        btnNext = this.findViewById(R.id.exo_next)
        btnPrevious = this.findViewById(R.id.exo_prev)
        btnRepeatToggle = this.findViewById(R.id.exo_repeat_toggle)
        btnRepeatToggle.visibility = View.GONE
        btnShuffle = this.findViewById(R.id.exo_shuffle)
        btnShuffle.visibility = View.GONE
        btnVr = this.findViewById(R.id.exo_vr)
        btnVr.visibility = View.GONE

        btnPlay.setOnClickListener(this)
        btnPause.setOnClickListener(this)
        btnFastForward.setOnClickListener(this)
        btnRewind.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnPrevious.setOnClickListener(this)

        seekBar = this.findViewById(R.id.exo_progress)
        seekBar.setPlayedColor(resources.getColor(R.color.colorAccent))
        seekBar.setBufferedColor(resources.getColor(R.color.grey))
        seekBar.setUnplayedColor(resources.getColor(R.color.black))
        seekBar.setScrubberColor(resources.getColor(R.color.colorAccent))
        seekBar.addListener(componentListener)

        tvCurTime = this.findViewById(R.id.exo_position)
        tvTime = this.findViewById(R.id.exo_duration)
    }

    private fun updateProgress() {
        var duration: Long? = Consts.TIME_UNSET
        var position: Long? = Consts.POSITION_UNSET.toLong()
        var bufferedPosition: Long? = 0
        player?.let {
            duration = it.duration
            position = it.currentPosition
            //log.d("XXX Duration:" + duration);
            //log.d("XXX Position:" + position);
            bufferedPosition = it.bufferedPosition

        }

        if (duration != Consts.TIME_UNSET) {
            //log.d("updateProgress Set Duration:" + duration);
            tvTime.text = stringForTime(duration!!)
        }

        if (!dragging && position != Consts.POSITION_UNSET.toLong() && duration != Consts.TIME_UNSET) {
            //log.d("updateProgress Set Position:" + position);
            tvCurTime.text = stringForTime(position!!)
            seekBar.setPosition(progressBarValue(position).toLong())
            seekBar.setDuration(progressBarValue(duration).toLong())
        }

        seekBar.setBufferedPosition(progressBarValue(bufferedPosition).toLong())
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
    private inner class ComponentListener : Player.EventListener, TimeBar.OnScrubListener, View.OnClickListener {

        override fun onScrubStart(timeBar: TimeBar, position: Long) {
            dragging = true
        }

        override fun onScrubMove(timeBar: TimeBar, position: Long) {
            player?.let{
                tvCurTime.text = stringForTime(position * it.duration / PROGRESS_BAR_MAX)
            }
        }

        override fun onScrubStop(timeBar: TimeBar, position: Long, canceled: Boolean) {
            dragging = false
            player?.let {
                it.seekTo(position * it.duration / PROGRESS_BAR_MAX)

            }
        }

        override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
            updateProgress()
        }

        override fun onPositionDiscontinuity(@Player.DiscontinuityReason reason: Int) {
            updateProgress()
        }

        override fun onTimelineChanged(timeline: Timeline?, manifest: Any?, @Player.TimelineChangeReason reason: Int) {
            updateProgress()
        }

        override fun onClick(view: View) {}
    }

    private fun progressBarValue(position: Long?): Int {
        var progressValue = 0
        player?.let {
            if (it.duration > 0) {
                if (position != null) {
                    progressValue = (position * PROGRESS_BAR_MAX / it.duration).toInt()
                }
            }
        }

        return progressValue
    }

    private fun positionValue(progress: Int): Long {
        var positionValue: Long = 0
        player?.let {
            positionValue = it.duration * progress / PROGRESS_BAR_MAX
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

    override fun onClick(v: View) {
        when (v.id) {
            R.id.exo_play ->
                player?.play()
            R.id.exo_pause ->
                player?.pause()
            R.id.exo_ffwd -> {
                //Do nothing for now
            }
            R.id.exo_rew -> {
                //Do nothing for now
            }
            R.id.exo_next -> {
                player?.playlistController?.playNext()
            }
            R.id.exo_prev -> {
                player?.playlistController?.playPrev()
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
