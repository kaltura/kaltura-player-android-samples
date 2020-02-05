package com.kaltura.player.offlinedemo;

import com.kaltura.tvplayer.OfflineManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OptionsJSON {

    private List<String> audioLangs;
    private boolean allAudioLangs;
    private List<String> textLangs;
    private boolean allTextLangs;
    private List<String> videoCodecs;
    private List<String> audioCodecs;
    private int videoWidth;
    private int videoHeight;
    private Map<String, Integer> videoBitrates;
    private boolean allowInefficientCodecs;

    public List<String> getAudioLangs() {
        return audioLangs;
    }

    public void setAudioLangs(List<String> audioLangs) {
        this.audioLangs = audioLangs;
    }

    public Boolean getAllAudioLangs() {
        return allAudioLangs;
    }

    public void setAllAudioLangs(Boolean allAudioLangs) {
        this.allAudioLangs = allAudioLangs;
    }

    public List<String> getTextLangs() {
        return textLangs;
    }

    public void setTextLangs(List<String> textLangs) {
        this.textLangs = textLangs;
    }

    public Boolean getAllTextLangs() {
        return allTextLangs;
    }

    public void setAllTextLangs(Boolean allTextLangs) {
        this.allTextLangs = allTextLangs;
    }

    public List<String> getVideoCodecs() {
        return videoCodecs;
    }

    public void setVideoCodecs(List<String> videoCodecs) {
        this.videoCodecs = videoCodecs;
    }

    public List<String> getAudioCodecs() {
        return audioCodecs;
    }

    public void setAudioCodecs(List<String> audioCodecs) {
        this.audioCodecs = audioCodecs;
    }

    public int getVideoWidth() {
        return videoWidth;
    }

    public void setVideoWidth(int videoWidth) {
        this.videoWidth = videoWidth;
    }

    public int getVideoHeight() {
        return videoHeight;
    }

    public void setVideoHeight(int videoHeight) {
        this.videoHeight = videoHeight;
    }

    public Map<String, Integer> getVideoBitrates() {
        return videoBitrates;
    }

    public void setVideoBitrates(Map<String, Integer> videoBitrates) {
        this.videoBitrates = videoBitrates;
    }

    public Boolean getAllowInefficientCodecs() {
        return allowInefficientCodecs;
    }

    public void setAllowInefficientCodecs(Boolean allowInefficientCodecs) {
        this.allowInefficientCodecs = allowInefficientCodecs;
    }

    public OfflineManager.SelectionPrefs toPrefs() {
        OfflineManager.SelectionPrefs opts = new OfflineManager.SelectionPrefs();
        opts.allAudioLanguages = allAudioLangs;
        opts.audioLanguages = audioLangs;
        opts.allTextLanguages = allTextLangs;
        opts.textLanguages = textLangs;
        opts.allowInefficientCodecs = allowInefficientCodecs;

        opts.audioCodecs = new ArrayList<>();
        if (audioCodecs != null) {
            for (String tag: audioCodecs) {
                if (tag.equals("mp4a")) {
                    opts.audioCodecs.add(OfflineManager.TrackCodec.MP4A);
                } else if (tag.equals("ac3")) {
                    opts.audioCodecs.add(OfflineManager.TrackCodec.AC3);
                } else if (tag.equals("eac3") || tag.equals("ec3")) {
                    opts.audioCodecs.add(OfflineManager.TrackCodec.EAC3);
                } else {
                    opts.audioCodecs.add(null);
                }
            }
        }

        opts.videoCodecs = new ArrayList<>();
        if (videoCodecs != null) {
            for (String tag: videoCodecs) {
                 opts.videoCodecs.add(tagToCodec(tag));
            }
        }

        opts.videoWidth = videoWidth;
        opts.videoHeight = videoHeight;

        if (videoBitrates != null) {
            HashMap<OfflineManager.TrackCodec, Integer> bitrates = new HashMap<>();

            for (Map.Entry<String, Integer> videoBitrateEntry : videoBitrates.entrySet()) {
                OfflineManager.TrackCodec codec = tagToCodec((String) ((Map.Entry) videoBitrateEntry).getKey());
                if (codec != null) {
                    bitrates.put(codec, (Integer) ((Map.Entry) videoBitrateEntry).getValue());
                }
            }

            opts.codecVideoBitrates = bitrates;

        }

        return opts;
    }

    private OfflineManager.TrackCodec tagToCodec(String tag) {
        switch (tag) {
            case "avc1":
                return OfflineManager.TrackCodec.AVC1;
            case "hevc":
            case "hvc1":
                return OfflineManager.TrackCodec.HEVC;

            default:
                return null;
        }
    }
}
