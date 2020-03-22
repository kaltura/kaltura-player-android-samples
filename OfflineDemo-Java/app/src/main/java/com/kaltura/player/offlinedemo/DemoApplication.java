package com.kaltura.player.offlinedemo;

import android.app.Application;

import com.kaltura.tvplayer.OfflineManager;

import java.io.IOException;

public class DemoApplication extends Application implements OfflineManager.ManagerStartCallback {

    public boolean offlineManagerStarted = false;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            OfflineManager.getInstance(this).start(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStarted() {
        offlineManagerStarted = true;
    }
}
