package com.kaltura.player.offlinedemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.kaltura.tvplayer.OfflineManager;

public class OfflineNotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // This notification receiver is only for the EXO downloads, don't use it for DTG provider as for that
        // app can create the notification on its own.
        OfflineManager offlineManager = OfflineManager.getInstance(context, OfflineManager.OfflineProvider.EXO);
        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {
                String pauseId = intent.getStringExtra("pause");
                String removeId = intent.getStringExtra("remove");
                if (action.equals("PauseButton") && pauseId != null) {
                    offlineManager.pauseAssetDownload(pauseId);
                } else if (action.equals("RemoveButton") && removeId != null) {
                    offlineManager.removeAsset(removeId);
                }
            }
        }
    }
}
