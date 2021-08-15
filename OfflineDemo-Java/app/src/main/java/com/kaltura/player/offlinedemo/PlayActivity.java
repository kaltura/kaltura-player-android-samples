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

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PlayerEvent;
import com.kaltura.playkit.Utils;
import com.kaltura.playkit.player.AudioTrack;
import com.kaltura.playkit.player.BaseTrack;
import com.kaltura.playkit.player.PKTracks;
import com.kaltura.playkit.player.TextTrack;
import com.kaltura.tvplayer.KalturaBasicPlayer;
import com.kaltura.tvplayer.KalturaOttPlayer;
import com.kaltura.tvplayer.KalturaOvpPlayer;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OTTMediaOptions;
import com.kaltura.tvplayer.OVPMediaOptions;
import com.kaltura.tvplayer.OfflineManager;
import com.kaltura.tvplayer.PlayerInitOptions;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
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
    private List<Item> testItems;

    private TextTrack currentTextTrack;
    private AudioTrack currentAudioTrack;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play);

        findViewById(R.id.playerRoot).setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN);

        playDrawable = ContextCompat.getDrawable(PlayActivity.this, R.drawable.ic_play_arrow_white_24dp);
        pauseDrawable = ContextCompat.getDrawable(PlayActivity.this, R.drawable.ic_pause_white_24dp);

        Bundle bundle = getIntent().getBundleExtra("assetBundle");
        boolean isOnlinePlayback = bundle != null && bundle.getBoolean("isOnlinePlayback", false);
        int position = bundle != null ? bundle.getInt("position", -1) : -1;
        int partnerId = bundle != null ? bundle.getInt("partnerId") : -1;

        if (isOnlinePlayback) {
            String itemsJson = Utils.readAssetToString(this, "items.json");
            Gson gson = new Gson();
            Type itemJsonType = new TypeToken<ArrayList<ItemJSON>>(){}.getType();
            List<ItemJSON> itemJson = gson.fromJson(itemsJson, itemJsonType);
            testItems = new ArrayList<>();
            for (int index = 0; index < itemJson.size(); index++) {
                testItems.add(itemJson.get(index).toItem());
            }
        }

        PlayerInitOptions options = new PlayerInitOptions(partnerId).setAutoPlay(true).setAllowCrossProtocolEnabled(true);

        if (isOnlinePlayback && testItems != null && !testItems.isEmpty()) {
            playAssetOnline(testItems, position, options);
        } else {
            player = KalturaBasicPlayer.create(this, options);
            OfflineManager manager = OfflineManager.getInstance(this, OfflineManager.OfflineProvider.DTG);
            if (getIntent().getDataString() != null) {
                PKMediaEntry entry = null;
                try {
                    entry = manager.getLocalPlaybackEntry(getIntent().getDataString());
                    player.setMedia(entry);
                } catch (IOException e) {
                    runOnUiThread(() -> Toast.makeText(this, "No asset id given", LENGTH_LONG).show());
                    e.printStackTrace();
                }
            } else {
                runOnUiThread(() -> Toast.makeText(this, "No asset id given", LENGTH_LONG).show());
            }
        }

        player.setPlayerView(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        ((ConstraintLayout)findViewById(R.id.playerRoot)).addView(player.getPlayerView());


        findViewById(R.id.fab_playpause).setOnClickListener(v -> togglePlayPause());

        findViewById(R.id.fab_replay).setOnClickListener(v -> player.replay());

        findViewById(R.id.fab_replay_10).setOnClickListener(v -> player.seekTo(player.getCurrentPosition() - 10000));

        findViewById(R.id.fab_forward_10).setOnClickListener(v -> player.seekTo(player.getCurrentPosition() + 10000));

        findViewById(R.id.fab_audio_track).setOnClickListener(v -> selectPlayerTrack(true));

        findViewById(R.id.fab_text_track).setOnClickListener(v -> selectPlayerTrack(false));

        addPlayerEventListeners();
    }

    private void playAssetOnline(List<Item> itemList, int position, PlayerInitOptions options) {
        Item item = itemList.get(position);
        if (item instanceof OTTItem) {
            player = KalturaOttPlayer.create(this, options);
            player.loadMedia((OTTMediaOptions) ((OTTItem) item).mediaOptions(), (mediaOptions, entry, error) -> {
                if (error != null) {
                    log.d("OTTMedia Error Extra = " + error.getExtra());
                    runOnUiThread(() -> Snackbar.make(
                            findViewById(android.R.id.content),
                            error.getMessage(),
                            Snackbar.LENGTH_LONG
                    ).show());
                } else {
                    log.d("OTTMediaAsset onEntryLoadComplete entry =" + entry.getId());
                }
            });
        } else if (item instanceof OVPItem) {
            player = KalturaOvpPlayer.create(this, options);
            player.loadMedia((OVPMediaOptions) ((OVPItem) item).mediaOptions(), (mediaOptions, entry, error) -> {
                if (error != null) {
                    log.d("OVPMedia Error Extra = " + error.getExtra());
                    runOnUiThread(() -> Snackbar.make(
                            findViewById(android.R.id.content),
                            error.getMessage(),
                            Snackbar.LENGTH_LONG
                    ).show());
                } else {
                    log.d("OVPMediaAsset onEntryLoadComplete entry =" + entry.getId());
                }
            });
        } else if (item instanceof BasicItem){
            if (item.getEntry() != null) {
                player = KalturaBasicPlayer.create(this, options);
                player.setMedia(item.getEntry());
            }
        } else {
            Toast.makeText(this, "No Player Type found", LENGTH_LONG).show();
        }
    }

    private void selectPlayerTrack(boolean audio) {
        List<String> trackTitles = new ArrayList<>();
        List<String> trackIds = new ArrayList<>();

        BaseTrack currentTrack;
        int currentIndex;

        if (audioTracks == null && textTracks == null) {
            return;
        }

        if (audio && audioTracks != null) {
            for (AudioTrack track : audioTracks) {
                String language = null;

                if (track != null) {
                    language = track.getLanguage();
                }

                if (language != null) {
                    trackIds.add(track.getUniqueId());
                    trackTitles.add(language);
                }
            }
        }

        if (!audio && textTracks != null) {
            for (TextTrack track : textTracks) {
                String language = null;

                if (track != null) {
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

        List<Integer> selected = new ArrayList<>(Collections.singletonList(currentIndex));

        ArrayList<CharSequence> charSequenceArrayList = new ArrayList<>();
        for (String title : trackTitles) {
            charSequenceArrayList.add(title);
        }
        CharSequence [] charSequenceArray = charSequenceArrayList.toArray(new CharSequence[0]);
        new AlertDialog.Builder(PlayActivity.this)
                .setTitle("Select track")
                .setSingleChoiceItems(charSequenceArray, selected.get(0), (dialog, which) -> selected.add(0,which))
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
            if (currentTextTrack == null && !textTracks.isEmpty()) {
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
    protected void onPause() {
        if (player != null && player.isPlaying()) {
            updatePlayPauseButton(player.isPlaying());
            player.onApplicationPaused();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            updatePlayPauseButton(player.isPlaying());
            player.onApplicationResumed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        player.destroy();
    }
}
