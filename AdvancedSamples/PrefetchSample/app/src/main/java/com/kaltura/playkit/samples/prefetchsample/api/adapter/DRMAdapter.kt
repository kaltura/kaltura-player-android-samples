package com.kaltura.playkit.samples.prefetchsample.api.adapter

import com.kaltura.playkit.PKRequestParams
import com.kaltura.playkit.Player

//Example for Custom License Adapter
internal class DRMAdapter : PKRequestParams.Adapter {
    override fun adapt(requestParams: PKRequestParams): PKRequestParams {
        requestParams.headers["customData"] = customData
        return requestParams
    }

    override fun updateParams(player: Player) {
        // TODO?
    }

    override fun getApplicationName(): String? {
        return null
    }

    companion object {
        var customData: String? = null
    }
}