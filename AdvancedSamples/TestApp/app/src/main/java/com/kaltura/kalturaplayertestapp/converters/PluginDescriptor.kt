package com.kaltura.kalturaplayertestapp.converters

import com.google.gson.JsonElement


class PluginDescriptor {
    var pluginName: String? = null
    var isBundle: Boolean? = false
    var params: JsonElement? = null
}
