package com.kaltura.playkit.samples.audioonlybasicsetup

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.kaltura.playkit.*
import com.kaltura.playkit.player.PKExternalSubtitle
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ima.IMAConfig
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.tvplayer.KalturaBasicPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.PlayerInitOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val log = PKLog.get("MainActivity")

    private val START_POSITION = 0L // position tp start playback in msec.

    private val MEDIA_FORMAT = PKMediaFormat.mp3

    private val SOURCE_URL = "http://www.largesound.com/ashborytour/sound/brobob.mp3"
    private val AD_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator="
    private val AD_TAG_URL_PRE = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator="

    private val LICENSE_URL: String? = null
    private var player: KalturaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val mediaEntry = createMediaEntry()

        loadPlaykitPlayer(mediaEntry)

        addPlayPauseButton()
    }

    private fun addAdEvents() {
        player?.addListener(this, AdEvent.contentPauseRequested) { event -> showArtworkForAudioContent(View.GONE) }

        player?.addListener(this, AdEvent.contentResumeRequested) { event -> showArtworkForAudioContent(View.VISIBLE) }

        player?.addListener(this, AdEvent.error) { event -> showArtworkForAudioContent(View.VISIBLE) }
    }

    private fun setExternalSubtitles(mediaEntry: PKMediaEntry) {

        val mList = ArrayList<PKExternalSubtitle>()

        val pkExternalSubtitle = PKExternalSubtitle()
                .setUrl("http://brenopolanski.com/html5-video-webvtt-example/MIB2-subtitles-pt-BR.vtt")
                .setMimeType(PKSubtitleFormat.vtt)
                .setLabel("External_Deutsch")
                .setLanguage("deu")
        mList.add(pkExternalSubtitle)

        val pkExternalSubtitleDe = PKExternalSubtitle()
                .setUrl("https://mkvtoolnix.download/samples/vsshort-en.srt")
                .setMimeType(PKSubtitleFormat.srt)
                .setLabel("External_English")
                .setLanguage("eng")
                .setDefault()
        mList.add(pkExternalSubtitleDe)

        mediaEntry.externalSubtitleList = mList
    }

    /**
     * Create [PKMediaEntry] with minimum necessary data.
     *
     * @return - the [PKMediaEntry] object.
     */
    private fun createMediaEntry(): PKMediaEntry {
        //Create media entry.
        val mediaEntry = PKMediaEntry()

        // Set External Subtitle
        setExternalSubtitles(mediaEntry)

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
        //Add clickListener.
        play_pause_button.setOnClickListener { _ ->

            player?.let {
                if (it.isPlaying) {
                    //If player is playing, change text of the button and pause.
                    play_pause_button.setText(R.string.play_text)
                    it.pause()
                } else {
                    //If player is not playing, change text of the button and play.
                    play_pause_button?.setText(R.string.pause_text)
                    it.play()
                }
            }
        }
    }

    private fun showArtworkForAudioContent(visibility: Int) {
        artwork_view.visibility = visibility
    }

    override fun onResume() {
        super.onResume()
        player?.let {
            play_pause_button.setText(R.string.pause_text)
            it.onApplicationResumed()
            it.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.destroy();
    }

    override fun onPause() {
        super.onPause()
        player?.onApplicationPaused()
    }

    fun loadPlaykitPlayer(pkMediaEntry: PKMediaEntry) {
        val playerInitOptions = PlayerInitOptions()
        playerInitOptions.setAutoPlay(true)

        // Audio Only setup
        playerInitOptions.setIsVideoViewHidden(true)

        // IMA Configuration
        val pkPluginConfigs = PKPluginConfigs()
        val adsConfig = getAdsConfig(AD_TAG_URL_PRE)
        pkPluginConfigs.setPluginConfig(IMAPlugin.factory.name, adsConfig)

        playerInitOptions.setPluginConfigs(pkPluginConfigs)

        player = KalturaBasicPlayer.create(this@MainActivity, playerInitOptions)
        addAdEvents()
        subscribeToTracksAvailableEvent()

        showArtworkForAudioContent(View.VISIBLE)

        player?.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT)
        val container = player_root
        container.addView(player?.playerView)
        player?.setMedia(pkMediaEntry, START_POSITION)
    }

    private fun getAdsConfig(adTagUrl: String): IMAConfig {
        val videoMimeTypes = ArrayList<String>()
        videoMimeTypes.add("video/mp4")
        videoMimeTypes.add("application/x-mpegURL")
        videoMimeTypes.add("application/dash+xml")
        return IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8)
    }

    private fun subscribeToTracksAvailableEvent() {
        player?.addListener(this, PlayerEvent.tracksAvailable) { event ->
            //When the track data available, this event occurs. It brings the info object with it.
            log.d("Event TRACKS_AVAILABLE")

            //Cast event to the TracksAvailable object that is actually holding the necessary data.
            val tracksAvailable = event as PlayerEvent.TracksAvailable

            //Obtain the actual tracks info from it. Default track index values are coming from manifest
            val tracks = tracksAvailable.tracksInfo
            val defaultAudioTrackIndex = tracks.defaultAudioTrackIndex
            val defaultTextTrackIndex = tracks.defaultTextTrackIndex
            if (tracks.audioTracks.size > 0) {
                log.d("Default Audio langae = " + tracks.audioTracks[defaultAudioTrackIndex].label)
            }
            if (tracks.textTracks.size > 0) {
                log.d("Default Text langae = " + tracks.textTracks[defaultTextTrackIndex].label)
                if (tracks.textTracks.size > 2) {
                    player?.changeTrack(tracks.textTracks[2].uniqueId)
                }
            }
            if (tracks.videoTracks.size > 0) {
                log.d("Default video isAdaptive = " + tracks.videoTracks[tracks.defaultAudioTrackIndex].isAdaptive + " bitrate = " + tracks.videoTracks[tracks.defaultAudioTrackIndex].bitrate)
            }
        }
    }
}
