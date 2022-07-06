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
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kaltura.playkit.PKPluginConfigs
import com.kaltura.playkit.PKRequestConfig
import com.kaltura.playkit.PlayerEvent
import com.kaltura.playkit.PlayerState
import com.kaltura.playkit.plugins.youbora.YouboraEvent
import com.kaltura.playkit.plugins.youbora.YouboraPlugin
import com.kaltura.playkit.plugins.youbora.pluginconfig.YouboraConfig.*
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
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

class MainActivity: AppCompatActivity() {

    //Tag for logging.
    private val TAG = MainActivity::class.java.simpleName

    private val START_POSITION = 0L // position for start playback in msec.

    companion object {
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        private val ASSET_ID = "548576"
        val PARTNER_ID = 3009
    }

    private var player: KalturaPlayer? = null
    private var playPauseButton: Button? = null
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null

    //Youbora analytics Constants
    val ACCOUNT_CODE = "your_account_code"
    val UNIQUE_USER_NAME = "your_app_logged_in_user_email_or_userId"
    val USER_EMAIL = "user_email"
    val MEDIA_TITLE = "your_media_title"
    val ENABLE_SMART_ADS = true

    val PARSE_MANIFEST = true
    val PARSE_CDN_NODE = true
    val PARSE_CDN_SWITCH_HEADER = true
    val PARSE_CDN_NODE_LIST = arrayListOf("Akamai", "Cloudfront", "Level3", "Fastly", "Highwinds")
    val PARSE_CDN_NAME_HEADERS = "x-cdn-forward"
    val PARSE_CDN_NODE_HEADERS = "x-node"
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
    val IS_LIVE_MEDIA = false

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
            player?.let {
                if (it.isPlaying) {
                    //If player is playing, change text of the button and pause.
                    playPauseButton?.setText(R.string.play_text)
                    it.pause()
                } else {
                    //If player is not playing, change text of the button and play.
                    playPauseButton?.setText(R.string.pause_text)
                    it.play()
                }
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
        player?.let { player ->
            playPauseButton?.setText(R.string.pause_text)
            player.onApplicationPaused()
        }
    }

    override fun onResume() {
        Log.d(TAG, "onResume")
        super.onResume()
        player?.let {  player ->
            playerState?.let {
                player.onApplicationResumed()
                player.play()
            }
        }
    }

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(PARTNER_ID)
        playerInitOptions.setAutoPlay(true)

        playerInitOptions.setPKRequestConfig(PKRequestConfig(true))

        // Youbora Configuration
        val pkPluginConfigs = PKPluginConfigs()
        pkPluginConfigs.setPluginConfig(YouboraPlugin.factory.name, getYouboraBundle())

        //val youboraConfigJson = getYouboraConfig() //can be used instead of getYouboraBundle()
        //pkPluginConfigs.setPluginConfig(YouboraPlugin.factory.name, youboraConfigJson)

