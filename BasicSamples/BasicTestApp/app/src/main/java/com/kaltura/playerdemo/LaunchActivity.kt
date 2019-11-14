package com.kaltura.playerdemo

import android.content.Intent
import android.os.Bundle
import android.view.View

import androidx.appcompat.app.AppCompatActivity

import com.kaltura.playkit.player.PKHttpClientManager
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaOvpPlayer

class LaunchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)
        KalturaOttPlayer.initialize(this, 3009, "https://rest-us.ott.kaltura.com/v4_5/")
        KalturaOvpPlayer.initialize(this, 2215841, "https://cdnapisec.kaltura.com/")
        KalturaOvpPlayer.initialize(this, 2222401, "https://cdnapisec.kaltura.com/")
        KalturaOvpPlayer.initialize(this, 1091, "http://qa-apache-php7.dev.kaltura.com/")
        doConnectionsWarmup()

        findViewById<View>(R.id.btn_basic).setOnClickListener {startActivity(Intent(this@LaunchActivity, BasicDemoActivity::class.java)) }

        findViewById<View>(R.id.btn_ovp).setOnClickListener { startActivity(Intent(this@LaunchActivity, OVPDemoActivity::class.java)) }

        findViewById<View>(R.id.btn_ott).setOnClickListener { startActivity(Intent(this@LaunchActivity, OTTDemoActivity::class.java)) }
    }

    private fun doConnectionsWarmup() {
        PKHttpClientManager.setHttpProvider("okhttp")
        PKHttpClientManager.warmUp(
                "https://https://rest-us.ott.kaltura.com/crossdomain.xml",
                "https://rest-as.ott.kaltura.com/crossdomain.xml",
                "https://api-preprod.ott.kaltura.com/crossdomain.xml",
                "https://cdnapisec.kaltura.com/alive.html",
                "https://cfvod.kaltura.com/alive.html"
        )
    }
}
