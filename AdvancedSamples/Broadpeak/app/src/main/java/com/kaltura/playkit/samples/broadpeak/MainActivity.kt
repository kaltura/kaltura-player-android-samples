package com.kaltura.playkit.samples.broadpeak

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.*
import com.kaltura.playkit.plugins.broadpeak.BroadpeakConfig
import com.kaltura.playkit.plugins.broadpeak.BroadpeakEvent
import com.kaltura.playkit.plugins.broadpeak.BroadpeakPlugin
import com.kaltura.playkit.plugins.youbora.YouboraPlugin
import com.kaltura.playkit.plugins.youbora.pluginconfig.YouboraConfig
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.playkit.samples.broadpeak.databinding.ActivityMainBinding
import com.kaltura.tvplayer.*
import com.npaw.youbora.lib6.plugin.Options
import tv.broadpeak.smartlib.session.streaming.StreamingSessionOptions

class MainActivity : AppCompatActivity() {

    //Tag for logging.
    private val TAG = MainActivity::class.java.simpleName

    private val START_POSITION = -1L // position for start playback in seconds.
    private var currentMediaId = ""

    //Youbora analytics Constants
    val ACCOUNT_CODE = "accountocde"
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

//    companion object {
//        const val SERVER_URL = "phoenixUrl"
//        const val FIRST_ASSET_ID = "assetId-1"
//        const val SECOND_ASSET_ID = "assetId-2"
//        const val PARTNER_ID = 11111111
//        const val KS = ""
//        const val MEDIA_FORMAT = "FORMAT"
//    }

