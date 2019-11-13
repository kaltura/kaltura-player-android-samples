package com.kaltura.playkit.samples.youbora

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
import com.google.gson.JsonObject
import com.kaltura.playkit.PKPluginConfigs
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.plugins.youbora.YouboraEvent
import com.kaltura.playkit.plugins.youbora.YouboraPlugin
import com.kaltura.playkit.plugins.youbora.pluginconfig.YouboraConfig.*
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_ACCOUNT_CODE
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_AD_CAMPAIGN
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
import com.npaw.youbora.lib6.plugin.Options.Companion.KEY_USERNAME

class MainActivity : AppCompatActivity() {

    //Tag for logging.
    private val TAG = MainActivity::class.java.simpleName

    private val START_POSITION = 0L // position for start playback in msec.

    companion object {
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        private val ASSET_ID = "548576"
        val PARTNER_ID = 3009
    }

    //Youbora analytics Constants
    val ACCOUNT_CODE = "your_account_code"
    val UNIQUE_USER_NAME = "your_app_logged_in_user_email_or_userId"
    val MEDIA_TITLE = "your_media_title"
    val ENABLE_SMART_ADS = true
    private val CAMPAIGN = "your_campaign_name"
    val EXTRA_PARAM_1 = "playKitPlayer"
    val EXTRA_PARAM_2 = ""
    val GENRE = "your_genre"
    val TYPE = "your_type"
    val TRANSACTION_TYPE = "your_trasnsaction_type"
    val YEAR = "your_year"
    val CAST = "your_cast"
    val DIRECTOR = "your_director"
    private val OWNER = "your_owner"
    val PARENTAL = "your_parental"
    val PRICE = "your_price"
    val RATING = "your_rating"
    val AUDIO_TYPE = "your_audio_type"
    val AUDIO_CHANNELS = "your_audoi_channels"
    val DEVICE = "your_device"
    val QUALITY = "your_quality"


    private var player: KalturaPlayer? = null
    private var playPauseButton: Button? = null
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loadPlaykitPlayer()

