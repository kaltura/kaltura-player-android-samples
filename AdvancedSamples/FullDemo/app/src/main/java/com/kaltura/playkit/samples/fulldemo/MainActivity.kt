package com.kaltura.playkit.samples.fulldemo

import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.kaltura.playkit.samples.fulldemo.Consts.AD_LOAD_TIMEOUT
import com.kaltura.playkit.samples.fulldemo.Consts.AUTO_PLAY
import com.kaltura.playkit.samples.fulldemo.Consts.COMPANION_AD_HEIGHT
import com.kaltura.playkit.samples.fulldemo.Consts.COMPANION_AD_WIDTH
import com.kaltura.playkit.samples.fulldemo.Consts.MIME_TYPE
import com.kaltura.playkit.samples.fulldemo.Consts.MIN_AD_DURATION_FOR_SKIP_BUTTON
import com.kaltura.playkit.samples.fulldemo.Consts.PREFERRED_BITRATE
import com.kaltura.playkit.samples.fulldemo.Consts.START_FROM
import com.kaltura.playkit.samples.fulldemo.Consts.VIDEO_EXAMPLE_FRAGMENT_TAG
import com.kaltura.playkit.samples.fulldemo.Consts.VIDEO_PLAYLIST_FRAGMENT_TAG
import com.kaltura.playkit.samples.fulldemo.VideoListFragment.OnVideoListFragmentResumedListener
import com.kaltura.playkit.samples.fulldemo.VideoListFragment.OnVideoSelectedListener

class MainActivity : AppCompatActivity(), OnVideoSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener, OnVideoListFragmentResumedListener, VideoFragment.OnVideoFragmentViewCreatedListener, OrientationManager.OrientationListener {

