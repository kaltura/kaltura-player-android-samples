package com.kaltura.player.offlinedemo;

import java.util.List;

public class ExpectedValues {
    private Long estimatedSize;
    private Long downloadedSize;
    private List<String> audioLangs;
    private List<String> textLangs;

    public Long getEstimatedSize() {
        return estimatedSize;
    }

    public void setEstimatedSize(Long estimatedSize) {
        this.estimatedSize = estimatedSize;
    }

    public Long getDownloadedSize() {
        return downloadedSize;
    }

    public void setDownloadedSize(Long downloadedSize) {
        this.downloadedSize = downloadedSize;
    }

    public List<String> getAudioLangs() {
        return audioLangs;
    }

    public void setAudioLangs(List<String> audioLangs) {
        this.audioLangs = audioLangs;
    }

    public List<String> getTextLangs() {
        return textLangs;
    }

    public void setTextLangs(List<String> textLangs) {
        this.textLangs = textLangs;
    }
}
