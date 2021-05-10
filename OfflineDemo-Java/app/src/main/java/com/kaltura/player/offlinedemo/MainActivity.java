package com.kaltura.player.offlinedemo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kaltura.playkit.PKDrmParams;
import com.kaltura.playkit.PKLog;
import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.playkit.Utils;
import com.kaltura.tvplayer.KalturaPlayer;
import com.kaltura.tvplayer.OfflineManager;
import com.kaltura.tvplayer.offline.OfflineManagerSettings;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private PKLog log = PKLog.get("MainActivity");
    private OfflineManager manager;
    private HashMap<String, Item> itemMap = new HashMap<>();
    private Long startTime = 0L;
    private ListView assetListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        assetListView = findViewById(R.id.assetList);

        String itemsJson = Utils.readAssetToString(this, "items.json");

        Gson gson = new Gson();

        Type itemJsonType = new TypeToken<ArrayList<ItemJSON>>(){}.getType();

        List<ItemJSON> itemJson = gson.fromJson(itemsJson, itemJsonType);

        List<Item> testItems = new ArrayList<>();

        for (int index = 0; index < itemJson.size(); index++) {
            testItems.add(itemJson.get(index).toItem());
        }

        manager = OfflineManager.getInstance(this);
        manager.setOfflineManagerSettings(new OfflineManagerSettings().setHlsAudioBitrateEstimation(OfflineManagerSettings.DEFAULT_HLS_AUDIO_BITRATE_ESTIMATION));

        manager.setAssetStateListener(new OfflineManager.AssetStateListener() {

            @Override
            public void onStateChanged(@NonNull String assetId, @NonNull OfflineManager.DownloadType downloadType, @NonNull OfflineManager.AssetInfo assetInfo) {
                toast("onStateChanged");
                updateItemStatus(assetId);
            }

            @Override
            public void onAssetRemoved(@NonNull String assetId, @NonNull OfflineManager.DownloadType downloadType) {
                toast("onAssetRemoved");
                updateItemStatus(assetId);
            }

            @Override
            public void onAssetRemoveError(@NonNull String assetId, @NonNull OfflineManager.DownloadType downloadType, @NonNull Exception error) {
                toast("onAssetRemoveError");
                updateItemStatus(assetId);
            }

            @Override
            public void onAssetDownloadFailed(@NonNull String assetId, @NonNull OfflineManager.DownloadType downloadType, @NonNull Exception error) {
                toastLong("Download of" + error + "failed:" + error);
                updateItemStatus(assetId);
            }

            @Override
            public void onAssetDownloadComplete(@NonNull String assetId, @NonNull OfflineManager.DownloadType downloadType) {
                log.d("onAssetDownloadComplete");
                log.d("onAssetDownloadComplete:" + (SystemClock.elapsedRealtimeNanos() - startTime));
                toast("Complete");
                updateItemStatus(assetId);
            }

            @Override
            public void onAssetPrefetchComplete(@NonNull String assetId, @NonNull OfflineManager.DownloadType downloadType) {
                log.d("onAssetPrefetchComplete $assetId totalDownloadTime: ${SystemClock.elapsedRealtimeNanos() - startTime}");

                if (downloadType == OfflineManager.DownloadType.FULL) {
                    toast("Complete");
                } else {
                    toast("Prefetched id =  $assetId");
                }
                updateItemStatus(assetId);
            }

            @Override
            public void onAssetDownloadPending(@NonNull String assetId, @NonNull OfflineManager.DownloadType downloadType) {
                updateItemStatus(assetId);
            }

            @Override
            public void onAssetDownloadPaused(@NonNull String assetId, @NonNull OfflineManager.DownloadType downloadType) {
                toast("Paused");
                updateItemStatus(assetId);
            }

            @Override
            public void onRegistered(@NonNull String assetId, @NonNull OfflineManager.DrmStatus drmStatus) {
                toast("onRegistered:" + drmStatus.currentRemainingTime + "seconds left");
                updateItemStatus(assetId);
            }

            @Override
            public void onRegisterError(@NonNull String assetId, @NonNull OfflineManager.DownloadType downloadType, @NonNull Exception error) {
                toastLong("onRegisterError:" + assetId + " " + error);
                updateItemStatus(assetId);
            }
        });

        manager.setDownloadProgressListener((assetId, bytesDownloaded, totalBytesEstimated, percentDownloaded) -> {
            log.d("[progress] " + assetId +": " + (bytesDownloaded / 1000) + "/" + (totalBytesEstimated / 1000));

            if (itemMap != null && itemMap.get(assetId) != null) {
                Objects.requireNonNull(itemMap.get(assetId)).setBytesDownloaded(bytesDownloaded);
                Objects.requireNonNull(itemMap.get(assetId)).setPercentDownloaded(percentDownloaded);
            } else {
                return;
            }

            runOnUiThread(() ->{
                assetListView.invalidateViews();
            });
        });

        ArrayAdapter<Item> itemArrayAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1);

        for (Item item : testItems) {
            if (item != null) {
                itemArrayAdapter.add(item);
                itemMap.put(item.id(), item);
            }
        }

        assetListView.setAdapter(itemArrayAdapter);

        assetListView.setOnItemClickListener((parent, view, position, id) -> {
            showActionsDialog((Item)parent.getItemAtPosition(position), position);
        });

        try {
            manager.start(() -> {
                log.d("manager started");
                for (Map.Entry<String, Item> stringItemEntry : itemMap.entrySet()) {
                    Item itemEntry = stringItemEntry.getValue();
                    itemEntry.setAssetInfo(manager.getAssetInfo(itemEntry.id()));
                }

                runOnUiThread(() -> {
                    assetListView.invalidateViews();
                });
            });
        } catch (IOException e) {
            log.e("IOException in Offline Manager start");
            e.printStackTrace();
        }
    }

    private void updateItemStatus(String assetId) {
        if (itemMap != null && itemMap.get(assetId) != null) {
            updateItemStatus(Objects.requireNonNull(itemMap.get(assetId)));
        }
    }

    private void showActionsDialog(Item item, int position) {
        String[] items = new String[]{"Prepare", "Start", "Pause", "Play-Offline", "Play-Online", "Remove", "Status", "Update"};

        new AlertDialog.Builder(MainActivity.this).setItems(items, (dialog, which) -> {
            switch (which) {
                case 0:
                    doPrepare(item);
                    break;
                case 1:
                    doStart(item);
                    break;
                case 2:
                    doPause(item);
                    break;
                case 3:
                    doOfflinePlayback(item);
                    break;
                case 4:
                    doOnlinePlayback(item, position);
                    break;
                case 5:
                    doRemove(item);
                    break;
                case 6:
                    doStatus(item);
                    break;
                case 7:
                    updateItemStatus(item);
                    break;
            }
        }).show();
    }

    private void updateItemStatus(Item item) {
        item.setAssetInfo(manager.getAssetInfo(item.id()));
        runOnUiThread(() -> assetListView.invalidateViews());
    }

    private void doStatus(Item item) {
        if (item instanceof KalturaItem) {
            OfflineManager.DrmStatus drmStatus = manager.getDrmStatus(item.id());

            if (drmStatus.isClear()) {
                toastLong("Clear");
                return;
            }

            String msg;

            if (drmStatus.isValid()) {
                msg = "Valid; will expire in " + drmStatus.currentRemainingTime + " seconds";
            } else {
                msg = "Expired or Error";
            }

            Snackbar.make(assetListView, msg, Snackbar.LENGTH_LONG).setDuration(5000).setAction("Renew", v -> {
                manager.setKalturaParams(KalturaPlayer.Type.ovp, ((KalturaItem) item).getPartnerId());
                manager.renewDrmAssetLicense(item.id(), ((KalturaItem) item).mediaOptions(), new OfflineManager.MediaEntryCallback() {
                    @Override
                    public void onMediaEntryLoaded(@NonNull String assetId, @NonNull PKMediaEntry mediaEntry) {
                        reduceLicenseDuration(mediaEntry, 300);
                    }

                    @Override
                    public void onMediaEntryLoadError(@NonNull Exception error) {
                        toastLong("onMediaEntryLoadError: " + error);
                    }
                });
            }).show();
        }
    }

    private void doRemove(Item item) {
        if (item.getAssetInfo() != null) {
            manager.removeAsset(item.getAssetInfo().getAssetId());
        } else {
            return;
        }
        updateItemStatus(item);
    }

    private void doOfflinePlayback(Item item) {
        Intent playerActivityIntent = new Intent(this, PlayActivity.class);
        if (item.getAssetInfo() != null) {
            playerActivityIntent.setData(Uri.parse(item.getAssetInfo().getAssetId()));
        }
        startActivity(playerActivityIntent);
    }

    private void doOnlinePlayback(Item item, int position) {
        Intent intent = new Intent(this, PlayActivity.class);
        Bundle bundle = new Bundle();
        bundle.putBoolean("isOnlinePlayback", true);
        bundle.putInt("position", position);
        if (item instanceof OTTItem) {
            bundle.putInt("partnerId", ((OTTItem) item).getPartnerId());
        }

        if (item instanceof OVPItem) {
            bundle.putInt("partnerId", ((OVPItem) item).getPartnerId());
        }

        intent.putExtra("assetBundle", bundle);

        startActivity(intent);
    }

    private void doPause(Item item) {
        if (item != null && item.id() != null) {
            manager.pauseAssetDownload(item.id());
            updateItemStatus(item);
        }
    }

    private void doStart(Item item) {
        log.d("doStart");
        if (item.getAssetInfo() == null) {
            return;
        }

        OfflineManager.AssetInfo assetInfo = item.getAssetInfo();

        startTime = SystemClock.elapsedRealtime();
        manager.startAssetDownload(assetInfo);
        updateItemStatus(item);
    }

    private void doPrepare(Item item) {

        if (item instanceof OTTItem) {
            manager.setKalturaParams(KalturaPlayer.Type.ott, ((OTTItem) item).getPartnerId());
            manager.setKalturaServerUrl(((OTTItem) item).getServerUrl());
        }

        if (item instanceof OVPItem) {
            manager.setKalturaParams(KalturaPlayer.Type.ovp, ((KalturaItem) item).getPartnerId());
            manager.setKalturaServerUrl(((KalturaItem) item).getServerUrl());
        }

        OfflineManager.PrepareCallback prepareCallback = new OfflineManager.PrepareCallback() {
            @Override
            public void onPrepared(@NonNull String assetId, @NonNull OfflineManager.AssetInfo assetInfo, @Nullable Map<OfflineManager.TrackType, List<OfflineManager.Track>> selected) {
                item.setAssetInfo(assetInfo);
                runOnUiThread(() -> {
                    Snackbar.make(assetListView, "Prepared", Snackbar.LENGTH_LONG).setDuration(5000).setAction("Start", v -> doStart(item)).show();
                    assetListView.invalidateViews();
                });
            }

            @Override
            public void onPrepareError(@NonNull String assetId, @NonNull Exception error) {
                toastLong("onPrepareError: " + error);
            }

            @Override
            public void onMediaEntryLoadError(@NonNull Exception error) {
                toastLong("onMediaEntryLoadError: " + error);
            }

            @Override
            public void onMediaEntryLoaded(@NonNull String assetId, @NonNull PKMediaEntry mediaEntry) {
                reduceLicenseDuration(mediaEntry, 300);
            }

            @Override
            public void onSourceSelected(@NonNull String assetId, @NonNull PKMediaSource source, @Nullable PKDrmParams drmParams) {

            }
        };

        OfflineManager.SelectionPrefs defaultPrefs = new OfflineManager.SelectionPrefs();
        defaultPrefs.videoHeight = 300;
        defaultPrefs.videoBitrate = 600000;
        defaultPrefs.videoWidth = 400;
        defaultPrefs.allAudioLanguages = true;
        defaultPrefs.allTextLanguages = true;
        defaultPrefs.allowInefficientCodecs = false;

        if (item instanceof KalturaItem) {
            if (!TextUtils.isEmpty(((KalturaItem) item).getServerUrl())) {
                manager.setKalturaServerUrl(((KalturaItem) item).getServerUrl());
            }
            manager.prepareAsset(((KalturaItem) item).mediaOptions(), item.getSelectionPrefs() != null ? item.getSelectionPrefs() : defaultPrefs, prepareCallback);
        } else {
            if (item.getEntry() != null) {
                manager.prepareAsset(item.getEntry(), item.getSelectionPrefs() != null ? item.getSelectionPrefs() : defaultPrefs, prepareCallback);
            }
        }

    }

    private void reduceLicenseDuration(PKMediaEntry mediaEntry, int seconds) {
        for (PKMediaSource source : mediaEntry.getSources()) {
            if (source.hasDrmParams()) {
                for (PKDrmParams params : source.getDrmData()) {
                    params.setLicenseUri(params.getLicenseUri() + "&rental_duration=" + seconds);
                }
            }
        }
    }

    private void toastLong(String msg) {
        runOnUiThread(() -> Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show());
    }

    private void toast(String msg) {
        runOnUiThread(() -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            // Removing listeners by setting it to null
            manager.setAssetStateListener(null);
            manager.setDownloadProgressListener(null);
            manager.stop();
        }
    }
}
