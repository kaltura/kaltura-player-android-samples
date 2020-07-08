package com.kaltura.playkit.samples.recyclerviewsample

data class MediaItem (var mediaId: String,
                      var mediaFormat: String,
                      var mediaImageView: String,
                      var isThumbnailActive: Boolean = true)