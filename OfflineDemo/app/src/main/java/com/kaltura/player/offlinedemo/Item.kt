package com.kaltura.player.offlinedemo

import com.kaltura.tvplayer.MediaOptions
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.OVPMediaOptions
import com.kaltura.tvplayer.OfflineManager

abstract class Item(val partnerId: Int, val serverUrl: String) {

    var assetInfo: OfflineManager.AssetInfo? = null
    var percentDownloaded: Float? = null
    var bytesDownloaded: Long? = null

    abstract fun id(): String
    abstract fun mediaOptions(): MediaOptions
    protected fun sizeMB(sizeBytes: Long?): Float {
        if (sizeBytes == null || sizeBytes <= 0) {
            return -1f
        }

        return sizeBytes.toFloat() / (1000*1000)
    }

    override fun toString(): String {
        val state = assetInfo?.state ?: OfflineManager.AssetDownloadState.none
        val progress = if (percentDownloaded != null) "%.1f".fmt(percentDownloaded) else "--"

        val sizeMB = "%.3f".fmt(sizeMB(assetInfo?.estimatedSize))
        return "${id()} @ $partnerId, $state\n$progress% / ${sizeMB}MB"
    }
}

class OVPItem(partnerId: Int, val entryId: String, serverUrl: String = "https://cdnapisec.kaltura.com"
) : Item(partnerId, serverUrl) {

    override fun id() = assetInfo?.assetId ?: entryId

    override fun mediaOptions() = OVPMediaOptions(entryId)
}

class OTTItem(partnerId: Int, val ottAssetId: String, serverUrl: String, val format: String) : Item(partnerId, serverUrl) {

    override fun id() = assetInfo?.assetId ?: ottAssetId

    override fun mediaOptions() = OTTMediaOptions().apply {
        assetId = ottAssetId
        formats = arrayOf(format)
    }
}
