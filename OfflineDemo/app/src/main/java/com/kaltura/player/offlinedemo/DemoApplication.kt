package com.kaltura.player.offlinedemo

import android.app.Application
import com.kaltura.tvplayer.OfflineManager

@Suppress("unused") // Used by Android
class DemoApplication: Application(), OfflineManager.ManagerStartCallback {

    var offlineManagerStarted = false

    override fun onStarted() {
        offlineManagerStarted = true
    }

    override fun onCreate() {
        super.onCreate()

        OfflineManager.getInstance(this).start(this)
    }
}
