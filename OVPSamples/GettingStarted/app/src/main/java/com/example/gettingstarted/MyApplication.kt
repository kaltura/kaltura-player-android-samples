package com.example.gettingstarted

import android.app.Application
import com.example.gettingstarted.FullscreenActivity.Companion.OVP_SERVER_URL
import com.example.gettingstarted.FullscreenActivity.Companion.PARTNER_ID
import com.kaltura.tvplayer.KalturaPlayer

@Suppress("unused")
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        KalturaPlayer.initializeOVP(this, PARTNER_ID, OVP_SERVER_URL)
        //KalturaOttPlayer.initialize(this, MainActivity.PARTNER_ID, MainActivity.SERVER_URL);
    }
}
