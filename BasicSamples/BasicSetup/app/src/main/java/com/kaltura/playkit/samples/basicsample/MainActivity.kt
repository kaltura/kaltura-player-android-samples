package com.kaltura.playkit.samples.basicsample

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.kaltura.playkit.PKDrmParams
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaFormat
import com.kaltura.playkit.PKMediaSource
import com.kaltura.tvplayer.KalturaBasicPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.PlayerInitOptions


class MainActivity : AppCompatActivity() {

    private val START_POSITION = 0L // position for start playback in msec.

    private val MEDIA_FORMAT = PKMediaFormat.hls
    private val SOURCE_URL = "https://cdnapisec.kaltura.com/p/2215841/sp/221584100/playManifest/entryId/1_w9zx2eti/protocol/https/format/applehttp/falvorIds/1_1obpcggb,1_yyuvftfz,1_1xdbzoa6,1_k16ccgto,1_djdf6bk8/a.m3u8"
    private val LICENSE_URL: String? = null

    private var player: KalturaPlayer? = null
    private var playPauseButton: Button? = null
    private var isFullScreen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mediaEntry = createMediaEntry()

        loadPlaykitPlayer(mediaEntry)

        addPlayPauseButton()

        showSystemUI()

        findViewById<View>(R.id.activity_main).setOnClickListener { v ->
            if (isFullScreen) {
                showSystemUI()
            } else {
                hideSystemUI()
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

    /**
     * Create [PKMediaEntry] with minimum necessary data.
     *
     * @return - the [PKMediaEntry] object.
     */
    private fun createMediaEntry(): PKMediaEntry {
        //Create media entry.
        val mediaEntry = PKMediaEntry()

        //Set id for the entry.
        mediaEntry.id = "testEntry"

        //Set media entry type. It could be Live,Vod or Unknown.
        //In this sample we use Vod.
        mediaEntry.mediaType = PKMediaEntry.MediaEntryType.Vod

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        val mediaSources = createMediaSources()

        //Set media sources to the entry.
        mediaEntry.sources = mediaSources

        return mediaEntry
    }

    /**
     * Create list of [PKMediaSource].
     *
     * @return - the list of sources.
     */
    private fun createMediaSources(): List<PKMediaSource> {

        //Create new PKMediaSource instance.
        val mediaSource = PKMediaSource()

        //Set the id.
        mediaSource.id = "testSource"

        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.url = SOURCE_URL

        //Set the format of the source. In our case it will be hls in case of mpd/wvm formats you have to to call mediaSource.setDrmData method as well
        mediaSource.mediaFormat = MEDIA_FORMAT

        // Add DRM data if required
        if (LICENSE_URL != null) {
            mediaSource.drmData = listOf(PKDrmParams(LICENSE_URL, PKDrmParams.Scheme.WidevineCENC))
        }

        return listOf(mediaSource)
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private fun addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById(R.id.play_pause_button)
        //Add clickListener.
        playPauseButton!!.setOnClickListener { v ->
            if (player!!.isPlaying) {
                //If player is playing, change text of the button and pause.
                playPauseButton!!.setText(R.string.play_text)
                player!!.pause()
            } else {
                //If player is not playing, change text of the button and play.
                playPauseButton!!.setText(R.string.pause_text)
                player!!.play()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (player != null) {
            if (playPauseButton != null) {
                playPauseButton!!.setText(R.string.pause_text)
            }
            player!!.onApplicationResumed()
            player!!.play()
        }
    }

    override fun onPause() {
        super.onPause()
        if (player != null) {
            player!!.onApplicationPaused()
        }
    }

    fun loadPlaykitPlayer(pkMediaEntry: PKMediaEntry) {
        val playerInitOptions = PlayerInitOptions()

        player = KalturaBasicPlayer.create(this@MainActivity, playerInitOptions)
        player!!.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player!!.playerView)
        player!!.setMedia(pkMediaEntry, START_POSITION)
    }
}
