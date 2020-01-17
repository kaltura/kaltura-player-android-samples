package com.kaltura.playkit.samples.mediapreviewsample.preview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.text.TextUtils
import com.kaltura.playkit.PKLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

open class GetPreviewFromSprite(val spriteUrl: String, var spriteSliceWidth: Int,
                                var spriteSliceHeight: Int, var spriteSlicesCount: Int) {

    private val log = PKLog.get("GetPreviewFromSprite")

    /**
     * Download the whole Sprite image which has preview image slices
     */
    open suspend fun downloadSpriteCoroutine(): HashMap<String, Bitmap>? {

        var spritesHashMap: HashMap<String, Bitmap>? = null

        return GlobalScope.async(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            var inputStream: InputStream? = null

            try {
                if (TextUtils.isEmpty(spriteUrl)) {
                    log.w("Sprite preview url is empty")
                }
                val url = URL(spriteUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.readTimeout = 120000
                connection.connectTimeout = 120000
                connection.requestMethod = "GET"
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == 200) {
                    inputStream = connection.inputStream
                } else {
                    log.e("Error downloading the image. Response code = " + connection.responseMessage)
                }
                spritesHashMap = framesFromImageStream(inputStream, spriteSlicesCount)
            } catch (exception: IOException) {
                log.e(exception.toString())
            } finally {
                connection?.disconnect()
            }

            spritesHashMap

        }.await()
    }

    /**
     * It excepts 1 row sprite image currently and multiple columns
     * Extract the image frames from Sprite image
     * Logic is to crop rectangle from sprite from left (left, top) (bottom, right) coordinates
     */
    private fun framesFromImageStream(inputStream: InputStream?, columns: Int): HashMap<String, Bitmap>? {

        val previewImagesHashMap: HashMap<String, Bitmap>? = HashMap()
        val options = BitmapFactory.Options()
        val bitmapRegionDecoder: BitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false)

        for (previewImageSize: Int in 0..columns) {
            val cropRect = Rect(previewImageSize * spriteSliceWidth, 0, (previewImageSize + 1) * spriteSliceWidth, spriteSliceHeight)
            val extractedImageBitmap: Bitmap = try {
                bitmapRegionDecoder.decodeRegion(cropRect, options)
            } catch (e: IllegalArgumentException) {
                log.e("The given height and width is out of rectangle which is outside the image.")
                return null
            }
            previewImagesHashMap?.put("" + previewImageSize, extractedImageBitmap)
        }

        bitmapRegionDecoder.recycle()

        return previewImagesHashMap
    }

}