        playerInitOptions.setPluginConfigs(pkPluginConfigs)

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)

        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player?.playerView)
        val ottMediaOptions = buildOttMediaOptions()
        player?.loadMedia(ottMediaOptions) { mediaOptions, entry, loadError ->
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
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = ASSET_ID
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaAsset.ks = null
        ottMediaAsset.formats = listOf("Mobile_Main")

        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)

        ottMediaOptions.startPosition = START_POSITION


        return ottMediaOptions
    }

    /**
     * JSON Youbora Configuration
     * @return YouboraConfigJSON
     */
    private fun getYouboraConfig(): JsonObject {

        // Youbora config json. Main config goes here.
        val youboraConfigJson = JsonObject()
        youboraConfigJson.addProperty("accountCode", ACCOUNT_CODE)
        youboraConfigJson.addProperty("username", UNIQUE_USER_NAME)
        youboraConfigJson.addProperty("userObfuscateIp", true)
        youboraConfigJson.addProperty("httpSecure", true)

//        // Backward compatibility, Use user Json instead
//        youboraConfigJson.addProperty("userEmail", USER_EMAIL)
//        youboraConfigJson.addProperty("userAnonymousId", "my anonymousId")
//        youboraConfigJson.addProperty("userType", "my user type")
//        youboraConfigJson.addProperty("userObfuscateIp", false)

        val userJson = JsonObject()
        userJson.addProperty("email", USER_EMAIL)
        userJson.addProperty("anonymousId", "my anonymousId")
        userJson.addProperty("type", "my user type")
        userJson.addProperty("obfuscateIp", "My ObfuscateIp")

        // Youbora ads configuration json.
        val adsJson = JsonObject()
        adsJson.addProperty("blockerDetected", false)
        adsJson.addProperty("campaign", "zzz")
        // Create AdMetaData
        val adMetaData = JsonObject()
        adMetaData.addProperty("year", "2022")
        adMetaData.addProperty("cast", "cast 2022")
        adMetaData.addProperty("director", "director 2022")
        adMetaData.addProperty("owner", "owner 2022")
        adMetaData.addProperty("parental", "parental 2022")
        adMetaData.addProperty("rating", "rating 2022")
        adMetaData.addProperty("device", "device 2022")
        adMetaData.addProperty("audioChannels", "audioChannels 2022")
        adsJson.add("metadata", adMetaData)
        adsJson.addProperty("campaign", CAMPAIGN)
        adsJson.addProperty("title", "ad title 2022")
        adsJson.addProperty("resource", "resource 2022")
        adsJson.addProperty("givenBreaks", 5)
        adsJson.addProperty("expectedBreaks", 4)
        // Create expectedPattern for Ads
        val expectedPatternJson = JsonObject()
        val preRoll = JsonArray()
        preRoll.add(2)
        val midRoll = JsonArray()
        midRoll.add(1)
        midRoll.add(4)
        val postRoll = JsonArray()
        postRoll.add(3)
        expectedPatternJson.add("pre", preRoll)
        expectedPatternJson.add("mid", midRoll)
        expectedPatternJson.add("post", postRoll)
        adsJson.add("expectedPattern", expectedPatternJson)
        // create adBreaksTime
        val adBreaksTimeArray = JsonArray()
        adBreaksTimeArray.add(0)
        adBreaksTimeArray.add(25)
        adBreaksTimeArray.add(60)
        adBreaksTimeArray.add(75)
        adsJson.add("adBreaksTime", adBreaksTimeArray)
        adsJson.addProperty("adGivenAds", 7)
        adsJson.addProperty("adCreativeId", "ad creativeId")
        adsJson.addProperty("adProvider", "ad provider")
        // Create Ad Custom Dimensions
        val adCustomDimensions = JsonObject()
        adCustomDimensions.addProperty("param1", "my adCustomDimension1")
        adCustomDimensions.addProperty("10", "my adCustomDimension10")
        adsJson.add("customDimension", adCustomDimensions)

        // Media entry json. [Content JSON]
        val mediaEntryJson = JsonObject()
        mediaEntryJson.addProperty("title", MEDIA_TITLE)
        mediaEntryJson.addProperty("contentIsLiveNoSeek", true)
        mediaEntryJson.addProperty("contentCdnCode", CONTENT_CDN_CODE)
        mediaEntryJson.addProperty("contentGenre", GENRE)
        mediaEntryJson.addProperty("contentEpisodeTitle", "xxxxxxx")
        mediaEntryJson.addProperty("contentPrice", PRICE)
        mediaEntryJson.addProperty("contentTransactionCode", TRANSACTION_TYPE)
        mediaEntryJson.addProperty("contentProgram", PROGRAM)
        mediaEntryJson.addProperty("isLive", IS_LIVE_MEDIA)
        mediaEntryJson.addProperty("contentBitrate", 480000)
        // Encoding JSON
        val encodingJson = JsonObject()
        encodingJson.addProperty("videoCodec", "video codec name")
        mediaEntryJson.add("contentEncodingCodecSettings", encodingJson)
        // Create Content MetaData
        val contentMetaData = JsonObject()
        contentMetaData.addProperty("year", "2022")
        contentMetaData.addProperty("cast", "cast 2022")
        contentMetaData.addProperty("director", "director 2022")
        contentMetaData.addProperty("owner", "owner 2022")
        contentMetaData.addProperty("parental", "parental 2022")
        contentMetaData.addProperty("rating", "rating 2022")
        contentMetaData.addProperty("device", "device 2022")
        contentMetaData.addProperty("audioChannels", "audioChannels 2022")
        mediaEntryJson.add("metadata", contentMetaData)
        // Create Content Custom Dimensions
        val contentCustomDimensions = JsonObject()
        contentCustomDimensions.addProperty("param1", "param1")
        contentCustomDimensions.addProperty("param2", "param2")
        mediaEntryJson.add("customDimensions", contentCustomDimensions)

        // Optional - Create Parse JSON object
        val parseJson = JsonObject()
        parseJson.addProperty("parseManifest", PARSE_MANIFEST)
        parseJson.addProperty("parseCdnNode", PARSE_CDN_NODE)
        parseJson.addProperty("parseCdnSwitchHeader", PARSE_CDN_SWITCH_HEADER)
        val parseCdnNodeListJsonArray = JsonArray()
        for(cdn in PARSE_CDN_NODE_LIST) {
            parseCdnNodeListJsonArray.add(cdn)
        }
        parseJson.add("cdnNodeList", parseCdnNodeListJsonArray)
        parseJson.addProperty("cdnNameHeaders", PARSE_CDN_NAME_HEADERS)
        parseJson.addProperty("parseNodeHeader", PARSE_CDN_NODE_HEADERS)
        parseJson.addProperty("parseCdnTTL", PARSE_CDN_TTL)

        // Optional - Device json o/w youbora will decide by its own.
        val deviceJson = JsonObject()
        deviceJson.addProperty("deviceCode", DEVICE_CODE)
        deviceJson.addProperty("deviceBrand", "Brand Xiaomi")
        deviceJson.addProperty("deviceCode", "Code Xiaomi")
        deviceJson.addProperty("deviceId", "Device ID Xiaomi")
        deviceJson.addProperty("deviceEdId", "EdId Xiaomi")
        deviceJson.addProperty("deviceModel", "Model MI3")
        deviceJson.addProperty("deviceOsName", "Android/Oreo")
        deviceJson.addProperty("deviceOsVersion", "8.1")
        deviceJson.addProperty("deviceType", "TvBox TYPE")
        deviceJson.addProperty("deviceName", "TvBox")
        deviceJson.addProperty("deviceIsAnonymous", "TvBox")

        // Optional: Create Network JSON object
        val networkJson = JsonObject()
        networkJson.addProperty("networkConnectionType", "Wireless")
        networkJson.addProperty("networkIP", "1.1.1.1")
        networkJson.addProperty("networkIsp", "Country ISP")

        // App JSON
        val appJson = JsonObject()
        appJson.addProperty("appName", "MyTestApp")
        appJson.addProperty("appReleaseVersion", "1.0.1")

        // Errors JSON
        val errorsJson = JsonObject()
        val errorJsonArray = JsonArray()
        errorJsonArray.add("exception1")
        errorJsonArray.add("exception2")
        errorsJson.add("errorsIgnore", errorJsonArray)

        // SessionMetrics JSON
        val sessionJson = JsonObject()
        sessionJson.addProperty("metricsKey", "metricsValue")

//        // Configure custom properties here, Backward Compatible
//        // Properties are actually metadata which are moved to Ad and Content respectively
//        val propertiesJson = JsonObject()
//        propertiesJson.addProperty("year", YEAR)
//        propertiesJson.addProperty("cast", CAST)
//        propertiesJson.addProperty("director", DIRECTOR)
//        propertiesJson.addProperty("owner", OWNER)
//        propertiesJson.addProperty("parental", PARENTAL)
//
//        propertiesJson.addProperty("rating", RATING)
//        propertiesJson.addProperty("audioType", AUDIO_TYPE)
//        propertiesJson.addProperty("audioChannels", AUDIO_CHANNELS)
//        propertiesJson.addProperty("device", DEVICE)
//        propertiesJson.addProperty("quality", QUALITY)
//
//
//        // Backward Compatible.
//        // These are custom dimensions which are moved to Ads and Content respectively
//        val extraParamJson = JsonObject()
//        extraParamJson.addProperty("param1", EXTRA_PARAM_1)
//        extraParamJson.addProperty("param2", EXTRA_PARAM_2)

        // Add all the json objects created before to the pluginEntry json.
        youboraConfigJson.add("media", mediaEntryJson)
        youboraConfigJson.add("app", appJson)
        youboraConfigJson.add("parse", parseJson)
        youboraConfigJson.add("network", networkJson)
        youboraConfigJson.add("device", deviceJson)
        youboraConfigJson.add("errors", errorsJson)
        youboraConfigJson.add("ads", adsJson)
        youboraConfigJson.add("sessionMetrics", sessionJson)
//        youboraConfigJson.add("properties", propertiesJson)
//        youboraConfigJson.add("extraParams", extraParamJson)

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
        optBundle.putString(KEY_USER_EMAIL, USER_EMAIL)

        optBundle.putBoolean(KEY_ENABLED, true)
        optBundle.putString(KEY_APP_NAME, "TestApp");
        optBundle.putString(KEY_APP_RELEASE_VERSION, "v1.0");

        //Media entry bundle.
        optBundle.putString(KEY_CONTENT_TITLE, MEDIA_TITLE)

        optBundle.putBoolean(KEY_PARSE_MANIFEST, PARSE_MANIFEST)
        optBundle.putBoolean(KEY_PARSE_CDN_NODE, PARSE_CDN_NODE)
        optBundle.putBoolean(KEY_PARSE_CDN_SWITCH_HEADER, PARSE_CDN_SWITCH_HEADER)
        optBundle.putStringArrayList(KEY_PARSE_CDN_NODE_LIST, PARSE_CDN_NODE_LIST)
        optBundle.putString(KEY_PARSE_CDN_NAME_HEADER, PARSE_CDN_NAME_HEADERS)
        optBundle.putInt(KEY_PARSE_CDN_TTL, PARSE_CDN_TTL)


        //Optional - Device bundle o/w youbora will decide by its own.
        optBundle.putString(KEY_DEVICE_CODE, DEVICE_CODE)
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
        optBundle.putString(KEY_CONTENT_CDN, CONTENT_CDN_CODE)

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
