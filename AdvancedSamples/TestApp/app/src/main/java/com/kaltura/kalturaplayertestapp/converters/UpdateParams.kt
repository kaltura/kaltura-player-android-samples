package com.kaltura.kalturaplayertestapp.converters

import com.kaltura.playkit.player.ABRSettings
import com.kaltura.playkit.player.PKLowLatencyConfig

class UpdateParams {

    // A must value to be passed in JSON
    var timerForSnackbar: Long? = null

    var isUpdateABRSettings: Boolean? = null
    var updatedABRSettings: ABRSettings? = null

    var isResetABRSettings: Boolean? = null

    var isUpdatePkLowLatencyConfig: Boolean? = null
    var updatePkLowLatencyConfig: PKLowLatencyConfig? = null

}