package com.kaltura.playkit.samples.fulldemo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import static com.kaltura.playkit.samples.fulldemo.Consts.*;

public class MainActivity extends AppCompatActivity implements VideoListFragment.OnVideoSelectedListener, SharedPreferences.OnSharedPreferenceChangeListener,
        VideoListFragment.OnVideoListFragmentResumedListener,
        VideoFragment.OnVideoFragmentViewCreatedListener ,OrientationManager.OrientationListener {

    private OrientationManager mOrientationManager;
    private int minAdDurationForSkipButton;
    private boolean isAutoPlay;
    private Long startPosition = 0L;
    private int adLoadTimeOut;
    private String videoMimeType;
    private int videoBitrate;
    private int companionAdWidth;
    private int companionAdHeight;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOrientationManager = new OrientationManager(this, SensorManager.SENSOR_DELAY_NORMAL, this);
        mOrientationManager.enable();
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(VIDEO_PLAYLIST_FRAGMENT_TAG) == null) {
            VideoListFragment videoListFragment = new VideoListFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(AUTO_PLAY, isAutoPlay);
            bundle.putLong(START_FROM, startPosition);
            bundle.putInt(MIN_AD_DURATION_FOR_SKIP_BUTTON, minAdDurationForSkipButton);
            bundle.putInt(AD_LOAD_TIMEOUT, adLoadTimeOut);
            bundle.putString(MIME_TYPE, videoMimeType);
            bundle.putInt(PREFERRED_BITRATE, videoBitrate);
            bundle.putInt(COMPANION_AD_WIDTH, companionAdWidth);
            bundle.putInt(COMPANION_AD_HEIGHT, companionAdHeight);

            videoListFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.video_example_container, videoListFragment,
                            VIDEO_PLAYLIST_FRAGMENT_TAG)
                    .commit();
        }
        setupSharedPreferences();
        orientAppUi();
    }


    private void setupSharedPreferences() {
        // Get all of the values from shared preferences to set it up
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        String companionAdDimentions = sharedPreferences.getString(getString(R.string.pref_companion_key), "0x0");
        String [] dimentions = companionAdDimentions.split("x");
        companionAdWidth = Integer.valueOf(dimentions[0]);
        companionAdHeight = Integer.valueOf(dimentions[1]);


        minAdDurationForSkipButton = 1000 * Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_min_ad_duration_for_skip_button_key),
                "" + Integer.valueOf(getString(R.string.pref_min_ad_duration_for_skip_button_default))));

        adLoadTimeOut = 1000 * Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_ad_load_timeout_key),
               "" +  Integer.valueOf(getString(R.string.pref_ad_load_timeout_default))));

        videoBitrate = Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_bitrate_key),
                "" + Integer.valueOf(getString(R.string.pref_bitrate_value))));
        String currentStartPosition = sharedPreferences.getString(getString(R.string.pref_start_from_key),
                "" + Integer.valueOf(getString(R.string.pref_start_from_default)));

        if ("".equals(currentStartPosition)) {
            currentStartPosition = "0";
        }
        startPosition = Long.valueOf(currentStartPosition);

        isAutoPlay = sharedPreferences.getBoolean(getString(R.string.pref_auto_play_key),
                getResources().getBoolean(R.bool.pref_auto_play_default));

        videoMimeType = sharedPreferences.getString(getString(R.string.pref_mime_type_key),
                getString(R.string.pref_mime_type_value));

        // Register the listener
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    private void orientAppUi() {
        int orientation = getResources().getConfiguration().orientation;
        boolean isLandscape = (orientation == Configuration.ORIENTATION_LANDSCAPE);
        // Hide the non-video content when in landscape so the video is as large as possible.
        FragmentManager fragmentManager = getSupportFragmentManager();
        VideoFragment videoFragment = (VideoFragment) fragmentManager.findFragmentByTag(VIDEO_EXAMPLE_FRAGMENT_TAG);

        Fragment videoListFragment = fragmentManager.findFragmentByTag(
                VIDEO_PLAYLIST_FRAGMENT_TAG);

        if (videoFragment != null) {
            // If the video playlist is onscreen (tablets) then hide that fragment.
            if (videoListFragment != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                if (isLandscape) {
                    fragmentTransaction.hide(videoListFragment);
                } else {
                    fragmentTransaction.show(videoListFragment);
                }
                fragmentTransaction.commit();
            }
            videoFragment.makeFullscreen(isLandscape);
            if (isLandscape) {
                hideStatusBar();
            } else {
                showStatusBar();
            }
        } else {
            // If returning to the list from a fullscreen video, check if the video
            // list fragment exists and is hidden. If so, show it.
            if (videoListFragment != null && videoListFragment.isHidden()) {
                fragmentManager.beginTransaction().show(videoListFragment).commit();
                showStatusBar();
            }
        }
    }

    @Override
    public void onVideoSelected(VideoItem videoItem) {

        VideoFragment videoFragment = (VideoFragment)
                getSupportFragmentManager().findFragmentByTag(VIDEO_EXAMPLE_FRAGMENT_TAG);

        // Add the video fragment if it's missing (phone form factor), but only if the user
        // manually selected the video.
        if (videoFragment == null) {
            VideoListFragment videoListFragment = (VideoListFragment) getSupportFragmentManager()
                    .findFragmentByTag(VIDEO_PLAYLIST_FRAGMENT_TAG);
            int videoPlaylistFragmentId = videoListFragment.getId();

            videoFragment = new VideoFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean(AUTO_PLAY, isAutoPlay);
            bundle.putLong(START_FROM, startPosition);
            bundle.putInt(MIN_AD_DURATION_FOR_SKIP_BUTTON, minAdDurationForSkipButton);
            bundle.putInt(AD_LOAD_TIMEOUT, adLoadTimeOut);
            bundle.putString(MIME_TYPE, videoMimeType);
            bundle.putInt(PREFERRED_BITRATE, videoBitrate);
            bundle.putInt(COMPANION_AD_WIDTH, companionAdWidth);
            bundle.putInt(COMPANION_AD_HEIGHT, companionAdHeight);

            videoFragment.setArguments(bundle);
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(videoPlaylistFragmentId, videoFragment, VIDEO_EXAMPLE_FRAGMENT_TAG)
                    .addToBackStack(null)
                    .commit();
        }
        videoFragment.loadVideo(videoItem);
        invalidateOptionsMenu();
        orientAppUi();
    }


    @Override
    public void onVideoListFragmentResumed() {
        invalidateOptionsMenu();
        orientAppUi();
    }

    private void hideStatusBar() {
        if (Build.VERSION.SDK_INT >= 16) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN);
            getSupportActionBar().hide();
        }
    }

    private void showStatusBar() {
        if (Build.VERSION.SDK_INT >= 16) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            getSupportActionBar().show();
        }
    }

    @Override
    public void onVideoFragmentViewCreated() {
        orientAppUi();
    }

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        orientAppUi();
    }

    @Override
    public void onOrientationChange(OrientationManager.ScreenOrientation screenOrientation) {
        switch(screenOrientation){
            case PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                break;
            case REVERSED_PORTRAIT:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT);
                break;
            case LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                break;
            case REVERSED_LANDSCAPE:
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                break;
        }
    }

    /**
     * Methods for setting up the menu
     **/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        /* Use AppCompatActivity's method getMenuInflater to get a handle on the menu inflater */
        MenuInflater inflater = getMenuInflater();
        /* Use the inflater's inflate method to inflate our visualizer_menu layout to this menu */
        inflater.inflate(R.menu.visualizer_menu, menu);
        /* Return true so that the visualizer_menu is displayed in the Toolbar */
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_ad_load_timeout_key))) {
            adLoadTimeOut = 1000 * Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_ad_load_timeout_key), getString(R.string.pref_ad_load_timeout_default) ));
        } else if (key.equals(getString(R.string.pref_min_ad_duration_for_skip_button_key))) {
            minAdDurationForSkipButton = 1000 * Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_min_ad_duration_for_skip_button_key),getString(R.string.pref_min_ad_duration_for_skip_button_default)));
        } else if (key.equals(getString(R.string.pref_auto_play_key))) {
            isAutoPlay = sharedPreferences.getBoolean(getString(R.string.pref_auto_play_key), getResources().getBoolean(R.bool.pref_auto_play_default));
        } else if (key.equals(getString(R.string.pref_start_from_key))) {
            String startFrom = "0";
            if (!"".equals(sharedPreferences.getString(getString(R.string.pref_start_from_key), getString(R.string.pref_start_from_default)))) {
                startFrom = sharedPreferences.getString(getString(R.string.pref_start_from_key), getString(R.string.pref_start_from_default));
            }
            startPosition = Long.valueOf(startFrom);
        } else if (key.equals(getString(R.string.pref_bitrate_key))) {
            videoBitrate = Integer.valueOf(sharedPreferences.getString(getString(R.string.pref_bitrate_key), getString(R.string.pref_bitrate_value)));
        } else if (key.equals(getString(R.string.pref_companion_key))) {
            String companionAdDimentions = sharedPreferences.getString(getString(R.string.pref_companion_key), "0x0");
            String [] dimentions = companionAdDimentions.split("x");
            companionAdWidth = Integer.valueOf(dimentions[0]);
            companionAdHeight = Integer.valueOf(dimentions[1]);
        } else if (key.equals(getString(R.string.pref_mime_type_key))) {
            videoMimeType = sharedPreferences.getString(getString(R.string.pref_mime_type_key), getString(R.string.pref_mime_type_value));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister VisualizerActivity as an OnPreferenceChangedListener to avoid any memory leaks.
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }


}
