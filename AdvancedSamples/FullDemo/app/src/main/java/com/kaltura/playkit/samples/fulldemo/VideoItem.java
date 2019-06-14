package com.kaltura.playkit.samples.fulldemo;

public final class VideoItem {

    private final int mThumbnailResourceId;
    private final String mTitle;
    private final String mVideoUrl;
    private final String mAdTagUrl;
    private final String mVideoLic;

    public VideoItem(String title, String videoUrl, String videoLic, String adTagUrl, int thumbnailResourceId) {
        super();
        mVideoUrl = videoUrl;
        mVideoLic = videoLic;
        mThumbnailResourceId = thumbnailResourceId;
        mTitle = title;
        mAdTagUrl = adTagUrl;

    }

    /**
     * Returns the video thumbnail image resource.
     */
    public int getImageResource() {
        return mThumbnailResourceId;
    }

    /**
     * Returns the title of the video item.
     */
    public String getTitle() {
        return mTitle;
    }

    /**
     * Returns the URL of the content video.
     */
    public String getVideoUrl() {
        return mVideoUrl;
    }

    /**
     * Returns the ad tag for the video.
     */
    public String getAdTagUrl() {
        return mAdTagUrl;
    }

    /**
     * Returns the Video lic url for the video.
     */
    public String getVideoLic() {
        return mVideoLic;
    }
}