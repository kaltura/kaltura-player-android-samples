package com.kaltura.playkitdemo

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView


class TrackItemAdapter(context: Context, textViewResourceId: Int, private val trackItems: Array<TrackItem?>) : ArrayAdapter<TrackItem>(context, textViewResourceId, trackItems) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val label = TextView(context)
        label.setTextColor(Color.BLACK)
        label.text = trackItems[position]!!.trackName
        return label
    }

    override fun getDropDownView(position: Int, convertView: View?,
                                 parent: ViewGroup): View {
        val label = TextView(context)
        label.setTextColor(Color.BLACK)
        label.text = trackItems[position]!!.trackName
        return label
    }

    override fun getCount(): Int {
        return trackItems.size
    }
}
