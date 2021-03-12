package com.kaltura.kalturaplayertestapp.converters

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kaltura.netkit.connect.request.RequestConfiguration
import com.kaltura.playkit.PKMediaFormat
import com.kaltura.playkit.PKRequestParams
import com.kaltura.playkit.PKSubtitlePreference
import com.kaltura.playkit.player.*
import com.kaltura.playkit.player.vr.VRSettings
import com.kaltura.tvplayer.KalturaPlayer


class PlayerConfig {
    var playerType: KalturaPlayer.Type? = null
    var playlistConfig : PlaylistConfig? = null
    val baseUrl: String? = null
    val partnerId: String? = null
    var ks: String? = null
    var startPosition: Long? = null
    var autoPlay: Boolean? = null
    var preload: Boolean? = null
    val allowCrossProtocolEnabled: Boolean? = null
    var preferredFormat: PKMediaFormat? = null
    var allowClearLead: Boolean? = null
    var enableDecoderFallback: Boolean? = null
    var secureSurface: Boolean? = null
    var cea608CaptionsEnabled: Boolean? = null
    var adAutoPlayOnResume: Boolean? = null
    var vrPlayerEnabled: Boolean? = null
    var isTunneledAudioPlayback: Boolean? = null
    var vrSettings: VRSettings? = null
    var isVideoViewHidden: Boolean? = null
    var maxAudioBitrate: Int? = null
    var maxAudioChannelCount: Int? = null
    var maxVideoBitrate: Int? = null
    var maxVideoSize: PKMaxVideoSize? = null
    var handleAudioBecomingNoisyEnabled: Boolean? = null
    var subtitlePreference: PKSubtitlePreference? = null

    var setSubtitleStyle: SubtitleStyleSettings? = null
    var aspectRatioResizeMode: PKAspectRatioResizeMode? = null
    var contentRequestAdapter: PKRequestParams.Adapter? = null
    var licenseRequestAdapter: PKRequestParams.Adapter? = null
    var loadControlBuffers: LoadControlBuffers? = null
    var updateParams: UpdateParams? = null
    var abrSettings: ABRSettings? = null
    var videoCodecSettings: VideoCodecSettings? = null
    var audioCodecSettings: AudioCodecSettings? = null
    var requestConfiguration: RequestConfiguration? = null
    var referrer: String? = null
    var forceSinglePlayerEngine: Boolean? = null
    var forceWidevineL3Playback : Boolean? = null
    var mediaList: List<Media>? = null
    var trackSelection: TrackSelection? = null
    var plugins: JsonArray? = null
    var playerConfig: JsonObject? = null
}
