package com.kaltura.playkit.samples.chromecastcafsample;

import com.google.gson.JsonObject;

public class KInitObjModel {

    private KLocaleModel mLocale;
    private String mPlatform;
    private String mSiteGuid;
    private int mDomainID;
    private String mUDID;
    private String mApiUser;
    private String mApiPass;

    public KInitObjModel() {
        mLocale = new KLocaleModel();
    }

    public JsonObject toJson() {
        //JsonObject initObj = new JsonObject();
        JsonObject initObj = new JsonObject();
        initObj.addProperty("ApiPass", mApiPass);
        initObj.addProperty("ApiUser", mApiUser);
        initObj.addProperty("UDID", mUDID);
        initObj.addProperty("DomainID", mDomainID);
        initObj.addProperty("SiteGuid", mSiteGuid);
        initObj.addProperty("Platform", mPlatform);
        initObj.add("Locale", mLocale.toJson());
        return initObj;
    }

    public void setLocale(KLocaleModel locale) {
        mLocale = locale;
    }

    public void setPlatform(String platform) {
        mPlatform = platform;
    }

    public void setSiteGuid(String siteGuid) {
        mSiteGuid = siteGuid;
    }

    public void setDomainID(int domainID) {
        mDomainID = domainID;
    }

    public void setUDID(String UDID) {
        mUDID = UDID;
    }

    public void setApiUser(String apiUser) {
        mApiUser = apiUser;
    }

    public void setApiPass(String apiPass) {
        mApiPass = apiPass;
    }
}