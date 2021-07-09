package com.kaltura.player.offlinedemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.tvplayer.OfflineManager

class RvOfflineAssetsAdapter(private val itemList: List<Item>,
                             private var onItemClickListener: OnAdapterItemClickListener): RecyclerView.Adapter<OfflineAssetViewHolder>() {

    interface OnAdapterItemClickListener {
        fun onItemClick(position: Int)
    }

    private var isPrefetchEnable: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflineAssetViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.view_item, parent, false)
        return OfflineAssetViewHolder(itemView, onItemClickListener)
    }

    override fun onBindViewHolder(offlineAssetViewHolder: OfflineAssetViewHolder, position: Int) {
        offlineAssetViewHolder.tvItemName.text = itemList[position].title()
        val assetStatus = itemList[position].assetInfo?.state ?: OfflineManager.AssetDownloadState.none
        offlineAssetViewHolder.tvItemStatus.text = "Asset Status: $assetStatus"
        if (assetStatus == OfflineManager.AssetDownloadState.none ||
            assetStatus == OfflineManager.AssetDownloadState.completed ||
            assetStatus == OfflineManager.AssetDownloadState.prefetched) {
            offlineAssetViewHolder.tvItemDownloadPerct.visibility = View.GONE
        } else {
            offlineAssetViewHolder.tvItemDownloadPerct.visibility = View.VISIBLE
            offlineAssetViewHolder.tvItemDownloadPerct.text = itemList[position].getDownloadPercentage()
        }
        if (itemList[position].isPrefetch && isPrefetchEnable) {
            offlineAssetViewHolder.tvItemIsPrefetch.visibility = View.VISIBLE
            offlineAssetViewHolder.tvItemIsPrefetch.text = "Prefetch Available"
        } else {
            offlineAssetViewHolder.tvItemIsPrefetch.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun getItemAtPosition(position: Int): Item {
        return itemList.get(position)
    }

    fun isPrefetchEnabled(isPrefetchEnable: Boolean) {
        this.isPrefetchEnable = isPrefetchEnable
    }
}