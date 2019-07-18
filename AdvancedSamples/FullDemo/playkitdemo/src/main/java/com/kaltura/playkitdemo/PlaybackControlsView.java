package com.kaltura.playkitdemo;

import android.content.Context;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.kaltura.android.exoplayer2.C;
import com.kaltura.android.exoplayer2.Timeline;
import com.kaltura.android.exoplayer2.ui.DefaultTimeBar;
import com.kaltura.android.exoplayer2.ui.TimeBar;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PlayerState;
import com.kaltura.playkit.ads.AdController;
import com.kaltura.tvplayer.KalturaPlayer;

import java.util.Formatter;
import java.util.Locale;

/**
 * Created by anton.afanasiev on 07/11/2016.
 */

public class PlaybackControlsView extends LinearLayout implements View.OnClickListener {

    private static final PKLog log = PKLog.get("PlaybackControlsView");
    private static final int PROGRESS_BAR_MAX = 100;

    private KalturaPlayer player;
    private PlayerState playerState;

    private Formatter formatter;
    private StringBuilder formatBuilder;

    private DefaultTimeBar seekBar;
    private TextView tvCurTime, tvTime;
    private ImageButton btnPlay, btnPause, btnFastForward, btnRewind, btnNext, btnPrevious, btnShuffle, btnRepeatToggle;

    private boolean dragging = false;

    private ComponentListener componentListener;

    private Runnable updateProgressAction = this::updateProgress;

    public PlaybackControlsView(Context context) {
        this(context, null);
    }

