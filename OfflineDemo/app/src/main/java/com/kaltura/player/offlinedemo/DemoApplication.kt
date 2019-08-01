package com.kaltura.player.offlinedemo

import android.app.Application
import com.kaltura.tvplayer.OfflineManager

@Suppress("unused") // Used by Android
class DemoApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        OfflineManager.getInstance(this)
    }
}
