package com.kaltura.playkitdemo

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.SensorManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.ads.interactivemedia.v3.api.StreamRequest
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.android.gms.security.ProviderInstaller
import com.google.android.material.snackbar.Snackbar
import com.google.gson.JsonObject
import com.kaltura.playkit.*
import com.kaltura.playkit.player.*
import com.kaltura.playkit.plugins.ads.AdCuePoints
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ima.IMAConfig
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.playkit.plugins.imadai.IMADAIConfig
import com.kaltura.playkit.plugins.imadai.IMADAIPlugin
import com.kaltura.playkit.plugins.kava.KavaAnalyticsConfig
import com.kaltura.playkit.plugins.kava.KavaAnalyticsPlugin
import com.kaltura.playkit.plugins.ott.OttEvent
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsConfig
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsEvent
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsPlugin
import com.kaltura.playkit.plugins.youbora.YouboraPlugin
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.playkit.providers.ovp.OVPMediaAsset
import com.kaltura.playkit.utils.Consts
import com.kaltura.playkitdemo.PartnersConfig.OVP_ENTRY_ID_CLEAR
import com.kaltura.playkitdemo.PartnersConfig.OVP_ENTRY_ID_DRM
import com.kaltura.playkitdemo.PartnersConfig.OVP_ENTRY_ID_HLS
import com.kaltura.playkitdemo.PartnersConfig.OVP_ENTRY_ID_LIVE
import com.kaltura.playkitdemo.PartnersConfig.OVP_ENTRY_ID_LIVE_1
import com.kaltura.playkitdemo.PartnersConfig.OVP_ENTRY_ID_VR
import com.kaltura.playkitdemo.PartnersConfig.OVP_FIRST_ENTRY_ID
import com.kaltura.playkitdemo.PartnersConfig.OVP_SECOND_ENTRY_ID
import com.kaltura.playkitdemo.PartnersConfig.SING_198_MEDIA_ID
import com.kaltura.playkitdemo.PartnersConfig.inLinePreAdTagUrl
import com.kaltura.playkitdemo.PartnersConfig.preMidPostAdTagUrl
import com.kaltura.playkitdemo.PartnersConfig.preMidPostSingleAdTagUrl
import com.kaltura.playkitdemo.PartnersConfig.preSkipAdTagUrl
import com.kaltura.tvplayer.*
import com.kaltura.tvplayer.config.PhoenixTVPlayerParams
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

//import com.kaltura.playkitvr.VRUtil;


class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener, OrientationManager.OrientationListener {

    private var player: KalturaPlayer? = null
    private var nowPlaying: Boolean = false
    private var isFullScreen: Boolean = false
    private var controlsView: PlaybackControlsView? = null

    private lateinit var progressBar: ProgressBar
    private lateinit var playerContainer: RelativeLayout
    private lateinit var spinerContainer: RelativeLayout
    private lateinit var fullScreenBtn: AppCompatImageView
    private lateinit var videoSpinner: Spinner
    private lateinit var audioSpinner: Spinner
    private lateinit var textSpinner: Spinner
    private lateinit var mOrientationManager: OrientationManager

    private var adCuePoints: AdCuePoints? = null
    private var userIsInteracting: Boolean = false
    private var tracksInfo: PKTracks? = null
    private var playerState: PlayerState? = null
    private var playerInitOptions: PlayerInitOptions? = null
    private var changeMediaIndex = -1
    private var START_POSITION: Long? = 0L//65L

    private val log = PKLog.get("MainActivity")
    private val isAdsEnabled = true
    private val isDAIMode = false

    private val IMA_PLUGIN = "IMA"
    private val DAI_PLUGIN = "DAI"
    private var READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST = 123

