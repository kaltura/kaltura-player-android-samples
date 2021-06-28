package com.kaltura.player.offlinedemo

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kaltura.dtg.DownloadItem
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PlayerEvent.*
import com.kaltura.playkit.Utils
import com.kaltura.playkit.player.AudioTrack
import com.kaltura.playkit.player.PKTracks
import com.kaltura.playkit.player.TextTrack
import com.kaltura.playkit.player.VideoTrack
import com.kaltura.tvplayer.KalturaBasicPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OfflineManager
import com.kaltura.tvplayer.PlayerInitOptions
import com.kaltura.tvplayer.*
import com.kaltura.tvplayer.config.PhoenixTVPlayerParams
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.android.synthetic.main.content_play.*

private val log = PKLog.get("PlayActivity")

class PlayActivity : AppCompatActivity() {

    private lateinit var player: KalturaPlayer
    private lateinit var playDrawable: Drawable
    private lateinit var pauseDrawable: Drawable

    private var audioTracks: List<AudioTrack>? = null
    private var textTracks: List<TextTrack>? = null
    private var videoTracks: List<VideoTrack>? = null

    private var currentTextTrack: TextTrack? = null
    private var currentAudioTrack: AudioTrack? = null
    private var currentVideoTrack: VideoTrack? = null

    private var testItems: List<Item>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        playerRoot.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN

        playDrawable = ContextCompat.getDrawable(this@PlayActivity, R.drawable.ic_play_arrow_white_24dp)!!
        pauseDrawable = ContextCompat.getDrawable(this@PlayActivity, R.drawable.ic_pause_white_24dp)!!

        val bundle = intent.getBundleExtra("assetBundle")
        val isOnlinePlayback = bundle?.getBoolean("isOnlinePlayback") ?: false
        val position = bundle?.getInt("position") ?: -1
        val partnerId = bundle?.getInt("partnerId")

        if (isOnlinePlayback) {
            val itemsJson = Utils.readAssetToString(this, "items.json")
            val gson = Gson()
            val items = gson.fromJson(itemsJson, Array<ItemJSON>::class.java)
            testItems = items.map { it.toItem() }
        }

        val options = PlayerInitOptions(partnerId).apply {
            autoplay = true
            allowCrossProtocolEnabled = true
        }
        isOnlinePlayback.let {
            if (it) {
                intent.dataString?.let {
                    playAssetOffline(it, options)
                } ?: run {
                    testItems?.let { itemList ->
                        playAssetOnline(itemList, position, options)
                    }
                }
            } else {
                intent.dataString?.let {
                    playAssetOffline(it, options)
                } ?: run {
                    Toast.makeText(this, "No asset id given", LENGTH_LONG).show()
                }
            }
        }

        player.setPlayerView(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        playerRoot.addView(player.playerView)

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

        fab_video_track.setOnClickListener {
            selectPlayerTrack(DownloadItem.TrackType.VIDEO)
        }

        fab_audio_track.setOnClickListener {
            selectPlayerTrack(DownloadItem.TrackType.AUDIO)
        }

        fab_text_track.setOnClickListener {
            selectPlayerTrack(DownloadItem.TrackType.TEXT)
        }

        addPlayerEventListeners()
    }

    private fun playAssetOffline(assetId: String, options: PlayerInitOptions) {
        val manager = OfflineManager.getInstance(this)
        player = KalturaBasicPlayer.create(this, options)
        val entry = manager.getLocalPlaybackEntry(assetId)
        player.setMedia(entry)
    }

