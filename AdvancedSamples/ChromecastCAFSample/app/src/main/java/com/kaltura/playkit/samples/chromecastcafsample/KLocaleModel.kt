package com.kaltura.playkit.samples.chromecastcafsample

import com.google.gson.JsonObject

data class KLocaleModel(var mLocaleLanguage: String = "Unknown", var mLocaleCountry: String = "",
                   var mLocaleDevice: String = "", var mLocaleUserState: String = "") {

    fun toJson(): JsonObject {
        val obj = JsonObject()
        obj.addProperty("LocaleCountry", mLocaleCountry)
        obj.addProperty("LocaleDevice", mLocaleDevice)
        obj.addProperty("LocaleLanguage", mLocaleLanguage)
        obj.addProperty("LocaleUserState", mLocaleUserState)
        return obj
    }

}