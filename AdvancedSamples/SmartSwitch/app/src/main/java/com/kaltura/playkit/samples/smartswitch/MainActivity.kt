package com.kaltura.playkit.samples.smartswitch

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.*
import com.kaltura.playkit.plugins.smartswitch.SmartSwitchEvent
import com.kaltura.playkit.plugins.smartswitch.SmartSwitchPlugin
import com.kaltura.playkit.plugins.smartswitch.pluginconfig.SmartSwitchConfig
import com.kaltura.playkit.plugins.youbora.YouboraPlugin
import com.kaltura.playkit.plugins.youbora.pluginconfig.YouboraConfig.*
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.*
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_ACCOUNT_CODE
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_AD_CAMPAIGN
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_APP_NAME
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_APP_RELEASE_VERSION
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_CONTENT_CDN
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_CONTENT_CHANNEL
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_CONTENT_ENCODING_AUDIO_CODEC
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_CONTENT_GENRE
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_CONTENT_METADATA
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_CONTENT_PRICE
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_CONTENT_TITLE
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_CONTENT_TRANSACTION_CODE
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_CONTENT_TYPE
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_CUSTOM_DIMENSION_1
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_CUSTOM_DIMENSION_2
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_DEVICE_BRAND
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_DEVICE_CODE
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_DEVICE_MODEL
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_DEVICE_OS_NAME
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_DEVICE_OS_VERSION
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_DEVICE_TYPE
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_ENABLED
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_PARSE_CDN_NAME_HEADER
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_PARSE_CDN_NODE
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_PARSE_CDN_NODE_LIST
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_PARSE_CDN_SWITCH_HEADER
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_PARSE_CDN_TTL
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_PARSE_MANIFEST
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_USERNAME
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_USER_EMAIL

class MainActivity : AppCompatActivity() {

    //Tag for logging.
    private val TAG = MainActivity::class.java.simpleName

    //Youbora analytics Constants
    val ACCOUNT_CODE = "kalturatest"
    val UNIQUE_USER_NAME = "your_app_logged_in_user_email_or_userId"
    val USER_EMAIL = "user_email"
    val MEDIA_TITLE = "your_media_title"
    val ENABLE_SMART_ADS = true

    val PARSE_MANIFEST = true
    val PARSE_CDN_NODE = true
    val PARSE_CDN_SWITCH_HEADER = true
    val PARSE_CDN_NODE_LIST = arrayListOf("Akamai", "Cloudfront", "Level3", "Fastly", "Highwinds")
    val PARSE_CDN_NAME_HEADERS = "x-cdn-forward"
    val PARSE_CDN_TTL = 60

    val CAMPAIGN = "your_campaign_name"
    val EXTRA_PARAM_1 = "playKitPlayer"
    val EXTRA_PARAM_2 = "zzzz"
    val GENRE = "your_genre"
    val TYPE = "your_type"
    val TRANSACTION_TYPE = "your_trasnsaction_type"
    val YEAR = "your_year"
    val CAST = "your_cast"
    val DIRECTOR = "your_director"
    val OWNER = "your_owner"
    val PARENTAL = "your_parental"
    val PRICE = "your_price"
    val RATING = "your_rating"
    val AUDIO_TYPE = "your_audio_type"
    val AUDIO_CHANNELS = "your_audoi_channels"
    val DEVICE = "your_device"
    val QUALITY = "your_quality"
    /**
    Follow this {@link http://mapi.youbora.com:8081/cdns}
     */
    val CONTENT_CDN_CODE = "your_cdn_code"
    val PROGRAM = "your_program"
    /**
    Follow this {@link http://mapi.youbora.com:8081/devices}
     */
    val DEVICE_CODE = "your_device_code"

    private val START_POSITION = 0L // position for start playback in msec.

    companion object {
        const val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        const val FIRST_ASSET_ID = "548576"
        const val SECOND_ASSET_ID = "548575"
        const val MEDIA_FORMAT = "Mobile_Main"

        const val PARTNER_ID = 3009
        const val KS = ""
    }

    private var player: KalturaPlayer? = null
    private var playPauseButton: Button? = null
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null
    private var currentlyPlayingAsset: String? = null
    private var change_media_button: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        change_media_button = findViewById(R.id.change_media_button)


