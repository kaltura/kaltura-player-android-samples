package com.kaltura.playkit.samples.prefetchsample.api.adapter

import com.kaltura.dtg.DownloadRequestParams

//Example for Custom License Adapter
internal class DownloadRequestAdapter : DownloadRequestParams.Adapter {

    override fun adapt(requestParams: DownloadRequestParams?): DownloadRequestParams {
        val map = if (requestParams?.headers == null) {
            mutableMapOf<String, String>()
        } else {
            requestParams.headers
        }

        map["xxxx"] = "zzzz"
        return DownloadRequestParams(requestParams?.url, map)
    }
}