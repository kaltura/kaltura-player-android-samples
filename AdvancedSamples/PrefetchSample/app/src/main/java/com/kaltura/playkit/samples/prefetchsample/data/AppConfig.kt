package com.kaltura.playkit.samples.prefetchsample.data

import com.kaltura.playkit.samples.prefetchsample.ItemJSON

data class AppConfig(
    var offlineConfig: OfflineConfig?,
    var items: Array<ItemJSON>
)