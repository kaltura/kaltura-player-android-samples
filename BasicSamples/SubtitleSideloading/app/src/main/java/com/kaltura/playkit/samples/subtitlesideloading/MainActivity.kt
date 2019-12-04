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

import com.kaltura.playkit.PKDrmParams
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaFormat
import com.kaltura.playkit.PKMediaSource
import com.kaltura.playkit.PKSubtitleFormat
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.player.AudioTrack
import com.kaltura.playkit.player.PKExternalSubtitle
import com.kaltura.playkit.player.PKTracks
import com.kaltura.playkit.player.SubtitleStyleSettings
import com.kaltura.playkit.player.TextTrack
import com.kaltura.playkit.player.VideoTrack
import com.kaltura.playkit.samples.subtitlesideloading.tracks.TrackItem
import com.kaltura.playkit.samples.subtitlesideloading.tracks.TrackItemAdapter
import com.kaltura.tvplayer.KalturaBasicPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.PlayerInitOptions

import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.atomic.AtomicInteger


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val TAG = "MainActivity"
    private val START_POSITION = 0L // position for start playback in msec.

    private val MEDIA_FORMAT = PKMediaFormat.hls
    private val SOURCE_URL = "https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8"
    private val LICENSE_URL: String? = null
    // Source to see subtitles any source can be used
    //   private static final String SOURCE_URL = "http://www.streambox.fr/playlists/test_001/stream.m3u8";

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

        val mediaEntry = createMediaEntry()

        loadPlaykitPlayer(mediaEntry)

        //Add simple play/pause button.
        addPlayPauseButton()

        //Initialize Android spinners view.
        initializeTrackSpinners()

        //Subscribe to the event which will notify us when track data is available.
        subscribeToTracksAvailableEvent()

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
     * Side load the external subtitles
     * @param mediaEntry PKMediaEntry object
     */

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
     * Just add a simple button which will start/pause playback.
     */
    private fun addPlayPauseButton() {
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById<View>(R.id.play_pause_button) as Button
        //Add clickListener.
        playPauseButton!!.setOnClickListener {
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

    /**
     * Here we are getting access to the Android Spinner views,
     * and set OnItemSelectedListener.
     */
    private fun initializeTrackSpinners() {
        tracksSelectionMenu = this.findViewById(R.id.tracks_selection_menu) as View
        videoSpinner = this.findViewById<View>(R.id.videoSpinner) as Spinner
        audioSpinner = this.findViewById<View>(R.id.audioSpinner) as Spinner
        textSpinner = this.findViewById<View>(R.id.textSpinner) as Spinner
        ccStyleSpinner = this.findViewById<View>(R.id.ccStyleSpinner) as Spinner
        tvSpinnerTitle = this.findViewById<View>(R.id.tvSpinnerTitle) as TextView
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

            //Cast event to the TracksAvailable object that is actually holding the necessary data.
            val tracksAvailable = event as PlayerEvent.TracksAvailable

            //Obtain the actual tracks info from it. Default track index values are coming from manifest
            val tracks = tracksAvailable.tracksInfo
            val defaultAudioTrackIndex = tracks.defaultAudioTrackIndex
            val defaultTextTrackIndex = tracks.defaultTextTrackIndex
            if (tracks.audioTracks.size > 0) {
                Log.d(TAG, "Default Audio langae = " + tracks.audioTracks[defaultAudioTrackIndex].label!!)
            }
            if (tracks.textTracks.size > 0) {
                Log.d(TAG, "Default Text langae = " + tracks.textTracks[defaultTextTrackIndex].label!!)
                if (tvSpinnerTitle != null && ccStyleSpinner != null) {
                    tvSpinnerTitle!!.visibility = View.VISIBLE
                    ccStyleSpinner!!.visibility = View.VISIBLE
                }
            }
            if (tracks.videoTracks.size > 0) {
                Log.d(TAG, "Default video isAdaptive = " + tracks.videoTracks[tracks.defaultAudioTrackIndex].isAdaptive + " bitrate = " + tracks.videoTracks[tracks.defaultAudioTrackIndex].bitrate)
            }
            //player.changeTrack(tracksAvailable.tracksInfo.getVideoTracks().get(1).getUniqueId());
            //Populate Android spinner views with received data.
            populateSpinnersWithTrackInfo(tracks)
        }

        player!!.addListener<PlayerEvent.VideoTrackChanged>(this, PlayerEvent.videoTrackChanged) { event -> Log.d(TAG, "Event VideoTrackChanged " + (event as PlayerEvent.VideoTrackChanged).newTrack.bitrate) }

        player!!.addListener<PlayerEvent.AudioTrackChanged>(this, PlayerEvent.audioTrackChanged) { event -> Log.d(TAG, "Event AudioTrackChanged " + (event as PlayerEvent.AudioTrackChanged).newTrack.language!!) }

        player!!.addListener<PlayerEvent.TextTrackChanged>(this, PlayerEvent.textTrackChanged) { event -> Log.d(TAG, "Event TextTrackChanged " + (event as PlayerEvent.TextTrackChanged).newTrack.language!!) }

        player!!.addListener<PlayerEvent.SubtitlesStyleChanged>(this, PlayerEvent.subtitlesStyleChanged) { event -> Log.d(TAG, "Event SubtitlesStyleChanged " + (event as PlayerEvent.SubtitlesStyleChanged).styleName) }
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

    /**
     * Create [PKMediaEntry] with minimum necessary data.
     *
     * @return - the [PKMediaEntry] object.
     */
    private fun createMediaEntry(): PKMediaEntry {
        //Create media entry.
        val mediaEntry = PKMediaEntry()

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


    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (!userIsInteracting) {
            return
        }
        //Get the selected TrackItem from adapter.
        val (_, uniqueId) = parent.getItemAtPosition(position) as TrackItem

        //Important! This will actually do the switch between tracks.
        player!!.changeTrack(uniqueId)
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
            player!!.onApplicationPaused()
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()

        if (player != null) {
            if (playPauseButton != null) {
                playPauseButton!!.setText(R.string.pause_text)
            }
            player!!.onApplicationResumed()
            player!!.play()
        }
    }

    fun loadPlaykitPlayer(pkMediaEntry: PKMediaEntry) {
        val playerInitOptions = PlayerInitOptions()
        playerInitOptions.setSubtitleStyle(defaultPositionDefault)
        playerInitOptions.setAutoPlay(true)

        player = KalturaBasicPlayer.create(this@MainActivity, playerInitOptions)
        player!!.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player!!.playerView)
        player!!.setMedia(pkMediaEntry, START_POSITION)

    }
}
