package com.kaltura.playkit.samples.mediaplaybackpreview

import android.os.Bundle
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    companion object {
        //Media entry configuration constants.
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        val PARTNER_ID = 3009
        val log = PKLog.get("MainActivity")
    }

    var mediaArray: ArrayList<String>  = ArrayList(listOf("548579","548577","548576","548575","548574","548573",
            "548572","548571","548570","548569","548579","548577","548576","548575","548574","548573",
            "548572","548571","548570","548569"))


    private val START_POSITION = 0L // position for start playback in msec.
    private var player: KalturaPlayer? = null
    private var playerState: PlayerState? = null

    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var layoutAdapter: PlayerListAdapter
    private var previousMediaPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadPlaykitPlayer()

        val mediaList = ArrayList<MediaItem>()
        for (media in mediaArray) {
            mediaList.add(MediaItem(media,""))
        }

        rvPlaybackPreviews.apply {
            linearLayoutManager = LinearLayoutManager(this@MainActivity)
            layoutAdapter = PlayerListAdapter(mediaList)
            layoutAdapter.setPlayer(player);
        }

        rvPlaybackPreviews.addOnScrollListener(object :PaginationScrollListener(linearLayoutManager) {
            override fun loadNextMedia(midVisibleItem: Int) {
                playMediaForOnePosition(midVisibleItem)
            }
            override fun pauseCurrentMedia() {
                player?.pause()
            }
        })

        rvPlaybackPreviews.layoutManager = linearLayoutManager
        rvPlaybackPreviews.adapter = layoutAdapter

        playMediaForOnePosition(0)
    }

    private fun playMediaForOnePosition(midVisibleItem: Int) {
        if (previousMediaPosition == midVisibleItem) {
            player?.play()
            return
        }

        if (player?.isPlaying!!) {
            player?.stop()
        }

        val mediaId: String = layoutAdapter.getItemAtPosition(midVisibleItem).mediaId
        if (previousMediaPosition == -1) {
            previousMediaPosition = midVisibleItem
            layoutAdapter.updateItemAtPosition(previousMediaPosition, false)
        } else {
            layoutAdapter.updateItemAtPosition(previousMediaPosition, true)
            previousMediaPosition = midVisibleItem
            layoutAdapter.updateItemAtPosition(previousMediaPosition, false)
        }
        buildFirstOttMediaOptions(mediaId)
    }

    private fun addPlayerStateListener() {
        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }

        player?.addListener(this, PlayerEvent.error) { event -> log.d("player ERROR " + event.error.message) }
    }

    override fun onPause() {
        log.d("onPause")
        super.onPause()
        player?.onApplicationPaused()
    }

    override fun onResume() {
        log.d("onResume")
        super.onResume()

        if (player != null && playerState != null) {
            player?.onApplicationResumed()
            player?.play()
        }
    }

    fun loadPlaykitPlayer() {
        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setAllowCrossProtocolEnabled(true)

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)

        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        // playerControls?.setPlayer(player)

        addPlayerStateListener()
    }

    private fun buildFirstOttMediaOptions(mediaId: String) {
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = mediaId
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaAsset.ks = null

        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = START_POSITION


        player?.loadMedia(ottMediaOptions) { entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }

    }
}
