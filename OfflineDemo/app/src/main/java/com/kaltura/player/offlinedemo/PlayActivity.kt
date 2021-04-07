package com.kaltura.player.offlinedemo

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PlayerEvent.*
import com.kaltura.playkit.player.AudioTrack
import com.kaltura.playkit.player.PKTracks
import com.kaltura.playkit.player.TextTrack
import com.kaltura.tvplayer.KalturaBasicPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OfflineManager
import com.kaltura.tvplayer.PlayerInitOptions
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.android.synthetic.main.content_play.*

private val log = PKLog.get("PlayActivity")

class PlayActivity : AppCompatActivity() {

    private lateinit var player: KalturaPlayer
    private lateinit var playDrawable: Drawable
    private lateinit var pauseDrawable: Drawable

    private var audioTracks: List<AudioTrack>? = null
    private var textTracks: List<TextTrack>? = null

    private var currentTextTrack: TextTrack? = null
    private var currentAudioTrack: AudioTrack? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        playerRoot.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN

        playDrawable = ContextCompat.getDrawable(this@PlayActivity, R.drawable.ic_play_arrow_white_24dp)!!
        pauseDrawable = ContextCompat.getDrawable(this@PlayActivity, R.drawable.ic_pause_white_24dp)!!

        val options = PlayerInitOptions().apply {
            autoplay = true
        }

        player = KalturaBasicPlayer.create(this, options)
        player.setPlayerView(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        playerRoot.addView(player.playerView)

        val manager = OfflineManager.getInstance(this)

        intent.dataString?.let {
            val entry = manager.getLocalPlaybackEntry(it)
            player.setMedia(entry)
        } ?: run {
            Toast.makeText(this, "No asset id given", LENGTH_LONG)
        }

        fab_playpause.setOnClickListener {
            togglePlayPause()
        }

        fab_replay.setOnClickListener {
            player.replay()
        }

        fab_replay_10.setOnClickListener {
            player.seekTo(player.currentPosition - 10000)
        }

        fab_forward_10.setOnClickListener {
            player.seekTo(player.currentPosition + 10000)
        }

        fab_audio_track.setOnClickListener {
            selectPlayerTrack(true)
        }

        fab_text_track.setOnClickListener {
            selectPlayerTrack(false)
        }

        addPlayerEventListeners()
    }

    private fun selectPlayerTrack(audio: Boolean) {
        val tracks = (if (audio) audioTracks else textTracks) ?: return
        val trackTitles = arrayListOf<String>()
        val trackIds = arrayListOf<String>()

        for (track in tracks) {
            val language =
                if (audio) (track as AudioTrack).language else (track as TextTrack).language
            if (language != null) {
                trackIds.add(track.uniqueId)
                trackTitles.add(language)
            }
        }

        if (trackIds.size < 1) {
            Toast.makeText(this, "No tracks to select from", LENGTH_LONG).show()
            return
        }

        val currentTrack = if (audio) currentAudioTrack else currentTextTrack
        val currentIndex = if (currentTrack != null) trackIds.indexOf(currentTrack.uniqueId) else -1
        val selected = intArrayOf(currentIndex)
        Builder(this)
            .setTitle("Select track")
            .setSingleChoiceItems(trackTitles.toTypedArray(), selected[0]) { _, i ->
                selected[0] = i
            }
            .setPositiveButton("OK") { _, _ ->
                if (selected[0] >= 0) {
                    player.changeTrack(trackIds[selected[0]])
                }
            }.show()
    }

    private fun addPlayerEventListeners() {
        player.addListener(this, playing) {
            updatePlayPauseButton(true)
        }

        player.addListener(this, tracksAvailable) {
            val tracksInfo: PKTracks = it.tracksInfo
            audioTracks = tracksInfo.audioTracks
            textTracks = tracksInfo.textTracks
            if (currentAudioTrack == null && audioTracks!!.isNotEmpty()) {
                currentAudioTrack = audioTracks!![tracksInfo.defaultAudioTrackIndex]
            }
            if (currentTextTrack == null && textTracks!!.isNotEmpty()) {
                currentTextTrack = textTracks!![tracksInfo.defaultTextTrackIndex]
            }
        }

        player.addListener(this, audioTrackChanged) {
            currentAudioTrack = it.newTrack
            currentAudioTrack?.let { track ->
                log.d("currentAudioTrack: ${track.uniqueId} ${track.language}")
            }
        }

        player.addListener(this, textTrackChanged) {
            currentTextTrack = it.newTrack
            log.d("currentTextTrack: $currentTextTrack")
        }
    }

    private fun togglePlayPause() {
        val playing = player.isPlaying

        if (playing) {
            player.pause()
        } else {
            player.play()
        }

        updatePlayPauseButton(!playing)
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        val next = if (isPlaying) pauseDrawable else playDrawable
        fab_playpause.setImageDrawable(next)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.destroy()
    }
}
