package com.kaltura.kalturaplayertestapp.converters

import AppOVPMediaOptions
import com.kaltura.playkit.providers.PlaylistMetadata
import com.kaltura.tvplayer.playlist.CountDownOptions

class PlaylistConfig {
    val loopEnabled = false
    val shuffleEnabled = false
    val autoContinue = true;
    val recoverOnError = false;
    val startIndex = 0
    val countDownOptions: CountDownOptions? = null
    val ks: String? = null
    val useApiCaptions = false
    val playlistId: String? = null
    val playlistMetadata: PlaylistMetadata? = null
    val ovpMediaOptionsList: List<AppOVPMediaOptions>? = null
    val ottMediaOptionsList: List<AppOTTMediaOptions>? = null
    val basicMediaOptionsList: List<AppBasicMediaOptions>? = null
}