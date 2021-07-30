package com.kaltura.playkit.samples.prefetchsample.ui.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.playkit.samples.prefetchsample.Item
import com.kaltura.playkit.samples.prefetchsample.R
import com.kaltura.tvplayer.OfflineManager

class RvOfflineAssetsAdapter(private val itemList: List<Item>, val itemClick: (Int) -> Unit): RecyclerView.Adapter<OfflineAssetViewHolder>() {

    private var isOfflineProviderExo: Boolean = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OfflineAssetViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val itemView = inflater.inflate(R.layout.view_item, parent, false)
        return OfflineAssetViewHolder(itemView, itemClick)
    }

    override fun onBindViewHolder(offlineAssetViewHolder: OfflineAssetViewHolder, position: Int) {
        offlineAssetViewHolder.tvItemName.text = itemList[position].title()
        val assetStatus = itemList[position].assetInfo?.state ?: OfflineManager.AssetDownloadState.none
        offlineAssetViewHolder.tvItemStatus.text = "Asset Status: $assetStatus ".plus(itemList[position].drmNotRegistered?.let {
            return@let if (it && assetStatus != OfflineManager.AssetDownloadState.none) { "(Drm Not Registered)" } else { "" }
        })
        if (assetStatus == OfflineManager.AssetDownloadState.none ||
            assetStatus == OfflineManager.AssetDownloadState.completed ||
            assetStatus == OfflineManager.AssetDownloadState.prefetched) {
            offlineAssetViewHolder.tvItemDownloadPerct.visibility = View.GONE
        } else {
            offlineAssetViewHolder.tvItemDownloadPerct.visibility = View.VISIBLE
            offlineAssetViewHolder.tvItemDownloadPerct.text = itemList[position].getDownloadPercentage()
        }

        if (isOfflineProviderExo) {
            offlineAssetViewHolder.cbItemIsPrefetch.visibility = View.VISIBLE
            if (assetStatus == OfflineManager.AssetDownloadState.prefetched) {
                offlineAssetViewHolder.cbItemIsPrefetch.isChecked = true
                offlineAssetViewHolder.tvItemIsPrefetch.visibility = View.VISIBLE
                offlineAssetViewHolder.tvItemIsPrefetch.text = "Prefetch Available"
            }

            offlineAssetViewHolder.cbItemIsPrefetch.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    itemList[position].isPrefetch = true
                    offlineAssetViewHolder.tvItemIsPrefetch.visibility = View.VISIBLE
                    offlineAssetViewHolder.tvItemIsPrefetch.text = "Prefetch Available"
                } else {
                    itemList[position].isPrefetch = false
                    offlineAssetViewHolder.tvItemIsPrefetch.visibility = View.GONE
                }
            }
        } else {
            offlineAssetViewHolder.cbItemIsPrefetch.visibility = View.GONE
            offlineAssetViewHolder.cbItemIsPrefetch.isChecked = false
            offlineAssetViewHolder.tvItemIsPrefetch.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun getItemAtPosition(position: Int): Item {
        return itemList[position]
    }

    fun isOfflineProviderExo(isExoProvider: Boolean) {
        isOfflineProviderExo = isExoProvider
    }

    fun getPositionOfItem(assetId: String): Int {
        for (index in itemList.indices) {
            if (TextUtils.equals(itemList[index].id(), assetId)) {
                return index
            }
        }

        return -1
    }
}