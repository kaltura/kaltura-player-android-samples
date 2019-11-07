package com.kaltura.kalturaplayertestapp

interface PlaybackControls {
    fun handleContainerClick()
    fun showControls(visibility: Int)
    fun setContentPlayerState(playerState: Enum<*>)
    fun setAdPlayerState(playerState: Enum<*>)
}