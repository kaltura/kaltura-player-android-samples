package com.kaltura.kalturaplayertestapp.converters

import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider


class AppOTTMediaOptions : AppMediaOptions() {
    var assetId: String? = null
    var assetType: APIDefines.KalturaAssetType? = null
    var contextType: APIDefines.PlaybackContextType? = null
    var assetReferenceType: APIDefines.AssetReferenceType? = null
    var urlType: APIDefines.KalturaUrlType? = null
    var protocol: String? = HTTPS
    var formats: Array<String>? = null
    var fileIds: Array<String>? = null

    companion object {
        const val HTTP = PhoenixMediaProvider.HttpProtocol.Http
        const val HTTPS = PhoenixMediaProvider.HttpProtocol.Https
    }
}
