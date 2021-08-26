package com.kaltura.playlist

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKRequestConfig
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.providers.PlaylistMetadata
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import com.kaltura.tvplayer.playlist.OTTPlaylistOptions
import com.kaltura.tvplayer.playlist.PlaylistEvent
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    companion object {
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        val PARTNER_ID = 3009
    }

    private val log = PKLog.get("MainActivity")
    private val ASSET_ID_1 = "548572"
    private val ASSET_ID_2 = "548571"
    private val ASSET_ID_3 = "548579"

    private val START_POSITION = 0L // position for start playback in msec.
    private var player: KalturaPlayer? = null
    private var isFullScreen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val OTTMediaOptions1 = buildOttMediaOptions(ASSET_ID_1)
        val OTTMediaOptions2 = buildOttMediaOptions(ASSET_ID_2)
        val OTTMediaOptions3 = buildOttMediaOptions(ASSET_ID_3)

        val mediaList = listOf(OTTMediaOptions1, OTTMediaOptions2, OTTMediaOptions3)

        loadKalturaPlaylist(mediaList)

        showSystemUI()

        findViewById<View>(R.id.activity_main).setOnClickListener { v ->
            if (isFullScreen) {
                showSystemUI()
            } else {
                hideSystemUI()
            }
        }

        btn_shuffle.visibility = View.GONE

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

    @SuppressLint("SetTextI18n")
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

    override fun onResume() {
        super.onResume()
        player?.let {
            it.onApplicationResumed()
            it.play()
        }

        playerControls.resume();
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.destroy();
    }

    override fun onPause() {
        super.onPause()
        player?.onApplicationPaused()
        playerControls.release()
    }

    private fun loadKalturaPlaylist(mediaList: List<OTTMediaOptions>) {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setReferrer("app://testing.app.com")
        playerInitOptions.setPKRequestConfig(PKRequestConfig(true))

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)
        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player?.playerView)

        playerControls.setPlayer(player)

        val ottPlaylistIdOptions = OTTPlaylistOptions()
        ottPlaylistIdOptions.playlistMetadata = PlaylistMetadata().setName("TestOTTPlayList").setId("1")
        ottPlaylistIdOptions.ottMediaOptionsList = mediaList

        player?.loadPlaylist(ottPlaylistIdOptions) { _, error ->
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTPlaylist OnPlaylistLoadListener  entry = " +  ottPlaylistIdOptions.playlistMetadata.name)
            }
        }

        addPlayerListeners()
    }

    private fun buildOttMediaOptions(ASSET_ID: String): OTTMediaOptions {
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = ASSET_ID
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaAsset.ks = null
        ottMediaAsset.referrer = "app://MyTestApp";
        ottMediaAsset.formats = listOf("Mobile_Main")
        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = START_POSITION
        return ottMediaOptions
    }
}

