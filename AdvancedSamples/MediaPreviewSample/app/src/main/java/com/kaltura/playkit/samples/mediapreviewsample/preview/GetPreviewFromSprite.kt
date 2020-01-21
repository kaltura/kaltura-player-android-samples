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

open class GetPreviewFromSprite(var spriteSliceWidth: Int,
                                var spriteSliceHeight: Int, var spriteSlicesCount: Int, var mediaEntryId: String) {

    private val log = PKLog.get("GetPreviewFromSprite")
    private var imageSpriteUrl = "http://cdnapi.kaltura.com/p/1982551/sp/198255100/thumbnail/entry_id/${mediaEntryId}/height/${spriteSliceHeight}/width/${spriteSliceWidth}/vid_slices/${spriteSlicesCount}"

    private val connectionReadTimeOut: Int = 120000
    private val connectionTimeOut: Int = 120000
    private val successResponseCode: Int = 200
    private val requestMethod: String = "GET"

    /**
     * Download the whole Sprite image which has preview image slices
     */
    open suspend fun downloadSpriteCoroutine(): HashMap<String, Bitmap>? {

        var spritesHashMap: HashMap<String, Bitmap>? = null

        return GlobalScope.async(Dispatchers.IO) {
            var connection: HttpURLConnection? = null
            var inputStream: InputStream? = null

            try {
                val url = URL(imageSpriteUrl)
                connection = url.openConnection() as HttpURLConnection
                connection.readTimeout = connectionReadTimeOut
                connection.connectTimeout = connectionTimeOut
                connection.requestMethod = requestMethod
                connection.doInput = true
                connection.connect()

                if (connection.responseCode == successResponseCode) {
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
            val cropRect = Rect(previewImageSize * (spriteSliceWidth - 2), 0, (previewImageSize + 1) * (spriteSliceWidth - 2), spriteSliceHeight)
            val extractedImageBitmap: Bitmap = try {
                bitmapRegionDecoder.decodeRegion(cropRect, options)
            } catch (e: IllegalArgumentException) {
                log.e("The given height and width is out of rectangle which is outside the image. ImageSpriteUrl: ${imageSpriteUrl}")
                return null
            }
            previewImagesHashMap?.put("" + previewImageSize, extractedImageBitmap)
        }

        bitmapRegionDecoder.recycle()

        return previewImagesHashMap
    }

}