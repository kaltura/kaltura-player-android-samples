package com.kaltura.kalturaplayertestapp.tracks

import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import android.util.SparseArray
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.kalturaplayertestapp.R
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.player.AudioTrack
import com.kaltura.playkit.player.PKTracks
import com.kaltura.playkit.utils.Consts
import com.kaltura.playkit.utils.Consts.*
import com.kaltura.tvplayer.KalturaPlayer
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

class TracksSelectionController(private val context: Context, private val player: KalturaPlayer?, val tracks: PKTracks?) {
    private val log = PKLog.get("TracksSelectionController")

    private var lastVideoTrackSelectionIndex = 0
    private var lastAudioTrackSelectionIndex = 0
    private var lastTextTrackSelectionIndex = 0

    init {
        lastVideoTrackSelectionIndex = tracks!!.getDefaultVideoTrackIndex()
        lastAudioTrackSelectionIndex = tracks.getDefaultAudioTrackIndex()
        lastTextTrackSelectionIndex = tracks.getDefaultTextTrackIndex()
    }

    private fun buildBitrateString(bitrate: Long): String {
        return if (bitrate == Consts.NO_VALUE)
            ""
        else
            String.format("%.2fMbit", bitrate / 1000000f)
    }

    private fun buildLanguageString(language: String): String {
        return if (TextUtils.isEmpty(language) || "und" == language)
            ""
        else
            language
    }

    private fun buildTracksSelectionView(): RecyclerView {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val recyclerView = inflater.inflate(R.layout.tracks_selection_recycle_view, null) as RecyclerView
        val layoutManager = LinearLayoutManager(context)

        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = layoutManager

        return recyclerView
    }

