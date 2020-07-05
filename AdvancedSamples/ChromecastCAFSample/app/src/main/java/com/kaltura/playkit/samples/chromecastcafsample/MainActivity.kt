package com.kaltura.playkit.samples.chromecastcafsample

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadOptions
import com.google.android.gms.cast.framework.*
import com.google.android.gms.cast.framework.media.RemoteMediaClient
import com.google.android.gms.common.api.PendingResult
import com.kaltura.playkit.PKMediaConfig
import com.kaltura.playkit.plugins.googlecast.caf.*
import com.kaltura.playkit.plugins.googlecast.caf.adsconfig.AdsConfig
import com.kaltura.playkit.plugins.googlecast.caf.basic.*
import com.kaltura.tvplayer.KalturaPlayer
import java.util.*


class MainActivity: AppCompatActivity() {

    //Tag for logging.
    private val TAG = MainActivity::class.java.simpleName
    //Media entry configuration constants.
    private val SOURCE_URL = "https://cdnapisec.kaltura.com/p/2215841/sp/221584100/playManifest/entryId/1_w9zx2eti/protocol/https/format/applehttp/falvorIds/1_1obpcggb,1_yyuvftfz,1_1xdbzoa6,1_k16ccgto,1_djdf6bk8/a.m3u8"
    private val ENTRY_ID = "entry_id"
    private val MEDIA_SOURCE_ID = "source_id"

    //Ad configuration constants.
    private val AD_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator="
    private val INCORRECT_AD_TAG_URL = "incorrect_ad_tag_url"
    private val PREFERRED_AD_BITRATE = 600

    private var mIntroductoryOverlay: IntroductoryOverlay? = null

    private val player: KalturaPlayer? = null
    private val mediaConfig: PKMediaConfig? = null
    private var changeMediaButton: Button? = null
    private var playPauseButton: Button? = null
    private var basicPlayPauseButton: Button? = null
    private var mCastStateListener: CastStateListener? = null
    private var mSessionManagerListener: SessionManagerListener<CastSession>? = null
    private var mCastContext: CastContext? = null
    private var mediaRouteMenuItem: MenuItem? = null
    private var mLocation: PlaybackLocation? = null
    private var mCastSession: CastSession? = null
    private val mSelectedMedia: MediaInfo? = null
    private var remoteMediaClient: RemoteMediaClient? = null

    enum class PlaybackLocation {
        LOCAL,
        REMOTE
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //NOTE - FOR OTT CASTING YOU HAVE TO CHANGE THE PRODUCT FLAVOUR TO OTT
        mCastStateListener = CastStateListener { newState ->
            if (newState != CastState.NO_DEVICES_AVAILABLE) {
                showIntroductoryOverlay()
            }
        }

        setupCastListener()
        mCastContext = CastContext.getSharedInstance(this)
        mCastContext!!.sessionManager.addSessionManagerListener(
                mSessionManagerListener!!, CastSession::class.java)
        // mCastContext.registerLifecycleCallbacksBeforeIceCreamSandwich(this, savedInstanceState);
        // mCastSession = mCastContext.getSessionManager().getCurrentCastSession();


