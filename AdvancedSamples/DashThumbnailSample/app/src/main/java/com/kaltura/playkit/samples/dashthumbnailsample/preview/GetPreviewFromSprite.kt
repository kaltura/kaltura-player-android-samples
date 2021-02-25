package com.kaltura.playkit.samples.dashthumbnailsample.preview

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.RectF
import androidx.core.graphics.toRect
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.FutureTarget
import com.bumptech.glide.request.target.Target.SIZE_ORIGINAL
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.player.thumbnail.ThumbnailInfo
import com.kaltura.playkit.samples.dashthumbnailsample.MainActivity
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

class GetPreviewFromSprite(var context: Context) {

    private val log = PKLog.get("GetPreviewFromSprite")
    private val imageThreadPoolExecutor: ExecutorService = Executors.newFixedThreadPool(10)

    fun downloadSpriteCoroutine(thumbnailInfo: ThumbnailInfo, currentlyPlayingMediaImageKey: String, isLiveMedia: Boolean): Future<Bitmap?>? {
        val addImageExtractionProcessToPool = AddImageExtractionProcessToPool(thumbnailInfo, context, currentlyPlayingMediaImageKey, isLiveMedia)
        return imageThreadPoolExecutor.submit(addImageExtractionProcessToPool)
    }

    fun terminateService() {
        imageThreadPoolExecutor.shutdownNow()
        log.d("Service Terminated")
    }

    private class AddImageExtractionProcessToPool(val thumbnailInfo: ThumbnailInfo?, var context: Context, var currentlyPlayingMediaImageKey: String, var isLiveMedia: Boolean): Callable<Bitmap?> {

        private val log = PKLog.get("ImageProcessing")

        override fun call(): Bitmap? {

            var extractedBitmap: Bitmap? = null

            thumbnailInfo?.let {
                val futureTarget: FutureTarget<Bitmap> = Glide.with(context)
                        .asBitmap()
                        .skipMemoryCache(false)
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .load(it.url)
                        .submit(SIZE_ORIGINAL, SIZE_ORIGINAL)

                val fetchedBitmap = futureTarget.get()
                log.d("Bitmap URL = ${it.url} ")
                log.d("Bitmap Received = ${fetchedBitmap}  Thread Name = ${Thread.currentThread().name}")
                extractedBitmap = convertBitmapAndExtractTile(fetchedBitmap, it, isLiveMedia)
            }
            return extractedBitmap
        }

        fun convertBitmapAndExtractTile(bitmap: Bitmap?, thumbnailInfo: ThumbnailInfo, isLiveMedia: Boolean): Bitmap? {
            val inputStream: InputStream = convertBitmapToStream(bitmap)
            return framesFromImageStream(inputStream, thumbnailInfo, currentlyPlayingMediaImageKey, isLiveMedia)
        }

        private fun convertBitmapToStream(bitmap: Bitmap?) : InputStream {
            val byteOutputStream = ByteArrayOutputStream()
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, byteOutputStream)
            val bitmapData: ByteArray = byteOutputStream.toByteArray()
            return ByteArrayInputStream(bitmapData)
        }

        private fun framesFromImageStream(inputStream: InputStream?, thumbnailInfo: ThumbnailInfo, currentlyPlayingMediaImageKey: String, isLiveMedia: Boolean): Bitmap? {
            val options = BitmapFactory.Options()
            options.inPreferredConfig = Bitmap.Config.RGB_565
            val bitmapRegionDecoder: BitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false)

            val cropRect: RectF? = MainActivity.getExtractedRectangle(thumbnailInfo)
            //log.e("cropRect: ${cropRect?.toString()}")

            val extractedImageBitmap: Bitmap = try {
                bitmapRegionDecoder.decodeRegion(cropRect?.toRect(), options)
            } catch (e: IllegalArgumentException) {
                log.e("The given height and width is out of rectangle which is outside the image. ImageSpriteUrl: ${thumbnailInfo.url}")
                bitmapRegionDecoder.recycle()
                return null
            }

            if (!isLiveMedia) {
                MainActivity.previewImageHashMap[currentlyPlayingMediaImageKey.plus(cropRect?.toString())] = extractedImageBitmap
            }

            bitmapRegionDecoder.recycle()

            return extractedImageBitmap
        }
    }
}

