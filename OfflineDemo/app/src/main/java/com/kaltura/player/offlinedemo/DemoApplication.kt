package com.kaltura.player.offlinedemo

import android.app.Application
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaOvpPlayer

@Suppress("unused") // Used by Android
class DemoApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        KalturaOttPlayer.initialize(this, 3009, "https://rest-us.ott.kaltura.com/v4_5/")
        KalturaOvpPlayer.initialize(this, 2215841, "https://cdnapisec.kaltura.com")
        KalturaOvpPlayer.initialize(this, 243342, "https://cdnapisec.kaltura.com")
        KalturaOvpPlayer.initialize(this, 2222401, "https://cdnapisec.kaltura.com")
        KalturaOvpPlayer.initialize(this, 4171, "https://cdnapisec.kaltura.com")
    }
}