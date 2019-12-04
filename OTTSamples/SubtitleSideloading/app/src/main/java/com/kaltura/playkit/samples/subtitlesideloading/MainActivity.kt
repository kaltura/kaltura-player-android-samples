package com.kaltura.playkit.samples.subtitlesideloading

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView

import androidx.appcompat.app.AppCompatActivity

import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PKSubtitleFormat
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.player.AudioTrack
import com.kaltura.playkit.player.PKExternalSubtitle
import com.kaltura.playkit.player.PKTracks
import com.kaltura.playkit.player.SubtitleStyleSettings
import com.kaltura.playkit.player.TextTrack
import com.kaltura.playkit.player.VideoTrack
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.playkit.samples.subtitlesideloading.tracks.TrackItem
import com.kaltura.playkit.samples.subtitlesideloading.tracks.TrackItemAdapter
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions

import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val TAG = "MainActivity"
    private val ASSET_ID = "548576"
    private val START_POSITION = 0L // position for start playback in msec.
    private var player: KalturaPlayer? = null
    private var playPauseButton: Button? = null

    //Android Spinner view, that will actually hold and manipulate tracks selection.
    private var videoSpinner: Spinner? = null
    private var audioSpinner: Spinner? = null
    private var textSpinner: Spinner? = null
    private var ccStyleSpinner: Spinner? = null
    private var tvSpinnerTitle: TextView? = null
    private var userIsInteracting: Boolean = false
    private var isFullScreen: Boolean = false
    private var tracksSelectionMenu: View? = null
    private var playerState: PlayerState? = null

    /**
     * Get the external subtitles list
     */

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

    private val defaultPositionDefault: SubtitleStyleSettings
        get() = SubtitleStyleSettings("DefaultStyle")

    private val styleForPositionOne: SubtitleStyleSettings
        get() = SubtitleStyleSettings("KidsStyle")
                .setBackgroundColor(Color.BLUE)
                .setTextColor(Color.WHITE)
                .setTextSizeFraction(SubtitleStyleSettings.SubtitleTextSizeFraction.SUBTITLE_FRACTION_50)
                .setWindowColor(Color.YELLOW)
                .setEdgeColor(Color.BLUE)
                .setTypeface(SubtitleStyleSettings.SubtitleStyleTypeface.MONOSPACE)
                .setEdgeType(SubtitleStyleSettings.SubtitleStyleEdgeType.EDGE_TYPE_DROP_SHADOW)

    private val styleForPositionTwo: SubtitleStyleSettings
        get() = SubtitleStyleSettings("AdultsStyle")
                .setBackgroundColor(Color.WHITE)
                .setTextColor(Color.BLUE)
                .setTextSizeFraction(SubtitleStyleSettings.SubtitleTextSizeFraction.SUBTITLE_FRACTION_100)
                .setWindowColor(Color.BLUE)
                .setEdgeColor(Color.BLUE)
                .setTypeface(SubtitleStyleSettings.SubtitleStyleTypeface.SANS_SERIF)
                .setEdgeType(SubtitleStyleSettings.SubtitleStyleEdgeType.EDGE_TYPE_DROP_SHADOW)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadPlaykitPlayer()

        findViewById<View>(R.id.activity_main).setOnClickListener { v ->
            if (isFullScreen) {
                tracksSelectionMenu!!.animate().translationY(0f)
                isFullScreen = false
            } else {
                tracksSelectionMenu!!.animate().translationY(-200f)
                isFullScreen = true
            }
        }

    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private fun addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById(R.id.play_pause_button)
        //Add clickListener.
        playPauseButton!!.setOnClickListener { v ->
            if (player != null) {
                val adController = player!!.getController(AdController::class.java)
                if (player!!.isPlaying || adController != null && adController.isAdDisplayed && adController.isAdPlaying) {
                    if (adController != null && adController.isAdDisplayed) {
                        adController.pause()
                    } else {
                        player!!.pause()
                    }
                    //If player is playing, change text of the button and pause.
                    playPauseButton!!.setText(R.string.play_text)
                } else {
                    if (adController != null && adController.isAdDisplayed) {
                        adController.play()
                    } else {
                        player!!.play()
                    }
                    //If player is not playing, change text of the button and play.
                    playPauseButton!!.setText(R.string.pause_text)

                }
            }
        }
    }

    /**
     * Here we are getting access to the Android Spinner views,
     * and set OnItemSelectedListener.
     */
    private fun initializeTrackSpinners() {
        tracksSelectionMenu = this.findViewById(R.id.tracks_selection_menu)
        videoSpinner = this.findViewById(R.id.videoSpinner)
        audioSpinner = this.findViewById(R.id.audioSpinner)
        textSpinner = this.findViewById(R.id.textSpinner)
        ccStyleSpinner = this.findViewById(R.id.ccStyleSpinner)
        tvSpinnerTitle = this.findViewById(R.id.tvSpinnerTitle)
        tvSpinnerTitle!!.visibility = View.INVISIBLE
        ccStyleSpinner!!.visibility = View.INVISIBLE

        textSpinner!!.onItemSelectedListener = this
        audioSpinner!!.onItemSelectedListener = this
        videoSpinner!!.onItemSelectedListener = this

        val stylesStrings = ArrayList<String>()
        stylesStrings.add(defaultPositionDefault.styleName)
        stylesStrings.add(styleForPositionOne.styleName)
        stylesStrings.add(styleForPositionTwo.styleName)
        val ccStyleAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, stylesStrings)
        ccStyleSpinner!!.adapter = ccStyleAdapter
        ccStyleSpinner!!.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                if (!userIsInteracting) {
                    return
                }

                if (position == 0) {
                    player!!.updateSubtitleStyle(defaultPositionDefault)
                } else if (position == 1) {
                    player!!.updateSubtitleStyle(styleForPositionOne)
                } else {
                    player!!.updateSubtitleStyle(styleForPositionTwo)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }
    }

    /**
     * Subscribe to the TRACKS_AVAILABLE event. This event will be sent
     * every time new source have been loaded and it tracks data is obtained
     * by the player.
     */
    private fun subscribeToTracksAvailableEvent() {
        player!!.addListener<PlayerEvent.TracksAvailable>(this, PlayerEvent.tracksAvailable) { event ->
            //When the track data available, this event occurs. It brings the info object with it.
            Log.d(TAG, "Event TRACKS_AVAILABLE")

            //Obtain the actual tracks info from it. Default track index values are coming from manifest
            val tracks = event.tracksInfo
            val defaultAudioTrackIndex = tracks.getDefaultAudioTrackIndex()
            val defaultTextTrackIndex = tracks.getDefaultTextTrackIndex()
            if (tracks.audioTracks.size > 0) {
                Log.d(TAG, "Default Audio language = " + tracks.audioTracks[defaultAudioTrackIndex].label)
            }
            if (tracks.textTracks.size > 0) {
                Log.d(TAG, "Default Text language = " + tracks.textTracks[defaultTextTrackIndex].label)
                if (tvSpinnerTitle != null && ccStyleSpinner != null) {
                    tvSpinnerTitle!!.visibility = View.VISIBLE
                    ccStyleSpinner!!.visibility = View.VISIBLE
                }
            }
            if (tracks.getVideoTracks().size > 0) {
                Log.d(TAG, "Default video isAdaptive = " + tracks.videoTracks[tracks.defaultAudioTrackIndex].isAdaptive + " bitrate = " + tracks.videoTracks[tracks.defaultAudioTrackIndex].bitrate)
            }
            //player.changeTrack(tracksAvailable.tracksInfo.getVideoTracks().get(1).getUniqueId());
            //Populate Android spinner views with received data.
            populateSpinnersWithTrackInfo(tracks)
        }

        player!!.addListener<PlayerEvent.VideoTrackChanged>(this, PlayerEvent.videoTrackChanged) { event -> Log.d(TAG, "Event VideoTrackChanged " + event.newTrack.getBitrate()) }

        player!!.addListener<PlayerEvent.AudioTrackChanged>(this, PlayerEvent.audioTrackChanged) { event -> Log.d(TAG, "Event AudioTrackChanged " + event.newTrack.getLanguage()!!) }

        player!!.addListener<PlayerEvent.TextTrackChanged>(this, PlayerEvent.textTrackChanged) { event -> Log.d(TAG, "Event TextTrackChanged " + event.newTrack.getLanguage()!!) }

        player!!.addListener<PlayerEvent.SubtitlesStyleChanged>(this, PlayerEvent.subtitlesStyleChanged) { event -> Log.d(TAG, "Event SubtitlesStyleChanged " + event.styleName) }

        player!!.addListener<PlayerEvent.StateChanged>(this, PlayerEvent.stateChanged) { event ->
            Log.d(TAG, "State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    /**
     * Populate Android Spinners with retrieved tracks data.
     * Here we are building custom [TrackItem] objects, with which
     * we will populate our custom spinner adapter.
     * @param tracks - [PKTracks] object with all tracks data in it.
     */
    private fun populateSpinnersWithTrackInfo(tracks: PKTracks) {

        //Build track items that are based on videoTrack data.
        val videoTrackItems = buildVideoTrackItems(tracks.videoTracks)
        //populate spinner with this info.
        applyAdapterOnSpinner(videoSpinner!!, videoTrackItems, tracks.defaultVideoTrackIndex)

        //Build track items that are based on audioTrack data.
        val audioTrackItems = buildAudioTrackItems(tracks.audioTracks)
        //populate spinner with this info.
        applyAdapterOnSpinner(audioSpinner!!, audioTrackItems, tracks.defaultAudioTrackIndex)

        //Build track items that are based on textTrack data.
        val textTrackItems = buildTextTrackItems(tracks.textTracks)
        //populate spinner with this info.
        applyAdapterOnSpinner(textSpinner!!, textTrackItems, tracks.defaultTextTrackIndex)
    }

    /**
     * Will build array of [TrackItem] objects.
     * Each [TrackItem] object will hold the readable name.
     * In this case the width and height of the video track.
     * If [VideoTrack] is adaptive, we will name it "Auto".
     * We use this name to represent the track selection options.
     * Also each [TrackItem] will hold the unique id of the track,
     * which should be passed to the player in order to switch to the desired track.
     * @param videoTracks - the list of available video tracks.
     * @return - array with custom [TrackItem] objects.
     */
    private fun buildVideoTrackItems(videoTracks: List<VideoTrack>): Array<TrackItem?> {
        //Initialize TrackItem array with size of videoTracks list.
        val trackItems = arrayOfNulls<TrackItem>(videoTracks.size)

        //Iterate through all available video tracks.
        for (i in videoTracks.indices) {
            //Get video track from index i.
            val videoTrackInfo = videoTracks[i]

            //Check if video track is adaptive. If so, give it "Auto" name.
            if (videoTrackInfo.isAdaptive) {
                //In this case, if this track is selected, the player will
                //adapt the playback bitrate automatically, based on user bandwidth and device capabilities.
                //Initialize TrackItem.
                trackItems[i] = TrackItem("Auto", videoTrackInfo.uniqueId)
            } else {

                //If it is not adaptive track, build readable name based on width and height of the track.
                val nameStringBuilder = StringBuilder()
                nameStringBuilder.append(videoTrackInfo.bitrate)

                //Initialize TrackItem.
                trackItems[i] = TrackItem(nameStringBuilder.toString(), videoTrackInfo.uniqueId)
            }
        }
        return trackItems
    }

    /**
     * Will build array of [TrackItem] objects.
     * Each [TrackItem] object will hold the readable name.
     * In this case the label of the audio track.
     * If [AudioTrack] is adaptive, we will name it "Auto".
     * We use this name to represent the track selection options.
     * Also each [TrackItem] will hold the unique id of the track,
     * which should be passed to the player in order to switch to the desired track.
     * @param audioTracks - the list of available audio tracks.
     * @return - array with custom [TrackItem] objects.
     */
    private fun buildAudioTrackItems(audioTracks: List<AudioTrack>): Array<TrackItem?> {
        //Initialize TrackItem array with size of audioTracks list.
        val trackItems = arrayOfNulls<TrackItem>(audioTracks.size)

        val channelMap = HashMap<Int, AtomicInteger>()
        for (i in audioTracks.indices) {
            if (channelMap.containsKey(audioTracks[i].channelCount)) {
                channelMap[audioTracks[i].channelCount]!!.incrementAndGet()
            } else {
                channelMap[audioTracks[i].channelCount] = AtomicInteger(1)
            }
        }
        var addChannel = false

        if (channelMap.keys.size > 0 && AtomicInteger(audioTracks.size).toString() != channelMap[audioTracks[0].channelCount]!!.toString()) {
            addChannel = true
        }


        //Iterate through all available audio tracks.
        for (i in audioTracks.indices) {
            val audioTrackInfo = audioTracks[i]
            var label: String? = ""
            if (audioTrackInfo != null) {
                label = if (audioTrackInfo.label != null) audioTrackInfo.label else if (audioTrackInfo.language != null) audioTrackInfo.language else ""
            }
            var bitrate = if (audioTrackInfo.bitrate > 0) "" + audioTrackInfo.bitrate else ""
            if (TextUtils.isEmpty(bitrate) && addChannel) {
                bitrate = buildAudioChannelString(audioTrackInfo.channelCount)
            }
            if (audioTrackInfo.isAdaptive) {
                bitrate += " Adaptive"
            }
            trackItems[i] = TrackItem("$label $bitrate", audioTrackInfo.uniqueId)
        }
        return trackItems
    }


    private fun buildAudioChannelString(channelCount: Int): String {
        when (channelCount) {
            1 -> return "Mono"
            2 -> return "Stereo"
            6, 7 -> return "Surround_5.1"
            8 -> return "Surround_7.1"
            else -> return "Surround"
        }
    }


    /**
     * Will build array of [TrackItem] objects.
     * Each [TrackItem] object will hold the readable name.
     * In this case the label of the text track.
     * We use this name to represent the track selection options.
     * Also each [TrackItem] will hold the unique id of the track,
     * which should be passed to the player in order to switch to the desired track.
     * @param textTracks - the list of available text tracks.
     * @return - array with custom [TrackItem] objects.
     */
    private fun buildTextTrackItems(textTracks: List<TextTrack>): Array<TrackItem?> {
        //Initialize TrackItem array with size of textTracks list.
        val trackItems = arrayOfNulls<TrackItem>(textTracks.size)

        //Iterate through all available text tracks.
        for (i in textTracks.indices) {

            //Get text track from index i.
            val textTrackInfo = textTracks[i]

            //Name TrackItem based on the text track label.
            val name = textTrackInfo.label
            trackItems[i] = TrackItem(name!!, textTrackInfo.uniqueId)
        }
        return trackItems
    }

    /**
     * Initialize and set custom adapter to the Android spinner.
     * @param spinner - spinner to which adapter should be applied.
     * @param trackItems - custom track items array.
     */
    private fun applyAdapterOnSpinner(spinner: Spinner, trackItems: Array<TrackItem?>, defaultSelectedIndex: Int) {
        //Initialize custom adapter.
        val trackItemAdapter = TrackItemAdapter(this, R.layout.track_items_list_row, trackItems)
        //Apply adapter on spinner.
        spinner.adapter = trackItemAdapter

        if (defaultSelectedIndex > 0) {
            spinner.setSelection(defaultSelectedIndex)
        }
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (!userIsInteracting) {
            return
        }
        //Get the selected TrackItem from adapter.
        val trackItem = parent.getItemAtPosition(position) as TrackItem

        //Important! This will actually do the switch between tracks.
        player!!.changeTrack(trackItem.uniqueId)
    }

    override fun onNothingSelected(parent: AdapterView<*>) {

    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        userIsInteracting = true
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        if (player != null) {
            if (playPauseButton != null) {
                playPauseButton!!.setText(R.string.pause_text)
            }
            player!!.onApplicationPaused()
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()

        if (player != null && playerState != null) {
            player!!.onApplicationResumed()
            player!!.play()
        }
    }

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setSubtitleStyle(defaultPositionDefault)
        playerInitOptions.setAllowCrossProtocolEnabled(true)
        playerInitOptions.setAutoPlay(true)


        /*
                  //PhoenixTVPlayerParams
                  "analyticsUrl": "https://analytics.kaltura.com/api_v3/index.php"
                  "ovpServiceUrl": "https://cdnapisec.kaltura.com"
                  "ovpPartnerId": 2254732
                  "uiConfId": 44267972
                 */


        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)
        player!!.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player!!.playerView)

        val ottMediaOptions = buildOttMediaOptions()
        player!!.loadMedia(ottMediaOptions) { entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                Log.d(TAG, "OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }

        //Add simple play/pause button.
        addPlayPauseButton()

        //Initialize Android spinners view.
        initializeTrackSpinners()

        //Subscribe to the event which will notify us when track data is available.
        subscribeToTracksAvailableEvent()
    }

    private fun buildOttMediaOptions(): OTTMediaOptions {
        val ottMediaOptions = OTTMediaOptions()
        ottMediaOptions.assetId = ASSET_ID
        ottMediaOptions.assetType = APIDefines.KalturaAssetType.Media
        ottMediaOptions.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaOptions.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaOptions.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaOptions.formats = arrayOf("Mobile_Main")
        ottMediaOptions.ks = null
        ottMediaOptions.startPosition = START_POSITION
        ottMediaOptions.externalSubtitles = externalSubtitles

        return ottMediaOptions
    }

    companion object {
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        val PARTNER_ID = 3009
    }
}
