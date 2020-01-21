# MediaPreview Sample

## Purpose

This PlayKit sample shows how to show a media preview when seekbar is being scrubbed.

## How to use

1. Open `MediaPreviewSample` project in Android Studio.
2. Compile.
3. This sample has an option to change media and ads are enabled.
4. For sample purpose, we are keeping two sample Image Sprites (https://www.w3schools.com/css/css_image_sprites.asp). Application needs to pass, height of image slice, width of image slice, number of slices and media entryId. We will download the image from our backend.
5. We are currently supporting the (1 row x n columns or no of slices) image sprite.
6. In `PlaybackControlsView.kt`, there is an inner class `ComponentListener` where in `onScrubMove` interface method has the logic showing the preview image while scrubbing.
7. `com.kaltura.playkit.samples.mediapreviewsample.preview` package has the class to download and extract images from image sprite.


## License

See [License and Copyright Information](https://github.com/kaltura/playkit-android-samples#license-and-copyright-information)
