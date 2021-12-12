package com.kaltura.playkit.samples.fulldemo

import com.kaltura.playkit.samples.fulldemo.Consts.AD_1
import com.kaltura.playkit.samples.fulldemo.Consts.AD_2
import com.kaltura.playkit.samples.fulldemo.Consts.AD_3
import com.kaltura.playkit.samples.fulldemo.Consts.AD_4
import com.kaltura.playkit.samples.fulldemo.Consts.AD_5
import com.kaltura.playkit.samples.fulldemo.Consts.AD_6
import com.kaltura.playkit.samples.fulldemo.Consts.AD_7
import com.kaltura.playkit.samples.fulldemo.Consts.AD_8
import com.kaltura.playkit.samples.fulldemo.Consts.AD_9
import com.kaltura.playkit.samples.fulldemo.Consts.AD_GOOGLE_SEARCH
import com.kaltura.playkit.samples.fulldemo.Consts.LIC_URL1
import com.kaltura.playkit.samples.fulldemo.Consts.LIVE_URL
import com.kaltura.playkit.samples.fulldemo.Consts.SOURCE_URL1
import java.util.ArrayList

class VideoMetadata(
        /** The title of the video.  */
        var title: String,
        /** The URL for the video.  */
        var videoUrl: String,
        /** The LICENSE for the video.  */
        var videoLic: String,
        /** The ad tag for the video  */
        var adTagUrl: String,
        /** The thumbnail image for the video.  */
        var thumbnail: Int) {

    companion object {
        val defaultVideoList: List<VideoItem>
            get() {
                val defaultVideos = ArrayList<VideoItem>()
                defaultVideos.add(VideoItem(
                        "Custom Ad Tag / Custom XML Ad Tag",
                        SOURCE_URL1,
                        LIC_URL1,
                        "custom",
                        R.drawable.k_image, null, null, null))


                defaultVideos.add(VideoItem(
                        "Pre-roll, Companion not skippable",
                        LIVE_URL,
                        LIC_URL1,
                        AD_9,
                        R.drawable.k_image, null, null, null))


                defaultVideos.add(VideoItem(
                        "Pre-roll, linear not skippable",
                        SOURCE_URL1,
                        LIC_URL1,
                        AD_1,
                        R.drawable.k_image, null, null, null))

                defaultVideos.add(VideoItem(
                        "Pre-roll, linear, skippable",
                        SOURCE_URL1,
                        LIC_URL1,
                        AD_2,
                        R.drawable.k_image, null, null, null))

                defaultVideos.add(VideoItem(
                        "VMAP",
                        SOURCE_URL1,
                        LIC_URL1,
                        AD_4,
                        R.drawable.k_image, null, null, null))

                defaultVideos.add(VideoItem(
                        "VMAP Pods",
                        SOURCE_URL1,
                        LIC_URL1,
                        AD_5,
                        R.drawable.k_image, null, null, null))

                defaultVideos.add(VideoItem(
                        "Wrapper",
                        SOURCE_URL1,
                        LIC_URL1,
                        AD_6,
                        R.drawable.k_image, null, null, null))

                defaultVideos.add(VideoItem(
                        "VMAP Pods Bump",
                        SOURCE_URL1,
                        LIC_URL1,
                        AD_7,
                        R.drawable.k_image, null, null, null))

                defaultVideos.add(VideoItem(
                        "VMAP Pods Bump every 10 sec",
                        SOURCE_URL1,
                        LIC_URL1,
                        AD_8,
                        R.drawable.k_image, null, null, null))

                defaultVideos.add(VideoItem(
                        "Google Search",
                        SOURCE_URL1,
                        LIC_URL1,
                        AD_GOOGLE_SEARCH,
                        R.drawable.k_image, null, null, null))

                defaultVideos.add(VideoItem(
                        "Post-roll",
                        SOURCE_URL1,
                        LIC_URL1,
                        AD_3,
                        R.drawable.k_image, null, null, null))

                defaultVideos.add(
                    VideoItem(
                        "Vod DAI",
                        SOURCE_URL1,
                        LIC_URL1,
                        null,
                        R.drawable.k_image,
                        null,
                        "2528370",
                        "tears-of-steel"
                    )
                )
                defaultVideos.add(
                    VideoItem(
                        "Live DAI",
                        SOURCE_URL1,
                        LIC_URL1,
                        null,
                        R.drawable.k_image,
                        "sN_IYUG8STe1ZzhIIE_ksA",
                        null,
                        null
                    )
                )
                
                return defaultVideos
            }
    }
}