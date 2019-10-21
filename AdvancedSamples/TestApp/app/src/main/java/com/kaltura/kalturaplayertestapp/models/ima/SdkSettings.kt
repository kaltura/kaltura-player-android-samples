package com.kaltura.kalturaplayertestapp.models.ima

data class SdkSettings(var numRedirects : Int = 4,
                       var autoPlayAdBreaks : Boolean = true,
                       var debugMode : Boolean = false,
                       @Transient var language : String = "en")