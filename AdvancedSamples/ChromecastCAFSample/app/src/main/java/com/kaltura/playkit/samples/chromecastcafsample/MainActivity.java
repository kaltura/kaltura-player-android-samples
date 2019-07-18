package com.kaltura.playkit.samples.chromecastcafsample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaLoadOptions;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.CastStateListener;
import com.google.android.gms.cast.framework.IntroductoryOverlay;
import com.google.android.gms.cast.framework.SessionManagerListener;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.kaltura.playkit.PKMediaConfig;
import com.kaltura.playkit.plugins.googlecast.caf.CAFCastBuilder;
import com.kaltura.playkit.plugins.googlecast.caf.KalturaCastBuilder;
import com.kaltura.playkit.plugins.googlecast.caf.KalturaPhoenixCastBuilder;
import com.kaltura.playkit.plugins.googlecast.caf.MediaInfoUtils;
import com.kaltura.playkit.plugins.googlecast.caf.adsconfig.AdsConfig;
import com.kaltura.tvplayer.KalturaPlayer;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    //Tag for logging.
    private static final String TAG = MainActivity.class.getSimpleName();
    private IntroductoryOverlay mIntroductoryOverlay;
    //Media entry configuration constants.
    private static final String SOURCE_URL = "https://cdnapisec.kaltura.com/p/2215841/sp/221584100/playManifest/entryId/1_w9zx2eti/protocol/https/format/applehttp/falvorIds/1_1obpcggb,1_yyuvftfz,1_1xdbzoa6,1_k16ccgto,1_djdf6bk8/a.m3u8";
    private static final String ENTRY_ID = "entry_id";
    private static final String MEDIA_SOURCE_ID = "source_id";

    //Ad configuration constants.
    private static final String AD_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";
    private static final String INCORRECT_AD_TAG_URL = "incorrect_ad_tag_url";
    private static final int PREFERRED_AD_BITRATE = 600;

    private KalturaPlayer player;
    private PKMediaConfig mediaConfig;
    private Button changeMediaButton;
    private Button playPauseButton;
    private CastStateListener mCastStateListener;
    private SessionManagerListener<CastSession> mSessionManagerListener;
    private CastContext mCastContext;
    private MenuItem mediaRouteMenuItem;
    private PlaybackLocation mLocation;
    private CastSession mCastSession;
    private MediaInfo mSelectedMedia;
    private RemoteMediaClient remoteMediaClient;
    public enum PlaybackLocation {
        LOCAL,
        REMOTE
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //NOTE - FOR OTT CASTING YOU HAVE TO CHANGE THE PRODUCT FLAVOUR TO OTT
        mCastStateListener = new CastStateListener() {
            @Override
            public void onCastStateChanged(int newState) {
                if (newState != CastState.NO_DEVICES_AVAILABLE) {
                    showIntroductoryOverlay();
                }
            }
        };

        setupCastListener();
        mCastContext = CastContext.getSharedInstance(this);
        mCastContext.getSessionManager().addSessionManagerListener(
                mSessionManagerListener, CastSession.class);
        // mCastContext.registerLifecycleCallbacksBeforeIceCreamSandwich(this, savedInstanceState);
        // mCastSession = mCastContext.getSessionManager().getCurrentCastSession();


        addCastOvpButton();
        addCastOttButton();
        addChangeMediaButton();


    }

    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.browse, menu);
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), menu, R.id.media_route_menu_item);
        return true;
    }

    private void addCastOvpButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById(R.id.cast_ovp_button);
        if ("ott".equals(BuildConfig.FLAVOR)) {
            playPauseButton.setVisibility(View.INVISIBLE);
        }

        //Add clickListener.
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRemoteMediaOvp(0,true);
                return;
            }
        });
    }

    private void addCastOttButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById(R.id.cast_ott_button);
        if ("ovp".equals(BuildConfig.FLAVOR)) {
            playPauseButton.setVisibility(View.INVISIBLE);
        }
        //Add clickListener.
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadRemoteMediaOtt(0,true);
                return;
            }
        });
    }

    private void addChangeMediaButton() {
        //Get reference to the play/pause button.
        changeMediaButton =  this.findViewById(R.id.change_media_button);
        changeMediaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PendingResult<RemoteMediaClient.MediaChannelResult> pendingResult = null;
                MediaLoadOptions loadOptions = new MediaLoadOptions.Builder().setAutoplay(true).setPlayPosition(0).build();
                String vastAdTag = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=" + 43543;
                if ("ovp".equals(BuildConfig.FLAVOR)) {
                    pendingResult = remoteMediaClient.load(getOvpCastMediaInfo("0_b7s02kjl", vastAdTag, CAFCastBuilder.AdTagType.VAST), loadOptions);
                    pendingResult.setResultCallback(new ResultCallback<RemoteMediaClient.MediaChannelResult>() {

                        @Override
                        public void onResult(@NonNull RemoteMediaClient.MediaChannelResult mediaChannelResult) {

                            JSONObject customData = mediaChannelResult.getCustomData();
                            if (customData != null) {
                                //log.v("loadMediaInfo. customData = " + customData.toString());
                            } else {
                                //log.v("loadMediaInfo. customData == null");
                            }
                        }
                    });
                } else {
                    pendingResult = remoteMediaClient.load(getOttCastMediaInfo("548571","Web_Main", "", null, CAFCastBuilder.HttpProtocol.Http), loadOptions);
                    pendingResult.setResultCallback(new ResultCallback<RemoteMediaClient.MediaChannelResult>() {

                        @Override
                        public void onResult(@NonNull RemoteMediaClient.MediaChannelResult mediaChannelResult) {

                            JSONObject customData = mediaChannelResult.getCustomData();
                            if (customData != null) {
                                //log.v("loadMediaInfo. customData = " + customData.toString());
                            } else {
                                //log.v("loadMediaInfo. customData == null");
                            }
                        }
                    });
                }
            }
        });
    }



    private void showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay.remove();
        }
        if ((mediaRouteMenuItem != null) && mediaRouteMenuItem.isVisible()) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mIntroductoryOverlay = new IntroductoryOverlay.Builder(
                            MainActivity.this, mediaRouteMenuItem)
                            .setTitleText("Introducing Cast")
                            .setSingleTime()
                            .setOnOverlayDismissedListener(
                                    new IntroductoryOverlay.OnOverlayDismissedListener() {
                                        @Override
                                        public void onOverlayDismissed() {
                                            mIntroductoryOverlay = null;
                                        }
                                    })
                            .build();
                    mIntroductoryOverlay.show();
                }
            });
        }
    }
    private void setupCastListener() {
        mSessionManagerListener = new SessionManagerListener<CastSession>() {

            @Override
            public void onSessionEnded(CastSession session, int error) {
                onApplicationDisconnected();
                invalidateOptionsMenu();
            }

            @Override
            public void onSessionResumed(CastSession session, boolean wasSuspended) {
                onApplicationConnected(session);
                mCastSession = session;
                invalidateOptionsMenu();
            }

            @Override
            public void onSessionResumeFailed(CastSession session, int error) {
                onApplicationDisconnected();
            }

            @Override
            public void onSessionStarted(CastSession session, String sessionId) {
                onApplicationConnected(session);
                invalidateOptionsMenu();
            }

            @Override
            public void onSessionStartFailed(CastSession session, int error) {
                onApplicationDisconnected();
                invalidateOptionsMenu();
            }

            @Override
            public void onSessionStarting(CastSession session) {
            }

            @Override
            public void onSessionEnding(CastSession session) {}

            @Override
            public void onSessionResuming(CastSession session, String sessionId) {}

            @Override
            public void onSessionSuspended(CastSession session, int reason) {}

            private void onApplicationConnected(CastSession castSession) {
                mCastSession = castSession;

                if (null != mSelectedMedia) {

                    if (player.isPlaying()) {
                        player.pause();
                        loadRemoteMediaOvp((int)player.getCurrentPosition(), true);
                        finish();
                        return;
                    } else {
                        updatePlaybackLocation(PlaybackLocation.REMOTE);
                    }
                }
                supportInvalidateOptionsMenu();
            }

            private void onApplicationDisconnected() {
                updatePlaybackLocation(PlaybackLocation.LOCAL);
                mLocation = PlaybackLocation.LOCAL;
                supportInvalidateOptionsMenu();
            }
        };
    }

    private void loadRemoteMediaOvp(int position, boolean autoPlay) {
        if (mCastSession == null) {
            return;
        }
        remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return;
        }
        remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
            @Override
            public void onStatusUpdated() {
                Intent intent = new Intent(MainActivity.this, ExpandedControlsActivity.class);
                startActivity(intent);
                //remoteMediaClient.removeListener(this);
            }

            @Override
            public void onMetadataUpdated() {
            }

            @Override
            public void onQueueStatusUpdated() {
            }

            @Override
            public void onPreloadStatusUpdated() {
            }

            @Override
            public void onSendingRemoteMediaRequest() {
            }

            @Override
            public void onAdBreakStatusUpdated() {
            }
        });

        PendingResult<RemoteMediaClient.MediaChannelResult> pendingResult = null;
        MediaLoadOptions loadOptions = new MediaLoadOptions.Builder().setAutoplay(true).setPlayPosition(position).build();
        String vastAdTag = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=" +  11223;
        //using QA partner 1091
        pendingResult = remoteMediaClient.load(getOvpCastMediaInfo("0_fl4ioobl", vastAdTag, CAFCastBuilder.AdTagType.VAST), loadOptions);
        pendingResult.setResultCallback(new ResultCallback<RemoteMediaClient.MediaChannelResult>() {

            @Override
            public void onResult(@NonNull RemoteMediaClient.MediaChannelResult mediaChannelResult) {

                JSONObject customData = mediaChannelResult.getCustomData();
                if (customData != null) {
                    //log.v("loadMediaInfo. customData = " + customData.toString());
                } else {
                    //log.v("loadMediaInfo. customData == null");
                }
            }
        });
    }

    private void loadRemoteMediaOtt(int position, boolean autoPlay) {
        if (mCastSession == null) {
            return;
        }
        remoteMediaClient = mCastSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            return;
        }
        remoteMediaClient.addListener(new RemoteMediaClient.Listener() {
            @Override
            public void onStatusUpdated() {
                Intent intent = new Intent(MainActivity.this, ExpandedControlsActivity.class);
                startActivity(intent);
                //remoteMediaClient.removeListener(this);
            }

            @Override
            public void onMetadataUpdated() {
            }

            @Override
            public void onQueueStatusUpdated() {
            }

            @Override
            public void onPreloadStatusUpdated() {
            }

            @Override
            public void onSendingRemoteMediaRequest() {
            }

            @Override
            public void onAdBreakStatusUpdated() {
            }
        });
        PendingResult<RemoteMediaClient.MediaChannelResult> pendingResult = null;
        MediaLoadOptions loadOptions = new MediaLoadOptions.Builder().setAutoplay(true).setPlayPosition(position).build();
        pendingResult = remoteMediaClient.load(getOttCastMediaInfo("548579","Web_Main", "", null, CAFCastBuilder.HttpProtocol.Http), loadOptions);
        pendingResult.setResultCallback(new ResultCallback<RemoteMediaClient.MediaChannelResult>() {

            @Override
            public void onResult(@NonNull RemoteMediaClient.MediaChannelResult mediaChannelResult) {

                JSONObject customData = mediaChannelResult.getCustomData();
                if (customData != null) {
                    //log.v("loadMediaInfo. customData = " + customData.toString());
                } else {
                    //log.v("loadMediaInfo. customData == null");
                }
            }
        });
    }

    private void updatePlaybackLocation(PlaybackLocation location) {
        mLocation = location;
    }


    public AdsConfig createAdsConfigVast(String adTagUrl) {
        return MediaInfoUtils.createAdsConfigVastInPosition(0, adTagUrl);
    }

    public AdsConfig createAdsConfigVmap(String adTagUrl) {
        return MediaInfoUtils.createAdsConfigVmap(adTagUrl);
    }

    private MediaInfo getOttCastMediaInfo(String mediaId, String mediaFormat, String adTagUrl, CAFCastBuilder.AdTagType adTagType, CAFCastBuilder.HttpProtocol protocol) {

        List<String> formats = null;
        if (mediaFormat != null) {
            formats = new ArrayList<>();
            formats.add(mediaFormat);
        }

        CAFCastBuilder phoenixCastBuilder = new KalturaPhoenixCastBuilder()
                .setMediaEntryId(mediaId)
                .setKs("")
                .setFormats(formats)
                .setStreamType(CAFCastBuilder.StreamType.VOD)
                .setAssetReferenceType(CAFCastBuilder.AssetReferenceType.Media)
                .setContextType(CAFCastBuilder.PlaybackContextType.Playback)
                .setMediaType(CAFCastBuilder.KalturaAssetType.Media)
                .setProtocol(protocol);

        if (!TextUtils.isEmpty(adTagUrl)) {
            if (adTagType == CAFCastBuilder.AdTagType.VAST) {
                phoenixCastBuilder.setAdsConfig(createAdsConfigVast(adTagUrl));
            } else {
                phoenixCastBuilder.setAdsConfig(createAdsConfigVmap(adTagUrl));
            }
            //phoenixCastBuilder.setDefaultTextLangaugeCode("en")
        }
        return returnResult(phoenixCastBuilder);
    }


    private MediaInfo getOvpCastMediaInfo(String entryId, String adTagUrl, CAFCastBuilder.AdTagType adTagType) {

        CAFCastBuilder ovpV3CastBuilder =  new KalturaCastBuilder()
                .setMediaEntryId(entryId)
                .setKs("")
                .setStreamType(CAFCastBuilder.StreamType.VOD);
        if (!TextUtils.isEmpty(adTagUrl)) {
            if (adTagType == CAFCastBuilder.AdTagType.VAST) {
                ovpV3CastBuilder.setAdsConfig(createAdsConfigVast(adTagUrl));
            } else {
                ovpV3CastBuilder.setAdsConfig(createAdsConfigVmap(adTagUrl));
            }
            //ovpV3CastBuilder.setDefaultTextLangaugeCode("en")

        }
        return returnResult(ovpV3CastBuilder);
    }


    private MediaInfo returnResult(CAFCastBuilder cafCastBuilder) {
        return cafCastBuilder.build();
    }

