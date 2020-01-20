package com.kaltura.kalturaplayertestapp.converters;

import com.kaltura.playkit.providers.PlaylistMetadata;
import com.kaltura.tvplayer.OTTMediaOptions;
import com.kaltura.tvplayer.OVPMediaOptions;
import com.kaltura.tvplayer.playlist.CountDownOptions;
import com.kaltura.tvplayer.playlist.PlaylistPKMediaEntry;

import java.util.List;


public class PlaylistConfig {
    private boolean loopEnabled;
    private boolean shuffleEnabled;
    private int startIndex;
    private CountDownOptions countDownOptions;
    private String ks;

    private boolean useApiCaptions;
    private String playlistId;

    private PlaylistMetadata playlistMetadata;

    private List<OVPMediaOptions> ovpMediaOptionsList;

    private List<OTTMediaOptions> ottMediaOptionsList;

    private List<PlaylistPKMediaEntry> playlistPKMediaEntryList;

    public boolean isLoopEnabled() {
        return loopEnabled;
    }

    public boolean isShuffleEnabled() {
        return shuffleEnabled;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public CountDownOptions getCountDownOptions() {
        return countDownOptions;
    }

    public String getKs() {
        return ks;
    }

    public boolean isUseApiCaptions() {
        return useApiCaptions;
    }

    public String getPlaylistId() {
        return playlistId;
    }

    public PlaylistMetadata getPlaylistMetadata() {
        return playlistMetadata;
    }

    public List<OVPMediaOptions> getOvpMediaOptionsList() {
        return ovpMediaOptionsList;
    }

    public List<OTTMediaOptions> getOttMediaOptionsList() {
        return ottMediaOptionsList;
    }

    public List<PlaylistPKMediaEntry> getPlaylistPKMediaEntryList() {
        return playlistPKMediaEntryList;
    }
}
