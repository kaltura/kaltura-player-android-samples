package com.kaltura.playkit.samples.changemedia

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.kaltura.playkit.*
import com.kaltura.playkit.plugins.ima.IMAConfig
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.tvplayer.KalturaBasicPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.PlayerInitOptions
import com.kaltura.tvplayer.config.MediaEntryCacheConfig
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {

    //The url of the first source to play
    private val FIRST_SOURCE_URL = "https://cdnapisec.kaltura.com/p/2215841/sp/221584100/playManifest/entryId/1_w9zx2eti/protocol/https/format/applehttp/falvorIds/1_1obpcggb,1_yyuvftfz,1_1xdbzoa6,1_k16ccgto,1_djdf6bk8/a.m3u8"
    //The url of the second source to play
    private val SECOND_SOURCE_URL = "http://cdnapi.kaltura.com/p/243342/sp/24334200/playManifest/entryId/0_uka1msg4/flavorIds/1_vqhfu6uy,1_80sohj7p/format/applehttp/protocol/http/a.m3u8"
    //id of the first entry
    private val FIRST_ENTRY_ID = "entry_id_1"
    //id of the second entry
    private val SECOND_ENTRY_ID = "entry_id_2"
    //id of the first media source.
    private val FIRST_MEDIA_SOURCE_ID = "source_id_1"
    //id of the second media source.
    private val SECOND_MEDIA_SOURCE_ID = "source_id_2"

    private var player: KalturaPlayer? = null
    private var isFullScreen: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Add simple play/pause button.
        addPlayPauseButton()

        //Init change media button which will switch between entries.
        initChangeMediaButton()

        loadPlaykitPlayer()

        //Prepare the first entry.
        prepareFirstEntry()

        showSystemUI()

        activity_main.setOnClickListener { v ->
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
     * Initialize the changeMedia button. On click it will change media.
     */
    private fun initChangeMediaButton() {
        //Set click listener.
        change_media_button.setOnClickListener { v ->
            //Change media.
            changeMedia()
        }
    }

    /**
     * Will switch between entries. If the first entry is currently active it will
     * prepare the second one. Otherwise it will prepare the first one.
     */
    private fun changeMedia() {

        //Check if id of the media entry that is set in mediaConfig.
        if (player?.mediaEntry?.id == FIRST_ENTRY_ID) {
            //If first one is active, prepare second one.
            prepareSecondEntry()
        } else {
            //If the second one is active, prepare the first one.
            prepareFirstEntry()
        }

        //Just reset the playPauseButton text to "Play".
        resetPlayPauseButtonToPauseText()
    }


    /**
     * Prepare the first entry.
     */
    private fun prepareFirstEntry() {
        //First. Create PKMediaEntry object.
        val mediaEntry = createFirstMediaEntry()

        //Prepare player with media configuration.
        player?.setMedia(mediaEntry, 0L)
    }

    /**
     * Prepare the second entry.
     */
    private fun prepareSecondEntry() {
        //Second. Create PKMediaEntry object.
        val mediaEntry = createSecondMediaEntry()

        //Prepare player with media configuration.
        player?.setMedia(mediaEntry, 0L)
    }

    /**
     * Create [PKMediaEntry] with minimum necessary data.
     *
     * @return - the [PKMediaEntry] object.
     */
    private fun createFirstMediaEntry(): PKMediaEntry {
        //Create media entry.
        val mediaEntry = PKMediaEntry()

        //Set id for the entry.
        mediaEntry.id = FIRST_ENTRY_ID

        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
        mediaEntry.mediaType = PKMediaEntry.MediaEntryType.Unknown

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        val mediaSources = createFirstMediaSources()

        //Set media sources to the entry.
        mediaEntry.sources = mediaSources

        return mediaEntry
    }

    /**
     * Create [PKMediaEntry] with minimum necessary data.
     *
     * @return - the [PKMediaEntry] object.
     */
    private fun createSecondMediaEntry(): PKMediaEntry {
        //Create media entry.
        val mediaEntry = PKMediaEntry()

        //Set id for the entry.
        mediaEntry.id = SECOND_ENTRY_ID

        //Set media entry type. It could be Live,Vod or Unknown.
        //For now we will use Unknown.
        mediaEntry.mediaType = PKMediaEntry.MediaEntryType.Unknown

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        val mediaSources = createSecondMediaSources()

        //Set media sources to the entry.
        mediaEntry.sources = mediaSources

        return mediaEntry
    }

    /**
     * Create list of [PKMediaSource].
     *
     * @return - the list of sources.
     */
    private fun createFirstMediaSources(): List<PKMediaSource> {
        //Init list which will hold the PKMediaSources.
        val mediaSources = ArrayList<PKMediaSource>()

        //Create new PKMediaSource instance.
        val mediaSource = PKMediaSource()

        //Set the id.
        mediaSource.id = FIRST_MEDIA_SOURCE_ID

        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.url = FIRST_SOURCE_URL

        //Set the format of the source. In our case it will be hls.
        mediaSource.mediaFormat = PKMediaFormat.hls

        //Add media source to the list.
        mediaSources.add(mediaSource)

        return mediaSources
    }

    /**
     * Create list of [PKMediaSource].
     *
     * @return - the list of sources.
     */
    private fun createSecondMediaSources(): List<PKMediaSource> {
        //Init list which will hold the PKMediaSources.
        val mediaSources = ArrayList<PKMediaSource>()

        //Create new PKMediaSource instance.
        val mediaSource = PKMediaSource()

        //Set the id.
        mediaSource.id = SECOND_MEDIA_SOURCE_ID

        //Set the content url. In our case it will be link to m3u8 source(.m3u8).
        mediaSource.url = SECOND_SOURCE_URL

        //Set the format of the source. In our case it will be hls.
        mediaSource.mediaFormat = PKMediaFormat.hls

        //Add media source to the list.
        mediaSources.add(mediaSource)

        return mediaSources
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private fun addPlayPauseButton() {
        //Add clickListener.
        play_pause_button.setOnClickListener {
            player?.let {
                if (it.isPlaying) {
                    //If player is playing, change text of the button and pause.
                    resetPlayPauseButtonToPlayText()
                    it.pause()
                } else {
                    resetPlayPauseButtonToPauseText()
                    it.play()
                }
            }
        }
    }

    private fun createIMAPlugin(adtag: String): PKPluginConfigs {

        //Initialize plugin configuration object.
        val pluginConfigs = PKPluginConfigs()

        //Initialize imaConfigs object.
        val imaConfigs = IMAConfig()
        imaConfigs.setAdTagUrl(adtag).enableDebugMode(true).adLoadTimeOut = 8

        //Set jsonObject to the main pluginConfigs object.
        pluginConfigs.setPluginConfig(IMAPlugin.factory.name, imaConfigs)

        //Return created PluginConfigs object.
        return pluginConfigs
    }

    /**
     * Just reset the play/pause button text to "Play".
     */
    private fun resetPlayPauseButtonToPlayText() {
        play_pause_button.setText(R.string.play_text)
    }

    private fun resetPlayPauseButtonToPauseText() {
        play_pause_button.setText(R.string.pause_text)
    }

    override fun onResume() {
        super.onResume()
        player?.let {
            resetPlayPauseButtonToPauseText()
            it.onApplicationResumed()
            it.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.destroy()
    }

    override fun onPause() {
        super.onPause()
        player?.onApplicationPaused()
    }

    fun loadPlaykitPlayer() {
        val playerInitOptions = PlayerInitOptions()
        playerInitOptions.setPKRequestConfig(PKRequestConfig(true))
        playerInitOptions.mediaEntryCacheConfig = MediaEntryCacheConfig(true, 10, 60000)
        playerInitOptions.setAutoPlay(true)

        player = KalturaBasicPlayer.create(this@MainActivity, playerInitOptions)
        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        val container = player_root
        container.addView(player?.playerView)
    }
}