package com.kaltura.kalturaplayertestapp.tracks

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.kalturaplayertestapp.R

class TracksAdapter constructor(trackItems: List<*>, lastTrackSelection: Int): RecyclerView.Adapter<TracksAdapter.ViewHolder>() {

    private var trackItems: List<TrackItem>? = null
    var trackItemId: String? = null
        private set
    var lastTrackSelection: Int = 0
        private set

    init {
        if (trackItems.size > 0) {
            if (trackItems[0] is TrackItem) {
                this.trackItems = trackItems as List<TrackItem>
                trackItemId = this.trackItems!![lastTrackSelection].uniqueId
            }
        }
        this.lastTrackSelection = lastTrackSelection
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layout = LayoutInflater.from(parent.context)
                .inflate(R.layout.track_selection_row_item, parent, false) as ConstraintLayout

        return ViewHolder(layout)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.radioButton.isChecked = position == lastTrackSelection
        holder.textView.text = trackItems!![position].trackDescription
    }


    override fun getItemCount(): Int {
        return if (trackItems != null) {
            trackItems!!.size
        } else 0
    }

    inner class ViewHolder(layout: ConstraintLayout): RecyclerView.ViewHolder(layout) {

        val textView: TextView
        val radioButton: RadioButton

        init {
            textView = layout.findViewById(R.id.tvTrackDescription)
            radioButton = layout.findViewById(R.id.rbTrackItem)

            radioButton.setOnClickListener {
                lastTrackSelection = adapterPosition
                if (trackItems != null) {
                    trackItemId = trackItems!![lastTrackSelection].uniqueId
                    notifyItemRangeChanged(0, trackItems!!.size)
                }
            }
        }
    }
}

