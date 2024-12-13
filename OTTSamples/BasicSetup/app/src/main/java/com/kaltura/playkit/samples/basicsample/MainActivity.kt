package com.kaltura.playkit.samples.basicsample

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKRequestConfig
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.playkit.samples.basicsample.databinding.ActivityMainBinding
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions

class MainActivity : AppCompatActivity() {

    private val log = PKLog.get("MainActivity")
    private val START_POSITION = 0L // position for start playback in msec.
    private var player: KalturaPlayer? = null
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        loadPlaykitPlayer()

        addPlayPauseButton()

        showSystemUI()

        binding.activityMain.setOnClickListener { v ->
            if (isFullScreen) {
                showSystemUI()
            } else {
                hideSystemUI()
            }
        }

    }

    private fun hideSystemUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
        } else {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar

                    or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar

                    or View.SYSTEM_UI_FLAG_IMMERSIVE)
        }
        isFullScreen = true
    }

    private fun showSystemUI() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN)
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        isFullScreen = false
    }


    /**
     * Just add a simple button which will start/pause playback.
     */
    private fun addPlayPauseButton() {
        //Add clickListener.
        binding.playPauseButton.setOnClickListener { v ->
            player?.let {
                if (it.isPlaying) {
                    //If player is playing, change text of the button and pause.
                    binding.playPauseButton.setText(R.string.play_text)
                    it.pause()
                } else {
                    //If player is not playing, change text of the button and play.
                    binding.playPauseButton.setText(R.string.pause_text)
                    it.play()
                }
            }
        }
    }

    private fun addPlayerStateListener() {
        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    override fun onResume() {
        super.onResume()
        player?.let {
            binding.playPauseButton.setText(R.string.pause_text)
            if (it.mediaEntry != null) {
                it.onApplicationResumed()
                it.play()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.destroy();
    }

    override fun onPause() {
        super.onPause()
        player?.onApplicationPaused()
    }

    fun loadPlaykitPlayer() {
        PKLog.setGlobalLevel(PKLog.Level.verbose)
        val playerInitOptions = PlayerInitOptions(ConfigurationProvider.getPartnerId())
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setPKRequestConfig(PKRequestConfig(true))
        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)
        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        val container = binding.playerRoot
        container.addView(player?.playerView)

        val ottMediaOptions = buildOttMediaOptions()
        player?.loadMedia(ottMediaOptions) { mediaOptions, entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }

        addPlayerStateListener()
    }

    private fun buildOttMediaOptions(): OTTMediaOptions {
        val adapterData: MutableMap<String, String> = HashMap()
        adapterData["codec"] = "HEVC"//"AVC"
        adapterData["quality"] = "UHD"
        adapterData["drm"] = if (ConfigurationProvider.getIsDrm()) "true" else "false"
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = ConfigurationProvider.getAssetId()
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.urlType = APIDefines.KalturaUrlType.Direct
        ottMediaAsset.streamerType = APIDefines.KalturaStreamerType.Mpegdash
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.adapterData = adapterData
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Https
//        ottMediaAsset.formats = listOf("DASH_HEVC")
//        ottMediaAsset.formats = listOf("DASH_WV")
//        ottMediaAsset.formats = listOf("BP_VOD_HLS", "BP_VOD_Dash")
        ottMediaAsset.ks = ConfigurationProvider.getKsToken()
        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = START_POSITION

        return ottMediaOptions
    }
    companion object {
    }
}
