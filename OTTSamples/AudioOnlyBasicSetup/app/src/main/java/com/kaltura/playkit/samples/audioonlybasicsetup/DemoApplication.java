package com.kaltura.playkit.samples.audioonlybasicsetup;

import android.app.Application;
import android.content.res.Configuration;

import com.kaltura.playkit.player.PKHttpClientManager;
import com.kaltura.tvplayer.KalturaPlayer;


public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        KalturaPlayer.initializeOTT(this, MainActivity.PARTNER_ID, MainActivity.SERVER_URL);
        doConnectionsWarmup();
    }
    private void doConnectionsWarmup() {
        PKHttpClientManager.setHttpProvider("okhttp");
        PKHttpClientManager.warmUp(
                "https://rest-as.ott.kaltura.com/crossdomain.xml",
                "https://api-preprod.ott.kaltura.com/crossdomain.xml",
                "https://cdnapisec.kaltura.com/favicon.ico",
                "https://cfvod.kaltura.com/favicon.ico"
        );
    }
    // Called by the system when the device configuration changes while your component is running.
    // Overriding this method is totally optional!
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    // This is called when the overall system is running low on memory,
    // and would like actively running processes to tighten their belts.
    // Overriding this method is totally optional!
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }
}