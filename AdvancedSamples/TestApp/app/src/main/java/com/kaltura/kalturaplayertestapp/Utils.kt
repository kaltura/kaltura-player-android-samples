package com.kaltura.kalturaplayertestapp

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.kaltura.playkit.PKPluginConfigs
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.*

class Utils {

    companion object {

        @JvmStatic
        fun parsePluginConfigs(json: JsonElement?): PKPluginConfigs {
            val configs = PKPluginConfigs()
            if (json != null && json.isJsonObject) {
                val obj = json.asJsonObject
                for ((pluginName, value) in obj.entrySet()) {
                    configs.setPluginConfig(pluginName, value)
                }
            }
            return configs
        }

        @JvmStatic
        fun safeObject(json: JsonObject, key: String): JsonObject? {
            val jsonElement = json.get(key)
            return if (jsonElement != null && jsonElement.isJsonObject) {
                jsonElement.asJsonObject
            } else null
        }

        @JvmStatic
        fun safeString(json: JsonObject, key: String): String? {
            val jsonElement = json.get(key)
            return if (jsonElement != null && jsonElement.isJsonPrimitive) {
                jsonElement.asString
            } else null
        }

        @JvmStatic
        fun safeBoolean(json: JsonObject, key: String): Boolean? {
            val jsonElement = json.get(key)
            return if (jsonElement != null && jsonElement.isJsonPrimitive) {
                jsonElement.asBoolean
            } else null
        }

        @JvmStatic
        fun safeInteger(json: JsonObject, key: String): Int? {
            val jsonElement = json.get(key)
            return if (jsonElement != null && jsonElement.isJsonPrimitive) {
                jsonElement.asInt
            } else null
        }

        @JvmStatic
        @Throws(IOException::class)
        fun getResponseFromHttpUrl(url: URL): String? {
            val urlConnection = url.openConnection() as HttpURLConnection
            try {
                val `in` = urlConnection.inputStream

                val scanner = Scanner(`in`)
                scanner.useDelimiter("\\A")

                val hasInput = scanner.hasNext()
                return if (hasInput) {
                    scanner.next()
                } else {
                    null
                }
            } finally {
                urlConnection.disconnect()
            }
        }
    }
}
