package com.kaltura.playkit.samples.mediaplaybackpreview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kaltura.tvplayer.KalturaPlayer
import kotlinx.android.synthetic.main.item_playback_preview.view.*

class PlayerListAdapter(private val mediaList: ArrayList<MediaItem>) : RecyclerView.Adapter<MediaViewHolder>() {

    private var kalturaPlayer: KalturaPlayer? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return MediaViewHolder(inflater, parent, kalturaPlayer)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        val mediaItem: MediaItem = mediaList[position]
        holder.bind(mediaItem)
    }

    override fun getItemCount(): Int = mediaList.size

    fun getItemAtPosition(position: Int): MediaItem {
        return mediaList[position]
    }

    fun setPlayer(player: KalturaPlayer?) {
        kalturaPlayer = player
    }

    fun updateItemAtPosition(position: Int, showMediaImage: Boolean) {
        val updatedMediaItem: MediaItem = this.mediaList.get(position)
        updatedMediaItem.addMediaImageView = showMediaImage
        this.mediaList[position] = updatedMediaItem
        notifyItemChanged(position);
    }
}

class MediaViewHolder(inflater: LayoutInflater, parent: ViewGroup, player: KalturaPlayer?) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.item_playback_preview, parent, false)) {
    private var holderPlayer: KalturaPlayer? = player

    fun bind(mediaItem: MediaItem) {
        if (mediaItem.addMediaImageView) {
            itemView.media_image.visibility = View.VISIBLE
            itemView.player_root.visibility = View.GONE
        } else {
            itemView.media_image.visibility = View.GONE
            itemView.player_root.visibility = View.VISIBLE
            if (holderPlayer?.playerView?.parent != null) {
                (holderPlayer?.playerView?.parent as ViewGroup).removeAllViews()
            }
            itemView.player_root.addView(holderPlayer?.playerView)
        }
    }
}

abstract class PaginationScrollListener(private val layoutManager: LinearLayoutManager) : RecyclerView.OnScrollListener() {
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        when (newState) {
            RecyclerView.SCROLL_STATE_IDLE -> {
                val firstVisibleItemPosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                loadNextMedia(firstVisibleItemPosition)
            }
            RecyclerView.SCROLL_STATE_DRAGGING ->{
                pauseCurrentMedia()
            }
            RecyclerView.SCROLL_STATE_SETTLING ->{
                pauseCurrentMedia()
            }
        }
    }

    protected abstract fun loadNextMedia(midVisibleItem: Int)
    protected abstract fun pauseCurrentMedia()

}