package com.kaltura.kalturaplayertestapp.converters;

import androidx.annotation.Nullable;

import com.kaltura.playkit.player.PKExternalSubtitle;
import com.kaltura.tvplayer.playlist.CountDownOptions;

import java.util.List;

public abstract class AppMediaOptions {
    public String ks;
    public Long startPosition;
    public String referrer;
    public List<PKExternalSubtitle> externalSubtitles;
    @Nullable public CountDownOptions countDownOptions;
}