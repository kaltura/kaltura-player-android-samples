package com.kaltura.kalturaplayertestapp.models

data class PlayerConfiguration(var mediaProvider : String? = null,
                               var ottBaseUrl : String? = null,
                               var ottPartnerId : Int? = null,
                               var ovpBaseUrl : String? = null,
                               var ovpPartnerId : Int? = null,
                               var uiConfId : Int? = null,
                               var ottAssetId : String? = null,
                               var ottFileId : String? = null,
                               var ovpEntryId : String? = null,
                               var autoPlay : Boolean? = null,
                               var preload : Boolean? = null,
                               var startPosition : Float? = null) {

    val PLAYER = "player"
    val AUDIO_LANG = "audioLanguage"
    val TEXT_LANG = "textLanguage"
    val PLAYBACK = "playback"
    val OFF = "off"
    val AUTOPLAY = "autoplay"
    val PRELOAD = "preload"
    // val START_TIME = "startTime"
    val CONFIG = "config"
    val PLUGINS = "plugins"
    val AUTO = "auto"
    val OPTIONS = "options"
    val UICONF_ID = "uiConfId"
    val PARTNER_ID = "partnerId"
    val REFERRER = "referrer"
    val KS = "ks"
    val SERVER_URL = "serverUrl"
    val ALLOW_CROSS_PROTOCOL_ENABLED = "allowCrossProtocolEnabled"
    val STREAM_PRIORITY = "streamPriority"
}