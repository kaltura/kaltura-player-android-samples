package com.kaltura.playkit.samples.prefetchsample

import android.annotation.SuppressLint
import android.text.TextUtils
import com.google.gson.JsonArray
import com.kaltura.playkit.PKDrmParams
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaSource
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ovp.OVPMediaAsset
import com.kaltura.tvplayer.*
import com.kaltura.tvplayer.OfflineManager.SelectionPrefs
import java.util.*

abstract class Item (val playerType: KalturaPlayer.Type, val selectionPrefs: SelectionPrefs?,
                     val title: String?,
                     val startPosition: Long?,
                     var isPrefetch: Boolean = false,
                     var plugins: JsonArray?,
                     var position: Int = -1,
                     var drmNotRegistered: Boolean? = false, // Just to understand if the asset has failed with drm registration
                     var isOfflineProviderExo: Boolean = false) {

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

    fun getDownloadPercentage(): String {
        val state = assetInfo?.state ?: OfflineManager.AssetDownloadState.none
        return if (percentDownloaded != null && state == OfflineManager.AssetDownloadState.started) "%.1f".fmt(percentDownloaded) + "% / " + sizeMB() else "--" + sizeMB()
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

fun String.fmt(vararg args: Any?): String = java.lang.String.format(Locale.ROOT, this, *args)

@SuppressLint("ParcelCreator")
object NULL : KalturaItem(KalturaPlayer.Type.basic,0, "", null, null, null, false, null) {
    override fun id(): String = TODO()
    override fun mediaOptions(): MediaOptions = TODO()
}

class BasicItem(
    playerType: KalturaPlayer.Type,
    private val id: String,
    private val url: String,
    private var licenseUrl: String?,
    prefs: SelectionPrefs? = null,
    title: String? = null,
    startPosition: Long?,
    isPrefetch: Boolean = false,
    plugins: JsonArray?,
): Item(playerType, prefs, title, startPosition, isPrefetch, plugins) {

    val log = PKLog.get("BasicItem")

    init {
        this.entry = PKMediaEntry().apply {
            id = this@BasicItem.id
            mediaType = PKMediaEntry.MediaEntryType.Vod
            sources = listOf(PKMediaSource().apply {
                id = this@BasicItem.id
                url = this@BasicItem.url
                licenseUrl = licenseUrl ?: ""
                if (!TextUtils.isEmpty(this@BasicItem.licenseUrl)) {
                    drmData = mutableListOf()
                    drmData.add(PKDrmParams(this@BasicItem.licenseUrl, PKDrmParams.Scheme.WidevineCENC))
                }
            })
        }

        log.d(entry?.id.toString())
    }

    override fun id() = id

    override fun title() = "$title ($id)"
}

abstract class KalturaItem(
    playerType: KalturaPlayer.Type,
    val partnerId: Int,
    val serverUrl: String,
    prefs: SelectionPrefs?,
    title: String?,
    startPosition: Long? = null,
    isPrefetch: Boolean = false,
    plugins: JsonArray?
): Item(playerType, prefs, title, startPosition, isPrefetch, plugins) {

    abstract fun mediaOptions(): MediaOptions

    override fun title() = "$title (${id()} @ $partnerId)"
}

class OVPItem(
    playerType: KalturaPlayer.Type,
    partnerId: Int,
    private val entryId: String,
    serverUrl: String? = null,
    private val ks: String?,
    prefs: SelectionPrefs? = null,
    title: String? = null,
    startPosition: Long? = null,
    isPrefetch: Boolean = false,
    plugins: JsonArray?
) : KalturaItem(playerType, partnerId, serverUrl ?: "https://cdnapisec.kaltura.com", prefs, title, startPosition, isPrefetch, plugins) {

    override fun id() = assetInfo?.assetId ?: entryId

    override fun mediaOptions(): OVPMediaOptions  {
        val ovpMediaAsset = OVPMediaAsset()
        ovpMediaAsset.entryId = entryId
        ovpMediaAsset.ks = ks
        val ovpMediaOptions = OVPMediaOptions(ovpMediaAsset)
        ovpMediaOptions.startPosition = startPosition
        return ovpMediaOptions
    }
}

class OTTItem(
    playerType: KalturaPlayer.Type,
    partnerId: Int,
    private val ottAssetId: String,
    serverUrl: String,
    private val ks: String?,
    private val format: String?,
    private val protocol: String?,
    prefs: SelectionPrefs? = null,
    title: String? = null,
    startPosition: Long? = null,
    isPrefetch: Boolean = false,
    plugins: JsonArray?
) : KalturaItem(playerType, partnerId, serverUrl, prefs, title, startPosition, isPrefetch, plugins) {

    override fun id() = assetInfo?.assetId ?: ottAssetId

    override fun mediaOptions() : OTTMediaOptions {
        val ottMediaAsset = OTTMediaAsset()

        ottMediaAsset.assetId = ottAssetId
        ottMediaAsset.formats = listOf(format)
        ottMediaAsset.protocol = protocol
        ottMediaAsset.ks = ks


        var ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = startPosition
        return ottMediaOptions
    }
}
