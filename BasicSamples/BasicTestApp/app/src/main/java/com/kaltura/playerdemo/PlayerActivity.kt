package com.kaltura.playerdemo

import android.os.Bundle
import android.view.ViewGroup

import androidx.appcompat.app.AppCompatActivity

import com.kaltura.playkit.PKLog
import com.kaltura.tvplayer.KalturaPlayer
import kotlinx.android.synthetic.main.activity_player.*

import org.greenrobot.eventbus.EventBus

class PlayerActivity : AppCompatActivity() {

    private val log = PKLog.get("PlayerActivity")
    private var player: KalturaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        EventBus.getDefault().post(this)
    }

    override fun onPause() {
        player?.onApplicationPaused()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        player?.let {
            it.onApplicationResumed()
            it.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.let {
            player_container.removeAllViews()
            it.stop()
            it.destroy()
            player = null
        }
    }

    fun setPlayer(player: KalturaPlayer) {
        this.player = player

        val container = findViewById<ViewGroup>(R.id.player_container)
        container.addView(player.playerView)
    }
}
