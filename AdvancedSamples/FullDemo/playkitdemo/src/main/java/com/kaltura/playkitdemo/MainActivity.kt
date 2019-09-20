package com.kaltura.playkitdemo

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.player.PKTracks
import com.kaltura.playkit.plugins.ads.AdCuePoints
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.PlayerInitOptions

class MainActivity : AppCompatActivity() {

    private val log = PKLog.get("MainActivity")
    private val IMA_PLUGIN : String = "IMA"
    private val DAI_PLUGIN : String = "DAI"
    val READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST : Int = 123
    val changeMediaIndex : Int = -1
    val START_POSITION : Long = 0L

    private var player: KalturaPlayer? = null
    private var controlsView: PlaybackControlsView? = null
    private var nowPlaying: Boolean = false
    private var isFullScreen: Boolean = false
    var progressBar: ProgressBar? = null
    private var playerContainer: RelativeLayout? = null
    private var spinerContainer: RelativeLayout? = null
    private var fullScreenBtn: AppCompatImageView? = null
    private var adCuePoints: AdCuePoints? = null
    private var videoSpinner: Spinner? = null
    private var audioSpinner:Spinner? = null
    private var textSpinner:Spinner? = null

    private var mOrientationManager: OrientationManager? = null
    private var userIsInteracting: Boolean = false
    private var tracksInfo: PKTracks? = null
    private val isAdsEnabled = false
    private val isDAIMode = false
    private var playerState: PlayerState? = null

    var playerInitOptions: PlayerInitOptions? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

}