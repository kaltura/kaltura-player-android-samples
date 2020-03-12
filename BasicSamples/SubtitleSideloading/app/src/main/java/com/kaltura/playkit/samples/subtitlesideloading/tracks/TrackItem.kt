package com.kaltura.playkit.samples.subtitlesideloading.tracks

data class TrackItem(var trackName: String?, //Readable name of the track. trackName
                     var uniqueId: String?) //Unique id, which should be passed to player in order to change track.
