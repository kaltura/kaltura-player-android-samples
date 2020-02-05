package com.kaltura.player.offlinedemo;

import android.util.Log;

import com.kaltura.playkit.PKMediaEntry;
import com.kaltura.playkit.PKMediaSource;
import com.kaltura.tvplayer.OfflineManager;

import java.util.ArrayList;
import java.util.List;

public class BasicItem extends Item {

    private String id;
    private String url;
    private OfflineManager.SelectionPrefs prefs;
    private String title;

    public BasicItem(String id, String url, OfflineManager.SelectionPrefs prefs, String title) {
        super(prefs, title);

        this.id = id;
        this.url = url;

        List<PKMediaSource> pkMediaSources = new ArrayList<>();
        PKMediaSource pkMediaSource = new PKMediaSource().setId(this.id).setUrl(this.url);
        pkMediaSources.add(pkMediaSource);

        this.setEntry(new PKMediaEntry()
                .setId(this.id)
                .setMediaType(PKMediaEntry.MediaEntryType.Vod)
                .setSources(pkMediaSources));

        Log.d("Item", this.getEntry().toString());
    }

    @Override
    String id() {
        return id;
    }

    @Override
    String title() {
        return title + "(" + id + ")";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public OfflineManager.SelectionPrefs getPrefs() {
        return prefs;
    }

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