    private lateinit var mOrientationManager: OrientationManager
    private var minAdDurationForSkipButton: Int = 0
    private var isAutoPlay: Boolean = false
    private var startPosition: Long = 0L
    private var adLoadTimeOut: Int = 0
    private var videoMimeType: String? = null
    private var videoBitrate: Int = 0
    private var companionAdWidth: Int = 0
    private var companionAdHeight: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mOrientationManager = OrientationManager(this, SensorManager.SENSOR_DELAY_NORMAL, this)
        mOrientationManager.enable()
        val fragmentManager = supportFragmentManager
        if (fragmentManager.findFragmentByTag(VIDEO_PLAYLIST_FRAGMENT_TAG) == null) {
            val videoListFragment = VideoListFragment()
            val bundle = Bundle()
            bundle.putBoolean(AUTO_PLAY, isAutoPlay)
            bundle.putLong(START_FROM, startPosition)
            bundle.putInt(MIN_AD_DURATION_FOR_SKIP_BUTTON, minAdDurationForSkipButton)
            bundle.putInt(AD_LOAD_TIMEOUT, adLoadTimeOut)
            bundle.putString(MIME_TYPE, videoMimeType)
            bundle.putInt(PREFERRED_BITRATE, videoBitrate)
            bundle.putInt(COMPANION_AD_WIDTH, companionAdWidth)
            bundle.putInt(COMPANION_AD_HEIGHT, companionAdHeight)

            videoListFragment.setArguments(bundle)
            supportFragmentManager.beginTransaction()
                    .add(R.id.video_example_container, videoListFragment,
                            VIDEO_PLAYLIST_FRAGMENT_TAG)
                    .commit()
        }
        setupSharedPreferences()
        orientAppUi()
    }


    private fun setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)

        val companionAdDimentions = sharedPreferences.getString(getString(R.string.pref_companion_key), "0x0")
        val dimentions = companionAdDimentions?.split("x".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray()
        companionAdWidth = Integer.valueOf(dimentions!![0])
        companionAdHeight = Integer.valueOf(dimentions[1])

        minAdDurationForSkipButton = 1000 * Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_min_ad_duration_for_skip_button_key),
                "" + Integer.valueOf(getString(R.string.pref_min_ad_duration_for_skip_button_default)))!!)

        adLoadTimeOut = 1000 * Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_ad_load_timeout_key),
                "" + Integer.valueOf(getString(R.string.pref_ad_load_timeout_default)))!!)

        videoBitrate = Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_bitrate_key),
                "" + Integer.valueOf(getString(R.string.pref_bitrate_value)))!!)

        var currentStartPosition = sharedPreferences.getString(getString(R.string.pref_start_from_key),
                "" + Integer.valueOf(getString(R.string.pref_start_from_default)))

        if ("" == currentStartPosition) {
            currentStartPosition = "0"
        }
        startPosition = java.lang.Long.valueOf(currentStartPosition!!)

        isAutoPlay = sharedPreferences.getBoolean(getString(R.string.pref_auto_play_key),
                resources.getBoolean(R.bool.pref_auto_play_default))

        videoMimeType = sharedPreferences.getString(getString(R.string.pref_mime_type_key),
                getString(R.string.pref_mime_type_value))

        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }

    private fun orientAppUi() {
        val orientation = resources.configuration.orientation
        val isLandscape = orientation == Configuration.ORIENTATION_LANDSCAPE
        // Hide the non-video content when in landscape so the video is as large as possible.
        val fragmentManager = supportFragmentManager
        val videoFragment = fragmentManager.findFragmentByTag(VIDEO_EXAMPLE_FRAGMENT_TAG) as VideoFragment?

        val videoListFragment = fragmentManager.findFragmentByTag(
                VIDEO_PLAYLIST_FRAGMENT_TAG)

        if (videoFragment != null) {
            // If the video playlist is onscreen (tablets) then hide that fragment.
            if (videoListFragment != null) {
                val fragmentTransaction = fragmentManager.beginTransaction()
                if (isLandscape) {
                    fragmentTransaction.hide(videoListFragment)
                } else {
                    fragmentTransaction.show(videoListFragment)
                }
                fragmentTransaction.commit()
            }
            videoFragment.makeFullscreen(isLandscape)
            if (isLandscape) {
                hideStatusBar()
            } else {
                showStatusBar()
            }
        } else {
            // If returning to the list from a fullscreen video, check if the video
            // list fragment exists and is hidden. If so, show it.
            if (videoListFragment != null && videoListFragment.isHidden) {
                fragmentManager.beginTransaction().show(videoListFragment).commit()
                showStatusBar()
            }
        }
    }

    override fun onVideoSelected(videoItem: VideoItem) {

        var videoFragment = supportFragmentManager.findFragmentByTag(VIDEO_EXAMPLE_FRAGMENT_TAG) as VideoFragment?

        // Add the video fragment if it's missing (phone form factor), but only if the user
        // manually selected the video.
        if (videoFragment == null) {
            val videoListFragment = supportFragmentManager
                    .findFragmentByTag(VIDEO_PLAYLIST_FRAGMENT_TAG) as VideoListFragment?
            val videoPlaylistFragmentId = videoListFragment?.id

            videoFragment = VideoFragment()
            val bundle = Bundle()
            bundle.putBoolean(AUTO_PLAY, isAutoPlay)
            bundle.putLong(START_FROM, startPosition)
            bundle.putInt(MIN_AD_DURATION_FOR_SKIP_BUTTON, minAdDurationForSkipButton)
            bundle.putInt(AD_LOAD_TIMEOUT, adLoadTimeOut)
            bundle.putString(MIME_TYPE, videoMimeType)
            bundle.putInt(PREFERRED_BITRATE, videoBitrate)
            bundle.putInt(COMPANION_AD_WIDTH, companionAdWidth)
            bundle.putInt(COMPANION_AD_HEIGHT, companionAdHeight)

            videoFragment.arguments = bundle
            supportFragmentManager
                    .beginTransaction()
                    .replace(videoPlaylistFragmentId!!, videoFragment, VIDEO_EXAMPLE_FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit()
        }
        videoFragment.loadVideo(videoItem)
        invalidateOptionsMenu()
        orientAppUi()
    }


    override fun onVideoListFragmentResumed() {
        invalidateOptionsMenu()
        orientAppUi()
    }

    private fun hideStatusBar() {
        if (Build.VERSION.SDK_INT >= 16) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
            supportActionBar?.hide()
        }
    }

    private fun showStatusBar() {
        if (Build.VERSION.SDK_INT >= 16) {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            supportActionBar?.show()
        }
    }

    override fun onVideoFragmentViewCreated() {
        orientAppUi()
    }

    override fun onConfigurationChanged(configuration: Configuration) {
        super.onConfigurationChanged(configuration)
        orientAppUi()
    }

    override fun onOrientationChange(screenOrientation: OrientationManager.ScreenOrientation) {
        when (screenOrientation) {
            OrientationManager.ScreenOrientation.PORTRAIT -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            OrientationManager.ScreenOrientation.REVERSED_PORTRAIT -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
            OrientationManager.ScreenOrientation.LANDSCAPE -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            OrientationManager.ScreenOrientation.REVERSED_LANDSCAPE -> requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
        }
    }

    /**
     * Methods for setting up the menu
     */
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        val inflater = menuInflater
        /* Use the inflater's inflate method to inflate our visualizer_menu layout to this menu */
        inflater.inflate(R.menu.visualizer_menu, menu)
        /* Return true so that the visualizer_menu is displayed in the Toolbar */
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            val startSettingsActivity = Intent(this, SettingsActivity::class.java)
            startActivity(startSettingsActivity)
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        if (key == getString(R.string.pref_ad_load_timeout_key)) {
            adLoadTimeOut = 1000 * Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_ad_load_timeout_key), getString(R.string.pref_ad_load_timeout_default))!!)
        } else if (key == getString(R.string.pref_min_ad_duration_for_skip_button_key)) {
            minAdDurationForSkipButton = 1000 * Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_min_ad_duration_for_skip_button_key), getString(R.string.pref_min_ad_duration_for_skip_button_default))!!)
        } else if (key == getString(R.string.pref_auto_play_key)) {
            isAutoPlay = sharedPreferences.getBoolean(getString(R.string.pref_auto_play_key), resources.getBoolean(R.bool.pref_auto_play_default))
        } else if (key == getString(R.string.pref_start_from_key)) {
            var startFrom: String = "0"
            if ("" != sharedPreferences.getString(getString(R.string.pref_start_from_key), getString(R.string.pref_start_from_default))) {
                startFrom = sharedPreferences.getString(getString(R.string.pref_start_from_key), getString(R.string.pref_start_from_default))
            }
            startPosition = java.lang.Long.valueOf(startFrom)
        } else if (key == getString(R.string.pref_bitrate_key)) {
            videoBitrate = Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_bitrate_key), getString(R.string.pref_bitrate_value)))
        } else if (key == getString(R.string.pref_companion_key)) {
            val companionAdDimentions = sharedPreferences.getString(getString(R.string.pref_companion_key), "0x0")
            val dimentions = companionAdDimentions.split("x".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            companionAdWidth = Integer.valueOf(dimentions[0])
            companionAdHeight = Integer.valueOf(dimentions[1])
        } else if (key == getString(R.string.pref_mime_type_key)) {
            videoMimeType = sharedPreferences.getString(getString(R.string.pref_mime_type_key), getString(R.string.pref_mime_type_value))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this)
    }


}