        loadPlaykitPlayer()

        findViewById<View>(R.id.activity_main).setOnClickListener { v ->
            if (isFullScreen) {
                showSystemUI()
            } else {
                hideSystemUI()
            }
        }

        change_media_button?.setOnClickListener {
            currentlyPlayingAsset?.let {
                if (it == FIRST_ASSET_ID) {
                    loadSecondOttMedia()
                } else {
                    loadFirstOttMedia()
                }
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
        //Get reference to the play/pause button.
        playPauseButton = this.findViewById<View>(R.id.play_pause_button) as Button
        //Add clickListener.
        playPauseButton?.setOnClickListener {
            if (player!!.isPlaying) {
                //If player is playing, change text of the button and pause.
                playPauseButton?.setText(R.string.play_text)
                player?.pause()
            } else {
                //If player is not playing, change text of the button and play.
                playPauseButton?.setText(R.string.pause_text)
                player?.play()
            }
        }
    }

    private fun addPlayerStateListener() {
        player?.addListener<PlayerEvent.StateChanged>(this, PlayerEvent.stateChanged) { event ->
            Log.d(TAG, "State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        player?.let {
            if (playPauseButton != null) {
                playPauseButton?.setText(R.string.pause_text)
            }
            it.onApplicationPaused()
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()

        if (player != null && playerState != null) {
            player?.onApplicationResumed()
            player?.play()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.destroy()
    }

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setPKRequestConfig(PKRequestConfig(true))

        // SmartSwitch Configuration
        val pkPluginConfigs = PKPluginConfigs()
        val optionalParams: HashMap<String, String> = HashMap()
        optionalParams.put("OPTION_PARAM_KEY_1", "OPTION_PARAM_VALUE_1")
        optionalParams.put("OPTION_PARAM_KEY_2", "OPTION_PARAM_VALUE_2")

        val smartSwitchConfig = SmartSwitchConfig("YOUR_ACCOUNT_CODE", optionalParams)
        pkPluginConfigs.setPluginConfig(SmartSwitchPlugin.factory.name, smartSwitchConfig)
        pkPluginConfigs.setPluginConfig(YouboraPlugin.factory.name, getYouboraBundle())

        playerInitOptions.setPluginConfigs(pkPluginConfigs)

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)

        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        player?.addListener(this, PlayerEvent.error) { event ->
            Log.i(TAG, "PLAYER ERROR " + event.error.message!!)
        }

        player?.addListener(this, SmartSwitchEvent.error) { event ->
            Log.i(TAG, "SmartSwitch ERROR " + event.errorMessage)
        }

        player?.addListener(this, InterceptorEvent.cdnSwitched) { event ->
            Log.i(TAG, "InterceptorEvent CDN_SWITCHED " + event.cdnCode)
        }

        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player?.playerView)
        val ottMediaOptions = buildOttMediaOptions(FIRST_ASSET_ID)
        player?.loadMedia(ottMediaOptions) { mediaOptions, entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_SHORT).show()
            } else {
                Log.i(TAG, "OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }

        //Add simple play/pause button.
        addPlayPauseButton()

        showSystemUI()

        addPlayerStateListener()
    }

    private fun buildOttMediaOptions(assetId: String): OTTMediaOptions {
        currentlyPlayingAsset = assetId
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = assetId
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaAsset.ks = KS
        ottMediaAsset.formats = listOf(MEDIA_FORMAT)
        return OTTMediaOptions(ottMediaAsset)
    }

    private fun loadFirstOttMedia() {
        val ottMediaOptions = buildOttMediaOptions(FIRST_ASSET_ID)
        ottMediaOptions.startPosition = START_POSITION
        player?.loadMedia(ottMediaOptions) { mediaOptions, entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_SHORT).show()
            } else {
                Log.i(TAG, "OTTMedia onEntryLoadComplete entry = " + entry.id)
            }
        }
    }