//    private void setMediaMetadata(BasicCastBuilder basicCastBuilder, ConverterGoogleCast converterGoogleCast) {
//
//        MediaMetadata mediaMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
//        ConverterMediaMetadata converterMediaMetadata = converterGoogleCast.getMediaMetadata();
//
//        if (converterMediaMetadata != null) { // MediaMetadata isn't mandatory
//
//            String title = converterMediaMetadata.getTitle();
//            String subTitle = converterMediaMetadata.getSubtitle();
//            ConverterImageUrl image = converterMediaMetadata.getImageUrl();
//
//            if (!TextUtils.isEmpty(title)) {
//                mediaMetadata.putString(MediaMetadata.KEY_TITLE, title);
//            }
//
//            if (!TextUtils.isEmpty(subTitle)) {
//                mediaMetadata.putString(MediaMetadata.KEY_SUBTITLE, subTitle);
//            }
//
//            if (image != null) {
//                Uri uri = null;
//                String url = image.getURL();
//                int width = image.getWidth();
//                int height = image.getHeight();
//
//                if (!TextUtils.isEmpty(url)) {
//                    uri = Uri.parse(url);
//                }
//
//                if (uri != null && width != 0 && height != 0) {
//                    mediaMetadata.addImage(new WebImage(uri, width, height));
//                }
//
//            }
//            basicCastBuilder.setMetadata(mediaMetadata);
//        }
//    }

    @Override
    protected void onResume() {
        mCastContext.addCastStateListener(mCastStateListener);
        super.onResume();
    }

    @Override
    protected void onPause() {
        mCastContext.removeCastStateListener(mCastStateListener);
        super.onPause();
    }
}
