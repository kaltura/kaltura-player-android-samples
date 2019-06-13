package com.kaltura.playkit.samples.chromecastcafsample;

import com.google.gson.JsonObject;

public class KLocaleModel {

    private String mLocaleLanguage;
    private String mLocaleCountry;
    private String mLocaleDevice;
    private String mLocaleUserState;

    public KLocaleModel(String localeLanguage, String localeCountry, String localeDevice) {
        this();
        mLocaleLanguage = localeLanguage;
        mLocaleCountry = localeCountry;
        mLocaleDevice = localeDevice;
    }

    public JsonObject toJson() {
        JsonObject obj = new JsonObject();
        obj.addProperty("LocaleCountry", mLocaleCountry);
        obj.addProperty("LocaleDevice", mLocaleDevice);
        obj.addProperty("LocaleLanguage", mLocaleLanguage);
        obj.addProperty("LocaleUserState", mLocaleUserState);
        return obj;
    }

    public KLocaleModel () {
        mLocaleUserState = "Unknown";
    }

    public void setLocaleLanguage(String localeLanguage) {
        mLocaleLanguage = localeLanguage;
    }

    public void setLocaleCountry(String localeCountry) {
        mLocaleCountry = localeCountry;
    }

    public void setLocaleDevice(String localeDevice) {
        mLocaleDevice = localeDevice;
    }

    public void setLocaleUserState(String localeUserState) {
        mLocaleUserState = localeUserState;
    }
}