# DashThumbnail Sample

## Purpose

This PlayKit sample shows how to show a media preview when seekbar is being scrubbed.
These preview images are coming in Dash manifest file.

## How to use

1. Open `DashThumbnailSample` project in Android Studio.
2. Compile.
3. This sample is about the Image representation set present in the Dash manifest.
4. Application has to pass mediaId if they are using our backend. In case, if you have playback url, `buildUsingBasicPlayer` can be kept `true` and then replace the url with `FIRST_SOURCE_URL`
5. In `PlaybackControlsView.kt`, there is an inner class `ComponentListener` where in `onScrubMove` interface method has the logic showing the preview image while scrubbing.
6. `com.kaltura.playkit.samples.dashthumbnailsample.preview` package has the class to download and extract images from image sprite.
7. On each seek, we are getting the thumbnail Bitmap using our ExecutorService with Glide and saving Bitmap in Hashmap.
8. Saving thumbnail or Bitmap in hashmap will save the network call in case if seek happens for the same position again.
9. We are using Glide MemCache and DiskCache both.
10. For Vod content only, we are storing the Bitmap in Hashmap. For live each time, there will be a network call to fetch it because Timeline for Live is changing everytime.


## License

See [License and Copyright Information](https://github.com/kaltura/kaltura-player-android-samples#license-and-copyright-information)
