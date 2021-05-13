package com.kaltura.kalturaplayertestapp.converters

import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.player.PKExternalSubtitle
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider

class Media {
    var entryId: String? = null   // ovp
    var referenceId: String? = null // ovp
    var redirectFromEntryId:  Boolean = true // ovp
    var ks: String? = null        // ovp or ott
    var assetId: String? = null   // ott
    var format: String? = null    // ott
    var fileId: String? = null   // ott

    @get:JvmName("getAssetType")
    var assetType: String? = null // ott

    var playbackContextType: String? = null // ott
    var assetReferenceType: String? = null // ott
    var protocol: String? = null // ott
    var pkMediaEntry: PKMediaEntry? = null // player without provider
    var mediaAdTag: String? = null
    var useApiCaptions: Boolean = false
    var urlType: String? = null
    var streamerType: String? = null
    var adapterData: Map<String,String>? = null
    var externalSubtitles: List<PKExternalSubtitle>? = null

    fun getAssetType(): APIDefines.KalturaAssetType? {
        if (assetType == null) {
            return null
        }

        if (APIDefines.KalturaAssetType.Media.value == assetType?.toLowerCase()) {
            return APIDefines.KalturaAssetType.Media
        } else if (APIDefines.KalturaAssetType.Epg.value == assetType?.toLowerCase()) {
            return APIDefines.KalturaAssetType.Epg
        } else if (APIDefines.KalturaAssetType.Recording.value == assetType?.toLowerCase()) {
            return APIDefines.KalturaAssetType.Recording
        }
        return null
    }

    @JvmName("setAsset")
    fun setAssetType(assetType: String) {
        this.assetType = assetType
    }

    fun getPlaybackContextType(): APIDefines.PlaybackContextType? {
        if (playbackContextType == null) {
            return null
        }

        if (APIDefines.PlaybackContextType.Playback.value.toLowerCase() == playbackContextType?.toLowerCase()) {
            return APIDefines.PlaybackContextType.Playback
        } else if (APIDefines.PlaybackContextType.StartOver.value.toLowerCase() == playbackContextType?.toLowerCase()) {
            return APIDefines.PlaybackContextType.StartOver
        } else if (APIDefines.PlaybackContextType.Trailer.value.toLowerCase() == playbackContextType?.toLowerCase()) {
            return APIDefines.PlaybackContextType.Trailer
        } else if (APIDefines.PlaybackContextType.Catchup.value.toLowerCase() == playbackContextType?.toLowerCase()) {
            return APIDefines.PlaybackContextType.Catchup
        }
        return null
    }

    @JvmName("setPlaybackContext")
    fun setPlaybackContextType(playbackContextType: String) {
        this.playbackContextType = playbackContextType
    }

    fun getAssetReferenceType(): APIDefines.AssetReferenceType? {
        if (assetReferenceType == null) {
            return null
        }
        if (APIDefines.AssetReferenceType.Media.value.toLowerCase() == assetReferenceType?.toLowerCase()) {
            return APIDefines.AssetReferenceType.Media
        } else if (APIDefines.AssetReferenceType.ExternalEpg.value.toLowerCase() == assetReferenceType?.toLowerCase()) {
            return APIDefines.AssetReferenceType.ExternalEpg
        } else if (APIDefines.AssetReferenceType.InternalEpg.value.toLowerCase() == assetReferenceType?.toLowerCase()) {
            return APIDefines.AssetReferenceType.InternalEpg
        } else if (APIDefines.AssetReferenceType.Npvr.value.toLowerCase() == assetReferenceType?.toLowerCase()) {
            return APIDefines.AssetReferenceType.Npvr
        }
        return null
    }

    @JvmName("setAssetReference")
    fun setAssetReferenceType(assetReferenceType: String) {
        this.assetReferenceType = assetReferenceType
    }

    @JvmName("getProtocolName")
    fun getProtocol(): String? {
        if (protocol == null) {
            return null
        }
        if (PhoenixMediaProvider.HttpProtocol.All.toLowerCase() == protocol?.toLowerCase()) {
            return PhoenixMediaProvider.HttpProtocol.All
        } else if (PhoenixMediaProvider.HttpProtocol.Http.toLowerCase() == protocol?.toLowerCase()) {
            return PhoenixMediaProvider.HttpProtocol.Http
        } else if (PhoenixMediaProvider.HttpProtocol.Https.toLowerCase() == protocol?.toLowerCase()) {
            return PhoenixMediaProvider.HttpProtocol.Https
        }
        return null
    }

    fun getUrlType(): APIDefines.KalturaUrlType? {
        if (urlType == null) {
            return null
        }
        if (APIDefines.KalturaUrlType.Direct.value.toLowerCase() == urlType?.toLowerCase()) {
            return APIDefines.KalturaUrlType.Direct
        } else if (APIDefines.KalturaUrlType.PlayManifest.value.toLowerCase() == urlType?.toLowerCase()) {
            return APIDefines.KalturaUrlType.PlayManifest
        }
        return null
    }

    fun getStreamerType(): APIDefines.KalturaStreamerType? {
        if (streamerType == null) {
            return null
        }
        
        if (APIDefines.KalturaStreamerType.Mpegdash.value.toLowerCase() == streamerType?.toLowerCase()) {
            return APIDefines.KalturaStreamerType.Mpegdash;
        } else if (APIDefines.KalturaStreamerType.Applehttp.value.toLowerCase() == streamerType?.toLowerCase()) {
            return APIDefines.KalturaStreamerType.Applehttp;
        } else if (APIDefines.KalturaStreamerType.Url.value.toLowerCase() == streamerType?.toLowerCase()) {
            return APIDefines.KalturaStreamerType.Url;
        } else if (APIDefines.KalturaStreamerType.Smothstreaming.value.toLowerCase() == streamerType?.toLowerCase()) {
            return APIDefines.KalturaStreamerType.Smothstreaming;
        } else if (APIDefines.KalturaStreamerType.None.value.toLowerCase() == streamerType?.toLowerCase()) {
            return APIDefines.KalturaStreamerType.None;
        }
        return null
    }
}