package com.kaltura.playkit.samples.recyclerviewsample

import android.app.Application
import android.content.res.Configuration

import com.kaltura.playkit.player.PKHttpClientManager
import com.kaltura.tvplayer.KalturaOttPlayer

class DemoApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        KalturaOttPlayer.initialize(this, MainActivity.PARTNER_ID, MainActivity.SERVER_URL)
        doConnectionsWarmup()
    }

    private fun doConnectionsWarmup() {
        PKHttpClientManager.setHttpProvider("okhttp")
        PKHttpClientManager.warmUp(
                "https://rest-us.ott.kaltura.com/crossdomain.xml",
                "https://cdnapisec.kaltura.com/favicon.ico",
                "https://cfvod.kaltura.com/favicon.ico"
        )
    }

    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    override fun onLowMemory() {
        super.onLowMemory()
    }
}