package com.kaltura.playkit.samples.fulldemo;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;


public class ConverterYoubora extends ConverterPlugin {
    private JsonPrimitive accountCode;
    private JsonPrimitive username;
    private JsonPrimitive haltOnError;
    private JsonPrimitive enableAnalytics;
    private JsonPrimitive enableSmartAds;

    private JsonObject media;
    private JsonObject ads;
    private JsonObject properties;
    private JsonObject extraParams;

    public ConverterYoubora(
            JsonPrimitive accountCode,
            JsonPrimitive username,
            JsonPrimitive haltOnError,
            JsonPrimitive enableAnalytics,
            JsonPrimitive enableSmartAds,
            JsonObject media, JsonObject ads, JsonObject extraParams, JsonObject properties) {

        this.accountCode = accountCode;
        this.username = username;
        this.haltOnError = haltOnError;
        this.enableAnalytics = enableAnalytics;
        this.enableSmartAds = enableSmartAds;
        this.media = media;
        this.ads = ads;
        this.extraParams = extraParams;
        this.properties = properties;
    }


    @Override
    public JsonObject toJson() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.add("accountCode", accountCode);
        jsonObject.add("username", username);
        jsonObject.add("haltOnError", haltOnError);
        jsonObject.add("enableAnalytics", enableAnalytics);
        jsonObject.add("enableSmartAds", enableSmartAds);

        jsonObject.add("media", media);
        jsonObject.add("ads", ads);
        jsonObject.add("properties", properties);
        jsonObject.add("extraParams", extraParams);
        return jsonObject;
    }
}
