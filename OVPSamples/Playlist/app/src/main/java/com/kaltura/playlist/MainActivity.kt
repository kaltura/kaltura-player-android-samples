package com.kaltura.playlist

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.providers.PlaylistMetadata
import com.kaltura.tvplayer.KalturaOvpPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OVPMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import com.kaltura.tvplayer.playlist.CountDownOptions
import com.kaltura.tvplayer.playlist.OVPPlaylistIdOptions
import com.kaltura.tvplayer.playlist.OVPPlaylistOptions
import com.kaltura.tvplayer.playlist.PlaylistEvent
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        val PARTNER_ID = 2068231
        val SERVER_URL = "https://cdnapisec.kaltura.com"
    }

    private val log = PKLog.get("MainActivity")
    private val START_POSITION = 0L // position for start playback in msec.
    private var player: KalturaPlayer? = null

    private val ENTRY_ID_1 = "1_jnlsx5nv"
    private val ENTRY_ID_2 = "0_w0hpzdni"
    private val ENTRY_ID_3 = "1_j9v8qs8h"
    private var isFullScreen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val ovpPlaylistOptions1 = buildOvpMediaOptions(ENTRY_ID_1)
        val ovpPlaylistOptions2 = buildOvpMediaOptions(ENTRY_ID_2)
        val ovpPlaylistOptions3 = buildOvpMediaOptions(ENTRY_ID_3)

        val mediaList = listOf(ovpPlaylistOptions1, ovpPlaylistOptions2, ovpPlaylistOptions3)

        loadKalturaPlaylist(mediaList)

        showSystemUI()

        findViewById<View>(R.id.activity_main).setOnClickListener { v ->
            if (isFullScreen) {
                showSystemUI()
            } else {
                hideSystemUI()
            }
        }

        btn_shuffle.text = "Shuffle : ${player?.playlistController?.isShuffleEnabled}"

        btn_shuffle.setOnClickListener {
            player?.let {
                it.playlistController.shuffle(!it.playlistController.isShuffleEnabled)
                btn_shuffle.text = "Shuffle : ${it.playlistController.isShuffleEnabled}"
            }
        }
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

    private fun addPlayerStateListener() {

        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from ${event.oldState} to ${event.newState}")
            playerControls.setPlayerState(event.newState)
        }

        player?.addListener(this, AdEvent.contentResumeRequested) { event ->
            log.d("CONTENT_RESUME_REQUESTED")
            playerControls.setPlayerState(PlayerState.READY)
        }

        player?.addListener(this, PlaylistEvent.playlistCountDownStart) { event ->
            log.d("playlistCountDownStart currentPlayingIndex = ${event.currentPlayingIndex} durationMS = ${event.countDownOptions.durationMS}");
        }

        player?.addListener(this, PlaylistEvent.playlistCountDownEnd) { event ->
            log.d("playlistCountDownEnd currentPlayingIndex = ${event.currentPlayingIndex} durationMS = ${event.countDownOptions.durationMS}");
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

    override fun onPause() {
        super.onPause()
        player?.onApplicationPaused()
        playerControls.release()
    }

    /**
     * To load a playlist by entryId list, use [OVPMediaOptions] & [OVPPlaylistOptions] when calling loadPlaylist method.
     */

    fun loadKalturaPlaylist(ovpPlaylistIdOptions: List<OVPMediaOptions>) {
        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setReferrer("app://testing.app.com")
        player = KalturaOvpPlayer.create(this@MainActivity, playerInitOptions)

        player?.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player?.playerView)

        val ovpPlaylistOptions = OVPPlaylistOptions()
        ovpPlaylistOptions.playlistMetadata = PlaylistMetadata().setName("TestOVPPlayList").setId("2")
        ovpPlaylistOptions.ovpMediaOptionsList = ovpPlaylistIdOptions
        ovpPlaylistOptions.countDownOptions = CountDownOptions()

        player?.loadPlaylist(ovpPlaylistOptions) { _, error ->
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OVPPlaylist OnPlaylistLoadListener  entry = " +  ovpPlaylistOptions.playlistMetadata.name)
            }
        }

        addPlayerStateListener()
    }

    /**
     * To load a playlist by ID, use [OVPPlaylistIdOptions] when calling loadPlaylistById method.
     */
    private fun loadKalturaPlaylistById() {
        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setReferrer("app://testing.app.com")
        player = KalturaOvpPlayer.create(this@MainActivity, playerInitOptions)

        player?.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT)
        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player?.playerView)

        val ovpPlaylistIdOptions = OVPPlaylistIdOptions()
        ovpPlaylistIdOptions.playlistId = "0_w0hpzdni"
        ovpPlaylistIdOptions.loopEnabled = true
        ovpPlaylistIdOptions.shuffleEnabled = false

        player?.loadPlaylistById(ovpPlaylistIdOptions) { _, error ->
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OVPPlaylist OnPlaylistLoadListener  entry = " +  ovpPlaylistIdOptions.playlistId)
            }
        }

        addPlayerStateListener()
    }

    private fun buildOvpMediaOptions(ENTRY_ID: String): OVPMediaOptions {
        val ovpMediaOptions = OVPMediaOptions()
        ovpMediaOptions.entryId = ENTRY_ID
        ovpMediaOptions.ks = null
        ovpMediaOptions.startPosition = START_POSITION
        ovpMediaOptions.countDownOptions = CountDownOptions();

        return ovpMediaOptions
    }
}
