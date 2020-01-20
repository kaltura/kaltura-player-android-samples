package com.kaltura.kalturaplayertestapp.converters

import com.kaltura.playkit.providers.PlaylistMetadata
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.OVPMediaOptions
import com.kaltura.tvplayer.playlist.CountDownOptions
import com.kaltura.tvplayer.playlist.PlaylistPKMediaEntry

class PlaylistConfig {
    val isLoopEnabled = false
    val isShuffleEnabled = false
    val startIndex = 0
    val countDownOptions: CountDownOptions? = null
    val ks: String? = null
    val isUseApiCaptions = false
    val playlistId: String? = null
    val playlistMetadata: PlaylistMetadata? = null
    val ovpMediaOptionsList: List<OVPMediaOptions>? = null
    val ottMediaOptionsList: List<OTTMediaOptions>? = null
    val playlistPKMediaEntryList: List<PlaylistPKMediaEntry>? = null

}