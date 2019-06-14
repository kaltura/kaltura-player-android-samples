package com.kaltura.playkit.samples.fulldemo;

import java.util.ArrayList;
import java.util.List;

import static com.kaltura.playkit.samples.fulldemo.Consts.*;

public class VideoMetadata {

    /** The thumbnail image for the video. **/
    public int thumbnail;

    /** The title of the video. **/
    public String title;

    /** The URL for the video. **/
    public String videoUrl;

    /** The LICENSE for the video. **/
    public String videoLic;

    /** The ad tag for the video **/
    public String adTagUrl;

    public VideoMetadata(String title, String videoUrl, String videoLic, String adTagUrl, int thumbnail) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.videoLic = videoLic;
        this.adTagUrl = adTagUrl;
        this.thumbnail = thumbnail;
    }


    public static List<VideoItem> getDefaultVideoList() {
        List<VideoItem> defaultVideos = new ArrayList<>();
        defaultVideos.add(new VideoItem(
                "Custom Ad Tag / Custom XML Ad Tag",
                SOURCE_URL1,
                LIC_URL1,
                "custom",
                R.drawable.k_image));


        defaultVideos.add(new VideoItem(
                "Pre-roll, Companion not skippable",
                LIVE_URL,
                LIC_URL1,
                AD_9,
                R.drawable.k_image));


        defaultVideos.add(new VideoItem(
                "Pre-roll, linear not skippable",
                SOURCE_URL1,
                LIC_URL1,
                AD_1,
                R.drawable.k_image));

        defaultVideos.add(new VideoItem(
                "Pre-roll, linear, skippable",
                SOURCE_URL1,
                LIC_URL1,
                AD_2,
                R.drawable.k_image));

        defaultVideos.add(new VideoItem(
                "VMAP",
                SOURCE_URL1,
                LIC_URL1,
                AD_4,
                R.drawable.k_image));

        defaultVideos.add(new VideoItem(
                "VMAP Pods",
                SOURCE_URL1,
                LIC_URL1,
                AD_5,
                R.drawable.k_image));

        defaultVideos.add(new VideoItem(
                "Wrapper",
                SOURCE_URL1,
                LIC_URL1,
                AD_6,
                R.drawable.k_image));

        defaultVideos.add(new VideoItem(
                "VMAP Pods Bump",
                SOURCE_URL1,
                LIC_URL1,
                AD_7,
                R.drawable.k_image));

        defaultVideos.add(new VideoItem(
                "VMAP Pods Bump every 10 sec",
                SOURCE_URL1,
                LIC_URL1,
                AD_8,
                R.drawable.k_image));

        defaultVideos.add(new VideoItem(
                "Google Search",
                SOURCE_URL1,
                LIC_URL1,
                AD_GOOGLE_SEARCH,
                R.drawable.k_image));

        defaultVideos.add(new VideoItem(
                "Post-roll",
                SOURCE_URL1,
                LIC_URL1,
                AD_3,
                R.drawable.k_image));
        defaultVideos.add(new VideoItem(
                "voot ad",
                VOOT_URL1,
                LIC_URL1,
                AD_VOOT1,
                R.drawable.k_image));

        return defaultVideos;
    }
}