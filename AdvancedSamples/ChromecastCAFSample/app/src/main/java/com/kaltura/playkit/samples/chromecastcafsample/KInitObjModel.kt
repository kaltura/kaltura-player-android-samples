package com.kaltura.playkit.samples.chromecastcafsample

import com.google.gson.JsonObject

data class KInitObjModel(var mLocale: KLocaleModel, var mPlatform: String, var mSiteGuid: String, var mDomainID: Integer,
                    var mUDID: String, var mApiUser: String, var mApiPass: String) {

    init {
        mLocale = KLocaleModel()
    }

    fun toJson(): JsonObject {
        //JsonObject initObj = new JsonObject();
        val initObj = JsonObject()
        initObj.addProperty("ApiPass", mApiPass)
        initObj.addProperty("ApiUser", mApiUser)
        initObj.addProperty("UDID", mUDID)
        initObj.addProperty("DomainID", mDomainID)
        initObj.addProperty("SiteGuid", mSiteGuid)
        initObj.addProperty("Platform", mPlatform)
        initObj.add("Locale", mLocale.toJson())
        return initObj
    }
}