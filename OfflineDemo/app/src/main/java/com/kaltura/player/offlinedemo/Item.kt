package com.kaltura.player.offlinedemo

import com.kaltura.tvplayer.MediaOptions
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.OVPMediaOptions
import com.kaltura.tvplayer.OfflineManager

interface Item {
    var percentDownloaded: Float?
    var totalBytesEstimated: Long?
    var bytesDownloaded: Long?
    val serverUrl: String
    val partnerId: Int
    var assetInfo: OfflineManager.AssetInfo?

    fun id() = assetInfo?.assetId
    fun mediaOptions(): MediaOptions
}

enum class OTTItem(
    override val partnerId: Int,
    val ottAssetId: String,
    override val serverUrl: String
) : Item {

    ottOne(2250, "3817050", "https://rest-as.ott.kaltura.com/v5_0_3/api_v3");

    override var assetInfo: OfflineManager.AssetInfo? = null
    override var percentDownloaded: Float? = null
    override var totalBytesEstimated: Long? = null
    override var bytesDownloaded: Long? = null

    override fun id() = assetInfo?.assetId ?: ottAssetId

    override fun mediaOptions() = OTTMediaOptions().apply {
        assetId = ottAssetId
    }
}

enum class OVPItem(
    override val partnerId: Int,
    val entryId: String,
    override val serverUrl: String = "https://cdnapisec.kaltura.com"
) : Item {

    one(1851571, "0_pl5lbfo0"),
    two(2222401, "0_vcggu66e"),
    three(2222401, "1_2hsw7gwj");

    override var assetInfo: OfflineManager.AssetInfo? = null
    override var percentDownloaded: Float? = null
    override var totalBytesEstimated: Long? = null
    override var bytesDownloaded: Long? = null

    override fun id() = assetInfo?.assetId ?: entryId

    override fun toString(): String {
        val state = OfflineManager.AssetDownloadState.completed
        val progress = if (percentDownloaded != null) "%.1f".fmt(percentDownloaded) else "--"
        return "$entryId @ $partnerId, $state\n$progress% / ${(totalBytesEstimated?.div(1024*1024) ?: "--" )}MB"
    }

    override fun mediaOptions() = OVPMediaOptions(entryId)
}