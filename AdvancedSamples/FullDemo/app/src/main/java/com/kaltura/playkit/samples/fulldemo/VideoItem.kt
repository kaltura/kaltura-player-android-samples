package com.kaltura.playkit.samples.fulldemo

data class VideoItem(
        /**
         * Returns the title of the video item.
         */
        val title: String,
        /**
         * Returns the URL of the content video.
         */
        val videoUrl: String,
        /**
         * Returns the Video lic url for the video.
         */
        val videoLic: String,
        /**
         * Returns the ad tag for the video.
         */
        val adTagUrl: String,
        /**
         * Returns the video thumbnail image resource.
         */
        val imageResource: Int)