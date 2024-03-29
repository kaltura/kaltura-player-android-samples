package com.kaltura.playkit.samples.prefetchsample.ui;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.kaltura.android.exoplayer2.C;
import com.kaltura.android.exoplayer2.offline.Download;
import com.kaltura.android.exoplayer2.offline.DownloadManager;
import com.kaltura.android.exoplayer2.scheduler.Requirements;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.samples.prefetchsample.OfflineNotificationReceiver;
import com.kaltura.playkit.samples.prefetchsample.R;
import com.kaltura.tvplayer.OfflineManager;
import com.kaltura.tvplayer.offline.exo.ExoOfflineNotificationHelper;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class OfflineCustomNotification extends ExoOfflineNotificationHelper {

    PKLog log = PKLog.get(OfflineCustomNotification.class.getSimpleName());

    private final NotificationManagerCompat notificationManager;
    private final NotificationCompat.Builder notificationBuilder;
    private boolean areActionButtonsAdded;
    private final Intent pauseIntent;
    private final Intent removeIntent;

    public OfflineCustomNotification(Context context, String channelId) {
        super(context, channelId);
        notificationManager = NotificationManagerCompat.from(context);
        notificationBuilder = new NotificationCompat.Builder(context.getApplicationContext(), getChannelID());
        notificationBuilder.setAllowSystemGeneratedContextualActions(false);
        pauseIntent = new Intent(context, OfflineNotificationReceiver.class).setAction("PauseButton");
        removeIntent = new Intent(context, OfflineNotificationReceiver.class).setAction("RemoveButton");
        notificationBuilder.setSmallIcon(R.drawable.ic_cloud_download_black_24dp);
        notificationBuilder.setContentIntent(null);
    }

    @Override
    public Notification buildNotification(Context context,
                                          @Nullable PendingIntent contentIntent,
                                          int notificationId,
                                          @NonNull List<Download> downloads,
                                          @Requirements.RequirementFlags int notMetRequirements) {

        log.d(" Custom Notification: buildNotification");

        if (downloads.size() > 0) {
            Download download = downloads.get(0);
            log.d("download.request.id => " + download.request.id);
            log.d("download.state => " + download.state);
            float downloadPercentage = download.getPercentDownloaded();
            if (downloadPercentage != C.PERCENTAGE_UNSET) {
                return getProgressNotification(context, download, notMetRequirements);
            }
        }
        return removeProgressNotification(notificationId);
    }

    @Override
    public DownloadManager.Listener getDownloadManagerListener(Context context) {
        return new DownloadManager.Listener() {
            @Override
            public void onDownloadChanged(@NotNull DownloadManager downloadManager, @NonNull Download download, @Nullable Exception finalException) {
                Notification notification;
                if (download.state == Download.STATE_COMPLETED || (download.state == Download.STATE_STOPPED)) {
                    notification = getStaticNotification(OfflineManager.AssetDownloadState.completed);
                } else if (download.state == Download.STATE_FAILED) {
                    notification = getStaticNotification(OfflineManager.AssetDownloadState.failed);
                } else if (download.state == Download.STATE_REMOVING) {
                    notification = getStaticNotification(OfflineManager.AssetDownloadState.removing);
                } else if (download.state == Download.STATE_QUEUED) {
                    notification = getStaticNotification(OfflineManager.AssetDownloadState.paused);
                } else {
                    return;
                }
                notificationManager.notify(56324, notification);
            }
        };
    }

    public Notification removeProgressNotification(int notificationId) {
        notificationBuilder.setProgress(0,0, false);
        Notification notification = notificationBuilder.build();
        notificationManager.notify(notificationId, notification);
        return notification;
    }

    public Notification getProgressNotification(Context context,
                                                Download download,
                                                @Requirements.RequirementFlags int notMetRequirements) {
        notificationManager.cancelAll();

        int titleId = R.string.downloading_asset;
        if ((notMetRequirements & Requirements.NETWORK_UNMETERED) != 0) {
            titleId = R.string.download_paused_for_wifi;
        } else if ((notMetRequirements & Requirements.NETWORK) != 0) {
            titleId = R.string.download_paused_for_network;
        }

        notificationBuilder.setContentTitle(context.getString(titleId));
        notificationBuilder.setShowWhen(false);

        if (download != null && !areActionButtonsAdded) {
            pauseIntent.putExtra("pause", download.request.id);
            removeIntent.putExtra("remove", download.request.id);
            PendingIntent pausePendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pausePendingIntent = PendingIntent.getBroadcast(context, new Random().nextInt(), pauseIntent, PendingIntent.FLAG_IMMUTABLE);
            } else {
                pausePendingIntent = PendingIntent.getBroadcast(context, new Random().nextInt(), pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            PendingIntent removePendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                removePendingIntent = PendingIntent.getBroadcast(context, new Random().nextInt(), removeIntent, PendingIntent.FLAG_IMMUTABLE);
            } else {
                removePendingIntent = PendingIntent.getBroadcast(context, new Random().nextInt(), removeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            }
            notificationBuilder.addAction(R.drawable.pause, "Pause", pausePendingIntent);
            notificationBuilder.addAction(android.R.drawable.ic_menu_close_clear_cancel, "Remove", removePendingIntent);
            areActionButtonsAdded = true;
        }

        if (download != null) {
            int percentage = (int) download.getPercentDownloaded();
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText(percentage + "%"));
            notificationBuilder.setProgress(100, percentage , false);
            notificationBuilder.setOngoing(true);
        }
        return notificationBuilder.build();
    }

    public Notification getStaticNotification(OfflineManager.AssetDownloadState assetDownloadState) {
        notificationManager.cancelAll();
        notificationBuilder.clearActions();
        areActionButtonsAdded = false;

        switch (assetDownloadState) {
            case removing:
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Removing"));
                break;
            case completed:
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Completed"));
                break;
            case failed:
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Failed"));
                break;
            case paused:
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Paused"));
                break;
            case none:
                notificationBuilder.setStyle(new NotificationCompat.BigTextStyle().bigText("Something went wrong"));
            default:
                break;
        }

        notificationBuilder.setContentTitle("Offline Asset");
        notificationBuilder.setOngoing(false);
        notificationBuilder.setShowWhen(true);
        return notificationBuilder.build();
    }
}
