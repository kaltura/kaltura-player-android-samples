package com.kaltura.playkitdemo

import android.app.Application
import android.content.res.Configuration

import com.kaltura.playkit.player.PKHttpClientManager
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaOvpPlayer
import com.kaltura.tvplayer.KalturaPlayer

import com.kaltura.playkitdemo.PartnersConfig.OTT_PARTNER_ID
import com.kaltura.playkitdemo.PartnersConfig.OTT_SERVER_URL
import com.kaltura.playkitdemo.PartnersConfig.OVP_PARTNER_ID
import com.kaltura.playkitdemo.PartnersConfig.OVP_PARTNER_ID_CLEAR
import com.kaltura.playkitdemo.PartnersConfig.OVP_PARTNER_ID_DRM
import com.kaltura.playkitdemo.PartnersConfig.OVP_PARTNER_ID_HLS
import com.kaltura.playkitdemo.PartnersConfig.OVP_PARTNER_ID_LIVE
import com.kaltura.playkitdemo.PartnersConfig.OVP_PARTNER_ID_LIVE_1
import com.kaltura.playkitdemo.PartnersConfig.OVP_PARTNER_ID_VR
import com.kaltura.playkitdemo.PartnersConfig.OVP_SERVER_URL
import com.kaltura.playkitdemo.PartnersConfig.OVP_SERVER_URL_CLEAR
import com.kaltura.playkitdemo.PartnersConfig.OVP_SERVER_URL_DRM
import com.kaltura.playkitdemo.PartnersConfig.OVP_SERVER_URL_HLS
import com.kaltura.playkitdemo.PartnersConfig.OVP_SERVER_URL_LIVE
import com.kaltura.playkitdemo.PartnersConfig.OVP_SERVER_URL_LIVE_1
import com.kaltura.playkitdemo.PartnersConfig.OVP_SERVER_URL_VR


class DemoApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        KalturaOttPlayer.initialize(this, OTT_PARTNER_ID, OTT_SERVER_URL)
        KalturaOvpPlayer.initialize(this, OVP_PARTNER_ID, OVP_SERVER_URL)
        KalturaOvpPlayer.initialize(this, OVP_PARTNER_ID_HLS, OVP_SERVER_URL_HLS)
        KalturaOvpPlayer.initialize(this, OVP_PARTNER_ID_DRM, OVP_SERVER_URL_DRM)
        KalturaOvpPlayer.initialize(this, OVP_PARTNER_ID_VR, OVP_SERVER_URL_VR)
        KalturaOvpPlayer.initialize(this, OVP_PARTNER_ID_CLEAR, OVP_SERVER_URL_CLEAR)
        KalturaOvpPlayer.initialize(this, OVP_PARTNER_ID_LIVE, OVP_SERVER_URL_LIVE)
        KalturaOvpPlayer.initialize(this, OVP_PARTNER_ID_LIVE_1, OVP_SERVER_URL_LIVE_1)
        doConnectionsWarmup()
    }

    private fun doConnectionsWarmup() {
        PKHttpClientManager.setHttpProvider("okhttp")
        PKHttpClientManager.warmUp(
                "https://rest-as.ott.kaltura.com/crossdomain.xml",
                "https://api-preprod.ott.kaltura.com/crossdomain.xml",
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