package com.kaltura.playkit.samples.basicsample

import android.content.Context
import android.content.res.Configuration
import androidx.multidex.MultiDexApplication

import com.kaltura.playkit.player.PKHttpClientManager
import com.kaltura.tvplayer.KalturaOttPlayer

class DemoApplication : MultiDexApplication() {

    companion object {
        private lateinit var instance: DemoApplication
        fun getApplicationContext(): Context {
            return instance.applicationContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

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