    private var player: KalturaPlayer? = null
    private var isFullScreen: Boolean = false
    private var playerState: PlayerState? = null
    private var currentlyPlayingAsset: String? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)
        loadPlaykitPlayer()

        binding.root.setOnClickListener {
            if (isFullScreen) {
                showSystemUI()
            } else {
                hideSystemUI()
            }
        }

        binding.changeMediaButton.setOnClickListener {
            currentlyPlayingAsset?.let {
                player?.updatePluginConfig(YouboraPlugin.factory.name, getYouboraBundle(it, null))
                if (it == ConfigurationProvider.getAssetId()) {
                    loadSecondOttMedia()
                } else {
                    loadFirstOttMedia()
                }
            }
        }
    }

    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar

                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar

                or View.SYSTEM_UI_FLAG_IMMERSIVE)
        isFullScreen = true
    }

    private fun showSystemUI() {
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        isFullScreen = false
    }

    /**
     * Just add a simple button which will start/pause playback.
     */
    private fun addPlayPauseButton() {
        binding.playPauseButton.setOnClickListener {
            if (player!!.isPlaying) {
                //If player is playing, change text of the button and pause.
                binding.playPauseButton.setText(R.string.play_text)
                player?.pause()
            } else {
                //If player is not playing, change text of the button and play.
                binding.playPauseButton.setText(R.string.pause_text)
                player?.play()
            }
        }
    }

    private fun addPlayerStateListener() {
        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            Log.d(TAG, "State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    override fun onPause() {
        Log.d(TAG, "onPause")
        super.onPause()
        player?.let {
            binding.playPauseButton.setText(R.string.pause_text)
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
        Log.d(TAG, "onDestroy")
        super.onDestroy()
        player?.destroy()
    }

    fun loadPlaykitPlayer() {

        val playerInitOptions = PlayerInitOptions(ConfigurationProvider.getPartnerId())
        playerInitOptions.setAutoPlay(true)
        playerInitOptions.setPKRequestConfig(PKRequestConfig(true))

        // Broadpeak Configuration
        val pkPluginConfigs = PKPluginConfigs()
        val broadpeakConfig = BroadpeakConfig().apply {
            analyticsAddress = "https://analytics.kaltura.com/api_v3/index.php"
            nanoCDNHost = ""
            broadpeakDomainNames = "*"
            uuid = "" // app user - uuid
            deviceType = "Android"
            adCustomReference = "myCustomReference"
            nanoCDNResolvingRetryDelay = 60000
            customParameters = hashMapOf(Pair("customParamKey", "customParamValue"))
            adParameters = hashMapOf(Pair("customAdParamKey", "customAdParamValue"))

            val opt: HashMap<Int, Any> = HashMap()
            opt[StreamingSessionOptions.SESSION_KEEPALIVE_FREQUENCY] = 8000
            opt[StreamingSessionOptions.USERAGENT_AD_EVENT] = "kalturatest-useragent"
            opt[StreamingSessionOptions.ULTRA_LOW_LATENCY_SUPPORT] = false

            options = opt
        }

        pkPluginConfigs.setPluginConfig(BroadpeakPlugin.factory.name, broadpeakConfig)

        pkPluginConfigs.setPluginConfig(YouboraPlugin.factory.name, getYouboraBundle(ConfigurationProvider.getAssetId(), null))

        pkPluginConfigs.playerActivity = this

        playerInitOptions.setPluginConfigs(pkPluginConfigs)

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)

        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        player?.addListener(this, PlayerEvent.error) { event ->
            Log.i(TAG, "PLAYER ERROR " + event.error.message!!)
        }

        player?.addListener(this, PlayerEvent.playing) { event ->
            Log.i(TAG, "PLAYING EVENT")
        }

        player?.addListener(this, BroadpeakEvent.error) { event ->
            Log.i(TAG, "BROADPEAK ERROR " + event.errorMessage)
        }
        player?.addListener(this, InterceptorEvent.sourceUrlSwitched) { event ->
            Log.d(TAG, "BROADPEAK SOURCE URL SWITCHED: " + event.originalUrl + " to " + event.updatedUrl)
            player?.updatePluginConfig(YouboraPlugin.factory.name, getYouboraBundle(currentMediaId, event.originalUrl ?: "unknown"))
        }
        player?.addListener(this, PlayerEvent.simidAdBegin) { event ->
            Log.i(TAG, "SimidAdBegin")
        }
        player?.addListener(this, PlayerEvent.simidAdEnd) { event ->
            Log.i(TAG, "SimidAdEnd")
        }

        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player?.playerView)

        loadFirstOttMedia()

        //Add simple play/pause button.
        addPlayPauseButton()

        showSystemUI()

        addPlayerStateListener()
    }

    private fun getYouboraBundle(mediaId : String?, originalUrl: String?): Bundle {

        val optBundle = Bundle()

        //Youbora config bundle. Main config goes here.
        optBundle.putString(Options.KEY_ACCOUNT_CODE, ACCOUNT_CODE)
        optBundle.putString(Options.KEY_USERNAME, UNIQUE_USER_NAME)
        optBundle.putString(Options.KEY_USER_EMAIL, USER_EMAIL)

        optBundle.putBoolean(Options.KEY_ENABLED, true)
        optBundle.putString(Options.KEY_APP_NAME, "TestApp");
        optBundle.putString(Options.KEY_APP_RELEASE_VERSION, "v1.0");

        //Media entry bundle.
        optBundle.putString(Options.KEY_CONTENT_TITLE, MEDIA_TITLE)
       // optBundle.putBoolean(Options.KEY_PARSE_MANIFEST, true);
       // optBundle.putBoolean(Options.KEY_PARSE_CDN_NODE, true);

       // optBundle.putBoolean(Options.KEY_PARSE_MANIFEST, PARSE_MANIFEST)
       // optBundle.putBoolean(Options.KEY_PARSE_CDN_NODE, PARSE_CDN_NODE)
        optBundle.putBoolean(Options.KEY_PARSE_CDN_SWITCH_HEADER, PARSE_CDN_SWITCH_HEADER)
        optBundle.putStringArrayList(Options.KEY_PARSE_CDN_NODE_LIST, PARSE_CDN_NODE_LIST)
        optBundle.putString(Options.KEY_PARSE_CDN_NAME_HEADER, PARSE_CDN_NAME_HEADERS)
        optBundle.putInt(Options.KEY_PARSE_CDN_TTL, PARSE_CDN_TTL)


        //Optional - Device bundle o/w youbora will decide by its own.
        optBundle.putString(Options.KEY_DEVICE_CODE, DEVICE_CODE)
        optBundle.putString(Options.KEY_DEVICE_BRAND, "Xiaomi")
        optBundle.putString(Options.KEY_DEVICE_MODEL, "Mii3")
        optBundle.putString(Options.KEY_DEVICE_TYPE, "TvBox")
        optBundle.putString(Options.KEY_DEVICE_OS_NAME, "Android/Oreo")
        optBundle.putString(Options.KEY_DEVICE_OS_VERSION, "8.1")

        //Youbora ads configuration bundle.
        optBundle.putString(Options.KEY_AD_CAMPAIGN, CAMPAIGN)

        //Configure custom properties here:
        optBundle.putString(Options.KEY_CONTENT_GENRE, GENRE)
        optBundle.putString(Options.KEY_CONTENT_TYPE, TYPE)
        optBundle.putString(Options.KEY_CONTENT_TRANSACTION_CODE, TRANSACTION_TYPE)
        optBundle.putString(Options.KEY_CONTENT_CDN, CONTENT_CDN_CODE)

        optBundle.putString(Options.KEY_CONTENT_PRICE, PRICE)
        optBundle.putString(Options.KEY_CONTENT_ENCODING_AUDIO_CODEC, AUDIO_TYPE)
        optBundle.putString(Options.KEY_CONTENT_CHANNEL, AUDIO_CHANNELS)

        val contentMetadataBundle = Bundle()

        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_YEAR, YEAR)
        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_CAST, CAST)
        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_DIRECTOR, DIRECTOR)
        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_OWNER, OWNER)
        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_PARENTAL, PARENTAL)
        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_RATING, RATING)
        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_QUALITY, QUALITY)

        optBundle.putBundle(Options.KEY_CONTENT_METADATA, contentMetadataBundle)

        //You can add some extra params here:
        optBundle.putString(Options.KEY_CUSTOM_DIMENSION_1, EXTRA_PARAM_1)
        optBundle.putString(Options.KEY_CUSTOM_DIMENSION_2, EXTRA_PARAM_2 + "-" + mediaId)
        if (!TextUtils.isEmpty(originalUrl)) {
            optBundle.putString(Options.KEY_CUSTOM_DIMENSION_3, originalUrl)
        }
        return optBundle
    }

    private fun buildOttMediaOptions(assetId: String): OTTMediaOptions {
        val adapterData: MutableMap<String, String> = HashMap()

        adapterData["codec"] = "AVC"
        adapterData["quality"] = "UHD"
        adapterData["drm"] = "true"

        currentlyPlayingAsset = assetId
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = assetId
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.All
        ottMediaAsset.urlType = APIDefines.KalturaUrlType.Direct
        ottMediaAsset.streamerType = APIDefines.KalturaStreamerType.Mpegdash
        ottMediaAsset.ks = ConfigurationProvider.getKsToken()
        ConfigurationProvider.getFileFormatsString()?.let { fileFormats ->
            ottMediaAsset.formats = listOf(fileFormats)
        }
        ottMediaAsset.adapterData = adapterData
        return OTTMediaOptions(ottMediaAsset)
    }

    private fun loadFirstOttMedia() {
        currentMediaId = ConfigurationProvider.getAssetId()
        val ottMediaOptions = buildOttMediaOptions(currentMediaId)
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
        currentMediaId = ConfigurationProvider.getAssetId()
        val ottMediaOptions = buildOttMediaOptions(currentMediaId)
        ottMediaOptions.startPosition = START_POSITION
        player?.loadMedia(ottMediaOptions) { mediaOptions, entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_SHORT).show()
            } else {
                Log.i(TAG, "OTTMedia onEntryLoadComplete entry = " + entry.id)
            }
        }
    }
}