        addCastOvpButton()
        addCastBasicButton()
        addCastOttButton()
        addChangeMediaButton()


    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.browse, menu)
        mediaRouteMenuItem = CastButtonFactory.setUpMediaRouteButton(applicationContext, menu, R.id.media_route_menu_item)
        return true
    }

    private fun addCastOvpButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById(R.id.cast_ovp_button)
        if ("ott" == BuildConfig.FLAVOR) {
            playPauseButton!!.visibility = View.INVISIBLE
        }

        //Add clickListener.
        playPauseButton!!.setOnClickListener(View.OnClickListener {
            loadRemoteMediaOvp(0, true)
            return@OnClickListener
        })
    }

    private fun addCastBasicButton() {
        //Get reference to the play/pause button.
        basicPlayPauseButton = this.findViewById(R.id.cast_basic_button)
        if ("ott" == BuildConfig.FLAVOR) {
            basicPlayPauseButton!!.visibility = View.INVISIBLE
        }

        //Add clickListener.
        basicPlayPauseButton!!.setOnClickListener(View.OnClickListener {
            loadRemoteMediaBasic(0, true)
            return@OnClickListener
        })
    }

    private fun addCastOttButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById(R.id.cast_ott_button)
        if ("ovp" == BuildConfig.FLAVOR) {
            playPauseButton!!.visibility = View.INVISIBLE
        }
        //Add clickListener.
        playPauseButton!!.setOnClickListener(View.OnClickListener {
            // usually protocol should be https!!!
            loadRemoteMediaOtt(0, true, CAFCastBuilder.HttpProtocol.Http /* CAFCastBuilder.HttpProtocol.Https */)
            return@OnClickListener
        })
    }

    private fun addChangeMediaButton() {
        //Get reference to the play/pause button.
        changeMediaButton = this.findViewById(R.id.change_media_button)
        if (changeMediaButton != null) {
            changeMediaButton!!.setOnClickListener {
                var pendingResult: PendingResult<RemoteMediaClient.MediaChannelResult>? = null
                val loadOptions = MediaLoadOptions.Builder().setAutoplay(true).setPlayPosition(0).build()
                val vastAdTag = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=" + 43543
                if ("ovp" == BuildConfig.FLAVOR) {
                    pendingResult = remoteMediaClient!!.load(getOvpCastMediaInfo("0_b7s02kjl", vastAdTag, CAFCastBuilder.AdTagType.VAST, null), loadOptions)
                    pendingResult!!.setResultCallback { mediaChannelResult ->
                        val customData = mediaChannelResult.customData
                        if (customData != null) {
                            //log.v("loadMediaInfo. customData = " + customData.toString());
                        } else {
                            //log.v("loadMediaInfo. customData == null");
                        }
                    }
                } else {
                    val protocol = CAFCastBuilder.HttpProtocol.Http /* CAFCastBuilder.HttpProtocol.Https */
                    pendingResult = remoteMediaClient!!.load(getOttCastMediaInfo("548571", "Web_Main", "", null, protocol, null), loadOptions)
                    pendingResult!!.setResultCallback { mediaChannelResult ->
                        val customData = mediaChannelResult.customData
                        if (customData != null) {
                            //log.v("loadMediaInfo. customData = " + customData.toString());
                        } else {
                            //log.v("loadMediaInfo. customData == null");
                        }
                    }
                }
            }
        }
    }


    private fun showIntroductoryOverlay() {
        if (mIntroductoryOverlay != null) {
            mIntroductoryOverlay!!.remove()
        }
        if (mediaRouteMenuItem != null && mediaRouteMenuItem!!.isVisible) {
            Handler().post {
                mIntroductoryOverlay = IntroductoryOverlay.Builder(
                        this@MainActivity, mediaRouteMenuItem!!)
                        .setTitleText("Introducing Cast")
                        .setSingleTime()
                        .setOnOverlayDismissedListener { mIntroductoryOverlay = null }
                        .build()
                mIntroductoryOverlay!!.show()
            }
        }
    }

    private fun setupCastListener() {
        mSessionManagerListener = object: SessionManagerListener<CastSession> {

            override fun onSessionEnded(session: CastSession, error: Int) {
                onApplicationDisconnected()
                invalidateOptionsMenu()
            }

            override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
                onApplicationConnected(session)
                mCastSession = session
                invalidateOptionsMenu()
            }

            override fun onSessionResumeFailed(session: CastSession, error: Int) {
                onApplicationDisconnected()
            }

            override fun onSessionStarted(session: CastSession, sessionId: String) {
                onApplicationConnected(session)
                invalidateOptionsMenu()
            }

            override fun onSessionStartFailed(session: CastSession, error: Int) {
                onApplicationDisconnected()
                invalidateOptionsMenu()
            }

            override fun onSessionStarting(session: CastSession) {}

            override fun onSessionEnding(session: CastSession) {}

            override fun onSessionResuming(session: CastSession, sessionId: String) {}

            override fun onSessionSuspended(session: CastSession, reason: Int) {}

            private fun onApplicationConnected(castSession: CastSession) {
                mCastSession = castSession

                if (null != mSelectedMedia) {

                    if (player!!.isPlaying) {
                        player.pause()
                        loadRemoteMediaOvp(player.currentPosition.toInt(), true)
                        finish()
                        return
                    } else {
                        updatePlaybackLocation(PlaybackLocation.REMOTE)
                    }
                }
                supportInvalidateOptionsMenu()
            }

            private fun onApplicationDisconnected() {
                updatePlaybackLocation(PlaybackLocation.LOCAL)
                mLocation = PlaybackLocation.LOCAL
                supportInvalidateOptionsMenu()
            }
        }
    }

    private fun loadRemoteMediaBasic(position: Int, autoPlay: Boolean) {
        if (mCastSession == null) {
            return
        }
        remoteMediaClient = mCastSession!!.remoteMediaClient
        if (remoteMediaClient == null) {
            return
        }
        remoteMediaClient!!.addListener(object: RemoteMediaClient.Listener {
            override fun onStatusUpdated() {
                val intent = Intent(this@MainActivity, ExpandedControlsActivity::class.java)
                startActivity(intent)
                //remoteMediaClient.removeListener(this);
            }

            override fun onMetadataUpdated() {}

            override fun onQueueStatusUpdated() {}

            override fun onPreloadStatusUpdated() {}

            override fun onSendingRemoteMediaRequest() {}

            override fun onAdBreakStatusUpdated() {}
        })

        var pendingResult: PendingResult<RemoteMediaClient.MediaChannelResult>? = null
        val loadOptions = MediaLoadOptions.Builder().setAutoplay(true).setPlayPosition(position.toLong()).build()
        val vastAdTag = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=" + 311552334242


        var playbackParams = PlaybackParams();
        playbackParams.poster = "https://cfvod.kaltura.com/p/2222401/sp/222240100/thumbnail/entry_id/1_f93tepsn/version/100011"
        playbackParams.id = "0_2jiaa9tb"
        playbackParams.duration = 741
        playbackParams.type = "Vod"
        playbackParams.dvr = false
        playbackParams.vr = null
        playbackParams.dvr = false
        playbackParams.progressive = listOf<Progressive>()
        playbackParams.hls = listOf<Hls>()


         //playbackParams.setDashSource("0_2jiaa9tb_973",
         //             "https://cdnapisec.kaltura.com/p/2222401/sp/222240100/playManifest/entryId/1_f93tepsn/protocol/https/format/mpegdash/flavorIds/1_7cgwjy2a,1_xc3jlgr7,1_cn83nztu,1_pgoeohrs/a.mpd",
         //       "https://udrm.kaltura.com/cenc/widevine/license?custom_data=eyJjYV9zeXN0ZW0iOiJPVlAiLCJ1c2VyX3Rva2VuIjoiZGpKOE1qSXlNalF3TVh6Q1B5ZDRyd0tiUlVRQTA2RktoNFNtMWJyWDE2LW01dnh5ODBLa0JEWTN0ME9yUklCajZ4WTc2SWZWRVJFcmItZkNLN01uN1VJV1VWaU92S0JaV0h6V09paGRkdGVlc211b0lLaGQ3d2VhbkE9PSIsImFjY291bnRfaWQiOjIyMjI0MDEsImNvbnRlbnRfaWQiOiIxX2Y5M3RlcHNuIiwiZmlsZXMiOiIxXzdjZ3dqeTJhLDFfeGMzamxncjcsMV9jbjgzbnp0dSwxX3Bnb2VvaHJzIn0%3D&signature=9tenGJdH1qEVhrI1f1fnKbvNBKA%3D")


        playbackParams.setHlsSource("0_2jiaa9tb_971",
                "https://cdnapisec.kaltura.com/p/1734751/sp/173475100/playManifest/entryId/1_3o1seqnv/protocol/https/format/applehttp/flavorIds/1_l7xu37er,1_9p9dlin6,1_h6cxfg0z,1_fdpeg81m/a.m3u8")


        //playbackParams.setProgressivehSource("0_2jiaa9tb_974",
        //        "http://qa-apache-php7.dev.kaltura.com/p/1091/sp/1091/playManifest/entryId/0_rccv43zr/flavorIds/0_qm8yk6bg,0_5artpfqc,0_ee3vdk7h,0_jkit9w6a,0_8qt13atw,0_rgajlpf4/deliveryProfileId/261/protocol/http/format/url/name/a.mp4")

        val metadata : Metadata = Metadata()
        metadata.description = ""
        metadata.name = "Audio Tracks"
        metadata.tags = "";
        playbackParams.metadata = metadata
        playbackParams.captions =  getExternalVttCaptions()

        //pendingResult = remoteMediaClient!!.load(getBasicCastMediaInfo(playbackParams, vastAdTag, CAFCastBuilder.AdTagType.VAST), loadOptions)
        pendingResult = remoteMediaClient!!.load(getBasicCastMediaInfo(playbackParams, "", null), loadOptions)


        pendingResult!!.setResultCallback { mediaChannelResult ->
            val customData = mediaChannelResult.customData
            if (customData != null) {
                //log.v("loadMediaInfo. customData = " + customData.toString());
            } else {
                //log.v("loadMediaInfo. customData == null");
            }
        }
    }

    private fun getExternalVttCaptions(): List<Caption> {
        val caption1 = Caption()
        caption1.isDefault = false
        caption1.type = "srt"
        caption1.label = "Ger"
        caption1.language = "nl"
        caption1.url = "https://qa-nginx-vod.dev.kaltura.com/api_v3/index.php/service/caption_captionasset/action/serveWebVTT/captionAssetId/0_kozg4x1x/version/2/segmentIndex/1.vtt"
        val caption2 = Caption()
        caption2.isDefault = false
        caption2.type = "srt"
        caption2.label = "Rus"
        caption2.language = "ru"
        caption2.url = "https://qa-nginx-vod.dev.kaltura.com/api_v3/index.php/service/caption_captionasset/action/serveWebVTT/captionAssetId/0_njhnv6na/version/2/segmentIndex/1.vtt"
        val caption3 = Caption()
        caption3.isDefault = false
        caption3.type = "srt"
        caption3.label = "Eng"
        caption3.language = "en"
        caption3.url = "https://qa-nginx-vod.dev.kaltura.com/api_v3/index.php/service/caption_captionasset/action/serveWebVTT/captionAssetId/0_kozg4x1x/version/2/segmentIndex/1.vtt"
        var captions: List<Caption> = mutableListOf(caption1, caption2, caption3)
        return captions
    }

    private fun loadRemoteMediaOvp(position: Int, autoPlay: Boolean) {
        if (mCastSession == null) {
            return
        }
        remoteMediaClient = mCastSession!!.remoteMediaClient
        if (remoteMediaClient == null) {
            return
        }
        remoteMediaClient!!.addListener(object: RemoteMediaClient.Listener {
            override fun onStatusUpdated() {
                val intent = Intent(this@MainActivity, ExpandedControlsActivity::class.java)
                startActivity(intent)
                //remoteMediaClient.removeListener(this);
            }

            override fun onMetadataUpdated() {}

            override fun onQueueStatusUpdated() {}

            override fun onPreloadStatusUpdated() {}

            override fun onSendingRemoteMediaRequest() {}

            override fun onAdBreakStatusUpdated() {}
        })

        var pendingResult: PendingResult<RemoteMediaClient.MediaChannelResult>? = null
        val loadOptions = MediaLoadOptions.Builder().setAutoplay(true).setPlayPosition(position.toLong()).build()
        val vastAdTag = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=" + 31122334242
        //using QA partner 1091

        pendingResult = remoteMediaClient!!.load(getOvpCastMediaInfo("0_fl4ioobl", vastAdTag, CAFCastBuilder.AdTagType.VAST, getExternalVttCaptions()), loadOptions)


        pendingResult!!.setResultCallback { mediaChannelResult ->
            val customData = mediaChannelResult.customData
            if (customData != null) {
                //log.v("loadMediaInfo. customData = " + customData.toString());
            } else {
                //log.v("loadMediaInfo. customData == null");
            }
        }
    }

    private fun loadRemoteMediaOtt(position: Int, autoPlay: Boolean, protocol : CAFCastBuilder.HttpProtocol) {
        if (mCastSession == null) {
            return
        }
        remoteMediaClient = mCastSession!!.remoteMediaClient
        if (remoteMediaClient == null) {
            return
        }
        remoteMediaClient!!.addListener(object: RemoteMediaClient.Listener {
            override fun onStatusUpdated() {
                val intent = Intent(this@MainActivity, ExpandedControlsActivity::class.java)
                startActivity(intent)
                //remoteMediaClient.removeListener(this);
            }

            override fun onMetadataUpdated() {}

            override fun onQueueStatusUpdated() {}

            override fun onPreloadStatusUpdated() {}

            override fun onSendingRemoteMediaRequest() {}

            override fun onAdBreakStatusUpdated() {}
        })

        var pendingResult: PendingResult<RemoteMediaClient.MediaChannelResult>? = null
        val loadOptions = MediaLoadOptions.Builder().setAutoplay(true).setPlayPosition(position.toLong()).build()

        pendingResult = remoteMediaClient!!.load(getOttCastMediaInfo("548579","Web_Main", "", null, protocol, getExternalVttCaptions()), loadOptions)
        pendingResult!!.setResultCallback { mediaChannelResult ->
            val customData = mediaChannelResult.customData
            if (customData != null) {
                //log.v("loadMediaInfo. customData = " + customData.toString());
            } else {
                //log.v("loadMediaInfo. customData == null");
            }
        }
    }

    private fun updatePlaybackLocation(location: PlaybackLocation) {
        mLocation = location
    }


    fun createAdsConfigVast(adTagUrl: String): AdsConfig {
        return MediaInfoUtils.createAdsConfigVastInPosition(0, adTagUrl)
    }

    fun createAdsConfigVmap(adTagUrl: String): AdsConfig {
        return MediaInfoUtils.createAdsConfigVmap(adTagUrl)
    }

    private fun getBasicCastMediaInfo(playbackParams: PlaybackParams, adTagUrl: String, adTagType: CAFCastBuilder.AdTagType?): MediaInfo {

        val basicCastBuilder = KalturaBasicCAFCastBuilder(playbackParams)
                .setStreamType(CAFCastBuilder.StreamType.VOD)
        if (!TextUtils.isEmpty(adTagUrl)) {
            if (adTagType == CAFCastBuilder.AdTagType.VAST) {
                basicCastBuilder.setAdsConfig(createAdsConfigVast(adTagUrl))
            } else {
                basicCastBuilder.setAdsConfig(createAdsConfigVmap(adTagUrl))
            }
            basicCastBuilder.setDefaultTextLangaugeCode("en")
        }
        return basicCastBuilder.build()
    }

    private fun getOttCastMediaInfo(mediaId: String, mediaFormat: String?, adTagUrl: String, adTagType: CAFCastBuilder.AdTagType?, protocol: CAFCastBuilder.HttpProtocol, vttExternalCaptions : List<Caption>?) : MediaInfo {

        var formats: MutableList<String>? = null
        if (mediaFormat != null) {
            formats = ArrayList()
            formats.add(mediaFormat)
        }

        val phoenixCastBuilder = KalturaPhoenixCastBuilder()
                .setMediaEntryId(mediaId)
                .setKs("")
                .setFormats(formats)
                .setStreamType(CAFCastBuilder.StreamType.VOD)
                .setAssetReferenceType(CAFCastBuilder.AssetReferenceType.Media)
                .setContextType(CAFCastBuilder.PlaybackContextType.Playback)
                .setMediaType(CAFCastBuilder.KalturaAssetType.Media)
                .setProtocol(protocol)

        if (vttExternalCaptions != null) {
            phoenixCastBuilder.setExternalVttCaptions(vttExternalCaptions)
        }
        if (!TextUtils.isEmpty(adTagUrl)) {
            if (adTagType == CAFCastBuilder.AdTagType.VAST) {
                phoenixCastBuilder.setAdsConfig(createAdsConfigVast(adTagUrl))
            } else {
                phoenixCastBuilder.setAdsConfig(createAdsConfigVmap(adTagUrl))
            }
            //phoenixCastBuilder.setDefaultTextLangaugeCode("en")
        }
        return returnResult(phoenixCastBuilder)
    }


    private fun getOvpCastMediaInfo(entryId: String, adTagUrl: String, adTagType: CAFCastBuilder.AdTagType?, externalVttCaptions: List<Caption>?): MediaInfo {

        val ovpV3CastBuilder = KalturaCastBuilder()
                .setMediaEntryId(entryId)
                .setKs("")
                .setStreamType(CAFCastBuilder.StreamType.VOD)

        if (externalVttCaptions != null) {
            ovpV3CastBuilder.setExternalVttCaptions(externalVttCaptions)
        }

        if (!TextUtils.isEmpty(adTagUrl)) {
            if (adTagType == CAFCastBuilder.AdTagType.VAST) {
                ovpV3CastBuilder.setAdsConfig(createAdsConfigVast(adTagUrl))
            } else {
                ovpV3CastBuilder.setAdsConfig(createAdsConfigVmap(adTagUrl))
            }
        }
        //ovpV3CastBuilder.setDefaultTextLangaugeCode("en")
        return returnResult(ovpV3CastBuilder)
    }

    private fun returnResult(cafCastBuilder: CAFCastBuilder<*>): MediaInfo {
        return cafCastBuilder.build()
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

    override fun onResume() {
        mCastContext!!.addCastStateListener(mCastStateListener!!)
        super.onResume()
    }

    override fun onPause() {
        mCastContext!!.removeCastStateListener(mCastStateListener)
        super.onPause()
    }
}
