package com.kaltura.playkit.samples.recyclerriewsample

data class MediaItem (var mediaId: String,
                      var mediaFormat: String,
                      var mediaImageView: String,
                      var isThumbnailActive: Boolean = true)