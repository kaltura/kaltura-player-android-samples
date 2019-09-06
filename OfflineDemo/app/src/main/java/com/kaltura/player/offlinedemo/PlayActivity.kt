package com.kaltura.player.offlinedemo

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.kaltura.playkit.PlayerEvent
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OfflineManager
import com.kaltura.tvplayer.PlayerInitOptions
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.android.synthetic.main.content_play.*

class PlayActivity : AppCompatActivity() {

    private lateinit var player: KalturaPlayer
    private lateinit var playDrawable: Drawable
    private lateinit var pauseDrawable: Drawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        playerRoot.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN

        playDrawable = ContextCompat.getDrawable(this@PlayActivity, R.drawable.ic_play_arrow_white_24dp)!!
        pauseDrawable = ContextCompat.getDrawable(this@PlayActivity, R.drawable.ic_pause_white_24dp)!!

        val options = PlayerInitOptions().apply {
            autoplay = true
        }

        player = KalturaPlayer.createBasicPlayer(this, options)
        player.setPlayerView(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        playerRoot.addView(player.playerView)


        val manager = OfflineManager.getInstance(this)

        intent.dataString?.let {
            manager.sendAssetToPlayer(it, player)
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

        player.addListener(this, PlayerEvent.playing) {
            updatePlayPauseButton(true)
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
