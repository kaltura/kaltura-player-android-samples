package com.kaltura.playkit.samples.broadpeak

import android.content.Context
import androidx.multidex.MultiDexApplication

import com.kaltura.tvplayer.KalturaOttPlayer

class DemoApplication: MultiDexApplication() {

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
}