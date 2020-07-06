package com.kaltura.playkit.samples.mediaplaybackpreview

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.kaltura.tvplayer.KalturaPlayer
import kotlinx.android.synthetic.main.item_playback_preview.view.*

class PlayerListAdapter(private val mediaList: ArrayList<MediaItem>, var itemClickListener: UserClickedForMediaPlayback) : RecyclerView.Adapter<MediaViewHolder>() {

    private var kalturaPlayer: KalturaPlayer? = null
    private var mediaViewHolder: MediaViewHolder? = null

    interface UserClickedForMediaPlayback {
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        mediaViewHolder = MediaViewHolder(inflater, parent, kalturaPlayer, parent.context, itemClickListener)
        return mediaViewHolder as MediaViewHolder
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        mediaViewHolder = holder
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

    fun getMediaHolder(): MediaViewHolder? {
        return mediaViewHolder
    }

    fun updateItemAtPosition(position: Int, showMediaImage: Boolean) {
        val updatedMediaItem: MediaItem = this.mediaList.get(position)
        updatedMediaItem.addMediaImageView = showMediaImage
        this.mediaList[position] = updatedMediaItem
        notifyItemChanged(position);
    }
}

class MediaViewHolder(inflater: LayoutInflater, parent: ViewGroup, player: KalturaPlayer?,
                      context: Context, var itemClickListener: PlayerListAdapter.UserClickedForMediaPlayback) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.item_playback_preview, parent, false)) {
    private var holderPlayer: KalturaPlayer? = player
    private var ctx: Context = context

    fun bind(mediaItem: MediaItem) {
        if (mediaItem.addMediaImageView) {
            itemView.media_image.visibility = View.VISIBLE
            itemView.player_root.visibility = View.GONE
            itemView.player_control_view.visibility = View.GONE
            itemView.player_control_view.setPlayer(null)
            itemView.player_control_view.release()
        } else {
            if (holderPlayer?.playerView?.parent != null) {
                (holderPlayer?.playerView?.parent as ViewGroup).removeAllViews()
            }
            itemView.player_root.addView(holderPlayer?.playerView)
            itemView.player_control_view.setPlayer(holderPlayer)
            itemView.player_root.visibility = View.VISIBLE
            itemView.player_control_view.visibility = View.VISIBLE
            itemView.player_control_view.resume()
            itemView.media_image.visibility = View.GONE
        }
        Glide.with(ctx)
                .load("http://images-or.ott.kaltura.com/Service.svc/GetImage/p/${MainActivity.PARTNER_ID}/entry_id/${mediaItem.mediaImageView}")
                .into(itemView.media_image)
        itemView.player_root.setOnClickListener {
            holderPlayer?.let {
                if (it.isPlaying) {
                    it.pause()
                } else {
                    it.play()
                }
            }
        }

        if (itemView.media_image.visibility == View.VISIBLE) {
            itemView.media_image.setOnClickListener {
                itemClickListener.onItemClick(adapterPosition)
            }
        }
    }

    fun getControlsView(): PlaybackControlsView {
        return itemView.player_control_view
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