package com.kaltura.kalturaplayertestapp.models.ima

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.kaltura.playkit.ads.AdTagType

data class UiConfFormatIMAConfig(var adTagUrl : String,
                                 var adTagType : AdTagType = AdTagType.VAST,
                                 var alwaysStartWithPreroll : Boolean) {

    val DEFAULT_AD_LOAD_TIMEOUT = 5
    val DEFAULT_CUE_POINTS_CHANGED_DELAY = 2000
    val DEFAULT_AD_LOAD_COUNT_DOWN_TICK = 250

    val AD_TAG_LANGUAGE = "language"
    val AD_TAG_TYPE = "adTagType"
    val AD_TAG_URL = "adTagUrl"
    val AD_VIDEO_BITRATE = "videoBitrate"
    val AD_VIDEO_MIME_TYPES = "videoMimeTypes"
    val AD_ATTRIBUTION_UIELEMENT = "adAttribution"
    val AD_COUNTDOWN_UIELEMENT = "adCountDown"
    val AD_LOAD_TIMEOUT = "adLoadTimeOut"
    val AD_ENABLE_DEBUG_MODE = "enableDebugMode"
    val AD_ALWAYES_START_WITH_PREROLL = "alwaysStartWithPreroll"

    private var adsRenderingSettings: AdsRenderingSettings? = null
    private var sdkSettings: SdkSettings? = null

    fun getAdsRenderingSettings(): AdsRenderingSettings {
        if (adsRenderingSettings == null) {
            adsRenderingSettings = AdsRenderingSettings()
        }
        return adsRenderingSettings as AdsRenderingSettings
    }

    fun getSdkSettings(): SdkSettings {
        if (sdkSettings == null) {
            sdkSettings = SdkSettings()
        }
        return sdkSettings as SdkSettings
    }

    fun toJson(): JsonObject { // to Json will return format like IMAConfig
        val jsonObject = JsonObject()
        jsonObject.addProperty(AD_TAG_LANGUAGE, getSdkSettings().language)
        jsonObject.addProperty(AD_TAG_TYPE, adTagType.name)
        jsonObject.addProperty(AD_TAG_URL, adTagUrl)
        jsonObject.addProperty(AD_VIDEO_BITRATE, getAdsRenderingSettings().bitrate)
        jsonObject.addProperty(AD_ATTRIBUTION_UIELEMENT, getAdsRenderingSettings().uiElements.adAttribution)
        jsonObject.addProperty(AD_COUNTDOWN_UIELEMENT, getAdsRenderingSettings().uiElements.adCountDown)
        jsonObject.addProperty(AD_LOAD_TIMEOUT, getAdsRenderingSettings().loadVideoTimeout)
        jsonObject.addProperty(AD_ENABLE_DEBUG_MODE, getSdkSettings().debugMode)
        jsonObject.addProperty(AD_ALWAYES_START_WITH_PREROLL, alwaysStartWithPreroll)

        val jArray = JsonArray()
        if (adsRenderingSettings?.mimeTypes != null) {
            for (mimeType in adsRenderingSettings?.mimeTypes!!) {
                val element = JsonPrimitive(mimeType)
                jArray.add(element)
            }
        }
        jsonObject.add(AD_VIDEO_MIME_TYPES, jArray)
        return jsonObject
    }

}