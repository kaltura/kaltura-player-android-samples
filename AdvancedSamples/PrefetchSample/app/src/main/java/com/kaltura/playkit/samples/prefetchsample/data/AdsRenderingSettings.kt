package com.kaltura.playkit.samples.prefetchsample.data

data class AdsRenderingSettings(var bitrate: Int = -1,
                                var loadVideoTimeout: Int = 0,
                                var mimeTypes: List<String>? = null,
                                var enableFocusSkipButton : Boolean = true,
                                var uiElements: UiElements = UiElements(true, true))