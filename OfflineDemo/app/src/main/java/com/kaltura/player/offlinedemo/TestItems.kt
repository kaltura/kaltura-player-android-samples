package com.kaltura.player.offlinedemo

import com.kaltura.tvplayer.OfflineManager
import com.kaltura.tvplayer.OfflineManager.SelectionPrefs


data class ItemOTTParamsJSON(
    val format: String?,
    val protocol: String?
)

data class ItemJSON(
    val id: String,
    val title: String?,
    val partnerId: Int?,
    val ks: String?,
    val env: String?,
    val url: String?,
    val licenseUrl: String?,
    val isPrefetch: Boolean = false,
    val options: OptionsJSON?,
    val expected: ExpectedValues?,
    val ott: Boolean = false,
    val ottParams: ItemOTTParamsJSON?
)

fun ItemJSON.toItem() : Item {

    return if (partnerId != null) {
        // Kaltura Item

        if (this.ott) {
            // OTT
            OTTItem(partnerId, this.id, env!!, ottParams?.format, ottParams?.protocol, options?.toPrefs(), title, isPrefetch)
        } else {
            // OVP
            OVPItem(partnerId, id, env, options?.toPrefs(), title, isPrefetch)
        }
    } else {
        BasicItem(id, url!!, licenseUrl, options?.toPrefs(), title, isPrefetch)
    }
}

data class ExpectedValues(
    val estimatedSize: Long?,
    val downloadedSize: Long?,
    val audioLangs: List<String>?,
    val textLangs: List<String>?
)

data class OptionsJSON(
    val audioLangs: List<String>?,
    val allAudioLangs: Boolean?,
    val textLangs: List<String>?,
    val allTextLangs: Boolean?,
    val videoCodecs: List<String>?,
    val audioCodecs: List<String>?,
    val videoWidth: Int?,
    val videoHeight: Int?,
    val videoBitrate: Int?,
    val videoBitrates: Map<String, Int>?,
    val allowInefficientCodecs: Boolean?
)

fun OptionsJSON.toPrefs() : SelectionPrefs {
    val opts = SelectionPrefs()
    opts.allAudioLanguages = allAudioLangs ?: false
    opts.audioLanguages = audioLangs
    opts.allTextLanguages = allTextLangs ?: false
    opts.textLanguages = textLangs
    opts.videoBitrate = videoBitrate
    opts.allowInefficientCodecs = allowInefficientCodecs ?: false

    audioCodecs?.let {
        opts.audioCodecs = it.map { tag  ->
            when (tag) {
                "mp4a" -> return@map OfflineManager.TrackCodec.MP4A
                "ac3" -> return@map OfflineManager.TrackCodec.AC3
                in "eac3", "ec3" -> return@map OfflineManager.TrackCodec.EAC3
                else -> return@map null
            }
        }.filterNotNull()
    }

    videoCodecs?.let {
        opts.videoCodecs = it.map { tag  ->
            return@map tagToCodec(tag)
        }.filterNotNull()
    }

    opts.videoWidth = videoWidth
    opts.videoHeight = videoHeight

    videoBitrates?.let {
        val bitrates = mutableMapOf<OfflineManager.TrackCodec, Int>()
        for ((codecId, bitrate) in it) {
            tagToCodec(codecId)?.let { codec ->
                bitrates[codec] = bitrate
            }
        }
        opts.codecVideoBitrates = bitrates
    }

    return opts
}

private fun tagToCodec(tag: String): OfflineManager.TrackCodec? {
    return when (tag) {
        "avc1" -> OfflineManager.TrackCodec.AVC1
        in "hevc", "hvc1" -> OfflineManager.TrackCodec.HEVC
        else -> null
    }
}
