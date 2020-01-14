package com.kaltura.playkit.samples.mediapreviewsample.preview

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
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

    private var spritesHashMap: HashMap<String, Bitmap>? = null
    val log = PKLog.get("GetPreviewFromSprite")

    open suspend fun downloadSpriteCoroutine(): HashMap<String, Bitmap>? {
        return GlobalScope.async(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            var inputStream: InputStream? = null

            try {
                val url = URL(spriteUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.readTimeout = 120000
                connection.connectTimeout = 120000
                connection.requestMethod = "GET"
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == 200) {
                    inputStream = connection.inputStream
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

    private fun framesFromImageStream(inputStream: InputStream?, columns: Int): HashMap<String, Bitmap>? {

        val previewImagesHashMap: HashMap<String, Bitmap>? = HashMap()
        val options = BitmapFactory.Options()
        val bitmapRegionDecoder: BitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false)

        for (previewImageSize: Int in 0..columns) {
            val cropRect = Rect(previewImageSize * spriteSliceWidth, 0, (previewImageSize + 1) * spriteSliceWidth, spriteSliceHeight)
            val extractedImageBitmap: Bitmap = bitmapRegionDecoder.decodeRegion(cropRect, options)
            previewImagesHashMap?.put("" + previewImageSize, extractedImageBitmap)
        }

        return previewImagesHashMap
    }

}