package com.kaltura.player.offlinedemo

import android.util.Log
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaSource
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.tvplayer.MediaOptions
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.OVPMediaOptions
import com.kaltura.tvplayer.OfflineManager
import com.kaltura.tvplayer.OfflineManager.SelectionPrefs

abstract class Item (val selectionPrefs: SelectionPrefs?, val title: String?, val isPrefetch: Boolean = false) {
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
    prefs: SelectionPrefs? = null,
    title: String? = null,
    isPrefetch: Boolean = false
): Item(prefs, title, isPrefetch) {

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

    override fun title() = "$title ($id)"
}

abstract class KalturaItem(
    val partnerId: Int,
    val serverUrl: String,
    prefs: SelectionPrefs?,
    title: String?,
    isPrefetch: Boolean = false
): Item(prefs, title, isPrefetch) {

    abstract fun mediaOptions(): MediaOptions

    override fun title() = "$title (${id()} @ $partnerId)"
}

class OVPItem(
    partnerId: Int,
    private val entryId: String,
    serverUrl: String? = null,
    prefs: SelectionPrefs? = null,
    title: String? = null,
    isPrefetch: Boolean = false
) : KalturaItem(partnerId, serverUrl ?: "https://cdnapisec.kaltura.com", prefs, title, isPrefetch) {

    override fun id() = assetInfo?.assetId ?: entryId

    override fun mediaOptions() = OVPMediaOptions(entryId)
}

class OTTItem(
    partnerId: Int,
    private val ottAssetId: String,
    serverUrl: String,
    private val format: String?,
    prefs: SelectionPrefs? = null,
    title: String? = null,
    isPrefetch: Boolean = false
) : KalturaItem(partnerId, serverUrl, prefs, title, isPrefetch) {

    override fun id() = assetInfo?.assetId ?: ottAssetId

    override fun mediaOptions() : OTTMediaOptions {
        val ottMediaAsset = OTTMediaAsset()

        ottMediaAsset.assetId = ottAssetId
        ottMediaAsset.formats = listOf(format)

        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = 0L
        return ottMediaOptions
    }
}
