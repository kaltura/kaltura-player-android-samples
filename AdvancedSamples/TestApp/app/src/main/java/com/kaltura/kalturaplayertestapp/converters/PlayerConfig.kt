package com.kaltura.kalturaplayertestapp.converters

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kaltura.netkit.connect.request.RequestConfiguration
import com.kaltura.playkit.PKRequestParams
import com.kaltura.playkit.player.ABRSettings
import com.kaltura.playkit.player.LoadControlBuffers
import com.kaltura.playkit.player.PKAspectRatioResizeMode
import com.kaltura.playkit.player.SubtitleStyleSettings
import com.kaltura.playkit.player.vr.VRSettings
import com.kaltura.tvplayer.KalturaPlayer


class PlayerConfig {
    var playerType: KalturaPlayer.Type? = null
    var baseUrl: String? = null
    var partnerId: String? = null
    var ks: String? = null
    var startPosition: Long? = null
    var autoPlay: Boolean? = null
    var preload: Boolean? = null
    var allowCrossProtocolEnabled: Boolean? = null
    var preferredFormat: String? = null
    var allowClearLead: Boolean? = null
    var enableDecoderFallback: Boolean? = null
    var secureSurface: Boolean? = null
    var adAutoPlayOnResume: Boolean? = null
    var vrPlayerEnabled: Boolean? = null
    var isTunneledAudioPlayback: Boolean? = null
    var vrSettings: VRSettings? = null
    var isVideoViewHidden: Boolean? = null
    var setSubtitleStyle: SubtitleStyleSettings? = null
    var aspectRatioResizeMode: PKAspectRatioResizeMode? = null
    var contentRequestAdapter: PKRequestParams.Adapter? = null
    var licenseRequestAdapter: PKRequestParams.Adapter? = null
    var loadControlBuffers: LoadControlBuffers? = null
    var abrSettings: ABRSettings? = null
    var requestConfiguration: RequestConfiguration? = null
    var referrer: String? = null
    var forceSinglePlayerEngine: Boolean? = null
    var mediaList: List<Media>? = null
    var trackSelection: TrackSelection? = null
    var plugins: JsonArray? = null
    var playerConfig: JsonObject? = null
}
