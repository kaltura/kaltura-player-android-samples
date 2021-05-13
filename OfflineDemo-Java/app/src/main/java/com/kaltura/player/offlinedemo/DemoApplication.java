package com.kaltura.player.offlinedemo;

import android.app.Application;

import com.kaltura.tvplayer.KalturaOttPlayer;
import com.kaltura.tvplayer.KalturaOvpPlayer;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        KalturaOttPlayer.initialize(this, 3009, "https://rest-us.ott.kaltura.com/v4_5/");
        KalturaOvpPlayer.initialize(this, 2215841, "https://cdnapisec.kaltura.com");
        KalturaOvpPlayer.initialize(this, 243342, "https://cdnapisec.kaltura.com");
        KalturaOvpPlayer.initialize(this, 2222401, "https://cdnapisec.kaltura.com");
        KalturaOvpPlayer.initialize(this, 4171, "https://cdnapisec.kaltura.com");
    }
}
