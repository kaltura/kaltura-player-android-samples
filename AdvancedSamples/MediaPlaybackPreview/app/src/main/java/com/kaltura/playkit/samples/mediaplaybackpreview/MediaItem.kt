package com.kaltura.playkit.samples.mediaplaybackpreview

data class MediaItem (var mediaId: String,
                      var fileType: String = "dash Main",
                      var mediaImageView: String,
                      var addMediaImageView: Boolean = true)