    private fun loadSecondOttMedia() {
        val ottMediaOptions = buildOttMediaOptions(SECOND_ASSET_ID)
        ottMediaOptions.startPosition = START_POSITION
        player?.loadMedia(ottMediaOptions) { mediaOptions, entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_SHORT).show()
            } else {
                Log.i(TAG, "OTTMedia onEntryLoadComplete entry = " + entry.id)
            }
        }
    }

    private fun getYouboraBundle(): Bundle {

        val optBundle = Bundle()

        //Youbora config bundle. Main config goes here.
        optBundle.putString(KEY_ACCOUNT_CODE, ACCOUNT_CODE)
        optBundle.putBoolean(KEY_PARSE_CDN_NODE, PARSE_CDN_NODE)

//        optBundle.putString(KEY_USERNAME, UNIQUE_USER_NAME)
//        optBundle.putString(KEY_USER_EMAIL, USER_EMAIL)
//
//        optBundle.putBoolean(KEY_ENABLED, true)
//        optBundle.putString(KEY_APP_NAME, "TestApp");
//        optBundle.putString(KEY_APP_RELEASE_VERSION, "v1.0");
//
//        //Media entry bundle.
//        optBundle.putString(KEY_CONTENT_TITLE, MEDIA_TITLE)
//        optBundle.putBoolean(KEY_PARSE_MANIFEST, true);
//        optBundle.putBoolean(KEY_PARSE_CDN_NODE, true);
//
//        optBundle.putBoolean(KEY_PARSE_MANIFEST, PARSE_MANIFEST)
//
//        optBundle.putBoolean(KEY_PARSE_CDN_SWITCH_HEADER, PARSE_CDN_SWITCH_HEADER)
//        optBundle.putStringArrayList(KEY_PARSE_CDN_NODE_LIST, PARSE_CDN_NODE_LIST)
//        optBundle.putString(KEY_PARSE_CDN_NAME_HEADER, PARSE_CDN_NAME_HEADERS)
//        optBundle.putInt(KEY_PARSE_CDN_TTL, PARSE_CDN_TTL)
//
//
//        //Optional - Device bundle o/w youbora will decide by its own.
//        optBundle.putString(KEY_DEVICE_CODE, DEVICE_CODE)
//        optBundle.putString(KEY_DEVICE_BRAND, "Xiaomi")
//        optBundle.putString(KEY_DEVICE_MODEL, "Mii3")
//        optBundle.putString(KEY_DEVICE_TYPE, "TvBox")
//        optBundle.putString(KEY_DEVICE_OS_NAME, "Android/Oreo")
//        optBundle.putString(KEY_DEVICE_OS_VERSION, "8.1")
//
//        //Youbora ads configuration bundle.
//        optBundle.putString(KEY_AD_CAMPAIGN, CAMPAIGN)
//
//        //Configure custom properties here:
//        optBundle.putString(KEY_CONTENT_GENRE, GENRE)
//        optBundle.putString(KEY_CONTENT_TYPE, TYPE)
//        optBundle.putString(KEY_CONTENT_TRANSACTION_CODE, TRANSACTION_TYPE)
//        optBundle.putString(KEY_CONTENT_CDN, CONTENT_CDN_CODE)
//
//        optBundle.putString(KEY_CONTENT_PRICE, PRICE)
//        optBundle.putString(KEY_CONTENT_ENCODING_AUDIO_CODEC, AUDIO_TYPE)
//        optBundle.putString(KEY_CONTENT_CHANNEL, AUDIO_CHANNELS)
//
//        val contentMetadataBundle = Bundle()
//
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_YEAR, YEAR)
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_CAST, CAST)
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_DIRECTOR, DIRECTOR)
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_OWNER, OWNER)
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_PARENTAL, PARENTAL)
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_RATING, RATING)
//        contentMetadataBundle.putString(KEY_CONTENT_METADATA_QUALITY, QUALITY)
//
//        optBundle.putBundle(KEY_CONTENT_METADATA, contentMetadataBundle)
//
//        //You can add some extra params here:
//        optBundle.putString(KEY_CUSTOM_DIMENSION_1, EXTRA_PARAM_1)
//        optBundle.putString(KEY_CUSTOM_DIMENSION_2, EXTRA_PARAM_2)

        return optBundle
    }
}
