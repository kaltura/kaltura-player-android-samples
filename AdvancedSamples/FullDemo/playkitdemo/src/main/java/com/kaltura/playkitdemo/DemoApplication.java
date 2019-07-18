package com.kaltura.playkitdemo;

import android.app.Application;
import android.content.res.Configuration;

import com.kaltura.playkit.player.PKHttpClientManager;
import com.kaltura.tvplayer.KalturaPlayer;


public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //KalturaPlayer.initializeOTT(this, MainActivity.VOOT_OTT_PARTNER_ID, MainActivity.VOOT_OTT_SERVER_URL);
        KalturaPlayer.initializeOTT(this, MainActivity.OTT_PARTNER_ID, MainActivity.OTT_SERVER_URL);
        KalturaPlayer.initializeOVP(this, MainActivity.OVP_PARTNER_ID, MainActivity.OVP_SERVER_URL);
        KalturaPlayer.initializeOVP(this, MainActivity.OVP_PARTNER_ID_HLS, MainActivity.OVP_SERVER_URL_HLS);
        KalturaPlayer.initializeOVP(this, MainActivity.OVP_PARTNER_ID_DRM, MainActivity.OVP_SERVER_URL_DRM);
        KalturaPlayer.initializeOVP(this, MainActivity.OVP_PARTNER_ID_VR, MainActivity.OVP_SERVER_URL_VR);
        KalturaPlayer.initializeOVP(this, MainActivity.OVP_PARTNER_ID_CLEAR, MainActivity.OVP_SERVER_URL_CLEAR);
        KalturaPlayer.initializeOVP(this, MainActivity.OVP_PARTNER_ID_LIVE, MainActivity.OVP_SERVER_URL_LIVE);
        KalturaPlayer.initializeOVP(this, MainActivity.OVP_PARTNER_ID_LIVE_1, MainActivity.OVP_SERVER_URL_LIVE_1);
        //doConnectionsWarmup();
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