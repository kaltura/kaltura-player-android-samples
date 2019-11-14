package com.kaltura.playerdemo

import android.os.Bundle
import android.view.ViewGroup

import androidx.appcompat.app.AppCompatActivity

import com.kaltura.playkit.PKLog
import com.kaltura.tvplayer.KalturaPlayer

import org.greenrobot.eventbus.EventBus

class PlayerActivity : AppCompatActivity() {

    private var player: KalturaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

        EventBus.getDefault().post(this)
    }

    override fun onPause() {
        if (player != null) {
            player!!.onApplicationPaused()
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (player != null) {
            player!!.onApplicationResumed()
            player!!.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            val container = findViewById<ViewGroup>(R.id.player_container)
            container.removeAllViews()

            player!!.stop()
            player!!.destroy()
            player = null
        }
    }

    fun setPlayer(player: KalturaPlayer) {
        this.player = player

        val container = findViewById<ViewGroup>(R.id.player_container)
        container.addView(player.playerView)
    }

    companion object {

        private val log = PKLog.get("PlayerActivity")
    }
}
