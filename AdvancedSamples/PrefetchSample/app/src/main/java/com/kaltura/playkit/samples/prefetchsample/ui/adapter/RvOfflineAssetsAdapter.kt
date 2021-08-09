package com.kaltura.playkit.samples.prefetchsample.ui.adapter

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
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
        return OfflineAssetViewHolder(itemView, itemClick, { position, isChecked, checkBoxView, prefetchTextView ->
            if (isChecked) {
                itemList[position].isPrefetch = true
                prefetchTextView.visibility = View.VISIBLE
                prefetchTextView.text = "Prefetch Available"
            } else {
                itemList[position].isPrefetch = false
                prefetchTextView.visibility = View.GONE
            }
        }, { position, cbItemIsPrefetch, tvItemIsPrefetch ->
            if (itemList[position].isSelectedForPrefetching) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.exo_white))
                itemList[position].isSelectedForPrefetching = false
                itemList[position].isPrefetch = false
                tvItemIsPrefetch.visibility = View.GONE
                cbItemIsPrefetch.isChecked = false
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.context, R.color.opaque_colorPrimary))
                itemList[position].isSelectedForPrefetching = true
                itemList[position].isPrefetch = true
                tvItemIsPrefetch.visibility = View.VISIBLE
                tvItemIsPrefetch.text = "Prefetch Available"
                cbItemIsPrefetch.isChecked = true
            }
        })
    }

    override fun onBindViewHolder(offlineAssetViewHolder: OfflineAssetViewHolder, position: Int) {
        offlineAssetViewHolder.tvItemName.text = itemList[position].title() + " " + itemList[position].playerType
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
        } else {
            offlineAssetViewHolder.cbItemIsPrefetch.visibility = View.GONE
        }

        if (assetStatus == OfflineManager.AssetDownloadState.prefetched || itemList[position].isPrefetch) {
            offlineAssetViewHolder.cbItemIsPrefetch.isChecked = true
            offlineAssetViewHolder.tvItemIsPrefetch.visibility = View.VISIBLE
            offlineAssetViewHolder.tvItemIsPrefetch.text = "Prefetch Available"
        } else {
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