package com.kaltura.kalturaplayertestapp.models.ima

data class SdkSettings(var numRedirects: Int = 4,
                       var autoPlayAdBreaks: Boolean = true,
                       var debugMode: Boolean = false,
                       var playerVersion: String,
                       var playerType: String,
                       @Transient var language: String = "en")