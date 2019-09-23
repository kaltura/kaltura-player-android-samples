package com.kaltura.playkitdemo

import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.AdapterView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.kaltura.playkit.PKDrmParams
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.player.MediaSupport
import com.kaltura.playkit.player.PKTracks
import com.kaltura.playkit.plugins.ads.AdCuePoints
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.PlayerInitOptions

class MainActivity : AppCompatActivity(), OrientationManager.OrientationListener {

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

    var mOrientationManager: OrientationManager? = null
    private var userIsInteracting: Boolean = false
    private var tracksInfo: PKTracks? = null
    private val isAdsEnabled = false
    private val isDAIMode = false
    private var playerState: PlayerState? = null

    var playerInitOptions: PlayerInitOptions? = null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //getPermissionToReadExternalStorage()
        initDrm()
        //PlayKitProfiler.init(this)

        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }

        mOrientationManager = OrientationManager(this, SensorManager.SENSOR_DELAY_NORMAL, this)
        mOrientationManager.enable()


    }

    private fun initDrm() {
        MediaSupport.initializeDrm(this) { supportedDrmSchemes, provisionPerformed, provisionError ->
            if (provisionPerformed) {
                if (provisionError != null) {
                    log.e("DRM Provisioning failed", provisionError)
                } else {
                    log.d("DRM Provisioning succeeded")
                }
            }
            log.d("DRM initialized; supported: $supportedDrmSchemes")

            // Now it's safe to look at `supportedDrmSchemes`
        }
    }

    override fun onOrientationChange(screenOrientation: OrientationManager.ScreenOrientation) {
        when (screenOrientation) {
            OrientationManager.ScreenOrientation.PORTRAIT -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            OrientationManager.ScreenOrientation.REVERSED_PORTRAIT -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            OrientationManager.ScreenOrientation.LANDSCAPE -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            OrientationManager.ScreenOrientation.REVERSED_LANDSCAPE -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
            else -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }

}