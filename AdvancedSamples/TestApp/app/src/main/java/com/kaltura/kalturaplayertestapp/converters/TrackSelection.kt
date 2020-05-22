package com.kaltura.kalturaplayertestapp.converters

import com.google.gson.JsonElement


class TrackSelection {
    var textSelectionMode: String? = null
    var textSelectionLanguage: String? = null
    var audioSelectionMode: String? = null
    var audioSelectionLanguage: String? = null
    var subtitleStyling: List<SubtitleStyling>? = null
}