package com.kaltura.playkit.samples.broadpeak

import androidx.multidex.MultiDexApplication

import com.kaltura.tvplayer.KalturaOttPlayer

class DemoApplication: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()
        KalturaOttPlayer.initialize(this, MainActivity.PARTNER_ID, MainActivity.SERVER_URL);
    }
}