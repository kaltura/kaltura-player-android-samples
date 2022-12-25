package com.kaltura.playkit.samples.chromecastcafsample

import android.content.Context

import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.framework.CastOptions
import com.google.android.gms.cast.framework.OptionsProvider
import com.google.android.gms.cast.framework.SessionProvider
import com.google.android.gms.cast.framework.media.*
import com.google.android.gms.common.images.WebImage

import java.util.Arrays

/**
 * Implements [OptionsProvider] to provide [CastOptions].
 */
class CastOptionsProviderKt: OptionsProvider {

    override fun getCastOptions(context: Context): CastOptions {
        val notificationOptions = NotificationOptions.Builder()
                .setActions(Arrays.asList(MediaIntentReceiver.ACTION_SKIP_NEXT,
                        MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
                        MediaIntentReceiver.ACTION_STOP_CASTING), intArrayOf(1, 2))
                .setTargetActivityClassName(ExpandedControlsActivity::class.java.name)
                .build()
        val mediaOptions = CastMediaOptions.Builder()
                //.setImagePicker(ImagePickerImpl())
                .setNotificationOptions(notificationOptions)
                .setExpandedControllerActivityClassName(ExpandedControlsActivity::class.java.name)
                .build()
        return CastOptions.Builder()
                .setReceiverApplicationId(context.getString(R.string.receiver_application_id))
                .setCastMediaOptions(mediaOptions)
                .build()
    }

    override fun getAdditionalSessionProviders(appContext: Context): List<SessionProvider>? {
        return null
    }


    private class ImagePickerImpl: ImagePicker() {
        override fun onPickImage(mediaMetadata: MediaMetadata, hints: ImageHints): WebImage? {
            if (mediaMetadata == null || !mediaMetadata.hasImages()) {
                return null
            }
            val images = mediaMetadata.images
            return if (images.size == 1) {
                images[0]
            } else {
                if (hints.type == ImagePicker.IMAGE_TYPE_MEDIA_ROUTE_CONTROLLER_DIALOG_BACKGROUND) {
                    images[0]
                } else {
                    images[1]
                }
            }
        }
    }
}
