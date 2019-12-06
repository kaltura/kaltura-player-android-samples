package com.kaltura.kalturaplayertestapp

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class EventsAdapter: RecyclerView.Adapter<EventsAdapter.EventItemViewHolder>() {

    private var eventsList: List<String>? = null

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): EventItemViewHolder {
        val context = viewGroup.context
        val layoutIdForListItem = R.layout.event_list_item
        val inflater = LayoutInflater.from(context)
        val shouldAttachToParentImmediately = false

        val view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParentImmediately)
        val viewHolder = EventItemViewHolder(view)

        viewHolder.itemView.setBackgroundColor(Color.LTGRAY)
        return viewHolder
    }

    override fun onBindViewHolder(holder: EventItemViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return if (null != eventsList) eventsList!!.size else 0

    }

    fun notifyData(eventsList: List<String>) {
        //Log.d("notifyData ", eventsList.size() + "");
        this.eventsList = eventsList
        notifyDataSetChanged()
    }

    inner class EventItemViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {

        internal var eventNumberView: TextView
        internal var eventDesc: TextView

        init {

            eventNumberView = itemView.findViewById(R.id.event_item_number)
            eventDesc = itemView.findViewById(R.id.event_view_holder_instance)
        }

        internal fun bind(listIndex: Int) {
            eventNumberView.text = "$listIndex:"
            eventDesc.text = eventsList!![listIndex]
        }
    }
}