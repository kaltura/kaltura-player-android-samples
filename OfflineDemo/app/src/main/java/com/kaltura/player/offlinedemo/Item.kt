package com.kaltura.player.offlinedemo

import android.util.Log
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaSource
import com.kaltura.tvplayer.MediaOptions
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.OVPMediaOptions
import com.kaltura.tvplayer.OfflineManager
import com.kaltura.tvplayer.OfflineManager.SelectionPrefs

abstract class Item (val selectionPrefs: SelectionPrefs?) {
    var entry: PKMediaEntry? = null
    var assetInfo: OfflineManager.AssetInfo? = null
    var percentDownloaded: Float? = null
    var bytesDownloaded: Long? = null

    abstract fun id(): String
    abstract fun title(): String

    private fun sizeMB(): String {
        val sizeBytes = assetInfo?.estimatedSize
        if (sizeBytes == null || sizeBytes <= 0) {
            return "--"
        }

        return "%.3f".fmt(sizeBytes.toFloat() / (1000*1000)) + "mb"
    }

    override fun toString(): String {
        val state = assetInfo?.state ?: OfflineManager.AssetDownloadState.none

        var string = "${title()}, $state\n"
        if (state == OfflineManager.AssetDownloadState.started) {
            string += if (percentDownloaded != null) "%.1f".fmt(percentDownloaded) + "% / " else "--"
        }
        string += sizeMB()

        return string
    }
}

class BasicItem(
    private val id: String,
    private val url: String,
    prefs: SelectionPrefs? = null
): Item(prefs) {

    init {
        this.entry = PKMediaEntry().apply {
            id = this@BasicItem.id
            mediaType = PKMediaEntry.MediaEntryType.Vod
            sources = listOf(PKMediaSource().apply {
                id = this@BasicItem.id
                url = this@BasicItem.url
            })
        }

        Log.d("Item", entry.toString())
    }

    override fun id() = id

    override fun title() = id
}

abstract class KalturaItem(
    val partnerId: Int,
    val serverUrl: String,
    prefs: SelectionPrefs?
): Item(prefs) {

    abstract fun mediaOptions(): MediaOptions

    override fun title() = "${id()} @ $partnerId"
}

class OVPItem(
    partnerId: Int,
    private val entryId: String,
    serverUrl: String = "https://cdnapisec.kaltura.com",
    prefs: SelectionPrefs? = null
) : KalturaItem(partnerId, serverUrl, prefs) {

    override fun id() = assetInfo?.assetId ?: entryId

    override fun mediaOptions() = OVPMediaOptions(entryId)
}

class OTTItem(
    partnerId: Int,
    val ottAssetId: String,
    serverUrl: String,
    val format: String,
    prefs: SelectionPrefs? = null
) : KalturaItem(partnerId, serverUrl, prefs) {

    override fun id() = assetInfo?.assetId ?: ottAssetId

    override fun mediaOptions() = OTTMediaOptions().apply {
        assetId = ottAssetId
        formats = arrayOf(format)
    }
}