    public PlaybackControlsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlaybackControlsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.exo_playback_control_view, this);
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        componentListener = new ComponentListener();
        initPlaybackControls();
    }

    private void initPlaybackControls() {

        btnPlay = this.findViewById(R.id.exo_play);
        btnPause = this.findViewById(R.id.exo_pause);
        btnFastForward = this.findViewById(R.id.exo_ffwd);
        btnFastForward.setVisibility(GONE);
        btnRewind = this.findViewById(R.id.exo_rew);
        btnRewind.setVisibility(GONE);
        btnNext = this.findViewById(R.id.exo_next);
        btnPrevious = this.findViewById(R.id.exo_prev);
        btnRepeatToggle = this.findViewById(R.id.exo_repeat_toggle);
        btnRepeatToggle.setVisibility(GONE);
        btnShuffle = this.findViewById(R.id.exo_shuffle);
        btnShuffle.setVisibility(GONE);

        btnPlay.setOnClickListener(this);
        btnPause.setOnClickListener(this);
        btnFastForward.setOnClickListener(this);
        btnRewind.setOnClickListener(this);
        btnNext.setOnClickListener(this);
        btnPrevious.setOnClickListener(this);

        seekBar = this.findViewById(R.id.exo_progress);
        seekBar.setPlayedColor(getResources().getColor(R.color.colorAccent));
        seekBar.setBufferedColor(getResources().getColor(R.color.grey));
        seekBar.setUnplayedColor(getResources().getColor(R.color.black));
        seekBar.setScrubberColor(getResources().getColor(R.color.colorAccent));
        seekBar.addListener(componentListener);

        tvCurTime = this.findViewById(R.id.exo_position);
        tvTime = this.findViewById(R.id.exo_duration);
    }


    private void updateProgress() {
        long duration = C.TIME_UNSET;
        long position = C.POSITION_UNSET;
        long bufferedPosition = 0;
        if (player != null) {
            AdController adController = player.getController(AdController.class);
            if (adController != null && adController.isAdDisplayed()) {
                duration = adController.getAdDuration();
                position = adController.getAdCurrentPosition();

                //log.d("adController Duration:" + duration);
                //log.d("adController Position:" + position);
            } else {
                duration = player.getDuration();
                position = player.getCurrentPosition();
                //log.d("Duration:" + duration);
                //log.d("Position:" + position);
                bufferedPosition = player.getBufferedPosition();
            }
        }

        if (duration != C.TIME_UNSET) {
            //log.d("updateProgress Set Duration:" + duration);
            tvTime.setText(stringForTime(duration));
        }

        if (!dragging && position != C.POSITION_UNSET && duration != C.TIME_UNSET) {
            //log.d("updateProgress Set Position:" + position);
            tvCurTime.setText(stringForTime(position));
            seekBar.setPosition(progressBarValue(position));
            seekBar.setDuration(progressBarValue(duration));
        }

        seekBar.setBufferedPosition(progressBarValue(bufferedPosition));
        // Remove scheduled updates.
        removeCallbacks(updateProgressAction);
        // Schedule an update if necessary.
        if (playerState != PlayerState.IDLE) {
            long delayMs = 500;
            postDelayed(updateProgressAction, delayMs);
        }
    }

    /**
     * Component Listener for Default time bar from ExoPlayer UI
     */
    private final class ComponentListener
            implements com.kaltura.android.exoplayer2.Player.EventListener, TimeBar.OnScrubListener, OnClickListener {

        @Override
        public void onScrubStart(TimeBar timeBar, long position) {
            dragging = true;
        }

        @Override
        public void onScrubMove(TimeBar timeBar, long position) {
            if (player != null) {
                tvCurTime.setText(stringForTime((position * player.getDuration()) / PROGRESS_BAR_MAX));
            }
        }

        @Override
        public void onScrubStop(TimeBar timeBar, long position, boolean canceled) {
            dragging = false;
            if (player != null) {
                player.seekTo((position * player.getDuration()) / PROGRESS_BAR_MAX);

            }
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            updateProgress();
        }

        @Override
        public void onPositionDiscontinuity(@com.kaltura.android.exoplayer2.Player.DiscontinuityReason int reason) {
            updateProgress();
        }

        @Override
        public void onTimelineChanged(Timeline timeline, @Nullable Object manifest, @com.kaltura.android.exoplayer2.Player.TimelineChangeReason int reason) {
            updateProgress();
        }

        @Override
        public void onClick(View view) {
        }
    }

    private int progressBarValue(long position) {
        int progressValue = 0;
        if (player != null) {

            long duration = player.getDuration();
            //log.d("position = "  + position);
            //log.d("duration = "  + duration);
            AdController adController = player.getController(AdController.class);
            if (adController != null && adController.isAdDisplayed()) {
                duration = adController.getAdDuration();
            }
            if (duration > 0) {
                //log.d("position = "  + position);
                progressValue = Math.round((position * PROGRESS_BAR_MAX) / duration);
            }
            //log.d("progressValue = "  + progressValue);
        }

        return progressValue;
    }

    private long positionValue(long progress) {
        long positionValue = 0;
        if (player != null) {
            long duration = player.getDuration();
            AdController adController = player.getController(AdController.class);
            if (adController != null && adController.isAdDisplayed()) {
                duration = adController.getAdDuration();
            }
            positionValue = Math.round((duration * progress) / PROGRESS_BAR_MAX);
        }

        return positionValue;
    }

    private String stringForTime(long timeMs) {

        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        formatBuilder.setLength(0);
        return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    public void setPlayer(KalturaPlayer player) {
        this.player = player;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
        updateProgress();
    }

    public void setSeekBarStateForAd(boolean isAdPlaying) {
        seekBar.setEnabled(!isAdPlaying);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.exo_play:
                if (player != null) {
                    player.play();
                }
                break;
            case R.id.exo_pause:
                if (player != null) {
                    player.pause();
                }
                break;
            case R.id.exo_ffwd:
                //Do nothing for now
                break;
            case R.id.exo_rew:
                ///Do nothing for now
                break;
            case R.id.exo_next:
                //Do nothing for now
                break;
            case R.id.exo_prev:
                //Do nothing for now
                break;
        }
    }

    public void release() {
        removeCallbacks(updateProgressAction);
    }

    public void resume() {
        updateProgress();
    }

}
