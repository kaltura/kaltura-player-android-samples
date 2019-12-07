package com.kaltura.playkit.samples.fulldemo

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive


class ConverterYoubora(
        private val accountCode: JsonPrimitive,
        private val username: JsonPrimitive,
        private val haltOnError: JsonPrimitive,
        private val enableAnalytics: JsonPrimitive,
        private val enableSmartAds: JsonPrimitive,
        private val media: JsonObject, private val ads: JsonObject, private val extraParams: JsonObject, private val properties: JsonObject) : ConverterPlugin() {

    override fun toJson(): JsonObject {
        val jsonObject = JsonObject()
        jsonObject.add("accountCode", accountCode)
        jsonObject.add("username", username)
        jsonObject.add("haltOnError", haltOnError)
        jsonObject.add("enableAnalytics", enableAnalytics)
        jsonObject.add("enableSmartAds", enableSmartAds)

        jsonObject.add("media", media)
        jsonObject.add("ads", ads)
        jsonObject.add("properties", properties)
        jsonObject.add("extraParams", extraParams)
        return jsonObject
    }
}
