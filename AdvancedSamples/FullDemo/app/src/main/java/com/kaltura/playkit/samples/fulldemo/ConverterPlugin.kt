package com.kaltura.playkit.samples.fulldemo

import com.google.gson.JsonObject

abstract class ConverterPlugin {

    var pluginName: String = ""

    abstract fun toJson(): JsonObject

}
