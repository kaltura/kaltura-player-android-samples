package com.kaltura.player.offlinedemo;

import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.tvplayer.OfflineManager;

public abstract class Item {

    private PKMediaEntry entry;
    private OfflineManager.AssetInfo assetInfo;
    private Float percentDownloaded;
    private long bytesDownloaded;
    private OfflineManager.SelectionPrefs selectionPrefs;
    private String title;

    abstract String id();
    abstract String title();

    public Item(OfflineManager.SelectionPrefs selectionPrefs, String title) {
        this.selectionPrefs = selectionPrefs;
        this.title = title;
    }

    public PKMediaEntry getEntry() {
        return entry;
    }

    public void setEntry(PKMediaEntry entry) {
        this.entry = entry;
    }

    public OfflineManager.AssetInfo getAssetInfo() {
        return assetInfo;
    }

    public void setAssetInfo(OfflineManager.AssetInfo assetInfo) {
        this.assetInfo = assetInfo;
    }

    public Float getPercentDownloaded() {
        return percentDownloaded;
    }

    public void setPercentDownloaded(Float percentDownloaded) {
        this.percentDownloaded = percentDownloaded;
    }

    public long getBytesDownloaded() {
        return bytesDownloaded;
    }

    public void setBytesDownloaded(long bytesDownloaded) {
        this.bytesDownloaded = bytesDownloaded;
    }

    public OfflineManager.SelectionPrefs getSelectionPrefs() {
        return selectionPrefs;
    }

    public void setSelectionPrefs(OfflineManager.SelectionPrefs selectionPrefs) {
        this.selectionPrefs = selectionPrefs;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    private String sizeMB() {
        Long sizeBytes = null;

        if (assetInfo != null) {
            sizeBytes = assetInfo.getEstimatedSize();
        }

        if (sizeBytes == null ||sizeBytes <= 0) {
            return "--";
        }

        return String.format("%.3f%", (sizeBytes.floatValue() / (1000*1000))) + "mb";
    }

    @Override
    public String toString() {
        OfflineManager.AssetDownloadState state = OfflineManager.AssetDownloadState.none;
        if (assetInfo != null) {
            state = assetInfo.getState();
        }

        String string = title() + ","+  state + "\n";

        if (state == OfflineManager.AssetDownloadState.started) {
            if (percentDownloaded != null){
                string += String.format("%.1f", percentDownloaded) + "% / ";
            } else {
                string += "--";
            }
        }

        string += sizeMB();

        return string;
    }
}
