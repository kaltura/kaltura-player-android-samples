package com.kaltura.kalturaplayertestapp.converters;

import com.kaltura.playkit.player.PKExternalSubtitle;
import com.kaltura.tvplayer.playlist.CountDownOptions;

import java.util.List;

public abstract class AppMediaOptions {
    public String ks;
    public Long startPosition;
    public String referrer;
    public List<PKExternalSubtitle> externalSubtitles;
    public CountDownOptions countDownOptions;
}