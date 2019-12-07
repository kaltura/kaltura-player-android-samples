package com.kaltura.playkit.samples.fulldemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class VideoItemAdapter(context: Context, private val mLayoutResourceId: Int, data: List<VideoItem>) : ArrayAdapter<VideoItem>(context, mLayoutResourceId, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val videoItemHolder: VideoItemHolder
        var row = convertView

        // Check if it's recycled.
        if (row == null) {
            val inflater = LayoutInflater.from(context)
            row = inflater.inflate(mLayoutResourceId, parent, false)
            videoItemHolder = VideoItemHolder()
            videoItemHolder.title = row!!.findViewById(R.id.videoItemText)
            videoItemHolder.image = row.findViewById(R.id.videoItemImage)
            row.tag = videoItemHolder
        } else {
            videoItemHolder = row.tag as VideoItemHolder
        }

        val item = getItem(position)
        if (item != null) {
            videoItemHolder.title!!.text = item.title
            videoItemHolder.image!!.setImageResource(item.imageResource)
        }

        return row
    }

    /**
     * Holds the UI element equivalents of a VideoItem.
     */
    private inner class VideoItemHolder {

        internal var title: TextView? = null
        internal var image: ImageView? = null
    }

}