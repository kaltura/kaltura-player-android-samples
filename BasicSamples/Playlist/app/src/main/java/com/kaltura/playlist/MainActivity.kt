package com.kaltura.playlist

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.*
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.providers.PlaylistMetadata
import com.kaltura.tvplayer.KalturaBasicPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.PlayerInitOptions
import com.kaltura.tvplayer.playlist.BasicMediaOptions
import com.kaltura.tvplayer.playlist.BasicPlaylistOptions
import com.kaltura.tvplayer.playlist.CountDownOptions
import com.kaltura.tvplayer.playlist.PlaylistEvent
import kotlinx.android.synthetic.main.activity_main.*

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

    private fun loadPlaylistToPlayer(basicMediaOptionsList: List<BasicMediaOptions>) {
        val playerInitOptions = PlayerInitOptions()

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
