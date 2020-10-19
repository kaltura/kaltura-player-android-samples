package com.kaltura.playlist

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.*
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ima.IMAConfig
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.playkit.providers.PlaylistMetadata
import com.kaltura.tvplayer.KalturaBasicPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.PlayerInitOptions
import com.kaltura.tvplayer.playlist.BasicMediaOptions
import com.kaltura.tvplayer.playlist.BasicPlaylistOptions
import com.kaltura.tvplayer.playlist.CountDownOptions
import com.kaltura.tvplayer.playlist.PlaylistEvent
import kotlinx.android.synthetic.main.activity_main.*
import java.util.ArrayList

class MainActivity : AppCompatActivity() {
    private val log = PKLog.get("MainActivity")
    private val START_POSITION = 0L // position for start playback in msec.

    private val MEDIA_FORMAT = PKMediaFormat.hls
    private val SOURCE0_ENTRY_ID = "0_uka1msg4" // 915014
    private val SOURCE_URL0 = "http://cdnapi.kaltura.com/p/243342/sp/24334200/playManifest/entryId/0_uka1msg4/flavorIds/1_vqhfu6uy,1_80sohj7p/format/applehttp/protocol/http/a.m3u8"
    private val SOURCE1_ENTRY_ID = "0_wu32qrt3" // 915014
    private val SOURCE_URL1 = "http://cdntesting.qa.mkaltura.com/p/1091/sp/109100/playManifest/entryId/0_wu32qrt3/protocol/http/format/applehttp/flavorIds/0_m4f9cdk9,0_mhx8cxa3,0_1t0yf94g,0_av8gbt6s/a.m3u8"
    private val SOURCE2_ENTRY_ID = "0_aulfs5wq" // 915014
    private val SOURCE_URL2 = "http://cdntesting.qa.mkaltura.com/p/1091/sp/109100/playManifest/entryId/0_aulfs5wq/protocol/http/format/applehttp/flavorIds/0_ua2hes50,0_2lp0vd2v,0_p1tcunbj,0_yvkrcuzq/a.m3u8"

    private val LICENSE_URL: String? = null

    private var player: KalturaPlayer? = null
    private var isFullScreen: Boolean = false

