package com.kaltura.playkit.samples.fulldemo

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.utils.Consts
import com.kaltura.tvplayer.KalturaPlayer
import java.util.*

class PlaybackControlsView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : LinearLayout(context, attrs, defStyleAttr), View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private val log = PKLog.get("PlaybackControlsView")
    private val PROGRESS_BAR_MAX = 100
    private var player: KalturaPlayer? = null
    private var playerState: PlayerState? = null

    private val formatter: Formatter
    private val formatBuilder: StringBuilder

    private lateinit var seekBar: SeekBar
    private lateinit var tvCurTime: TextView
    private lateinit var tvTime: TextView
    private lateinit var btnPlay: ImageButton
    private lateinit var btnPause: ImageButton
    private lateinit var btnFastForward: ImageButton
    private lateinit var btnRewind: ImageButton
    private lateinit var btnNext: ImageButton
    private lateinit var btnPrevious: ImageButton

    private var dragging = false

    private val updateProgressAction = Runnable { updateProgress() }

    init {
        LayoutInflater.from(context).inflate(R.layout.playback_layout, this)
        formatBuilder = StringBuilder()
        formatter = Formatter(formatBuilder, Locale.getDefault())
        initPlaybackControls()
    }

    private fun initPlaybackControls() {

        btnPlay = this.findViewById(R.id.play)
        btnPause = this.findViewById(R.id.pause)
        btnFastForward = this.findViewById(R.id.ffwd)
        btnRewind = this.findViewById(R.id.rew)
        btnNext = this.findViewById(R.id.next)
        btnPrevious = this.findViewById(R.id.prev)

        btnPlay.setOnClickListener(this)
        btnPause.setOnClickListener(this)
        btnFastForward.setOnClickListener(this)
        btnRewind.setOnClickListener(this)
        btnNext.setOnClickListener(this)
        btnPrevious.setOnClickListener(this)

        seekBar = this.findViewById(R.id.mediacontroller_progress)
        seekBar.setOnSeekBarChangeListener(this)

        tvCurTime = this.findViewById(R.id.time_current)
        tvTime = this.findViewById(R.id.time)
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
                //log.d("XXX adController Duration:" + duration);
                //log.d("XXX adController Position:" + position);
            } else {
                duration = player?.duration
                position = player?.currentPosition
                //log.d("XXX Duration:" + duration);
                //log.d("XXX Position:" + position);
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
            seekBar.progress = progressBarValue(position)
        }

        seekBar.secondaryProgress = progressBarValue(bufferedPosition!!)
        // Remove scheduled updates.
        removeCallbacks(updateProgressAction)
        // Schedule an update if necessary.
        val adController = player?.getController(AdController::class.java)
        if (playerState != PlayerState.IDLE && adController != null && adController.adCurrentPosition >= 0) {
            val delayMs: Long = 1000
            postDelayed(updateProgressAction, delayMs)
        }
    }

    private fun progressBarValue(position: Long): Int {
        var progressValue = 0
        player?.let {
            var duration: Long? = it.duration
            val adController = it.getController(AdController::class.java)
            if (adController != null && adController.isAdDisplayed) {
                duration = adController.adDuration
            }
            if (duration!! > 0) {
                progressValue = (position * PROGRESS_BAR_MAX / duration).toInt()
            }
        }

        return progressValue
    }

    private fun positionValue(progress: Int): Long {
        var positionValue: Long = 0
        player?.let {
            var duration = it.duration
            val adController = it.getController(AdController::class.java)
            if (adController != null && adController.isAdDisplayed) {
                duration = adController.adDuration
            }
            positionValue = duration * progress / PROGRESS_BAR_MAX
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
            R.id.play ->
                player?.play()
            R.id.pause ->
                player?.pause()
            R.id.ffwd -> {
                //Do nothing for now
            }
            R.id.rew -> {
                //Do nothing for now
            }
            R.id.next -> {
                //Do nothing for now
            }
            R.id.prev -> {
                //Do nothing for now
            }
        }
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            tvCurTime.text = stringForTime(positionValue(progress))
        }
    }


    override fun onStartTrackingTouch(seekBar: SeekBar) {
        dragging = true
    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        dragging = false
        player?.seekTo(positionValue(seekBar.progress))
    }

    fun release() {
        removeCallbacks(updateProgressAction)
    }

    fun resume() {
        updateProgress()
    }
}