        findViewById<View>(R.id.activity_main).setOnClickListener { v ->
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
     * Subscribe to kaltura stats report event.
     * This event will be received each and every time
     * the analytics report is sent.
     */
    private fun subscribeToYouboraReportEvent() {
        //Subscribe to the event.
        player?.addListener<YouboraEvent.YouboraReport>(this, YouboraEvent.reportSent) { event ->

            //Get the event name from the report.
            val reportedEventName = event.reportedEventName
            Log.i(TAG, "Youbora report sent. Reported event name: $reportedEventName")
        }
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
        if (player != null) {
            if (playPauseButton != null) {
                playPauseButton?.setText(R.string.pause_text)
            }
            player?.onApplicationPaused()
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

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setAllowCrossProtocolEnabled(true)

        // Youbora Configuration
        val pkPluginConfigs = PKPluginConfigs()
        val youboraConfigJson = getYouboraConfig()

        pkPluginConfigs.setPluginConfig(YouboraPlugin.factory.name, getYouboraBundle())
        playerInitOptions.setPluginConfigs(pkPluginConfigs)

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)

        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player?.playerView)
        val ottMediaOptions = buildOttMediaOptions()
        player?.loadMedia(ottMediaOptions) { entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                Log.i(TAG, "OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }

        //Subscribe to analytics report event.
        subscribeToYouboraReportEvent()

        //Add simple play/pause button.
        addPlayPauseButton()

        showSystemUI()

        addPlayerStateListener()
    }

    private fun buildOttMediaOptions(): OTTMediaOptions {
        val ottMediaOptions = OTTMediaOptions()
        ottMediaOptions.assetId = ASSET_ID
        ottMediaOptions.assetType = APIDefines.KalturaAssetType.Media
        ottMediaOptions.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaOptions.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaOptions.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaOptions.ks = null
        ottMediaOptions.startPosition = START_POSITION
        ottMediaOptions.formats = arrayOf("Mobile_Main")

        return ottMediaOptions
    }

    /**
     * JSON Youbora Configuration
     * @return YouboraConfigJSON
     */
    private fun getYouboraConfig(): JsonObject {

        //Youbora config json. Main config goes here.
        val youboraConfigJson = JsonObject()
        youboraConfigJson.addProperty("accountCode", ACCOUNT_CODE)
        youboraConfigJson.addProperty("username", UNIQUE_USER_NAME)
        youboraConfigJson.addProperty("haltOnError", true)
        youboraConfigJson.addProperty("enableAnalytics", true)
        youboraConfigJson.addProperty("enableSmartAds", ENABLE_SMART_ADS)


        //Media entry json.
        val mediaEntryJson = JsonObject()
        mediaEntryJson.addProperty("title", MEDIA_TITLE)

        //Optional - Device json o/w youbora will decide by its own.
        val deviceJson = JsonObject()
        deviceJson.addProperty("deviceCode", "AndroidTV")
        deviceJson.addProperty("brand", "Xiaomi")
        deviceJson.addProperty("model", "Mii3")
        deviceJson.addProperty("type", "TvBox")
        deviceJson.addProperty("osName", "Android/Oreo")
        deviceJson.addProperty("osVersion", "8.1")

        //Youbora ads configuration json.
        val adsJson = JsonObject()
        adsJson.addProperty("adsExpected", true)
        adsJson.addProperty("campaign", CAMPAIGN)

        //Configure custom properties here:
        val propertiesJson = JsonObject()
        propertiesJson.addProperty("genre", GENRE)
        propertiesJson.addProperty("type", TYPE)
        propertiesJson.addProperty("transaction_type", TRANSACTION_TYPE)
        propertiesJson.addProperty("year", YEAR)
        propertiesJson.addProperty("cast", CAST)
        propertiesJson.addProperty("director", DIRECTOR)
        propertiesJson.addProperty("owner", OWNER)
        propertiesJson.addProperty("parental", PARENTAL)
        propertiesJson.addProperty("price", PRICE)
        propertiesJson.addProperty("rating", RATING)
        propertiesJson.addProperty("audioType", AUDIO_TYPE)
        propertiesJson.addProperty("audioChannels", AUDIO_CHANNELS)
        propertiesJson.addProperty("device", DEVICE)
        propertiesJson.addProperty("quality", QUALITY)

        //You can add some extra params here:
        val extraParamJson = JsonObject()
        extraParamJson.addProperty("param1", EXTRA_PARAM_1)
        extraParamJson.addProperty("param2", EXTRA_PARAM_2)

        //Add all the json objects created before to the pluginEntry json.
        youboraConfigJson.add("media", mediaEntryJson)
        youboraConfigJson.add("device", deviceJson)
        youboraConfigJson.add("ads", adsJson)
        youboraConfigJson.add("properties", propertiesJson)
        youboraConfigJson.add("extraParams", extraParamJson)

        return youboraConfigJson
    }

    /**
     * Bundle Youbora Configuration
     * @return YouboraConfigBundle
     */
    private fun getYouboraBundle(): Bundle {

        val optBundle = Bundle()

        //Youbora config bundle. Main config goes here.
        optBundle.putString(KEY_ACCOUNT_CODE, ACCOUNT_CODE)
        optBundle.putString(KEY_USERNAME, UNIQUE_USER_NAME)
        optBundle.putBoolean(KEY_ENABLED, true)

        //Media entry bundle.
        optBundle.putString(KEY_CONTENT_TITLE, MEDIA_TITLE)

        //Optional - Device bundle o/w youbora will decide by its own.
        optBundle.putString(KEY_DEVICE_CODE, "AndroidTV")
        optBundle.putString(KEY_DEVICE_BRAND, "Xiaomi")
        optBundle.putString(KEY_DEVICE_MODEL, "Mii3")
        optBundle.putString(KEY_DEVICE_TYPE, "TvBox")
        optBundle.putString(KEY_DEVICE_OS_NAME, "Android/Oreo")
        optBundle.putString(KEY_DEVICE_OS_VERSION, "8.1")

        //Youbora ads configuration bundle.
        optBundle.putString(KEY_AD_CAMPAIGN, CAMPAIGN)

        //Configure custom properties here:
        optBundle.putString(KEY_CONTENT_GENRE, GENRE)
        optBundle.putString(KEY_CONTENT_TYPE, TYPE)
        optBundle.putString(KEY_CONTENT_TRANSACTION_CODE, TRANSACTION_TYPE)

        optBundle.putString(KEY_CONTENT_PRICE, PRICE)
        optBundle.putString(KEY_CONTENT_ENCODING_AUDIO_CODEC, AUDIO_TYPE)
        optBundle.putString(KEY_CONTENT_CHANNEL, AUDIO_CHANNELS)

        val contentMetadataBundle = Bundle()

        contentMetadataBundle.putString(KEY_CONTENT_METADATA_YEAR, YEAR)
        contentMetadataBundle.putString(KEY_CONTENT_METADATA_CAST, CAST)
        contentMetadataBundle.putString(KEY_CONTENT_METADATA_DIRECTOR, DIRECTOR)
        contentMetadataBundle.putString(KEY_CONTENT_METADATA_OWNER, OWNER)
        contentMetadataBundle.putString(KEY_CONTENT_METADATA_PARENTAL, PARENTAL)
        contentMetadataBundle.putString(KEY_CONTENT_METADATA_RATING, RATING)
        contentMetadataBundle.putString(KEY_CONTENT_METADATA_QUALITY, QUALITY)

        optBundle.putBundle(KEY_CONTENT_METADATA, contentMetadataBundle)

        //You can add some extra params here:
        optBundle.putString(KEY_CUSTOM_DIMENSION_1, EXTRA_PARAM_1)
        optBundle.putString(KEY_CUSTOM_DIMENSION_2, EXTRA_PARAM_2)

        return optBundle
    }


}