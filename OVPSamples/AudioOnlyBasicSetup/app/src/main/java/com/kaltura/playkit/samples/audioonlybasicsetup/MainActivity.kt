package com.kaltura.playkit.samples.audioonlybasicsetup

import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.*
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.player.PKExternalSubtitle
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ima.IMAConfig
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.playkit.providers.ovp.OVPMediaAsset
import com.kaltura.tvplayer.KalturaOvpPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OVPMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity() {

    private val log = PKLog.get("MainActivity")

    private val START_POSITION = 0L // position for start playback in msec.
    private val AD_TAG_URL = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator="
    private val ENTRY_ID = "1_w9zx2eti"
    private var player: KalturaPlayer? = null
    private var playerState: PlayerState? = null

    private val externalSubtitles: List<PKExternalSubtitle>
        get() {

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

            return mList
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadPlaykitPlayer()

        addPlayPauseButton()
    }

    private fun addAdEvents() {
        player?.addListener(this, AdEvent.contentPauseRequested) { showArtworkForAudioContent(View.GONE) }

        player?.addListener(this, AdEvent.contentResumeRequested) { showArtworkForAudioContent(View.VISIBLE) }

        player?.addListener(this, AdEvent.error) { showArtworkForAudioContent(View.VISIBLE) }
    }

    private fun addPlayerStateListener() {
        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private fun addPlayPauseButton() {
        //Add clickListener.
        play_pause_button.setOnClickListener { v ->
            player?.let {
                val adController = it.getController(AdController::class.java)
                if (it.isPlaying || adController != null && adController.isAdDisplayed && adController.isAdPlaying) {
                    if (adController != null && adController.isAdDisplayed) {
                        adController.pause()
                    } else {
                        it.pause()
                    }
                    //If player is playing, change text of the button and pause.
                    play_pause_button.setText(R.string.play_text)
                } else {
                    if (adController != null && adController.isAdDisplayed) {
                        adController.play()
                    } else {
                        it.play()
                    }
                    //If player is not playing, change text of the button and play.
                    play_pause_button.setText(R.string.pause_text)
                }
            }
        }
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

    private fun showArtworkForAudioContent(visibility: Int) {
        artwork_view.visibility = visibility
    }

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        //playerInitOptions.setServerUrl(SERVER_URL);
        playerInitOptions.setAutoPlay(true)


        // Audio Only setup
        playerInitOptions.setIsVideoViewHidden(true)

        // IMA Configuration
        val pkPluginConfigs = PKPluginConfigs()
        val adsConfig = getAdsConfig(AD_TAG_URL)
        pkPluginConfigs.setPluginConfig(IMAPlugin.factory.name, adsConfig)

        playerInitOptions.setPluginConfigs(pkPluginConfigs)
        player = KalturaOvpPlayer.create(this@MainActivity, playerInitOptions)
        addAdEvents()
        subscribeToTracksAvailableEvent()

        showArtworkForAudioContent(View.VISIBLE)

        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        val container = player_root
        container.addView(player?.playerView)

        val ovpMediaOptions = buildOvpMediaOptions()
        player?.loadMedia(ovpMediaOptions) { mediaOptions, entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OVPMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }

        addPlayerStateListener()
    }

    private fun buildOvpMediaOptions(): OVPMediaOptions {
        var ovpMediaAsset = OVPMediaAsset()
        ovpMediaAsset.entryId = ENTRY_ID
        ovpMediaAsset.ks = null
        val ovpMediaOptions = OVPMediaOptions(ovpMediaAsset)
        ovpMediaOptions.startPosition = START_POSITION
        ovpMediaOptions.externalSubtitles = externalSubtitles

        return ovpMediaOptions
    }

    private fun getAdsConfig(adTagUrl: String): IMAConfig {
        val videoMimeTypes = ArrayList<String>()
        videoMimeTypes.add("video/mp4")
        videoMimeTypes.add("application/x-mpegURL")
        //videoMimeTypes.add("application/dash+xml");
        return IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8)
    }

    private fun subscribeToTracksAvailableEvent() {
        player?.addListener(this, PlayerEvent.tracksAvailable) { event ->
            //When the track data available, this event occurs. It brings the info object with it.
            log.d("Event TRACKS_AVAILABLE")

            //Cast event to the TracksAvailable object that is actually holding the necessary data.

            //Obtain the actual tracks info from it. Default track index values are coming from manifest
            val tracks = event.tracksInfo
            val defaultAudioTrackIndex = tracks.getDefaultAudioTrackIndex()
            val defaultTextTrackIndex = tracks.getDefaultTextTrackIndex()
            if (tracks.audioTracks.size > 0) {
                log.d("Default Audio language = " + tracks.audioTracks.get(defaultAudioTrackIndex).label)
            }
            if (tracks.textTracks.size > 0) {
                log.d("Default Text language = " + tracks.textTracks.get(defaultTextTrackIndex).label)
            }
            if (tracks.videoTracks.size > 0) {
                log.d("Default video isAdaptive = " + tracks.videoTracks.get(tracks.defaultAudioTrackIndex).isAdaptive + " bitrate = " + tracks.videoTracks.get(tracks.defaultAudioTrackIndex).bitrate)
            }
        }
    }

    companion object {
        val PARTNER_ID = 2215841
        val SERVER_URL = "https://cdnapisec.kaltura.com"
    }
}