    private val daiConfig6: IMADAIConfig
        get() {
            val assetTitle = "ERROR"
            val assetKey: String? = null
            val apiKey: String? = null
            val contentSourceId = "19823"
            val videoId = "ima-test"
            val streamFormat = StreamRequest.StreamFormat.HLS
            val licenseUrl: String? = null
            return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                    contentSourceId + "AAAA",
                    videoId,
                    apiKey,
                    streamFormat,
                    licenseUrl).enableDebugMode(true)
        }

    private val daiConfig5: IMADAIConfig
        get() {
            val assetTitle = "VOD - Google I/O"
            val assetKey: String? = null
            val apiKey: String? = null
            val contentSourceId = "2477953"
            val videoId = "googleio-highlights"
            val streamFormat = StreamRequest.StreamFormat.HLS
            val licenseUrl: String? = null
            return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                    contentSourceId,
                    videoId,
                    apiKey,
                    streamFormat,
                    licenseUrl).enableDebugMode(true)
        }

    private val daiConfig5_1: IMADAIConfig
        get() {
            val assetTitle = "AD5_1"
            val assetKey: String? = null
            val apiKey: String? = null
            val contentSourceId = "19823"
            val videoId = "ima-test"
            val streamFormat = StreamRequest.StreamFormat.HLS
            val licenseUrl: String? = null
            return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                    contentSourceId,
                    videoId,
                    apiKey,
                    streamFormat,
                    licenseUrl).enableDebugMode(true)
        }


    private val daiConfig4: IMADAIConfig
        get() {
            val assetTitle = "AD4"
            val apiKey: String? = null
            val contentSourceId = "2472176"
            val videoId = "2504847"
            val streamFormat = StreamRequest.StreamFormat.HLS
            val licenseUrl: String? = null
            return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                    contentSourceId,
                    videoId,
                    apiKey,
                    streamFormat,
                    licenseUrl)
        }

    private val daiConfig3: IMADAIConfig
        get() {
            val assetTitle = "BBB-widevine"
            val apiKey: String? = null
            val contentSourceId = "2474148"
            val videoId = "bbb-widevine"
            val streamFormat = StreamRequest.StreamFormat.DASH
            val licenseUrl = "https://proxy.uat.widevine.com/proxy"
            return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                    contentSourceId,
                    videoId,
                    apiKey,
                    streamFormat,
                    licenseUrl).enableDebugMode(true)
        }

    private val daiConfig2: IMADAIConfig
        get() {
            val assetTitle = "Live Video - Big Buck Bunny"
            val assetKey = "sN_IYUG8STe1ZzhIIE_ksA"
            val apiKey: String? = null
            val streamFormat = StreamRequest.StreamFormat.HLS
            val licenseUrl: String? = null
            return IMADAIConfig.getLiveIMADAIConfig(assetTitle,
                    assetKey,
                    apiKey,
                    streamFormat,
                    licenseUrl).setAlwaysStartWithPreroll(true).enableDebugMode(true)
        }

    private val daiConfig1: IMADAIConfig
        get() {
            val assetTitle = "VOD - Tears of Steel"
            val apiKey: String? = null
            val contentSourceId = "2477953"
            val videoId = "tears-of-steel"
            val streamFormat = StreamRequest.StreamFormat.HLS
            val licenseUrl: String? = null

            return IMADAIConfig.getVodIMADAIConfig(assetTitle,
                    contentSourceId,
                    videoId,
                    apiKey,
                    streamFormat,
                    licenseUrl).enableDebugMode(true).setAlwaysStartWithPreroll(true)
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //getPermissionToReadExternalStorage();
        initDrm()
        //PlayKitProfiler.init(this);
        try {
            ProviderInstaller.installIfNeeded(this)
        } catch (e: GooglePlayServicesRepairableException) {
            e.printStackTrace()
        } catch (e: GooglePlayServicesNotAvailableException) {
            e.printStackTrace()
        }

        mOrientationManager = OrientationManager(this, SensorManager.SENSOR_DELAY_NORMAL, this)
        mOrientationManager.enable()
        setContentView(R.layout.activity_main)

        log.i("PlayKitManager: " + PlayKitManager.CLIENT_TAG)

        registerChangeMedia()
        initProgressBar()
        controlsView = findViewById(R.id.playerControls)
        playerContainer = findViewById(R.id.player_container)
        initSpinners()
        registerFullScreenButton()

        var pkPluginConfigs = configurePlugins()
        //EXAMPLE FOR OVERRIDNG KAVA DEFAULT PLUGIN CONFIG
        pkPluginConfigs.setPluginConfig(KavaAnalyticsPlugin.factory.name, getKavaAnalyticsConfig(112233))



        // Basic Playkit Player
        //loadBasicKalturaPlayer(pkPluginConfigs);

        // OTT Playkit Player
        //loadKalturaPlayer(OTT_PARTNER_ID, KalturaPlayer.Type.ott, pkPluginConfigs);
        //loadKalturaPlayer(225, KalturaPlayer.Type.ott, pkPluginConfigs);
        loadKalturaPlayer(198, KalturaPlayer.Type.ott, pkPluginConfigs)


        // OVP Playkit Player
        //loadKalturaPlayer(OVP_PARTNER_ID_HLS, KalturaPlayer.Type.ovp, pkPluginConfigs);
    }

    private fun getKavaAnalyticsConfig(partnerId: Int): KavaAnalyticsConfig {
        return KavaAnalyticsConfig()
                .setApplicationVersion(BuildConfig.VERSION_NAME)
                .setPartnerId(partnerId)
                .setUserId("aaa@gmail.com")
                .setCustomVar1("Test1")
                .setApplicationVersion("Test123")
    }

    /**
     * Load KalturaPlayer only for OVP and OTT provider ( User loadBasicKalturaPlayer() method to use the Basic
     * KalturaPlayer preparation )
     *
     * @param mediaPartnerId Partner ID for OVP or OTT provider
     * @param playerType OVP or OTT < KalturaPlayer.Type >
     * @param pkPluginConfigs Plugin configs (Configurations like IMA Ads, Youbora etc)
     * for Kaltura Player, it is being passed in playerInitOptions
     */

    fun loadKalturaPlayer(mediaPartnerId: Int?, playerType: KalturaPlayer.Type, pkPluginConfigs: PKPluginConfigs) {

        playerInitOptions = PlayerInitOptions(mediaPartnerId)
        playerInitOptions?.setAutoPlay(true)
        playerInitOptions?.setPreload(true)
        playerInitOptions?.setSecureSurface(false)
        playerInitOptions?.setAdAutoPlayOnResume(true)
        playerInitOptions?.setAllowCrossProtocolEnabled(true)
        playerInitOptions?.setReferrer("app://MyApplicationDomain")
        // playerInitOptions.setLoadControlBuffers(new LoadControlBuffers());

        playerInitOptions?.setPluginConfigs(pkPluginConfigs)

        if (playerType == KalturaPlayer.Type.ott) {
            if (mediaPartnerId == 225 || mediaPartnerId == 198) {
                val phoenixTVPlayerParams = PhoenixTVPlayerParams()
                phoenixTVPlayerParams.analyticsUrl = "https://analytics.kaltura.com"
                phoenixTVPlayerParams.ovpPartnerId = 1774581
                phoenixTVPlayerParams.partnerId = mediaPartnerId
                if (mediaPartnerId == 225) {
                    phoenixTVPlayerParams.serviceUrl = "https://rest-as.ott.kaltura.com/v5_0_3/"
                } else {
                    phoenixTVPlayerParams.serviceUrl = "https://api-preprod.ott.kaltura.com/v5_2_8/"
                }
                phoenixTVPlayerParams.ovpServiceUrl = "http://cdnapi.kaltura.com/"
                playerInitOptions?.tvPlayerParams = phoenixTVPlayerParams
            }
            player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)
        } else if (playerType == KalturaPlayer.Type.ovp) {
            player = KalturaOvpPlayer.create(this@MainActivity, playerInitOptions)
        } else {
            log.e("Wrong player type is passed. Please check the loadOvpOttPlaykitPlayer method")
        }

        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        val container = findViewById<ViewGroup>(R.id.player_view)
        container.addView(player?.playerView)

        controlsView?.setPlayer(player)

        addPlayerListeners(progressBar)

        //------------ OVP/OTT Mock Methods -----------//

        if (playerType == KalturaPlayer.Type.ovp) {
            startSimpleOvpMediaLoadingHls()
            //startSimpleOvpMediaLoadingDRM();
            //startSimpleOvpMediaLoadingVR();
            //startSimpleOvpMediaLoadingClear();
            //startSimpleOvpMediaLoadingLive();
            //startSimpleOvpMediaLoadingLive1();
            //startOvpChangeMediaLoading(OVP_FIRST_ENTRY_ID, null);
        } else if (playerType == KalturaPlayer.Type.ott) {
            //startOttMediaLoading(OTT_ASSET_ID, null, PhoenixMediaProvider.HttpProtocol.Http, "Mobile_Main"); //3009
            startOttMediaLoading(SING_198_MEDIA_ID, null, PhoenixMediaProvider.HttpProtocol.Https, "Mobile_Devices_Main_HD_Dash") // 198

        } else if (playerType == KalturaPlayer.Type.basic) {
            // no media loading for basic
        }


        //------------ OVP/OTT Mock Methods -----------//
    }

    /**
     * Load Basic KalturaPlayer by the legendary way using PKMediaEntry and PKPluginConfigs
     * @param pkMediaEntry MediaEntry
     * @param pkPluginConfigs  Configurations like IMA Ads, Youbora etc
     */
    fun loadBasicKalturaPlayer(pkMediaEntry: PKMediaEntry, pkPluginConfigs: PKPluginConfigs) {
        playerInitOptions = PlayerInitOptions()

        playerInitOptions?.setPluginConfigs(pkPluginConfigs)

        player = KalturaBasicPlayer.create(this@MainActivity, playerInitOptions)
        player?.setMedia(pkMediaEntry, START_POSITION)
        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        val container = findViewById<ViewGroup>(R.id.player_view)
        container.addView(player?.playerView)

        controlsView?.setPlayer(player)

        addPlayerListeners(progressBar)
    }


    private fun buildOttMediaOptions(assetId: String, ks: String?, protocol: String, format: String?) {
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = assetId
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = protocol //PhoenixMediaProvider.HttpProtocol.Http/s
        ottMediaAsset.ks = ks

        if (format != null) {
            ottMediaAsset.formats = listOf(format)
        }
        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = START_POSITION


        player?.loadMedia(ottMediaOptions) { entry, error ->
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }
    }

    private fun buildOvpMediaOptions(entryId: String, ks: String?) {
        val ovpMediaAsset = OVPMediaAsset()
        ovpMediaAsset.entryId = entryId
        ovpMediaAsset.ks = ks
        val ovpMediaOptions = OVPMediaOptions(ovpMediaAsset)

        ovpMediaOptions.startPosition = START_POSITION

        player?.loadMedia(ovpMediaOptions) { entry, error ->
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OVPMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }
    }

    private fun initProgressBar() {
        progressBar = findViewById(R.id.progressBar)
        progressBar.visibility = View.INVISIBLE
    }

    private fun registerFullScreenButton() {
        fullScreenBtn = findViewById(R.id.full_screen_switcher)
        fullScreenBtn.setOnClickListener { v ->
            val orient: Int
            if (isFullScreen) {
                orient = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            } else {
                orient = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            }
            requestedOrientation = orient
        }
    }

    private fun registerChangeMedia() {
        val button = findViewById<Button>(R.id.changeMedia)
        button.setOnClickListener { v ->
            if (player != null) {
                changeMediaIndex++
                changeMedia()
                if (changeMediaIndex % 4 == 0) {
                    startOvpChangeMediaLoading(OVP_SECOND_ENTRY_ID, null)
                } else if (changeMediaIndex % 4 == 1) {
                    startOvpChangeMediaLoading(OVP_FIRST_ENTRY_ID, null)
                }
                if (changeMediaIndex % 4 == 2) {
                    startOvpChangeMediaLoading(OVP_SECOND_ENTRY_ID, null)
                }
                if (changeMediaIndex % 4 == 3) {
                    startOvpChangeMediaLoading(OVP_FIRST_ENTRY_ID, null)
                }
            }
        }
    }

    private fun startOttMediaLoading(assetId: String, ks: String?, protocol: String, format: String) {
        buildOttMediaOptions(assetId, ks, protocol, format)
    }

    private fun loadBasicKalturaPlayer(pkPluginConfigs: PKPluginConfigs) {
        val pkMediaEntry = createMediaEntry()
        loadBasicKalturaPlayer(pkMediaEntry, pkPluginConfigs)
    }

    private fun startOvpChangeMediaLoading(assetId: String, ks: String?) {
        buildOvpMediaOptions(assetId, ks)
    }

    private fun startSimpleOvpMediaLoadingHls() {
        buildOvpMediaOptions(OVP_ENTRY_ID_HLS, null)
    }

    private fun startSimpleOvpMediaLoadingDRM() {
        buildOvpMediaOptions(OVP_ENTRY_ID_DRM, null)
    }

    private fun startSimpleOvpMediaLoadingVR() {
        buildOvpMediaOptions(OVP_ENTRY_ID_VR, null)
    }

    private fun startSimpleOvpMediaLoadingClear() {
        buildOvpMediaOptions(OVP_ENTRY_ID_CLEAR, null)
    }

    private fun startSimpleOvpMediaLoadingLive() {
        buildOvpMediaOptions(OVP_ENTRY_ID_LIVE, null)
    }

    private fun startSimpleOvpMediaLoadingLive1() {
        buildOvpMediaOptions(OVP_ENTRY_ID_LIVE_1, null)
    }

    private fun getPermissionToReadExternalStorage() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
            }
            ActivityCompat.requestPermissions(this@MainActivity, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST)
        }
    }

    // Callback with the request from calling requestPermissions(...)
    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        // Make sure it's our original READ_CONTACTS request
        if (requestCode == READ_EXTERNAL_STORAGE_PERMISSIONS_REQUEST) {
            if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Read Storage permission granted", Toast.LENGTH_SHORT).show()
            } else {
                val showRationale = ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                if (showRationale) {
                    // do something here to handle degraded mode
                } else {
                    Toast.makeText(this, "Read Storage permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
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

    private fun simpleMediaEntry(id: String, contentUrl: String, licenseUrl: String, scheme: PKDrmParams.Scheme): PKMediaEntry {
        return PKMediaEntry()
                .setSources(listOf(PKMediaSource()
                        .setUrl(contentUrl)
                        .setDrmData(listOf(PKDrmParams(licenseUrl, scheme))
                        )))
                .setId(id)
    }

    private fun simpleMediaEntry(id: String, contentUrl: String): PKMediaEntry {
        return PKMediaEntry()
                .setSources(listOf(PKMediaSource()
                        .setUrl(contentUrl)))
                .setId(id)
    }

    /**
     * Create [PKMediaEntry] with minimum necessary data.
     *
     * @return - the [PKMediaEntry] object.
     */
    private fun createMediaEntry(): PKMediaEntry {
        //Create media entry.
        val mediaEntry = PKMediaEntry()

        //Set id for the entry.
        mediaEntry.id = "testEntry"

        //Set media entry type. It could be Live,Vod or Unknown.
        //In this sample we use Vod.
        mediaEntry.mediaType = PKMediaEntry.MediaEntryType.Vod

        //Create list that contains at least 1 media source.
        //Each media entry can contain a couple of different media sources.
        //All of them represent the same content, the difference is in it format.
        //For example same entry can contain PKMediaSource with dash and another
        // PKMediaSource can be with hls. The player will decide by itself which source is
        // preferred for playback.
        val mediaSources = createMediaSources()

        //Set media sources to the entry.
        mediaEntry.sources = mediaSources

        return mediaEntry
    }

    /**
     * Create list of [PKMediaSource].
     *
     * @return - the list of sources.
     */
    private fun createMediaSources(): List<PKMediaSource> {

        //Create new PKMediaSource instance.
        val mediaSource = PKMediaSource()

        //Set the id.
        mediaSource.id = "testSource"

        //Set the content url. In our case it will be link to hls source(.m3u8).
        mediaSource.url = MockParams.BASIC_SOURCE_URL

        //Set the format of the source. In our case it will be hls in case of mpd/wvm formats you have to to call mediaSource.setDrmData method as well
        mediaSource.mediaFormat = MockParams.BASIC_MEDIA_FORMAT

        // Add DRM data if required
        if (MockParams.BASIC_LICENSE_URL != null) {
            mediaSource.drmData = listOf(PKDrmParams(MockParams.BASIC_LICENSE_URL, PKDrmParams.Scheme.WidevineCENC))
        }

        return listOf(mediaSource)
    }

    private fun changeMedia() {
        if (changeMediaIndex % 4 == 0) {
            if (isAdsEnabled) {
                if (isDAIMode) {
                    promptMessage(DAI_PLUGIN, daiConfig2.assetTitle)
                    player?.updatePluginConfig(IMADAIPlugin.factory.name, daiConfig2)
                } else {
                    log.d("Play Ad preMidPostAdTagUrl")
                    promptMessage(IMA_PLUGIN, "preMidPostAdTagUrl")
                    player?.updatePluginConfig(IMAPlugin.factory.name, getAdsConfig(preMidPostAdTagUrl))
                }
            }
            player?.updatePluginConfig(YouboraPlugin.factory.name, getYouboraJsonObject("preMidPostAdTagUrl media2"))
        } else if (changeMediaIndex % 4 == 1) {
            if (isAdsEnabled) {
                if (isDAIMode) {
                    promptMessage(DAI_PLUGIN, daiConfig3.assetTitle)
                    player?.updatePluginConfig(IMADAIPlugin.factory.name, daiConfig3)
                } else {
                    log.d("Play Ad inLinePreAdTagUrl")
                    promptMessage(IMA_PLUGIN, "inLinePreAdTagUrl")
                    player?.updatePluginConfig(IMAPlugin.factory.name, getAdsConfig(inLinePreAdTagUrl))
                }
            }
            player?.updatePluginConfig(YouboraPlugin.factory.name, getYouboraJsonObject("inLinePreAdTagUrl media3"))
        }
        if (changeMediaIndex % 4 == 2) {
            if (isAdsEnabled) {
                if (isDAIMode) {
                    promptMessage(DAI_PLUGIN, daiConfig4.assetTitle)
                    player?.updatePluginConfig(IMADAIPlugin.factory.name, daiConfig4)
                } else {
                    log.d("Play NO Ad")
                    promptMessage(IMA_PLUGIN, "Enpty AdTag")
                    player?.updatePluginConfig(IMAPlugin.factory.name, getAdsConfig(""))
                }
            }
            player?.updatePluginConfig(YouboraPlugin.factory.name, getYouboraJsonObject("NO AD media4"))
        }
        if (changeMediaIndex % 4 == 3) {
            if (isAdsEnabled) {
                if (isDAIMode) {
                    promptMessage(DAI_PLUGIN, daiConfig5.assetTitle)
                    player?.updatePluginConfig(IMADAIPlugin.factory.name, daiConfig5)
                } else {
                    log.d("Play Ad preSkipAdTagUrl")
                    promptMessage(IMA_PLUGIN, "preSkipAdTagUrl")
                    player?.updatePluginConfig(IMAPlugin.factory.name, getAdsConfig(preSkipAdTagUrl))
                }
            }

            //                player.setPlayerBuffers(new LoadControlBuffers().
            //                        setMinPlayerBufferMs(2500).
            //                        setMaxPlayerBufferMs(50000).setAllowedVideoJoiningTimeMs(4000));

            player?.updatePluginConfig(YouboraPlugin.factory.name, getYouboraJsonObject("preSkipAdTagUrl media1"))
        }
    }

    private fun initSpinners() {
        spinerContainer = findViewById(R.id.spiner_container)
        videoSpinner = this.findViewById(R.id.videoSpinner)
        audioSpinner = this.findViewById(R.id.audioSpinner)
        textSpinner = this.findViewById(R.id.subtitleSpinner)

        textSpinner.onItemSelectedListener = this
        audioSpinner.onItemSelectedListener = this
        videoSpinner.onItemSelectedListener = this
    }

    private fun configurePlugins(): PKPluginConfigs {

        val pluginConfig = PKPluginConfigs()
        if (isAdsEnabled) {
            if (isDAIMode) {
                addIMADAIPluginConfig(pluginConfig, 1)
            } else {
                addIMAPluginConfig(pluginConfig)
            }
        }
        //addKaluraStatsPluginConfig(pluginConfigs, 1734751, "1_3o1seqnv");
        addYouboraPluginConfig(pluginConfig, "preMidPostSingleAdTagUrl Title1")
        //addKavaPluginConfig(pluginConfigs, 1734751, "1_3o1seqnv");
        //addPhoenixAnalyticsPluginConfig(pluginConfigs);
        //addTVPAPIAnalyticsPluginConfig(pluginConfigs);

        return pluginConfig
    }

    private fun addYouboraPluginConfig(pluginConfigs: PKPluginConfigs, title: String) {
        val pluginEntry = getYouboraJsonObject(title)

        //Set plugin entry to the plugin configs.
        pluginConfigs.setPluginConfig(YouboraPlugin.factory.name, pluginEntry)
    }

    private fun getYouboraJsonObject(title: String): JsonObject {
        val pluginEntry = JsonObject()

        pluginEntry.addProperty("accountCode", "kalturatest")
        pluginEntry.addProperty("username", "a@a.com")
        pluginEntry.addProperty("haltOnError", true)
        pluginEntry.addProperty("enableAnalytics", true)
        pluginEntry.addProperty("enableSmartAds", true)


        //Optional - Device json o/w youbora will decide by its own.
        val deviceJson = JsonObject()
        deviceJson.addProperty("deviceCode", "AndroidTV")
        deviceJson.addProperty("brand", "Xiaomi")
        deviceJson.addProperty("model", "Mii3")
        deviceJson.addProperty("type", "TvBox")
        deviceJson.addProperty("osName", "Android/Oreo")
        deviceJson.addProperty("osVersion", "8.1")


        //Media entry json.
        val mediaEntryJson = JsonObject()
        //mediaEntryJson.addProperty("isLive", isLive);
        mediaEntryJson.addProperty("title", title)

        //Youbora ads configuration json.
        val adsJson = JsonObject()
        adsJson.addProperty("adsExpected", true)
        adsJson.addProperty("campaign", "zzz")

        //Configure custom properties here:
        val propertiesJson = JsonObject()
        propertiesJson.addProperty("genre", "")
        propertiesJson.addProperty("type", "")
        propertiesJson.addProperty("transaction_type", "")
        propertiesJson.addProperty("year", "")
        propertiesJson.addProperty("cast", "")
        propertiesJson.addProperty("director", "")
        propertiesJson.addProperty("owner", "")
        propertiesJson.addProperty("parental", "")
        propertiesJson.addProperty("price", "")
        propertiesJson.addProperty("rating", "")
        propertiesJson.addProperty("audioType", "")
        propertiesJson.addProperty("audioChannels", "")
        propertiesJson.addProperty("device", "")
        propertiesJson.addProperty("quality", "")

        //You can add some extra params here:
        val extraParamJson = JsonObject()
        extraParamJson.addProperty("param1", "param1")
        extraParamJson.addProperty("param2", "param2")

        //Add all the json objects created before to the pluginEntry json.
        pluginEntry.add("device", deviceJson)
        pluginEntry.add("media", mediaEntryJson)
        pluginEntry.add("ads", adsJson)
        pluginEntry.add("properties", propertiesJson)
        pluginEntry.add("extraParams", extraParamJson)
        return pluginEntry
    }

    private fun addPhoenixAnalyticsPluginConfig(config: PKPluginConfigs) {
        val ks = "djJ8MTk4fHFftqeAPxdlLVzZBk0Et03Vb8on1wLsKp7cbOwzNwfOvpgmOGnEI_KZDhRWTS-76jEY7pDONjKTvbWyIJb5RsP4NL4Ng5xuw6L__BeMfLGAktkVliaGNZq9SXF5n2cMYX-sqsXLSmWXF9XN89io7-k="
        val phoenixAnalyticsConfig = PhoenixAnalyticsConfig(198, "http://api-preprod.ott.kaltura.com/v5_2_8/api_v3/", ks, 30)
        config.setPluginConfig(PhoenixAnalyticsPlugin.factory.name, phoenixAnalyticsConfig)
    }

    private fun addIMAPluginConfig(config: PKPluginConfigs) {
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator=";
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/3274935/preroll&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&description_url=[description_url]&correlator=[timestamp]";
        //"https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostpod&cmsid=496&vid=short_onecue&correlator=";

        log.d("Play Ad preSkipAdTagUrl")
        promptMessage(IMA_PLUGIN, "preSkipAdTagUrl")
        val adsConfig = getAdsConfig(preMidPostSingleAdTagUrl)
        config.setPluginConfig(IMAPlugin.factory.name, adsConfig)
    }

    private fun getAdsConfig(adTagUrl: String): IMAConfig {
        val videoMimeTypes = ArrayList<String>()
        videoMimeTypes.add("video/mp4")
        videoMimeTypes.add("application/x-mpegURL")
        videoMimeTypes.add("application/dash+xml")
        return IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8)
    }


    private fun getAdsConfigResponse(adResponse: String): IMAConfig {
        val videoMimeTypes = ArrayList<String>()
        videoMimeTypes.add("video/mp4")
        videoMimeTypes.add("application/x-mpegURL")
        // videoMimeTypes.add("application/dash+xml");
        return IMAConfig().setAdTagResponse(adResponse).setVideoMimeTypes(videoMimeTypes).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8)
    }

    //IMA DAI CONFIG
    private fun addIMADAIPluginConfig(config: PKPluginConfigs, daiType: Int) {
        when (daiType) {
            1 -> {
                promptMessage(DAI_PLUGIN, daiConfig1.assetTitle)
                val adsConfig = daiConfig1
                config.setPluginConfig(IMADAIPlugin.factory.name, adsConfig)
            }
            2 -> {
                promptMessage(DAI_PLUGIN, daiConfig2.assetTitle)
                val adsConfigLive = daiConfig2
                config.setPluginConfig(IMADAIPlugin.factory.name, adsConfigLive)
            }
            3 -> {
                promptMessage(DAI_PLUGIN, daiConfig3.assetTitle)
                val adsConfigDash = daiConfig3
                config.setPluginConfig(IMADAIPlugin.factory.name, adsConfigDash)
            }
            4 -> {
                promptMessage(DAI_PLUGIN, daiConfig4.assetTitle)
                val adsConfigVod2 = daiConfig4
                config.setPluginConfig(IMADAIPlugin.factory.name, adsConfigVod2)
            }
            5 -> {
                promptMessage(DAI_PLUGIN, daiConfig5.assetTitle)
                val adsConfig5 = daiConfig5
                config.setPluginConfig(IMADAIPlugin.factory.name, adsConfig5)
            }
            6 -> {
                promptMessage(DAI_PLUGIN, daiConfig6.assetTitle)
                val adsConfigError = daiConfig6
                config.setPluginConfig(IMADAIPlugin.factory.name, adsConfigError)
            }
            else -> {
            }
        }
    }

    private fun promptMessage(type: String, title: String) {
        Toast.makeText(this, "$type $title", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        controlsView?.release()
        player?.onApplicationPaused()
    }

    public override fun onDestroy() {
        if (player != null) {
            player?.removeListeners(this)
            player?.destroy()
            player = null
        }
        super.onDestroy()
    }

    private fun addPlayerListeners(appProgressBar: ProgressBar) {

        player?.addListener(this, AdEvent.contentResumeRequested) { event ->
            log.d("CONTENT_RESUME_REQUESTED")
            appProgressBar.visibility = View.INVISIBLE
            controlsView?.setSeekBarStateForAd(false)
            controlsView?.setPlayerState(PlayerState.READY)
        }

        player?.addListener(this, AdEvent.daiSourceSelected) { event ->
            log.d("DAI_SOURCE_SELECTED: " + event.sourceURL)

        }

        player?.addListener(this, AdEvent.contentPauseRequested) { event ->
            log.d("AD_CONTENT_PAUSE_REQUESTED")
            appProgressBar.visibility = View.VISIBLE
            controlsView?.setSeekBarStateForAd(true)
            controlsView?.setPlayerState(PlayerState.READY)
        }

        player?.addListener(this, AdEvent.adPlaybackInfoUpdated) { event ->
            log.d("AD_PLAYBACK_INFO_UPDATED")
            log.d("playbackInfoUpdated  = " + event.width + "/" + event.height + "/" + event.bitrate)
        }

        player?.addListener(this, AdEvent.cuepointsChanged) { event ->
            adCuePoints = event.cuePoints
            log.d("Has Postroll = " + adCuePoints?.hasPostRoll())
        }

        player?.addListener(this, AdEvent.adBufferStart) { event ->
            log.d("AD_BUFFER_START pos = " + event.adPosition)
            appProgressBar.visibility = View.VISIBLE
        }

        player?.addListener(this, AdEvent.adBufferEnd) { event ->
            log.d("AD_BUFFER_END pos = " + event.adPosition)
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.adFirstPlay) { event ->
            log.d("AD_FIRST_PLAY")
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.started) { event ->
            log.d("AD_STARTED w/h - " + event.adInfo.getAdWidth() + "/" + event.adInfo.getAdHeight())
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.resumed) { event ->
            log.d("Ad Event AD_RESUMED")
            nowPlaying = true
            appProgressBar.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.playHeadChanged) { event ->
            appProgressBar.visibility = View.INVISIBLE
            //log.d("received AD PLAY_HEAD_CHANGED " + event.adPlayHead);
        }

        player?.addListener(this, AdEvent.allAdsCompleted) { event ->
            log.d("Ad Event AD_ALL_ADS_COMPLETED")
            appProgressBar.visibility = View.INVISIBLE
            adCuePoints?.let {
                it.hasPostRoll().let {
                    controlsView?.setPlayerState(PlayerState.IDLE)
                }
            }
        }

        player?.addListener(this, AdEvent.error) { event ->
            controlsView?.setSeekBarStateForAd(false)
            log.e("ERROR: " + event?.error?.errorType + ", " + event?.error?.message)
        }

        player?.addListener(this, AdEvent.skipped) { event ->
            log.d("Ad Event SKIPPED")
            nowPlaying = true
        }

        player?.addListener(this, PlayerEvent.surfaceAspectRationSizeModeChanged) { event -> log.d("resizeMode updated" + event.resizeMode) }

        /////// PLAYER EVENTS

        player?.addListener(this, PlayerEvent.play) { event ->
            log.d("Player Event PLAY")
            nowPlaying = true
        }

        player?.addListener(this, PlayerEvent.playing) { event ->
            log.d("Player Event PLAYING")
            appProgressBar.visibility = View.INVISIBLE
            nowPlaying = true
        }

        player?.addListener(this, PlayerEvent.pause) { event ->
            log.d("Player Event PAUSE")
            nowPlaying = false
        }

        player?.addListener(this, PlayerEvent.playbackRateChanged) { event -> log.d("playbackRateChanged event  rate = " + event.rate) }

        player?.addListener(this, PlayerEvent.tracksAvailable) { event ->
            //When the track data available, this event occurs. It brings the info object with it.
            tracksInfo = event.tracksInfo
            populateSpinnersWithTrackInfo(event.tracksInfo)
        }

        player?.addListener(this, PlayerEvent.playbackRateChanged) { event -> log.d("playbackRateChanged event  rate = " + event.rate) }

        player?.addListener(this, PlayerEvent.error) { event ->
            //When the track data available, this event occurs. It brings the info object with it.
            log.d("PlayerEvent.Error event  position = " + event?.error?.errorType + " errorMessage = " + event?.error?.message)
        }

        player?.addListener(this, PlayerEvent.ended) { event -> appProgressBar.visibility = View.INVISIBLE }

        player?.addListener(this, PlayerEvent.playheadUpdated) { event ->
            //When the track data available, this event occurs. It brings the info object with it.
            //log.d("playheadUpdated event  position = " + event.position + " duration = " + event.duration);
        }

        player?.addListener(this, PlayerEvent.videoFramesDropped) { event ->
            //log.d("VIDEO_FRAMES_DROPPED " + event.droppedVideoFrames);
        }

        player?.addListener(this, PlayerEvent.bytesLoaded) { event ->
            //log.d("BYTES_LOADED " + event.bytesLoaded);
        }

        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState

            if (event.newState == PlayerState.BUFFERING) {
                appProgressBar.visibility = View.VISIBLE
            }
            if ((event.oldState == PlayerState.LOADING || event.oldState == PlayerState.BUFFERING) && event.newState == PlayerState.READY) {
                appProgressBar.visibility = View.INVISIBLE

            }

            controlsView?.setPlayerState(event.newState)

        }

        /////Phoenix events

        player?.addListener(this, PhoenixAnalyticsEvent.bookmarkError) { event -> log.d("bookmarkErrorEvent errorCode = " + event.errorCode + " message = " + event.errorMessage) }

        player?.addListener(this, PhoenixAnalyticsEvent.concurrencyError) { event -> log.d("ConcurrencyErrorEvent errorCode = " + event.errorCode + " message = " + event.errorMessage) }

        player?.addListener(this, PhoenixAnalyticsEvent.error) { event -> log.d("Phoenox Analytics errorEvent errorCode = " + event.errorCode + " message = " + event.errorMessage) }

        player?.addListener(this, PhoenixAnalyticsEvent.error) { event -> log.d("Phoenox Analytics errorEvent errorCode = " + event.errorCode + " message = " + event.errorMessage) }

        player?.addListener(this, OttEvent.ottEvent) { event -> log.d("Concurrency event = " + event.type) }
    }

    override fun onResume() {
        log.d("Application onResume")
        super.onResume()

        if (player != null && playerState != null) {
            player?.onApplicationResumed()
        }

        controlsView?.resume()
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        setFullScreen(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE)
        super.onConfigurationChanged(newConfig)
        Log.v("orientation", "state = " + newConfig.orientation)
    }


    private fun setFullScreen(isFullScreen: Boolean) {
        val params = playerContainer.layoutParams as RelativeLayout.LayoutParams
        // Checks the orientation of the screen
        this.isFullScreen = isFullScreen
        if (isFullScreen) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            fullScreenBtn.setImageResource(R.drawable.ic_no_fullscreen)
            spinerContainer.visibility = View.GONE
            params.height = RelativeLayout.LayoutParams.MATCH_PARENT
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT

        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            fullScreenBtn.setImageResource(R.drawable.ic_fullscreen)
            spinerContainer.visibility = View.VISIBLE
            params.height = resources.getDimension(R.dimen.player_height).toInt()
            params.width = RelativeLayout.LayoutParams.MATCH_PARENT
        }
        playerContainer.requestLayout()
    }

    /**
     * populating spinners with track info.
     *
     * @param tracksInfo - the track info.
     */
    private fun populateSpinnersWithTrackInfo(tracksInfo: PKTracks) {

        //Retrieve info that describes available tracks.(video/audio/subtitle).
        val videoTrackItems = obtainRelevantTrackInfo(Consts.TRACK_TYPE_VIDEO, tracksInfo.videoTracks)
        //populate spinner with this info.

        applyAdapterOnSpinner(videoSpinner, videoTrackItems, tracksInfo.defaultVideoTrackIndex)

        val audioTrackItems = obtainRelevantTrackInfo(Consts.TRACK_TYPE_AUDIO, tracksInfo.audioTracks)
        applyAdapterOnSpinner(audioSpinner, audioTrackItems, tracksInfo.defaultAudioTrackIndex)

        val subtitlesTrackItems = obtainRelevantTrackInfo(Consts.TRACK_TYPE_TEXT, tracksInfo.textTracks)
        applyAdapterOnSpinner(textSpinner, subtitlesTrackItems, tracksInfo.defaultTextTrackIndex)
    }

    /**
     * Obtain info that user is interested in.
     * For example if user want to display in UI bitrate of the available tracks,
     * he can do it, by obtaining the tackType of video, and getting the getBitrate() from videoTrackInfo.
     *
     * @param trackType  - tyoe of the track you are interested in.
     * @param trackInfos - all availables tracks.
     * @return
     */
    private fun obtainRelevantTrackInfo(trackType: Int, trackInfos: List<BaseTrack>): Array<TrackItem?> {
        val trackItems = arrayOfNulls<TrackItem>(trackInfos.size)
        when (trackType) {
            Consts.TRACK_TYPE_VIDEO -> {
                val tvVideo = this.findViewById<TextView>(R.id.tvVideo)
                changeSpinnerVisibility(videoSpinner, tvVideo, trackInfos)

                for (i in trackInfos.indices) {
                    val videoTrackInfo = trackInfos[i] as VideoTrack
                    if (videoTrackInfo.isAdaptive) {
                        trackItems[i] = TrackItem("Auto", videoTrackInfo.uniqueId)
                    } else {
                        trackItems[i] = TrackItem(videoTrackInfo.bitrate.toString(), videoTrackInfo.uniqueId)
                    }
                }
            }
            Consts.TRACK_TYPE_AUDIO -> {
                val tvAudio = this.findViewById<TextView>(R.id.tvAudio)
                changeSpinnerVisibility(audioSpinner, tvAudio, trackInfos)
                //Map<Integer, AtomicInteger> channelMap = new HashMap<>();
                val channelSparseIntArray = SparseArray<AtomicInteger>()

                for (i in trackInfos.indices) {
                    if (channelSparseIntArray.get((trackInfos[i] as AudioTrack).channelCount) != null) {
                        channelSparseIntArray.get((trackInfos[i] as AudioTrack).channelCount).incrementAndGet()
                    } else {
                        channelSparseIntArray.put((trackInfos[i] as AudioTrack).channelCount, AtomicInteger(1))
                    }
                }
                var addChannel = false
                if (channelSparseIntArray.size() > 0 && AtomicInteger(trackInfos.size).toString() != channelSparseIntArray.get((trackInfos[0] as AudioTrack).channelCount).toString()) {
                    addChannel = true
                }
                for (i in trackInfos.indices) {
                    val audioTrackInfo = trackInfos[i] as AudioTrack
                    var label: String? = if (audioTrackInfo.label != null) audioTrackInfo.label else audioTrackInfo.language
                    var bitrate = if (audioTrackInfo.bitrate > 0) "" + audioTrackInfo.bitrate else ""
                    if (TextUtils.isEmpty(bitrate) && addChannel) {
                        bitrate = buildAudioChannelString(audioTrackInfo.channelCount)
                    }
                    if (audioTrackInfo.isAdaptive) {
                        if (!TextUtils.isEmpty(bitrate)) {
                            bitrate += " Adaptive"
                        } else {
                            bitrate = "Adaptive"
                        }
                        if (label == null) {
                            label = ""
                        }
                    }
                    trackItems[i] = TrackItem("$label $bitrate", audioTrackInfo.uniqueId)
                }
            }
            Consts.TRACK_TYPE_TEXT -> {
                val tvSubtitle = this.findViewById<TextView>(R.id.tvText)
                changeSpinnerVisibility(textSpinner, tvSubtitle, trackInfos)

                for (i in trackInfos.indices) {

                    val textTrackInfo = trackInfos[i] as TextTrack
                    val lang = if (textTrackInfo.label != null) textTrackInfo.label else "unknown"
                    trackItems[i] = TrackItem(lang!!, textTrackInfo.uniqueId)
                }
            }
        }
        return trackItems
    }

    private fun changeSpinnerVisibility(spinner: Spinner?, textView: TextView, trackInfos: List<BaseTrack>) {
        //hide spinner if no data available.
        if (trackInfos.isEmpty()) {
            textView.visibility = View.GONE
            spinner?.visibility = View.GONE
        } else {
            textView.visibility = View.VISIBLE
            spinner?.visibility = View.VISIBLE
        }
    }

    private fun applyAdapterOnSpinner(spinner: Spinner, trackInfo: Array<TrackItem?>, defaultSelectedIndex: Int) {
        val trackItemAdapter = TrackItemAdapter(this, R.layout.track_items_list_row, trackInfo)
        spinner.adapter = trackItemAdapter
        if (defaultSelectedIndex > 0) {
            spinner.setSelection(defaultSelectedIndex)
        }
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        userIsInteracting = true
    }

    override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (!userIsInteracting) {
            return
        }
        val trackItem = parent.getItemAtPosition(position) as TrackItem
        //tell to the player, to switch track based on the user selection.

        player?.changeTrack(trackItem.uniqueId)

        //String selectedIndex = getQualityIndex(BitRateRange.QualityType.Auto, currentTracks.getVideoTracks());
    }

    override fun onNothingSelected(parent: AdapterView<*>) {

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

    private fun getQualityIndex(videoQuality: BitRateRange.QualityType, videoTrackInfo: List<VideoTrack>): String? {
        var uniqueTrackId: String? = null
        var bitRateValue: Long = 0
        var bitRateRange: BitRateRange? = null

        when (videoQuality) {
            BitRateRange.QualityType.Low -> {
                bitRateRange = BitRateRange.lowQuality
                val lowBitrateMatchedTracks = getVideoTracksInRange(videoTrackInfo, bitRateRange)
                Collections.sort(lowBitrateMatchedTracks, bitratesComperator())

                for (track in lowBitrateMatchedTracks) {
                    bitRateValue = track.bitrate
                    if (isBitrateInRange(bitRateValue, bitRateRange.low, bitRateRange.high)) {
                        uniqueTrackId = track.uniqueId
                        break
                    }
                }
            }
            BitRateRange.QualityType.Mediun -> {
                bitRateRange = BitRateRange.medQuality
                val medBitratesMatchedTracks = getVideoTracksInRange(videoTrackInfo, bitRateRange)
                Collections.sort(medBitratesMatchedTracks, bitratesComperator())

                for (track in medBitratesMatchedTracks) {
                    bitRateValue = track.bitrate
                    if (isBitrateInRange(bitRateValue, bitRateRange.low, bitRateRange.high)) {
                        uniqueTrackId = track.uniqueId
                        break
                    }
                }
            }
            BitRateRange.QualityType.High -> {
                bitRateRange = BitRateRange.highQuality
                Collections.sort(videoTrackInfo, bitratesComperator())
                for (entry in videoTrackInfo) {
                    bitRateValue = entry.bitrate
                    if (bitRateValue >= bitRateRange.low) {
                        uniqueTrackId = entry.uniqueId
                        break
                    }
                }
            }
            BitRateRange.QualityType.Auto -> for (track in videoTrackInfo) {
                if (track.isAdaptive) {
                    uniqueTrackId = track.uniqueId
                    break
                }
            }
            else -> for (track in videoTrackInfo) {
                if (track.isAdaptive) {
                    uniqueTrackId = track.uniqueId
                    break
                }
            }
        }

        //null protection
        if (uniqueTrackId == null && tracksInfo != null) {
            tracksInfo?.defaultVideoTrackIndex
        }
        return uniqueTrackId
    }

    private fun isBitrateInRange(bitRate: Long, low: Long, high: Long): Boolean {
        return low <= bitRate && bitRate <= high
    }

    private fun bitratesComperator(): Comparator<VideoTrack> {
        return Comparator { track1, track2 -> java.lang.Long.valueOf(track1.bitrate).compareTo(track2.bitrate) }
    }

    private fun getVideoTracksInRange(videoTracks: List<VideoTrack>, bitRateRange: BitRateRange?): List<VideoTrack> {
        val videoTrackInfo = ArrayList<VideoTrack>()
        var bitRate: Long
        for (track in videoTracks) {
            bitRate = track.bitrate
            if (bitRate >= bitRateRange!!.low && bitRate <= bitRateRange.high) {
                videoTrackInfo.add(track)
            }
        }
        return videoTrackInfo
    }

    private fun buildAudioChannelString(channelCount: Int): String {
        when (channelCount) {
            1 -> return "Mono"
            2 -> return "Stereo"
            6, 7 -> return "Surround_5.1"
            8 -> return "Surround_7.1"
            else -> return "Surround"
        }
    }

    //Example for Custom Licens Adapter
    class DRMAdapter : PKRequestParams.Adapter {
        var customData: String? = null
        override fun adapt(requestParams: PKRequestParams): PKRequestParams {
            requestParams.headers["customData"] = customData
            return requestParams
        }

        override fun updateParams(player: Player) {
            // TODO?
        }

        override fun getApplicationName(): String? {
            return null
        }
    }

    private fun changeBasicMediaOptions(pkMediaEntry: PKMediaEntry?) {
        if (pkMediaEntry != null) {
            player?.setMedia(pkMediaEntry, START_POSITION)
        } else {
            log.d("PKMediaEntry is null")
        }
    }
}
