package com.kaltura.playkit.samples.prefetchsample

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OfflineAssetViewHolder(itemView: View, val itemClick: (Int) -> Unit): RecyclerView.ViewHolder(itemView) {

    internal var tvItemName: TextView
    internal var tvItemStatus: TextView
    internal var tvItemDownloadPerct: TextView
    internal var tvItemIsPrefetch: TextView

    init {
        tvItemName = itemView.findViewById(R.id.tv_item_name)
        tvItemStatus = itemView.findViewById(R.id.tv_item_status)
        tvItemDownloadPerct = itemView.findViewById(R.id.tv_item_download_perct)
        tvItemIsPrefetch = itemView.findViewById(R.id.tv_item_is_prefetch)
        itemView.setOnClickListener{ itemClick(adapterPosition) }
    }
}