    private fun subscribeToAdEvents() {

        player!!.addListener(this, AdEvent.started) { event ->
            //Some events holds additional data objects in them.
            //In order to get access to this object you need first cast event to
            //the object it belongs to. You can learn more about this kind of objects in
            //our documentation.

            //Then you can use the data object itself.
            val adInfo = event.adInfo

            //Print to log content type of this ad.
            log.d("ad event received: " + event.eventType().name
                    + ". Additional info: ad content type is: "
                    + adInfo.getAdContentType())
        }

        player!!.addListener(this, AdEvent.contentResumeRequested) { event -> log.d("ADS_PLAYBACK_ENDED") }

        player!!.addListener(this, AdEvent.adPlaybackInfoUpdated) { event ->
            log.d("AD_PLAYBACK_INFO_UPDATED  = " + event.width + "/" + event.height + "/" + event.bitrate)
        }

        player!!.addListener(this, AdEvent.skippableStateChanged) { event -> log.d("SKIPPABLE_STATE_CHANGED") }

        player!!.addListener(this, AdEvent.adRequested) { event ->
            log.d("AD_REQUESTED adtag = " + event.adTagUrl)
        }

        player!!.addListener(this, AdEvent.playHeadChanged) { event ->
            val adEventProress = event
            //Log.d(TAG, "received AD PLAY_HEAD_CHANGED " + adEventProress.adPlayHead);
        }


        player!!.addListener(this, AdEvent.adBreakStarted) { event -> log.d("AD_BREAK_STARTED") }

        player!!.addListener(this, AdEvent.cuepointsChanged) { event ->
            log.d("AD_CUEPOINTS_UPDATED HasPostroll = " + event.cuePoints.hasPostRoll())
        }

        player!!.addListener(this, AdEvent.loaded) { event ->
            log.d("AD_LOADED " + event.adInfo.getAdIndexInPod() + "/" + event.adInfo.getTotalAdsInPod())
        }

        player!!.addListener(this, AdEvent.started) { event ->
            log.d("AD_STARTED w/h - " + event.adInfo.getAdWidth() + "/" + event.adInfo.getAdHeight())
        }

        player!!.addListener(this, AdEvent.resumed) { event -> log.d("AD_RESUMED") }

        player!!.addListener(this, AdEvent.paused) { event -> log.d("AD_PAUSED") }

        player!!.addListener(this, AdEvent.skipped) { event -> log.d("AD_SKIPPED") }

        player!!.addListener(this, AdEvent.allAdsCompleted) { event -> log.d("AD_ALL_ADS_COMPLETED") }

        player!!.addListener(this, AdEvent.completed) { event -> log.d("AD_COMPLETED") }

        player!!.addListener(this, AdEvent.firstQuartile) { event -> log.d("FIRST_QUARTILE") }

        player!!.addListener(this, AdEvent.midpoint) { event ->
            log.d("MIDPOINT")
            if (player != null) {
                val adController = player!!.getController(AdController::class.java)
                if (adController != null) {
                    if (adController.isAdDisplayed) {
                        log.d("AD CONTROLLER API: " + adController.adCurrentPosition + "/" + adController.adDuration)
                    }
                    //Log.d(TAG, "adController.getCuePoints().getAdCuePoints().size());
                    //Log.d(TAG, adController.getAdInfo().toString());
                    //adController.skip();
                }
            }
        }

        player!!.addListener(this, AdEvent.thirdQuartile) { event -> log.d("THIRD_QUARTILE") }

        player!!.addListener(this, AdEvent.adBreakEnded) { event -> log.d("AD_BREAK_ENDED") }

        player!!.addListener(this, AdEvent.adClickedEvent) { event ->
            log.d("AD_CLICKED url = " + event.clickThruUrl)
        }

        player!!.addListener(this, AdEvent.error) { event ->
            log.d("AD_ERROR : " + event.error.errorType.name)
        }

        player!!.addListener(this, PlayerEvent.error) { event -> log.d("PLAYER ERROR " + event.error.message!!) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val basicMediaOptions0 = createMediaEntry(SOURCE0_ENTRY_ID, SOURCE_URL0)
        val basicMediaOptions1 =  createMediaEntry(SOURCE1_ENTRY_ID, SOURCE_URL1)
        val basicMediaOptions2 = createMediaEntry(SOURCE2_ENTRY_ID, SOURCE_URL2)

        val mediaList = listOf(basicMediaOptions0, basicMediaOptions1, basicMediaOptions2)

        loadPlaylistToPlayer(mediaList)

        showSystemUI()

        activity_main.setOnClickListener { v ->
            if (isFullScreen) {
                showSystemUI()
            } else {
                hideSystemUI()
            }
        }

        btn_shuffle.visibility = View.GONE;
//        btn_shuffle.setOnClickListener {
//            player?.let {
//                it.playlistController.shuffle(!it.playlistController.isShuffleEnabled)
//                btn_shuffle.text = "Shuffle : ${it.playlistController.isShuffleEnabled}"
//            }
//        }

    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
        }
        isFullScreen = true
    }

