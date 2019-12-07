package com.kaltura.kalturaplayertestapp.models

data class Configuration(var id: String? = null, var title: String? = null, var json: String? = null, var type: Int? = null) {

    companion object {
        val JSON = 0
        val FOLDER = 1
    }

}