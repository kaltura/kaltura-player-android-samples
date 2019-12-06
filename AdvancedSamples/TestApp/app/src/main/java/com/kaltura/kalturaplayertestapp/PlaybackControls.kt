package com.kaltura.kalturaplayertestapp

/**
 * Created by gilad.nadav on 3/18/18.
 */

interface PlaybackControls {
    fun handleContainerClick()
    fun showControls(visibility: Int)
    fun setContentPlayerState(playerState: Enum<*>?)
    fun setAdPlayerState(playerState: Enum<*>?)
    fun setAdPluginName(adPluginName: String)
}
