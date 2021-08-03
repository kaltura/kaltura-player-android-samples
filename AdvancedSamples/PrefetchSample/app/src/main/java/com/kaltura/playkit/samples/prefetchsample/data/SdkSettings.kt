package com.kaltura.playkit.samples.prefetchsample

import com.kaltura.playkit.PlayKitManager

data class SdkSettings(var numRedirects: Int = 4,
                       var autoPlayAdBreaks: Boolean = true,
                       var debugMode: Boolean = false,
                       var playerVersion: String = "kaltura-vp-android",
                       var playerType: String = PlayKitManager.VERSION_STRING,
                       @Transient var language: String = "en",
                       var sessionId: String? = null)
