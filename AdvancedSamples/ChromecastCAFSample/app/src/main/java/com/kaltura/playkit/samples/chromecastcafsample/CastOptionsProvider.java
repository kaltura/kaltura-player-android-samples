package com.kaltura.playkit.samples.chromecastcafsample;

import com.google.android.gms.cast.LaunchOptions;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.*;
import com.google.android.gms.common.images.WebImage;

import android.content.Context;

import java.util.Arrays;
import java.util.List;

/**
 * Implements {@link OptionsProvider} to provide {@link CastOptions}.
 */
public class CastOptionsProvider implements OptionsProvider {

    @Override
    public CastOptions getCastOptions(Context context) {
        NotificationOptions notificationOptions = new NotificationOptions.Builder()
                .setActions(Arrays.asList(MediaIntentReceiver.ACTION_SKIP_NEXT,
                        MediaIntentReceiver.ACTION_TOGGLE_PLAYBACK,
                        MediaIntentReceiver.ACTION_STOP_CASTING), new int[]{1, 2})
                .setTargetActivityClassName(ExpandedControlsActivity.class.getName())
                .build();
        CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
                .setImagePicker(new ImagePickerImpl())
                .setNotificationOptions(notificationOptions)
                .setExpandedControllerActivityClassName(ExpandedControlsActivity.class.getName())
                .build();
        /** Following lines enable Cast Connect */
        LaunchOptions launchOptions = new LaunchOptions.Builder()
                .setAndroidReceiverCompatible(true)
                .build();
        return new CastOptions.Builder()
                .setLaunchOptions(launchOptions)
                .setReceiverApplicationId(context.getString(R.string.receiver_application_id))
                .setCastMediaOptions(mediaOptions)
                .build();
    }

    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context appContext) {
        return null;
    }

    private static class ImagePickerImpl extends ImagePicker {

        @Override
        public WebImage onPickImage(MediaMetadata mediaMetadata, ImageHints hints) {
            int type = hints.getType();
            if ((mediaMetadata == null) || !mediaMetadata.hasImages()) {
                return null;
            }
            List<WebImage> images = mediaMetadata.getImages();
            if (images.size() == 1) {
                return images.get(0);
            } else {
                if (type == ImagePicker.IMAGE_TYPE_MEDIA_ROUTE_CONTROLLER_DIALOG_BACKGROUND) {
                    return images.get(0);
                } else {
                    return images.get(1);
                }
            }
        }
    }
}