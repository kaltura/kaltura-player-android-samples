package com.kaltura.player.offlinedemo

import android.app.Application
import com.kaltura.tvplayer.KalturaOttPlayer

@Suppress("unused") // Used by Android
class DemoApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        KalturaOttPlayer.initialize(this, 3009, "https://rest-us.ott.kaltura.com/v4_5/api_v3/")
    }
}