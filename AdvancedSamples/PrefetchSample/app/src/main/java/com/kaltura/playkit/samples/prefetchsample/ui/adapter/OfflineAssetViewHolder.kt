package com.kaltura.playkit.samples.prefetchsample.ui.adapter

import android.view.View
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.playkit.samples.prefetchsample.R

class OfflineAssetViewHolder(itemView: View, val itemClick: (Int) -> Unit, val checkBoxClicked: (Int, Boolean, CheckBox, TextView) -> Unit): RecyclerView.ViewHolder(itemView) {

    internal var tvItemName: TextView
    internal var tvItemStatus: TextView
    internal var tvItemDownloadPerct: TextView
    internal var tvItemIsPrefetch: TextView
    internal var cbItemIsPrefetch: CheckBox

    init {
        tvItemName = itemView.findViewById(R.id.tv_item_name)
        tvItemStatus = itemView.findViewById(R.id.tv_item_status)
        tvItemDownloadPerct = itemView.findViewById(R.id.tv_item_download_perct)
        tvItemIsPrefetch = itemView.findViewById(R.id.tv_item_is_prefetch)
        cbItemIsPrefetch = itemView.findViewById(R.id.cb_item_prefetch)
        itemView.setOnClickListener { itemClick(adapterPosition) }
        cbItemIsPrefetch.visibility = View.GONE
        cbItemIsPrefetch.setOnCheckedChangeListener { _, isChecked ->
            checkBoxClicked(adapterPosition, isChecked, cbItemIsPrefetch, tvItemIsPrefetch)
        }
    }
}