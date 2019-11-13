package com.kaltura.kalturaplayertestapp.models

data class Configuration(var id: String? = null, var title: String? = null, var json: String? = null, var type: Int? = null) {

    companion object {
        @JvmField
        val JSON = 0
        @JvmField
        val FOLDER = 1
    }

}