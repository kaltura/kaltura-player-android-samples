package com.kaltura.playkit.samples.prefetchsample

import android.content.Context
import com.google.gson.Gson
import com.kaltura.playkit.Utils
import com.kaltura.playkit.samples.prefetchsample.data.AppConfig
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
    val startPosition: Long?,
    val licenseUrl: String?,
    val isPrefetch: Boolean = false,
    val options: OptionsJSON?,
    val ott: Boolean = false,
    val ottParams: ItemOTTParamsJSON?
)

fun ItemJSON.toItem() : Item {

    return if (partnerId != null) {
        // Kaltura Item

        if (this.ott) {
            // OTT
            OTTItem(partnerId, this.id, env!!, this.ks,  ottParams?.format, ottParams?.protocol, options?.toPrefs(), title, startPosition, isPrefetch)
        } else {
            // OVP
            OVPItem(partnerId, id, env, this.ks, options?.toPrefs(), title, startPosition, isPrefetch)
        }
    } else {
        BasicItem(id, url!!, licenseUrl, options?.toPrefs(), title, startPosition, isPrefetch)
    }
}

data class OptionsJSON(
    val audioLangs: List<String>?,
    val allAudioLangs: Boolean?,
    val textLangs: List<String>?,
    val allTextLangs: Boolean?,
    val videoCodecs: List<String>?,
    val videoBitrate: Int?,
    val audioCodecs: List<String>?,
    val videoWidth: Int?,
    val videoHeight: Int?,
    val videoBitrates: Map<String, Int>?,
    val allowInefficientCodecs: Boolean?
)

fun OptionsJSON.toPrefs() : SelectionPrefs {
    val opts = SelectionPrefs()
    opts.allAudioLanguages = allAudioLangs ?: false
    opts.videoBitrate = videoBitrate
    opts.audioLanguages = audioLangs
    opts.allTextLanguages = allTextLangs ?: false
    opts.textLanguages = textLangs
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

fun loadItemsFromJson(context: Context): AppConfig {
    val itemsJson = Utils.readAssetToString(context, "appConfig.json")
    return Gson().fromJson(itemsJson, AppConfig::class.java)
}
