
package com.kaltura.playkit.samples.prefetchsample.data;

import android.text.TextUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.kaltura.playkit.ads.AdTagType;
import com.kaltura.playkit.samples.prefetchsample.SdkSettings;

public class UiConfFormatIMAConfig {
    public static final int DEFAULT_AD_LOAD_TIMEOUT = 5;
    public static final int DEFAULT_CUE_POINTS_CHANGED_DELAY = 2000;
    public static final int DEFAULT_AD_LOAD_COUNT_DOWN_TICK = 250;

    public static final String AD_TAG_LANGUAGE     = "language";
    public static final String AD_TAG_TYPE         = "adTagType";
    public static final String AD_RESPONSE         = "adTagResponse";
    public static final String AD_TAG_URL          = "adTagUrl";
    public static final String AD_VIDEO_BITRATE    = "videoBitrate";
    public static final String AD_VIDEO_MIME_TYPES      = "videoMimeTypes";
    public static final String AD_ATTRIBUTION_UIELEMENT = "adAttribution";
    public static final String AD_COUNTDOWN_UIELEMENT   = "adCountDown";
    public static final String AD_LOAD_TIMEOUT          = "adLoadTimeOut";
    public static final String AD_ENABLE_DEBUG_MODE     = "enableDebugMode";
    public static final String AD_SESSION_ID            = "sessionId";
    public static final String AD_ALWAYES_START_WITH_PREROLL = "alwaysStartWithPreroll";

    private String adTagUrl;
    private String adResponse;
    private AdTagType adTagType = AdTagType.VAST;
    private AdsRenderingSettings adsRenderingSettings;
    private SdkSettings sdkSettings;
    private boolean alwaysStartWithPreroll;

    public String getAdTagUrl() {
        return adTagUrl;
    }
    public String getAdResponse() {
        return adResponse;
    }
    public AdTagType getAdTagType() {
        return adTagType;
    }

    public boolean isAlwaysStartWithPreroll() {
        return alwaysStartWithPreroll;
    }

    public AdsRenderingSettings getAdsRenderingSettings() {
        if (adsRenderingSettings == null) {
            adsRenderingSettings = new AdsRenderingSettings();
        }
        return adsRenderingSettings;
    }

    public SdkSettings getSdkSettings() {
        if (sdkSettings == null) {
            sdkSettings = new SdkSettings();
        }
        return sdkSettings;
    }

    public JsonObject toJson() { // to Json will return format like IMAConfig
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(AD_TAG_LANGUAGE, getSdkSettings().getLanguage());
        jsonObject.addProperty(AD_TAG_TYPE, adTagType.name());
        if (!TextUtils.isEmpty(adTagUrl)) {
            jsonObject.addProperty(AD_TAG_URL, adTagUrl);
        } else if (!TextUtils.isEmpty(adResponse)) {
            jsonObject.addProperty(AD_RESPONSE, adResponse);
        }

        jsonObject.addProperty(AD_VIDEO_BITRATE, getAdsRenderingSettings().getBitrate());
        jsonObject.addProperty(AD_ATTRIBUTION_UIELEMENT, getAdsRenderingSettings().getUiElements().getAdAttribution());
        jsonObject.addProperty(AD_COUNTDOWN_UIELEMENT, getAdsRenderingSettings().getUiElements().getAdCountDown());
        jsonObject.addProperty(AD_LOAD_TIMEOUT, getAdsRenderingSettings().getLoadVideoTimeout());
        jsonObject.addProperty(AD_ENABLE_DEBUG_MODE, getSdkSettings().getDebugMode());
        jsonObject.addProperty(AD_SESSION_ID, getSdkSettings().getSessionId());
        jsonObject.addProperty(AD_ALWAYES_START_WITH_PREROLL , alwaysStartWithPreroll);

        JsonArray jArray = new JsonArray();
        if (adsRenderingSettings.getMimeTypes() != null) {
            for (String mimeType : adsRenderingSettings.getMimeTypes()) {
                JsonPrimitive element = new JsonPrimitive(mimeType);
                jArray.add(element);
            }
        }
        jsonObject.add(AD_VIDEO_MIME_TYPES, jArray);
        return jsonObject;
    }
}