    private fun playAssetOnline(itemList: List<Item>, position: Int, options: PlayerInitOptions) {
        when (val item: Item = itemList[position]) {
            is OTTItem -> {
                player = KalturaOttPlayer.create(this, options)
                
                player.loadMedia(item.mediaOptions()) { mediaOptions, entry, error ->
                    if (error != null) {
                        log.d("OTTMedia Error error = " + error.message + " Extra = " + error.extra)
                        runOnUiThread {
                            Snackbar.make(
                                findViewById<View>(android.R.id.content),
                                error.message,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        log.d("OTTMediaAsset onEntryLoadComplete entry =" + entry.id)
                    }
                }
            }
            is OVPItem -> {
                player = KalturaOvpPlayer.create(this, options)
                player.loadMedia(item.mediaOptions()) { mediaOptions, entry, error ->
                    if (error != null) {
                        log.d("OVPMedia Error error = " + error.message + " Extra = " + error.extra)
                        runOnUiThread {
                            Snackbar.make(
                                findViewById<View>(android.R.id.content),
                                error.message,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        log.d("OVPMediaAsset onEntryLoadComplete entry =" + entry.id)
                    }
                }
            }
            is BasicItem -> {
                item.entry?.let {
                    player = KalturaBasicPlayer.create(this, options)
                    player.setMedia(it)
                }
            }
            else -> {
                Toast.makeText(this, "No Player Type found", LENGTH_LONG).show()
            }
        }
    }

    private fun selectPlayerTrack(trackType: DownloadItem.TrackType) {
        val trackTitles = arrayListOf<String>()
        val trackIds = arrayListOf<String>()

        when (trackType) {
            DownloadItem.TrackType.AUDIO -> {
                val tracks = audioTracks
                val trackTitles = arrayListOf<String>()
                val trackIds = arrayListOf<String>()

                if (tracks != null) {
                    for (track in tracks) {
                        val language = track.language
                        if (language != null) {
                            trackIds.add(track.uniqueId)
                            trackTitles.add(language)
                        }
                    }
                }
                if (trackIds.size < 1) {
                    Toast.makeText(this, "No tracks to select from", LENGTH_LONG).show()
                    return
                }

                val currentTrack = currentAudioTrack
                val currentIndex =
                    if (currentTrack != null) trackIds.indexOf(currentTrack.uniqueId) else -1
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
            DownloadItem.TrackType.TEXT -> {
                val tracks = textTracks
                val trackTitles = arrayListOf<String>()
                val trackIds = arrayListOf<String>()
                if (tracks != null) {
                    for (track in tracks) {
                        val language = track.language
                        if (language != null) {
                            trackIds.add(track.uniqueId)
                            trackTitles.add(language)
                        }
                    }
                }
                if (trackIds.size < 1) {
                    Toast.makeText(this, "No tracks to select from", LENGTH_LONG).show()
                    return
                }

                val currentTrack = currentTextTrack
                val currentIndex =
                    if (currentTrack != null) trackIds.indexOf(currentTrack.uniqueId) else -1
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
            DownloadItem.TrackType.VIDEO -> {
                val tracks = videoTracks
                val trackTitles = arrayListOf<String>()
                val trackIds = arrayListOf<String>()
                if (tracks != null) {
                    for (track in tracks) {
                        val bitrate = track.bitrate
                        if (bitrate != null) {
                            trackIds.add(track.uniqueId)
                            if (bitrate == 0L) {
                                trackTitles.add("Auto")
                            } else {
                                trackTitles.add(bitrate.toString())
                            }
                        }
                    }
                }
                if (trackIds.size < 1) {
                    Toast.makeText(this, "No tracks to select from", LENGTH_LONG).show()
                    return
                }

                val currentTrack = currentVideoTrack
                val currentIndex =
                    if (currentTrack != null) trackIds.indexOf(currentTrack.uniqueId) else -1
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
        }
    }

    private fun addPlayerEventListeners() {

        player.addListener(this, playing) {
            updatePlayPauseButton(true)
        }

        player.addListener(this, tracksAvailable) {
            val tracksInfo: PKTracks = it.tracksInfo
            audioTracks = tracksInfo.audioTracks
            textTracks = tracksInfo.textTracks
            videoTracks = tracksInfo.videoTracks
            if (currentAudioTrack == null && audioTracks!!.isNotEmpty()) {
                currentAudioTrack = audioTracks!![tracksInfo.defaultAudioTrackIndex]
            }
            if (currentTextTrack == null && textTracks!!.isNotEmpty()) {
                currentTextTrack = textTracks!![tracksInfo.defaultTextTrackIndex]
            }
            if (currentVideoTrack == null && videoTracks!!.isNotEmpty()) {
                currentVideoTrack = videoTracks!![tracksInfo.defaultVideoTrackIndex]
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

        player.addListener(this, videoTrackChanged) {
            currentVideoTrack = it.newTrack
            log.d("currentVideoTrack: $currentVideoTrack")
        }

        player.addListener(this, error) {
            var message: String? = it.error.message
            log.e("error: ${it.error.errorType} $message")
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
