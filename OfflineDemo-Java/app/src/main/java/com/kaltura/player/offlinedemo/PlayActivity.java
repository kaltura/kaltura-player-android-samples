package com.kaltura.player.offlinedemo;

import android.app.AlertDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.player.AudioTrack;
import com.kaltura.playkit.player.BaseTrack;
import com.kaltura.playkit.player.PKTracks;
import com.kaltura.playkit.player.TextTrack;
import com.kaltura.tvplayer.KalturaBasicPlayer;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OfflineManager;
import com.kaltura.tvplayer.PlayerInitOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;
import static android.widget.Toast.LENGTH_LONG;

public class PlayActivity extends AppCompatActivity {

    private PKLog log = PKLog.get("PlayActivity");

    private KalturaPlayer player;
    private Drawable playDrawable;
    private Drawable pauseDrawable;

    private List<AudioTrack> audioTracks;
    private List<TextTrack> textTracks;

    private TextTrack currentTextTrack;
    private AudioTrack currentAudioTrack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        findViewById(R.id.playerRoot).setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN);

        playDrawable = ContextCompat.getDrawable(PlayActivity.this, R.drawable.ic_play_arrow_white_24dp);
        pauseDrawable = ContextCompat.getDrawable(PlayActivity.this, R.drawable.ic_pause_white_24dp);

        PlayerInitOptions options = new PlayerInitOptions().setAutoPlay(true);

        player = KalturaBasicPlayer.create(this, options);
        player.setPlayerView(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ((ConstraintLayout)findViewById(R.id.playerRoot)).addView(player.getPlayerView());

        OfflineManager manager = OfflineManager.getInstance(this);

        if (getIntent().getDataString() != null) {
            PKMediaEntry entry = null;
            try {
                entry = manager.getLocalPlaybackEntry(getIntent().getDataString());
                player.setMedia(entry);
            } catch (IOException e) {
                runOnUiThread(() -> Toast.makeText(this, "No asset id given", LENGTH_LONG).show());
                e.printStackTrace();
            }

        }

        findViewById(R.id.fab_playpause).setOnClickListener(v -> togglePlayPause());

        findViewById(R.id.fab_replay).setOnClickListener(v -> player.replay());

        findViewById(R.id.fab_replay_10).setOnClickListener(v -> player.seekTo(player.getCurrentPosition() - 10000));

        findViewById(R.id.fab_forward_10).setOnClickListener(v -> player.seekTo(player.getCurrentPosition() + 10000));

        findViewById(R.id.fab_audio_track).setOnClickListener(v -> selectPlayerTrack(true));

        findViewById(R.id.fab_text_track).setOnClickListener(v -> selectPlayerTrack(false));

        addPlayerEventListeners();
    }

    private void selectPlayerTrack(boolean audio) {

        List<String> trackTitles = new ArrayList<>();
        List<String> trackIds = new ArrayList<>();

        BaseTrack currentTrack;
        int currentIndex;

        if (audioTracks == null && textTracks == null) {
            return;
        }

        if (audioTracks != null) {
            for (AudioTrack track : audioTracks) {
                String language = null;

                if (audio && track != null) {
                    language = track.getLanguage();
                }

                if (language != null) {
                    trackIds.add(track.getUniqueId());
                    trackTitles.add(language);
                }
            }
        }

        if (textTracks != null) {
            for (TextTrack track : textTracks) {
                String language = null;

                if (audio && track != null) {
                    language = track.getLanguage();
                }

                if (language != null) {
                    trackIds.add(track.getUniqueId());
                    trackTitles.add(language);
                }
            }
        }

        if (trackIds.size() < 1) {
            Toast.makeText(this, "No tracks to select from", LENGTH_LONG).show();
            return;
        }

        if (audio) {
            currentTrack = currentAudioTrack;
        } else {
            currentTrack = currentTextTrack;
        }

        if (currentTrack != null) {
            currentIndex = trackIds.indexOf(currentTrack.getUniqueId());
        } else {
            return;
        }

        List<Integer> selected = new ArrayList<>(currentIndex);

        new AlertDialog.Builder(PlayActivity.this)
                .setTitle("Select track")
                .setSingleChoiceItems((CharSequence[]) trackTitles.toArray(), selected.get(0), (dialog, which) -> selected.add(which))
                .setPositiveButton("OK", (dialog, which) -> {
                    if (selected.get(0) >= 0) {
                        player.changeTrack(trackIds.get(selected.get(0)));
                    }
                })
                .show();
    }

    private void addPlayerEventListeners() {

        player.addListener(this, PlayerEvent.playing, event -> updatePlayPauseButton(true));

        player.addListener(this, PlayerEvent.tracksAvailable, event -> {
            PKTracks tracksInfo = event.tracksInfo;
            audioTracks = tracksInfo.getAudioTracks();
            textTracks = tracksInfo.getTextTracks();
            if (currentAudioTrack == null && !audioTracks.isEmpty()) {
                currentAudioTrack = audioTracks.get(tracksInfo.getDefaultAudioTrackIndex());
            }
            if (currentTextTrack != null && !textTracks.isEmpty()) {
                currentTextTrack = textTracks.get(tracksInfo.getDefaultTextTrackIndex());
            }
        });

        player.addListener(this, PlayerEvent.audioTrackChanged, event -> {
            currentAudioTrack = event.newTrack;
            if (currentAudioTrack != null) {
                log.d("currentAudioTrack: " + currentAudioTrack.getUniqueId() + " " + currentAudioTrack.getLanguage());
            }
        });

        player.addListener(this, PlayerEvent.textTrackChanged, event -> {
            currentTextTrack = event.newTrack;
            log.d("currentTextTrack: " + currentTextTrack);
        });
    }

    private void togglePlayPause() {
        boolean playing = player.isPlaying();

        if (playing) {
            player.pause();
        } else {
            player.play();
        }

        updatePlayPauseButton(!playing);
    }

    private void updatePlayPauseButton(boolean isPlaying) {
        Drawable next;

        if (isPlaying){
            next = pauseDrawable;
        } else {
            next = playDrawable;
        }

        ((ImageView)findViewById(R.id.fab_playpause)).setImageDrawable(next);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.destroy();
    }
}
