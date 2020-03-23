package com.kaltura.kalturaplayertestapp.converters

import com.kaltura.playkit.PKMediaEntry.MediaEntryType

 abstract class AppPKPlaylistMedia {
    val id: String? = null
    val ks: String? = null
    val name: String? = null
    val tags: String? = null
    val dataUrl: String? = null
    val description: String? = null
    val thumbnailUrl: String? = null
    val flavorParamsIds: String? = null
    val type: MediaEntryType? = null
    val msDuration: Long = 0
    var metadata: Map<String, String>? = null
}