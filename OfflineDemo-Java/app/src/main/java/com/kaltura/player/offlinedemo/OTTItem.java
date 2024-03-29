package com.kaltura.player.offlinedemo;

import com.kaltura.playkit.providers.ott.OTTMediaAsset;
import com.kaltura.tvplayer.MediaOptions;
import com.kaltura.tvplayer.OTTMediaOptions;
import com.kaltura.tvplayer.OfflineManager;

import java.util.Collections;

public class OTTItem extends KalturaItem {

    private int partnerId;
    private String ottAssetId;
    private String serverUrl;
    private String ks;
    private String format;
    private String protocol;
    private OfflineManager.SelectionPrefs prefs;
    private String title;

    public OTTItem(int partnerId, String ottAssetId, String serverUrl, String ks, String format, String protocol, OfflineManager.SelectionPrefs prefs, String title) {
        super(partnerId, serverUrl, prefs, title);

        this.ottAssetId = ottAssetId;
        this.partnerId = partnerId;
        this.serverUrl = serverUrl;
        this.ks = ks;
        this.format = format;
        this.protocol = protocol;
        this.prefs = prefs;
        this.title = title;
    }

    @Override
    MediaOptions mediaOptions() {
        OTTMediaAsset ottMediaAsset = new OTTMediaAsset();
        ottMediaAsset.setAssetId(ottAssetId);
        ottMediaAsset.setProtocol(protocol);
        ottMediaAsset.setFormats(Collections.singletonList(format));
        OTTMediaOptions ottMediaOptions = new OTTMediaOptions(ottMediaAsset);

        return ottMediaOptions;
    }

    @Override
    String id() {
        OfflineManager.AssetInfo assetInfo= getAssetInfo();
        if (assetInfo == null) {
            return ottAssetId;
        }
        return assetInfo.getAssetId();
    }

    @Override
    public int getPartnerId() {
        return partnerId;
    }

    @Override
    public void setPartnerId(int partnerId) {
        this.partnerId = partnerId;
    }

    public String getOttAssetId() {
        return ottAssetId;
    }

    public void setOttAssetId(String ottAssetId) {
        this.ottAssetId = ottAssetId;
    }

    @Override
    public String getServerUrl() {
        return serverUrl;
    }

    @Override
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public String getKs() {
        return ks;
    }

    public void setKs(String ks) {
        this.ks = ks;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    @Override
    public OfflineManager.SelectionPrefs getPrefs() {
        return prefs;
    }

    @Override
    public void setPrefs(OfflineManager.SelectionPrefs prefs) {
        this.prefs = prefs;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }
}
