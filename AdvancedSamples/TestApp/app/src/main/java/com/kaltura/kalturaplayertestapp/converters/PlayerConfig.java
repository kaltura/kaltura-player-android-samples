package com.kaltura.kalturaplayertestapp.converters;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.kaltura.netkit.connect.request.RequestConfiguration;
import com.kaltura.playkit.PKMediaFormat;
import com.kaltura.playkit.PKRequestParams;
import com.kaltura.playkit.player.ABRSettings;
import com.kaltura.playkit.player.LoadControlBuffers;
import com.kaltura.playkit.player.PKAspectRatioResizeMode;
import com.kaltura.playkit.player.SubtitleStyleSettings;
import com.kaltura.playkit.player.vr.VRSettings;
import com.kaltura.tvplayer.KalturaPlayer;


import java.util.List;

/**
 * Created by gilad.nadav on 1/24/18.
 */

public class PlayerConfig {
    public KalturaPlayer.Type playerType;
    public String baseUrl;
    public String partnerId;
    public String ks;
    public Long startPosition;
    public Boolean autoPlay;
    public Boolean preload;
    public Boolean allowCrossProtocolEnabled;
    public PKMediaFormat preferredFormat;
    public Boolean allowClearLead;
    public Boolean enableDecoderFallback;
    public Boolean secureSurface;
    public Boolean adAutoPlayOnResume;
    public Boolean vrPlayerEnabled;
    public Boolean isTunneledAudioPlayback;
    public VRSettings vrSettings;
    public Boolean isVideoViewHidden;
    public SubtitleStyleSettings setSubtitleStyle;
    public PKAspectRatioResizeMode aspectRatioResizeMode;
    public PKRequestParams.Adapter contentRequestAdapter;
    public PKRequestParams.Adapter licenseRequestAdapter;
    public LoadControlBuffers loadControlBuffers;
    public ABRSettings abrSettings;
    public RequestConfiguration requestConfiguration;
    public String referrer;
    public String widgetId;
    public Boolean forceSinglePlayerEngine;
    public List<Media> mediaList;
    public TrackSelection trackSelection;
    public JsonArray plugins;
    public JsonObject playerConfig;

    public PlayerConfig() {}
}
