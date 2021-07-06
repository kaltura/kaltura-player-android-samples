package com.kaltura.player.offlinedemo

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class OfflineAssetViewHolder(itemView: View, onItemClickListener: RvOfflineAssetsAdapter.OnAdapterItemClickListener): RecyclerView.ViewHolder(itemView), View.OnClickListener {

    internal var tvItemName: TextView
    internal var tvItemStatus: TextView
    internal var tvItemDownloadPerct: TextView
    internal var tvItemIsPrefetch: TextView
    private var onItemClickListener: RvOfflineAssetsAdapter.OnAdapterItemClickListener

    init {
        tvItemName = itemView.findViewById(R.id.tv_item_name)
        tvItemStatus = itemView.findViewById(R.id.tv_item_status)
        tvItemDownloadPerct = itemView.findViewById(R.id.tv_item_download_perct)
        tvItemIsPrefetch = itemView.findViewById(R.id.tv_item_is_prefetch)
        this.onItemClickListener = onItemClickListener
        itemView.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        this.onItemClickListener.onItemClick(adapterPosition)
    }
}