    fun showTracksSelectionDialog(trackType: Int) {
        val trackItems = createTrackItems(trackType)
        if (trackItems.size <= 1) {
            return
        }
        val lastTrackSelection: Int
        when (trackType) {
            TRACK_TYPE_VIDEO -> lastTrackSelection = lastVideoTrackSelectionIndex
            TRACK_TYPE_AUDIO -> lastTrackSelection = lastAudioTrackSelectionIndex
            TRACK_TYPE_TEXT -> lastTrackSelection = lastTextTrackSelectionIndex
            else -> return
        }

        val recyclerView = buildTracksSelectionView()
        val adapter = TracksAdapter(trackItems, lastTrackSelection)
        recyclerView.adapter = adapter
        val builder = AlertDialog.Builder(context)

        builder.setTitle(getDialogTitle(trackType))
        builder.setPositiveButton(R.string.ok) { dialogInterface, i ->
            onTrackSelected(trackType, adapter.trackItemId, adapter.lastTrackSelection)
            dialogInterface.dismiss()
        }
        builder.setView(recyclerView)

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun onTrackSelected(trackType: Int, uniqueId: String?, lastTrackSelected: Int) {
        if (uniqueId == null) {
            return
        }

        when (trackType) {
            TRACK_TYPE_VIDEO -> lastVideoTrackSelectionIndex = lastTrackSelected
            TRACK_TYPE_AUDIO -> lastAudioTrackSelectionIndex = lastTrackSelected
            TRACK_TYPE_TEXT -> lastTextTrackSelectionIndex = lastTrackSelected
            else -> return
        }
        player?.changeTrack(uniqueId)
    }

    private fun getDialogTitle(trackType: Int): String {

        when (trackType) {
            Consts.TRACK_TYPE_VIDEO -> return "Video"
            Consts.TRACK_TYPE_AUDIO -> return "Audio"
            Consts.TRACK_TYPE_TEXT -> return "Text"
            else -> return ""
        }
    }

    fun createTrackItems(eventType: Int): List<TrackItem> {

        val trackItems = ArrayList<TrackItem>()
        if (tracks == null) {
            return trackItems
        }
        var trackItem: TrackItem
        when (eventType) {
            TRACK_TYPE_VIDEO -> {
                val videoTracksInfo = tracks.videoTracks
                for (i in videoTracksInfo.indices) {
                    val trackInfo = videoTracksInfo[i]
                    if (trackInfo.isAdaptive) {
                        trackItem = TrackItem(trackInfo.uniqueId, "Auto")
                    } else {
                        trackItem = TrackItem(trackInfo.uniqueId, buildBitrateString(trackInfo.bitrate))
                    }
                    trackItems.add(trackItem)
                }
            }
            TRACK_TYPE_AUDIO -> {
                val audioTracksInfo = tracks.audioTracks
                val channelSparseIntArray = SparseArray<AtomicInteger>()

                for (i in audioTracksInfo.indices) {
                    if (channelSparseIntArray.get((audioTracksInfo[i] as AudioTrack).channelCount) != null) {
                        channelSparseIntArray.get((audioTracksInfo[i] as AudioTrack).channelCount).incrementAndGet()
                    } else {
                        channelSparseIntArray.put((audioTracksInfo[i] as AudioTrack).channelCount, AtomicInteger(1))
                    }
                }
                var addChannel = false
                if (channelSparseIntArray.size() > 0 && AtomicInteger(audioTracksInfo.size).toString() != channelSparseIntArray.get((audioTracksInfo[0] as AudioTrack).channelCount).toString()) {
                    addChannel = true
                }
                for (i in audioTracksInfo.indices) {
                    val audioTrackInfo = audioTracksInfo[i] as AudioTrack
                    var label: String? = if (audioTrackInfo.label != null) audioTrackInfo.label else audioTrackInfo.language
                    var bitrate = if (audioTrackInfo.bitrate > 0) "" + audioTrackInfo.bitrate else ""
                    if (TextUtils.isEmpty(bitrate) && addChannel) {
                        bitrate = buildAudioChannelString(audioTrackInfo.channelCount)
                    }
                    if (audioTrackInfo.isAdaptive) {
                        if (!TextUtils.isEmpty(bitrate)) {
                            bitrate += " Adaptive"
                        } else {
                            bitrate = "Adaptive"
                        }
                        if (label == null) {
                            label = ""
                        }
                    }
                    trackItem = TrackItem(audioTrackInfo.uniqueId, "$label $bitrate")
                    trackItems.add(trackItem)
                }
            }
            TRACK_TYPE_TEXT -> {
                val textTracksInfo = tracks.textTracks
                for (i in textTracksInfo.indices) {
                    val trackInfo = textTracksInfo[i]
                    if (trackInfo.isAdaptive) {
                        trackItem = TrackItem(trackInfo.uniqueId, "Auto")
                    } else {
                        var lang = trackInfo.label
                        if (lang == null) {
                            lang = getFriendlyLanguageLabel(trackInfo.language)
                        }
                        trackItem = TrackItem(trackInfo.uniqueId, buildLanguageString(lang))
                    }
                    trackItems.add(trackItem)
                }
            }
        }//                if (audioTracksInfo != null && audioTracksInfo.size() > 1) {
        //                    for (int i = 0; i < audioTracksInfo.size(); i++) {
        //                        AudioTrack trackInfo = audioTracksInfo.get(i);
        //                        String lang = trackInfo.getLabel();
        //                        if (lang == null) {
        //                            lang = getFriendlyLanguageLabel(trackInfo.getLanguage());
        //                        }
        //                        if (trackInfo.isAdaptive()) {
        //                            trackItem = new TrackItem(trackInfo.getUniqueId(), buildLanguageString(lang) + " " + "Auto");
        //                        } else {
        //                            trackItem = new TrackItem(trackInfo.getUniqueId(), buildLanguageString(lang) + " " + buildBitrateString(trackInfo.getBitrate()));
        //                        }
        //                        trackItems.add(trackItem);
        //                    }
        //}
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

    private fun getFriendlyLanguageLabel(languageCode: String?): String {
        if (languageCode == null) {
            return ""
        }

        var lang: String?
        lang = Locale(languageCode).displayLanguage
        lang = lang!!.substring(0, 1).toUpperCase() + lang.substring(1)
        return lang ?: languageCode
    }

    fun setTrackLastSelectionIndex(trackType: Int, trackIndex: Int) {
        when (trackType) {
            TRACK_TYPE_VIDEO -> lastVideoTrackSelectionIndex = trackIndex
            TRACK_TYPE_AUDIO -> lastAudioTrackSelectionIndex = trackIndex
            TRACK_TYPE_TEXT -> lastTextTrackSelectionIndex = trackIndex
            else -> return
        }
    }
}