    private fun showSystemUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        isFullScreen = false
    }

    /**
     * Create [PKMediaEntry] with minimum necessary data.
     *
     * @return - the [PKMediaEntry] object.
     */
    private fun createMediaEntry(id: String, url : String): BasicMediaOptions {
        //Create media entry.
        val mediaEntry = PKMediaEntry()

        //Set id for the entry.
        mediaEntry.id = id

        //Set media entry type. It could be Live,Vod or Unknown.
        //In this sample we use Vod.
        mediaEntry.mediaType = PKMediaEntry.MediaEntryType.Vod

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        val mediaSources = createMediaSources(id, url)

        //Set media sources to the entry.
        mediaEntry.sources = mediaSources

        return BasicMediaOptions(mediaEntry, CountDownOptions(5000, true))
    }

    /**
     * Create list of [PKMediaSource].
     *
     * @return - the list of sources.
     */
    private fun createMediaSources(id: String, url: String) : List<PKMediaSource> {

        //Create new PKMediaSource instance.
        val mediaSource = PKMediaSource()

        //Set the id.
        mediaSource.id = id

        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.url = url

        //Set the format of the source. In our case it will be hls in case of mpd/wvm formats you have to to call mediaSource.setDrmData method as well
        mediaSource.mediaFormat = MEDIA_FORMAT

        // Add DRM data if required
        if (LICENSE_URL != null) {
            mediaSource.drmData = listOf(PKDrmParams(LICENSE_URL, PKDrmParams.Scheme.WidevineCENC))
        }

        return listOf(mediaSource)
    }

    override fun onResume() {
        super.onResume()
        player?.let {
            it.onApplicationResumed()
            it.play()
        }

        playerControls.resume();
    }

    override fun onPause() {
        super.onPause()
        player?.onApplicationPaused()
        playerControls.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.destroy();
    }

    private fun getAdsConfig(adTagUrl: String): IMAConfig {
        val videoMimeTypes = ArrayList<String>()
        videoMimeTypes.add("video/mp4")
        videoMimeTypes.add("application/x-mpegURL")
        videoMimeTypes.add("application/dash+xml")
        return IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8)
    }

    private fun loadPlaylistToPlayer(basicMediaOptionsList: List<BasicMediaOptions>) {
        val playerInitOptions = PlayerInitOptions()
        val pkPluginConfigs = PKPluginConfigs()
        val AD0 = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator="
        val adsConfig = getAdsConfig(AD0)
        pkPluginConfigs.setPluginConfig(IMAPlugin.factory.name, adsConfig)

        playerInitOptions.setPluginConfigs(pkPluginConfigs)
        val basicPlaylistIdOptions = BasicPlaylistOptions()
        basicPlaylistIdOptions.playlistMetadata = PlaylistMetadata().setName("TestOTTPlayList").setId("1")
        basicPlaylistIdOptions.basicMediaOptionsList = basicMediaOptionsList

        player = KalturaBasicPlayer.create(this@MainActivity, playerInitOptions)
        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        val container = player_root
        container.addView(player?.playerView)

        playerControls.setPlayer(player)

        player?.loadPlaylist(basicPlaylistIdOptions) { _, error ->
            if (error != null) {
                Snackbar.make(player_root, error.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("BasicPlaylist OnPlaylistLoadListener  entry = " +  basicPlaylistIdOptions.playlistMetadata.name)
            }
        }

        addPlayerListeners()
    }

    private fun addPlayerListeners() {
        player?.addListener(this, PlaylistEvent.playListLoaded) { event ->
            log.d("PLAYLIST playListLoaded")
            btn_shuffle.visibility = View.INVISIBLE
            //btn_shuffle.text = "Shuffle : ${player?.playlistController?.isShuffleEnabled}"
        }

        player?.addListener(this, PlaylistEvent.playListStarted) { event ->
            log.d("PLAYLIST playListStarted")
        }

//        player?.addListener(this, PlaylistEvent.playlistShuffleStateChanged) { event ->
//            log.d("PLAYLIST playlistShuffleStateChanged ${event.mode}")
//        }

        player?.addListener(this, PlaylistEvent.playlistLoopStateChanged) { event ->
            log.d("PLAYLIST playlistLoopStateChanged ${event.mode}")
        }

        player?.addListener(this, PlaylistEvent.playlistAutoContinueStateChanged) { event ->
            log.d("PLAYLIST playlistLoopStateChanged ${event.mode}")
        }

        player?.addListener(this, PlaylistEvent.playListEnded) { event ->
            log.d("PLAYLIST playListEnded")
        }

        player?.addListener(this, PlaylistEvent.playListError) { event ->
            log.d("PLAYLIST playListError")
            Toast.makeText(this, event.error.message, Toast.LENGTH_SHORT).show()
        }

        player?.addListener(this, PlaylistEvent.playListLoadMediaError) { event ->
            log.d("PLAYLIST PlaylistLoadMediaError")
            Toast.makeText(this, event.error.message, Toast.LENGTH_SHORT).show()
        }

        player?.addListener(this, PlaylistEvent.playlistCountDownStart) { event ->
            log.d("playlistCountDownStart currentPlayingIndex = " + event.currentPlayingIndex + " durationMS = " + event.playlistCountDownOptions?.durationMS);
        }

        player?.addListener(this, PlaylistEvent.playlistCountDownEnd) { event ->
            log.d("playlistCountDownEnd currentPlayingIndex = " + event.currentPlayingIndex + " durationMS = " + event.playlistCountDownOptions?.durationMS);
        }

        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from ${event.oldState} to ${event.newState}")
            playerControls.setPlayerState(event.newState)
        }

        player?.addListener(this, AdEvent.contentResumeRequested) { event ->
            log.d("CONTENT_RESUME_REQUESTED")
            playerControls.setPlayerState(PlayerState.READY)
        }
    }
}
