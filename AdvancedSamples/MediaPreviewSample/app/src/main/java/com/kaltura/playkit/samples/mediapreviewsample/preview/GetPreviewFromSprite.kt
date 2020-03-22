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
    private var imageSpriteUrl = "http://cdnapi.kaltura.com/p/2254732/sp/198255100/thumbnail/entry_id/${mediaEntryId}/width/${spriteSliceWidth}/vid_slices/${spriteSlicesCount}"

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
        options.inPreferredConfig = Bitmap.Config.RGB_565
        val bitmapRegionDecoder: BitmapRegionDecoder = BitmapRegionDecoder.newInstance(inputStream, false)

        for (previewImageIndex: Int in 0 until columns) {
            val cropRect = Rect((previewImageIndex * spriteSliceWidth), 0, (previewImageIndex * spriteSliceWidth + spriteSliceWidth), spriteSliceHeight)
            val extractedImageBitmap: Bitmap = try {
                bitmapRegionDecoder.decodeRegion(cropRect, options)
            } catch (e: IllegalArgumentException) {
                log.e("The given height and width is out of rectangle which is outside the image. ImageSpriteUrl: ${imageSpriteUrl}")
                previewImagesHashMap?.clear()
                bitmapRegionDecoder.recycle()
                return null
            }
            previewImagesHashMap?.put("" + previewImageIndex, extractedImageBitmap)
        }

        bitmapRegionDecoder.recycle()

        return previewImagesHashMap
    }

}

