package com.kaltura.kalturaplayertestapp

import AppOVPMediaOptions
import android.content.IntentFilter
import android.content.res.Configuration
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.kaltura.android.exoplayer2.upstream.HttpDataSource
import com.kaltura.dtg.exoparser.C.TRACK_TYPE_AUDIO
import com.kaltura.kalturaplayertestapp.converters.*
import com.kaltura.kalturaplayertestapp.models.ima.UiConfFormatIMAConfig
import com.kaltura.kalturaplayertestapp.models.ima.UiConfFormatIMADAIConfig
import com.kaltura.kalturaplayertestapp.network.NetworkChangeReceiver
import com.kaltura.kalturaplayertestapp.tracks.TracksSelectionController
import com.kaltura.netkit.connect.executor.APIOkRequestsExecutor
import com.kaltura.netkit.utils.ErrorElement
import com.kaltura.playkit.*
import com.kaltura.playkit.ads.AdController
import com.kaltura.playkit.player.MediaSupport
import com.kaltura.playkit.plugins.ads.AdCuePoints
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.fbads.fbinstream.FBInstreamConfig
import com.kaltura.playkit.plugins.fbads.fbinstream.FBInstreamPlugin
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.playkit.plugins.imadai.IMADAIPlugin
import com.kaltura.playkit.plugins.kava.KavaAnalyticsConfig
import com.kaltura.playkit.plugins.kava.KavaAnalyticsEvent
import com.kaltura.playkit.plugins.kava.KavaAnalyticsPlugin
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsConfig
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsEvent
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsPlugin
import com.kaltura.playkit.plugins.youbora.YouboraEvent
import com.kaltura.playkit.plugins.youbora.YouboraPlugin
import com.kaltura.playkit.plugins.youbora.pluginconfig.YouboraConfig
import com.kaltura.playkit.providers.PlaylistMetadata
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ovp.OVPMediaAsset
import com.kaltura.playkit.utils.Consts.TRACK_TYPE_TEXT
import com.kaltura.playkit.utils.Consts.TRACK_TYPE_VIDEO
import com.kaltura.playkitvr.VRController
import com.kaltura.tvplayer.*
import com.kaltura.tvplayer.config.PhoenixTVPlayerParams
import com.kaltura.tvplayer.config.TVPlayerParams
import com.kaltura.tvplayer.playlist.*
import java.net.UnknownHostException
import java.text.SimpleDateFormat
import java.util.*

class PlayerActivity: AppCompatActivity(), Observer {

    private val log = PKLog.get("PlayerActivity")

    companion object {
        const val PLAYER_CONFIG_JSON_KEY = "player_config_json_key"
        const val PLAYER_CONFIG_TITLE_KEY = "player_config_title_key"
    }
    private val dateFormat = SimpleDateFormat("HH:mm:ss.SSS")
    private var backButtonPressed: Boolean = false
    private val gson = Gson()
    private var player: KalturaPlayer? = null
    private val tvPlayerParams: TVPlayerParams? = null
    private lateinit var initOptions: PlayerInitOptions
    private var playerConfigTitle: String? = null
    private var playerInitOptionsJson: String? = null

    private val uiConfId: Int? = null
    private val ks: String? = null
    private var mediaList: List<Media>? = null
    private var eventsListRecyclerAdapter: EventsAdapter? = null
    private var eventsListView: RecyclerView? = null
    private val eventsList = ArrayList<String>()
    private val searchedEventsList = ArrayList<String>()
    private var searchLogPattern = ""
    private var progressBar: ProgressBar? = null
    private var searchView: SearchView? = null
    private var tracksSelectionController: TracksSelectionController? = null
    private var appPlayerInitConfig: PlayerConfig? = null
    private var currentPlayedMediaIndex = 0
    private var playbackControlsView: PlaybackControlsView? = null
    private var adCuePoints: AdCuePoints? = null
    private var allAdsCompeted: Boolean = false
    private var playbackControlsManager: PlaybackControlsManager? = null
    private var isFirstOnResume = true
    private var isPlayingOnPause: Boolean = false

    private var networkChangeReceiver: NetworkChangeReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        networkChangeReceiver = NetworkChangeReceiver
        eventsListView = findViewById(R.id.events_list)
        val layoutManager = LinearLayoutManager(this)
        eventsListView?.setLayoutManager(layoutManager)
        eventsListRecyclerAdapter = EventsAdapter()
        eventsListView?.setAdapter(eventsListRecyclerAdapter)
        searchView = findViewById(R.id.search_events)
        progressBar = findViewById(R.id.videoProgressBar)
        addSearchListener()

        playerConfigTitle = intent.extras?.getString(PlayerActivity.PLAYER_CONFIG_TITLE_KEY)
        playerInitOptionsJson = intent.extras?.getString(PlayerActivity.PLAYER_CONFIG_JSON_KEY)

        require(!(playerConfigTitle == null || playerInitOptionsJson == null)) { "Must pass extra " + PlayerActivity.PLAYER_CONFIG_JSON_KEY }
        initDrm()

        appPlayerInitConfig = gson.fromJson(playerInitOptionsJson, PlayerConfig::class.java)

        appPlayerInitConfig.let {

        }

        appPlayerInitConfig?.let {
            if (appPlayerInitConfig?.requestConfiguration != null) {
                APIOkRequestsExecutor.getSingleton().requestConfiguration = appPlayerInitConfig?.requestConfiguration
                APIOkRequestsExecutor.getSingleton().setNetworkErrorEventListener { errorElement -> log.d("XXX NetworkError code = " + errorElement.code + " " + errorElement.message) }
            }

            val playerType = appPlayerInitConfig?.playerType
            if (KalturaPlayer.Type.basic == playerType) {
                buildPlayer(it, currentPlayedMediaIndex, playerType)
            } else if (KalturaPlayer.Type.ovp == playerType) {
                buildPlayer(it, currentPlayedMediaIndex, playerType)
            } else if (KalturaPlayer.Type.ott == playerType) {
                buildPlayer(it, currentPlayedMediaIndex, playerType)
            }
        } ?: run {
            showMessage(R.string.error_empty_input)
        }
    }

    fun playNext() {
        //playbackControlsManager?.setAdPlayerState(null)
        //playbackControlsManager?.setContentPlayerState(null)
        if (player != null) {
            tracksSelectionController = null
            player?.stop()
        }
        if (player?.playlistController != null) {
            player?.playlistController?.playNext()
            playbackControlsManager?.updatePrevNextImgBtnFunctionality(player?.playlistController?.currentMediaIndex
                    ?: 0, player?.playlistController?.playlist?.mediaListSize ?: 0)

            val playerDuration =  player?.duration ?: 0
            player?.playlistController?.isAutoContinueEnabled?.let {
                if (!it || playerDuration <= 0) {
                    playbackControlsManager?.setSeekBarVisibiliy(View.INVISIBLE)
                    playbackControlsManager?.handleContainerClick()
                }
            }
        }

    }

    fun playPrev() {
        //playbackControlsManager?.setAdPlayerState(null)
        //playbackControlsManager?.setContentPlayerState(null)
        if (player != null) {
            tracksSelectionController = null
            player?.stop()
        }
        if (player?.playlistController != null) {
            player?.playlistController?.playPrev()
            playbackControlsManager?.updatePrevNextImgBtnFunctionality(player?.playlistController?.currentMediaIndex
                    ?: 0, player?.playlistController?.playlist?.mediaListSize ?: 0)
        }
        val playerDuration =  player?.duration ?: 0
        player?.playlistController?.isAutoContinueEnabled?.let {
            if (!it || playerDuration <= 0) {
                playbackControlsManager?.setSeekBarVisibiliy(View.INVISIBLE)
                playbackControlsManager?.handleContainerClick()
            }
        }
    }

    fun changeMedia() {
        if (player != null) {
            tracksSelectionController = null
            player?.stop()
        }
        mediaList?.let {
            updatePluginsConfig(it.get(currentPlayedMediaIndex))

            if (KalturaPlayer.Type.ovp == appPlayerInitConfig?.playerType) {
                val ovpMediaOptions = buildOvpMediaOptions(0L, currentPlayedMediaIndex) ?: return

                player?.loadMedia(ovpMediaOptions) { entry, error ->
                    var entryId = ""
                    if (entry != null) {
                        entryId = entry.getId()
                    }
                    log.d("OVPMedia onEntryLoadComplete; $entryId; $error")
                    handleOnEntryLoadComplete(error)
                }
            } else if (KalturaPlayer.Type.ott == appPlayerInitConfig?.playerType) {
                val ottMediaOptions = buildOttMediaOptions(0L, currentPlayedMediaIndex) ?: return

                player?.loadMedia(ottMediaOptions) { entry, error ->
                    var entryId = ""
                    if (entry != null) {
                        entryId = entry.getId()
                    }
                    log.d("OTTMedia onEntryLoadComplete; $entryId ; $error")
                    handleOnEntryLoadComplete(error)
                }
            } else if (KalturaPlayer.Type.basic == appPlayerInitConfig?.playerType) run {
                val mediaEntry = it.get(currentPlayedMediaIndex).pkMediaEntry
                if (appPlayerInitConfig?.vrSettings != null) {
                    mediaEntry?.setIsVRMediaType(true)
                }

                if (it.get(currentPlayedMediaIndex).externalSubtitles != null) {
                    mediaEntry?.externalSubtitleList = it.get(currentPlayedMediaIndex).externalSubtitles
                }
                player?.setMedia(mediaEntry, 0L)
            } else {
                log.e("Error no such player type <" + appPlayerInitConfig?.playerType + ">")
            }
        }

    }

    fun updatePluginsConfig(media: Media) {
        if(initOptions.pluginConfigs.hasConfig(IMAPlugin.factory.name)) run {
            val imaJson = initOptions.pluginConfigs.getPluginConfig(IMAPlugin.factory.name) as JsonObject
            if (media.mediaAdTag != null) {
                imaJson.addProperty("adTagUrl", media.mediaAdTag)
            }
            //IMAConfig imaPluginConfig = gson.fromJson(imaJson, IMAConfig.class);
            //Example to update the AdTag
            //imaPluginConfig.setAdTagUrl("http://externaltests.dev.kaltura.com/playKitApp/adManager/customAdTags/vmap/inline/ima_pre_mid_post_bumber2.xml");
            initOptions.pluginConfigs.setPluginConfig(IMAPlugin.factory.name, imaJson)
        } else if (initOptions.pluginConfigs.hasConfig(IMADAIPlugin.factory.getName()))
        {
            val imadaiJson = initOptions.pluginConfigs.getPluginConfig(IMADAIPlugin.factory.name) as JsonObject
            //IMADAIConfig imaPluginConfig = gson.fromJson(imadaiJson, IMADAIConfig.class);
            initOptions.pluginConfigs.setPluginConfig(IMAPlugin.factory.name, imadaiJson)
        } else if (initOptions.pluginConfigs.hasConfig(FBInstreamPlugin.factory.getName())) {
            val fbAds =  initOptions.pluginConfigs.getPluginConfig(FBInstreamPlugin.factory.getName());
            initOptions.pluginConfigs.setPluginConfig(FBInstreamPlugin.factory.getName(), fbAds);
        }

//        //EXAMPLE if there are no auto replacers in this format ->  {{key}}
//        if (initOptions.pluginConfigs.hasConfig(YouboraPlugin.factory.getName())) {
//            JsonObject youboraJson = (JsonObject) initOptions.pluginConfigs.getPluginConfig(YouboraPlugin.factory.getName());
//            YouboraConfig youboraPluginConfig = gson.fromJson(youboraJson, YouboraConfig.class);
//            Properties properties = new Properties();
//            properties.setGenre("AAAA");
//            properties.setOwner("SONY");
//            properties.setQuality("HD");
//            properties.setPrice("122");
//            properties.setYear("2018");
//            youboraPluginConfig.setProperties(properties);
//            initOptions.pluginConfigs.setPluginConfig(YouboraPlugin.factory.getName(), youboraPluginConfig.toJson());
//        }
    }

    private fun handleOnEntryLoadComplete(error: ErrorElement?) {
        if (error != null) {
            log.d("Load Error Extra = " + error.extra)
            Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            playbackControlsView?.playPauseToggle?.setBackgroundResource(R.drawable.play)
            playbackControlsManager?.showControls(View.VISIBLE)
        } else {
            if (!initOptions.autoplay) {
                playbackControlsManager?.showControls(View.VISIBLE)
            }
        }
    }

    fun clearLogView() {
        eventsList.clear()
        searchedEventsList.clear()
        searchLogPattern = ""
    }

    fun getCurrentPlayedMediaIndex(): Int {
        if (player?.playlistController != null) {
            return player?.playlistController?.currentMediaIndex ?: 0
        }
        return currentPlayedMediaIndex
    }

    fun setCurrentPlayedMediaIndex(currentPlayedMediaIndex: Int) {
        this.currentPlayedMediaIndex = currentPlayedMediaIndex
    }

    private fun addSearchListener() {
        searchView?.setOnQueryTextListener(object: SearchView.OnQueryTextListener {

            override fun onQueryTextSubmit(query: String): Boolean {

                val queryToLowerCase = query.toLowerCase()
                searchLogPattern = queryToLowerCase
                searchedEventsList.clear()
                for (eventItem in eventsList) {
                    if (eventItem.toLowerCase().contains(queryToLowerCase)) {
                        searchedEventsList.add(eventItem)
                    }
                }
                eventsListRecyclerAdapter?.notifyData(searchedEventsList)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (TextUtils.isEmpty(newText)) {
                    searchedEventsList.clear()
                    searchLogPattern = ""
                    eventsListRecyclerAdapter?.notifyData(eventsList)
                    return false
                }
                return false
            }
        })
    }

    private fun buildPlayer(appPlayerInitConfig: PlayerConfig, playListMediaIndex: Int, playerType: KalturaPlayer.Type) {
        var player: KalturaPlayer

        val appPluginConfigJsonObject = appPlayerInitConfig.plugins
        //        int playerUiConfId = -1;
        //        if (appPlayerInitConfig.uiConf != null) {
        //            playerUiConfId = Integer.valueOf(appPlayerInitConfig.uiConf.id);
        //        }
        mediaList = appPlayerInitConfig.mediaList

        val partnerId = if (appPlayerInitConfig.partnerId != null) Integer.valueOf(appPlayerInitConfig.partnerId) else null
        initOptions = PlayerInitOptions(partnerId)
                .setAutoPlay(appPlayerInitConfig.autoPlay)
                .setKs(appPlayerInitConfig.ks)
                .setPreload(appPlayerInitConfig.preload)
                .setReferrer(appPlayerInitConfig.referrer)
                .setAllowCrossProtocolEnabled(appPlayerInitConfig.allowCrossProtocolEnabled)
                .setPreferredMediaFormat(appPlayerInitConfig.preferredFormat)
                .setSecureSurface(appPlayerInitConfig.secureSurface)
                .setAspectRatioResizeMode(appPlayerInitConfig.aspectRatioResizeMode)
                .setAbrSettings(appPlayerInitConfig.abrSettings)
                .setLoadControlBuffers(appPlayerInitConfig.loadControlBuffers)
                .setSubtitleStyle(appPlayerInitConfig.setSubtitleStyle)
                .setAllowClearLead(appPlayerInitConfig.allowClearLead)
                .setEnableDecoderFallback(appPlayerInitConfig.enableDecoderFallback)
                .setAdAutoPlayOnResume(appPlayerInitConfig.adAutoPlayOnResume)
                .setVrPlayerEnabled(appPlayerInitConfig.vrPlayerEnabled)
                .setVRSettings(appPlayerInitConfig.vrSettings)
                .setIsVideoViewHidden(appPlayerInitConfig.isVideoViewHidden)
                .setContentRequestAdapter(appPlayerInitConfig.contentRequestAdapter)
                .setLicenseRequestAdapter(appPlayerInitConfig.licenseRequestAdapter)
                .forceSinglePlayerEngine(appPlayerInitConfig.forceSinglePlayerEngine)
                .setTunneledAudioPlayback(appPlayerInitConfig.isTunneledAudioPlayback)
                .setPluginConfigs(convertPluginsJsonArrayToPKPlugins(appPluginConfigJsonObject))

        appPlayerInitConfig.trackSelection?.let {
            it.audioSelectionMode?.let { selectionModeAudio ->
                initOptions.setAudioLanguage(it.audioSelectionLanguage, PKTrackConfig.Mode.valueOf(selectionModeAudio))
            }

            it.textSelectionMode?.let { selectionModeText ->
                initOptions.setTextLanguage(it.textSelectionLanguage, PKTrackConfig.Mode.valueOf(selectionModeText))
            }

        }


        if (KalturaPlayer.Type.ovp == playerType) {

            // inorder to generate retry error need also to remove and un install app -> KalturaOvpPlayer.create(this, 1091, "http://qa-apache-php7.dev.kaltura.com/");
            //            if (partnerId == 1091) {
            //                TVPlayerParams tvPlayerParams = new TVPlayerParams();
            //                tvPlayerParams.analyticsUrl = "https://analytics.kaltura.com";
            //                tvPlayerParams.uiConfId = 1774581;
            //                tvPlayerParams.partnerId = 1091;
            //                tvPlayerParams.serviceUrl = "http://httpbin.org/status/401?";
            //                initOptions.tvPlayerParams = tvPlayerParams;
            //            }

            player = KalturaOvpPlayer.create(this@PlayerActivity, initOptions)
            setPlayer(player)

            val ovpMediaOptions = buildOvpMediaOptions(appPlayerInitConfig.startPosition, playListMediaIndex)
            if (ovpMediaOptions != null) {

                player?.loadMedia(ovpMediaOptions) { entry, error ->
                    if (error != null) {
                        log.d("OVPMedia Error Extra = " + error.getExtra())
                        Snackbar.make(findViewById<View>(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG).show()
                        playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.play)
                        playbackControlsManager?.showControls(View.VISIBLE)
                    } else {
                        log.d("OVPMedia onEntryLoadComplete entry =" + entry.getId())
                    }
                }
            } else {
                // PLAYLIST
                handleOvpPlayerPlaylist(appPlayerInitConfig, player)
            }
        } else if (KalturaPlayer.Type.ott == playerType) {
            if (partnerId == 198) {
                val phoenixTVPlayerParams = PhoenixTVPlayerParams()
                phoenixTVPlayerParams.analyticsUrl = "https://analytics.kaltura.com"
                phoenixTVPlayerParams.ovpPartnerId = 1774581
                phoenixTVPlayerParams.partnerId = 198
                phoenixTVPlayerParams.serviceUrl = "https://api-preprod.ott.kaltura.com/v5_2_8/"
                phoenixTVPlayerParams.ovpServiceUrl = "http://cdnapi.kaltura.com/"
                initOptions?.tvPlayerParams = phoenixTVPlayerParams
            }

            if (partnerId == 3079) {
                val phoenixTVPlayerParams = PhoenixTVPlayerParams();
                phoenixTVPlayerParams.analyticsUrl = "https://analytics.kaltura.com";
                phoenixTVPlayerParams.ovpPartnerId = 1774581;
                phoenixTVPlayerParams.partnerId = 3079;
                phoenixTVPlayerParams.serviceUrl = "https://rest.irs1.ott.kaltura.com/v5_2_4/";
                phoenixTVPlayerParams.ovpServiceUrl = "http://cdnapi.kaltura.com/";
                initOptions?.tvPlayerParams = phoenixTVPlayerParams;
            }

            player = KalturaOttPlayer.create(this@PlayerActivity, initOptions)
            setPlayer(player)
            val ottMediaOptions = buildOttMediaOptions(appPlayerInitConfig.startPosition, playListMediaIndex)
            if (ottMediaOptions != null) {
                player?.loadMedia(ottMediaOptions) { entry, error ->
                    if (error != null) {
                        log.d("OTTMedia Error Extra = " + error.getExtra())
                        Snackbar.make(findViewById<View>(android.R.id.content), error.getMessage(), Snackbar.LENGTH_LONG).show()
                        playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.play)
                        if (playbackControlsView != null) {
                            playbackControlsManager?.showControls(View.VISIBLE)
                        }
                    } else {
                        log.d("OTTMedia onEntryLoadComplete  entry = " + entry.getId())
                    }
                }
            } else {
                // PLAYLIST
                handleOttPlayerPlaylist(appPlayerInitConfig, player)
            }
        } else if (KalturaPlayer.Type.basic == playerType) {
            player = KalturaBasicPlayer.create(this@PlayerActivity, initOptions)
            setPlayer(player)
            val mediaEntry = appPlayerInitConfig.mediaList?.get(currentPlayedMediaIndex)?.pkMediaEntry
            if (appPlayerInitConfig.mediaList != null && appPlayerInitConfig.mediaList?.get(currentPlayedMediaIndex) != null) {
                mediaEntry?.setIsVRMediaType(true)
                player.setMedia(mediaEntry, appPlayerInitConfig.startPosition)
            } else {
                // PLAYLIST
                handleBasicPlayerPlaylist(appPlayerInitConfig, player)
            }
        } else {
            log.e("Failed to initialize player...")
            return
        }

        playbackControlsManager = PlaybackControlsManager(this, player, playbackControlsView)
        if (!initOptions.autoplay) {
            playbackControlsManager?.showControls(View.VISIBLE)
        } else {
            playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.pause)
        }

        if (appPlayerInitConfig.playlistConfig != null) {
            var listSize : Int? =  null
            if (appPlayerInitConfig.playerType == KalturaPlayer.Type.ovp) {
                if (appPlayerInitConfig.playlistConfig?.playlistId == null) {
                    listSize = appPlayerInitConfig.playlistConfig?.ovpMediaOptionsList?.size ?: 0
                }
            } else if (appPlayerInitConfig.playerType == KalturaPlayer.Type.ott) {
                listSize = appPlayerInitConfig.playlistConfig?.ottMediaOptionsList?.size ?: 0
            } else if (appPlayerInitConfig.playerType == KalturaPlayer.Type.basic) {
                listSize = appPlayerInitConfig.playlistConfig?.basicMediaOptionsList?.size ?: 0
            }

            if (listSize != null && listSize > 0) {
                playbackControlsManager?.addChangeMediaImgButtonsListener(listSize)
                playbackControlsManager?.updatePrevNextImgBtnFunctionality(currentPlayedMediaIndex, listSize)
            }
        } else {
            appPlayerInitConfig.mediaList?.let {
                if (it.size > 1) {
                    playbackControlsManager?.addChangeMediaImgButtonsListener(it.size)
                }
                playbackControlsManager?.updatePrevNextImgBtnFunctionality(currentPlayedMediaIndex, it.size)
            }
        }
    }

    private fun handleOvpPlayerPlaylist(appPlayerInitConfig: PlayerConfig, player: KalturaOvpPlayer?) {
        if (appPlayerInitConfig.playlistConfig?.playlistId != null) {
            var mediaList = appPlayerInitConfig.playlistConfig?.ovpMediaOptionsList

            val ovpPlaylistIdOptions = OVPPlaylistIdOptions()
            ovpPlaylistIdOptions.startIndex = appPlayerInitConfig.playlistConfig?.startIndex ?: 0
            ovpPlaylistIdOptions.ks = appPlayerInitConfig.playlistConfig?.ks ?: ""
            ovpPlaylistIdOptions.playlistCountDownOptions = appPlayerInitConfig.playlistConfig?.countDownOptions
                    ?: CountDownOptions()
            ovpPlaylistIdOptions.playlistId = appPlayerInitConfig.playlistConfig?.playlistId
            ovpPlaylistIdOptions.useApiCaptions = appPlayerInitConfig.playlistConfig?.useApiCaptions ?: false
            ovpPlaylistIdOptions.loopEnabled = appPlayerInitConfig.playlistConfig?.loopEnabled ?: false
            //ovpPlaylistIdOptions.shuffleEnabled = appPlayerInitConfig.playlistConfig?.shuffleEnabled ?: false
            ovpPlaylistIdOptions.autoContinue = appPlayerInitConfig.playlistConfig?.autoContinue ?: true
            ovpPlaylistIdOptions.recoverOnError = appPlayerInitConfig.playlistConfig?.recoverOnError ?: false

            player?.loadPlaylistById(ovpPlaylistIdOptions) { playlistController, error ->
                if (error != null) {
                    Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
                } else {
                    setCurrentPlayedMediaIndex(ovpPlaylistIdOptions.startIndex)
                    playbackControlsManager?.addChangeMediaImgButtonsListener(playlistController.playlist.mediaListSize)
                    playbackControlsManager?.updatePrevNextImgBtnFunctionality(ovpPlaylistIdOptions.startIndex, playlistController.playlist.mediaListSize)
                }
            }
        } else {
            var mediaList = appPlayerInitConfig.playlistConfig?.ovpMediaOptionsList

            val ovpPlaylistOptions = OVPPlaylistOptions()
            ovpPlaylistOptions.startIndex = appPlayerInitConfig.playlistConfig?.startIndex ?: 0
            ovpPlaylistOptions.ks = appPlayerInitConfig.playlistConfig?.ks ?: ""
            ovpPlaylistOptions.playlistCountDownOptions = appPlayerInitConfig.playlistConfig?.countDownOptions
                    ?: CountDownOptions()
            ovpPlaylistOptions.playlistMetadata = appPlayerInitConfig.playlistConfig?.playlistMetadata
                    ?: PlaylistMetadata().setName("TestOTTPlayList").setId("1")
            ovpPlaylistOptions.ovpMediaOptionsList = convertToOvpMediaOptionsList(mediaList)
            ovpPlaylistOptions.loopEnabled = appPlayerInitConfig.playlistConfig?.loopEnabled ?: false
            //ovpPlaylistOptions.shuffleEnabled = appPlayerInitConfig.playlistConfig?.shuffleEnabled ?: false
            ovpPlaylistOptions.autoContinue = appPlayerInitConfig.playlistConfig?.autoContinue ?: true
            ovpPlaylistOptions.recoverOnError = appPlayerInitConfig.playlistConfig?.recoverOnError ?: false

            player?.loadPlaylist(ovpPlaylistOptions) { playlistController, error ->
                if (error != null) {
                    Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
                } else {
                    setCurrentPlayedMediaIndex(ovpPlaylistOptions.startIndex)
                    //playbackControlsManager?.addChangeMediaImgButtonsListener(playlistController.playlist.mediaListSize)
                    //playbackControlsManager?.updatePrevNextImgBtnFunctionality(ovpPlaylistOptions.startIndex, playlistController.playlist.mediaListSize)
                }
            }
        }
    }

    private fun convertToOvpMediaOptionsList(mediaList: List<AppOVPMediaOptions>?): MutableList<OVPMediaOptions>? {
        val ovpMediasOptions = mutableListOf<OVPMediaOptions>()
        if (mediaList == null) {
            return ovpMediasOptions;
        }

        mediaList.forEach {
            var ovpMediaAsset = OVPMediaAsset()
            ovpMediaAsset.entryId = it.entryId
            ovpMediaAsset.ks = it.ks
            ovpMediaAsset.referrer = it.referrer

            var ovpMediaOptions = OVPMediaOptions(ovpMediaAsset)
            ovpMediaOptions.setUseApiCaptions(it.useApiCaptions)
            ovpMediaOptions.playlistCountDownOptions = it.countDownOptions
            ovpMediaOptions.startPosition = it.startPosition
            ovpMediasOptions.add(ovpMediaOptions)
        }
        return ovpMediasOptions
    }

    private fun handleOttPlayerPlaylist(appPlayerInitConfig: PlayerConfig, player: KalturaOttPlayer?) {

        var mediaList = appPlayerInitConfig.playlistConfig?.ottMediaOptionsList

        val ottPlaylistIdOptions = OTTPlaylistOptions()
        ottPlaylistIdOptions.startIndex = appPlayerInitConfig.playlistConfig?.startIndex ?: 0
        ottPlaylistIdOptions.ks = appPlayerInitConfig.playlistConfig?.ks ?: ""
        ottPlaylistIdOptions.playlistCountDownOptions = appPlayerInitConfig.playlistConfig?.countDownOptions
                ?: CountDownOptions()
        ottPlaylistIdOptions.playlistMetadata = appPlayerInitConfig.playlistConfig?.playlistMetadata
                ?: PlaylistMetadata().setName("TestOTTPlayList").setId("1")
        ottPlaylistIdOptions.ottMediaOptionsList = convertToOttMediaOptionsList(mediaList)
        ottPlaylistIdOptions.loopEnabled = appPlayerInitConfig.playlistConfig?.loopEnabled ?: false
        //ottPlaylistIdOptions.shuffleEnabled = appPlayerInitConfig.playlistConfig?.shuffleEnabled ?: false
        ottPlaylistIdOptions.autoContinue = appPlayerInitConfig.playlistConfig?.autoContinue ?: true
        ottPlaylistIdOptions.recoverOnError = appPlayerInitConfig.playlistConfig?.recoverOnError ?: false

        player?.loadPlaylist(ottPlaylistIdOptions) { playlistController, error ->
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            } else {
                setCurrentPlayedMediaIndex(ottPlaylistIdOptions.startIndex)
                //playbackControlsManager?.addChangeMediaImgButtonsListener(playlistController.playlist.mediaListSize)
                //playbackControlsManager?.updatePrevNextImgBtnFunctionality(ottPlaylistIdOptions.startIndex, playlistController.playlist.mediaListSize)
            }
        }
    }

    private fun convertToOttMediaOptionsList(mediaList: List<AppOTTMediaOptions>?): MutableList<OTTMediaOptions>? {
        val ottMediasOptions = mutableListOf<OTTMediaOptions>()
        if (mediaList == null) {
            return ottMediasOptions;
        }

        mediaList.forEach {
            var ottMediaAsset = OTTMediaAsset()
            ottMediaAsset.assetId = it.assetId
            ottMediaAsset.urlType = it.urlType
            ottMediaAsset.assetReferenceType = it.assetReferenceType
            ottMediaAsset.protocol = it.protocol
            ottMediaAsset.contextType = it.contextType
            ottMediaAsset.assetType = it.assetType
            ottMediaAsset.ks = it.ks
            ottMediaAsset.referrer = it.referrer

            var mediaFilesList = mutableListOf<String>()
            it.fileIds.let {
                if (it != null) {
                    for (fileId in it) {
                        mediaFilesList.add(fileId)

                    }
                }
            }
            if (!mediaFilesList.isEmpty()) {
                ottMediaAsset.mediaFileIds = mediaFilesList
            }

            var mediaFormatsList = mutableListOf<String>()
            it.formats.let {
                if (it != null) {
                    for (format in it) {
                        mediaFormatsList.add(format)
                    }
                }
            }

            if (!mediaFormatsList.isEmpty()) {
                ottMediaAsset.formats =  mediaFormatsList
            }

            var ottMediaOptions = OTTMediaOptions(ottMediaAsset)
            ottMediaOptions.playlistCountDownOptions = it.countDownOptions
            ottMediaOptions.startPosition = it.startPosition
            ottMediasOptions.add(ottMediaOptions)
        }
        return ottMediasOptions
    }

    private fun handleBasicPlayerPlaylist(appPlayerInitConfig: PlayerConfig, player: KalturaBasicPlayer?) {

        var mediaList = appPlayerInitConfig.playlistConfig?.basicMediaOptionsList

        val basicPlaylistOptions = BasicPlaylistOptions()
        basicPlaylistOptions.startIndex = appPlayerInitConfig.playlistConfig?.startIndex ?: 0
        basicPlaylistOptions.playlistMetadata = appPlayerInitConfig.playlistConfig?.playlistMetadata
                ?: PlaylistMetadata().setName("TestBasicPlayList").setId("1")
        basicPlaylistOptions.playlistCountDownOptions = appPlayerInitConfig.playlistConfig?.countDownOptions
                ?: CountDownOptions()
        basicPlaylistOptions.basicMediaOptionsList = convertToBasicMediaOptionsList(mediaList)
        basicPlaylistOptions.loopEnabled = appPlayerInitConfig.playlistConfig?.loopEnabled ?: false
        //basicPlaylistOptions.shuffleEnabled = appPlayerInitConfig.playlistConfig?.shuffleEnabled ?: false
        basicPlaylistOptions.autoContinue = appPlayerInitConfig.playlistConfig?.autoContinue ?: true
        basicPlaylistOptions.recoverOnError = appPlayerInitConfig.playlistConfig?.recoverOnError ?: false

        player?.loadPlaylist(basicPlaylistOptions) { playlistController, error ->
            if (error != null) {
                Snackbar.make(findViewById(android.R.id.content), error.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("BasicPlaylist OnPlaylistLoadListener  entry = " + basicPlaylistOptions.playlistMetadata.name)
                setCurrentPlayedMediaIndex(basicPlaylistOptions.startIndex)
                //playbackControlsManager?.addChangeMediaImgButtonsListener(playlistController.playlist.mediaListSize)
                //playbackControlsManager?.updatePrevNextImgBtnFunctionality(basicPlaylistOptions.startIndex, playlistController.playlist.mediaListSize)
            }
        }
    }

    private fun convertToBasicMediaOptionsList(mediaList: List<AppBasicMediaOptions>?): MutableList<BasicMediaOptions>? {
        val basicMediasOptionsList = mutableListOf<BasicMediaOptions>()
        if (mediaList == null) {
            return basicMediasOptionsList;
        }
        mediaList.forEach {
            var basicMediaOptions = BasicMediaOptions(it.pkMediaEntry, it.countDownOptions)


            basicMediasOptionsList.add(basicMediaOptions)
        }
        return basicMediasOptionsList
    }


    private fun buildOttMediaOptions(startPosition: Long?, playListMediaIndex: Int): OTTMediaOptions? {
        val ottMedia = mediaList?.get(playListMediaIndex) ?: return null

        var ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = ottMedia.assetId
        ottMediaAsset.assetType = ottMedia.getAssetType()
        ottMediaAsset.contextType = ottMedia.getPlaybackContextType()
        ottMediaAsset.assetReferenceType = ottMedia.getAssetReferenceType()
        ottMediaAsset.protocol = ottMedia.protocol
        ottMediaAsset.ks = ottMedia.ks
        ottMediaAsset.urlType = ottMedia.getUrlType()
        if (ottMedia.format != null) {
            ottMediaAsset.setFormats(listOf(ottMedia.format))
        }
        if (ottMedia.fileId != null) {
            ottMediaAsset.setMediaFileIds(listOf(ottMedia.fileId))
        }


        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = startPosition
        ottMediaOptions.externalSubtitles = ottMedia.externalSubtitles

        return ottMediaOptions
    }

    private fun buildOvpMediaOptions(startPosition: Long?, playListMediaIndex: Int): OVPMediaOptions? {
        val ovpMedia = mediaList?.get(playListMediaIndex) ?: return null

        var ovpMediaAsset = OVPMediaAsset()
        ovpMediaAsset.entryId = ovpMedia.entryId
        ovpMediaAsset.ks = ovpMedia.ks
        val ovpMediaOptions = OVPMediaOptions(ovpMediaAsset)

        ovpMediaOptions.startPosition = startPosition
        ovpMediaOptions.isUseApiCaptions = ovpMedia.useApiCaptions
        ovpMediaOptions.externalSubtitles = ovpMedia.externalSubtitles

        return ovpMediaOptions
    }

    private fun setPlayerListeners() {
        //////// AD Events

        player?.addListener(this, AdEvent.error) { event ->
            log.d("AD ERROR")
            updateEventsLogsList("ad:\n" + event.eventType().name)
            val adError = event as AdEvent.Error
            playbackControlsManager?.setAdPlayerState(adError.type)
        }

        player?.addListener(this, AdEvent.cuepointsChanged) { event ->
            log.d("AD CUEPOINTS CHANGED")
            updateEventsLogsList("ad:\n" + event.eventType().name)
            adCuePoints = event.cuePoints
            adCuePoints?.let {
                if (!it.getAdPluginName().isNullOrEmpty()) {
                    playbackControlsManager?.setAdPluginName(it.getAdPluginName());
                }
            }
        }

        player?.addListener(this, AdEvent.completed) { event ->
            updateEventsLogsList("ad:\n" + event.eventType().name)
            log.d("AD COMPLETED")
        }

        player?.addListener(this, AdEvent.allAdsCompleted) { event ->
            updateEventsLogsList("ad:\n" + event.eventType().name)
            log.d("AD ALL_ADS_COMPLETED")
            playbackControlsManager?.setAdPlayerState(AdEvent.Type.ALL_ADS_COMPLETED)
            allAdsCompeted = true
            if (isPlaybackEndedState()) {
                progressBar?.setVisibility(View.GONE)
                playbackControlsManager?.showControls(View.VISIBLE)
                playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.replay)
            }
        }

        player?.addListener(this, AdEvent.contentPauseRequested) { event ->
            updateEventsLogsList("ad:\n" + event.eventType().name)
            log.d("AD CONTENT_PAUSE_REQUESTED")
            playbackControlsManager?.setAdPlayerState(AdEvent.Type.CONTENT_PAUSE_REQUESTED)

            adCuePoints?.let {
                if (!initOptions.autoplay && IMADAIPlugin.factory.name != it.getAdPluginName()) {
                    playbackControlsManager?.showControls(View.INVISIBLE)
                }
            }

            progressBar?.setVisibility(View.VISIBLE)
        }

        player?.addListener(this, AdEvent.contentResumeRequested) { event ->
            updateEventsLogsList("ad:\n" + event.eventType().name)
            log.d("AD CONTENT_RESUME_REQUESTED")
            playbackControlsManager?.setAdPlayerState(AdEvent.Type.CONTENT_RESUME_REQUESTED)
            playbackControlsManager?.showControls(View.INVISIBLE)
        }

        player?.addListener(this, AdEvent.loaded) { event ->
            updateEventsLogsList("ad:\n" + event.eventType().name)
            log.d("AD LOADED")
            playbackControlsManager?.setAdPlayerState(AdEvent.Type.LOADED)
        }

        player?.addListener(this, AdEvent.started) { event ->
            updateEventsLogsList("ad:\n" + event.eventType().name)
            log.d("AD STARTED")
            playbackControlsManager?.setAdPlayerState(AdEvent.Type.STARTED)
            playbackControlsManager?.setSeekBarVisibiliy(View.VISIBLE)
            playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.pause)
            allAdsCompeted = false
            val adInfo = (event as AdEvent.AdStartedEvent).adInfo
            adCuePoints?.let {
                if (!initOptions.autoplay && IMADAIPlugin.factory.name != it.getAdPluginName()) {
                    playbackControlsManager?.showControls(View.INVISIBLE)
                }
            }



            playbackControlsManager?.showControls(View.INVISIBLE)
            progressBar?.setVisibility(View.INVISIBLE)
        }

        player?.addListener(this, AdEvent.paused) { event ->
            updateEventsLogsList("ad:\n" + event.eventType().name)
            log.d("AD PAUSED")
            playbackControlsManager?.setAdPlayerState(AdEvent.Type.PAUSED)
            playbackControlsManager?.showControls(View.VISIBLE)
            playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.play)
        }

        player?.addListener(this, AdEvent.resumed) { event ->
            updateEventsLogsList("ad:\n" + event.eventType().name)
            log.d("AD RESUMED")
            playbackControlsManager?.setAdPlayerState(AdEvent.Type.RESUMED)
            playbackControlsManager?.showControls(View.INVISIBLE)
            playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.pause)
        }

        player?.addListener(this, AdEvent.tapped) { event ->
            updateEventsLogsList("ad:\n" + event.eventType().name)
            log.d("AD TAPPED")
            playbackControlsManager?.handleContainerClick()
        }

        player?.addListener(this, AdEvent.skipped) { event ->
            updateEventsLogsList("ad:\n" + event.eventType().name)
            log.d("AD SKIPPED")
            playbackControlsManager?.setAdPlayerState(AdEvent.Type.SKIPPED)
        }

        player?.addListener(this, AdEvent.adBufferStart) { event ->
            log.d("AD_BUFFER_START pos = " + event.adPosition)
            progressBar?.setVisibility(View.VISIBLE)
        }

        player?.addListener(this, AdEvent.adBufferEnd) { event ->
            log.d("AD_BUFFER_END pos = " + event.adPosition)
            progressBar?.setVisibility(View.INVISIBLE)
        }

        /////// PLAYER EVENTS

        //        player.addListener(this, PlayerEvent.play, event -> {
        //            log.d("Player PLAY");
        //
        //        });


        player?.addListener(this, PlayerEvent.loadedMetadata) { event ->
            log.d("PLAYER LoadedMetadata")
            updateEventsLogsList("player:\n" + event.eventType().name)
        }

        player?.addListener(this, PlayerEvent.durationChanged) { event ->
            log.d("PLAYER DurationChanged")
            updateEventsLogsList("player:\n" + event.eventType().name)
        }

        player?.addListener(this, PlayerEvent.playing) { event ->
            log.d("PLAYER PLAYING")
            playbackControlsManager?.setSeekBarVisibiliy(View.VISIBLE)
            if (player?.playlistController != null) {
                playbackControlsManager?.updatePrevNextImgBtnFunctionality(player?.playlistController?.currentMediaIndex
                        ?: 0, player?.playlistController?.playlist?.mediaListSize ?: 0)
            }

            updateEventsLogsList("player:\n" + event.eventType().name)
            progressBar?.setVisibility(View.INVISIBLE)
            playbackControlsManager?.setContentPlayerState(event.eventType())
            playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.pause)
            playbackControlsManager?.showControls(View.INVISIBLE)
        }

        player?.addListener(this, PlayerEvent.pause) { event ->
            log.d("PLAYER PAUSE")
            updateEventsLogsList("player:\n" + event.eventType().name)
        }

        player?.addListener(this, PlayerEvent.stopped) { event ->
            log.d("PLAYER STOPPED")
            updateEventsLogsList("player:\n" + event.eventType().name)
            if (player?.playlistController == null || (player?.playlistController?.isAutoContinueEnabled ?: true)) {
                playbackControlsManager?.showControls(View.INVISIBLE)
            }
            playbackControlsManager?.setContentPlayerState(event.eventType())
        }

        player?.addListener(this, PlayerEvent.ended) { event ->
            log.d("PLAYER ENDED")
            if (player?.playlistController != null) {
                playbackControlsManager?.updatePrevNextImgBtnFunctionality(player?.playlistController?.currentMediaIndex
                        ?: 0, player?.playlistController?.playlist?.mediaListSize ?: 0)
            }

            if (adCuePoints == null || adCuePoints != null && !adCuePoints!!.hasPostRoll() || IMADAIPlugin.factory.name == adCuePoints!!.getAdPluginName()) {
                val isAutoContinueEnabled = player?.playlistController?.isAutoContinueEnabled ?: false
                if (!isAutoContinueEnabled) {
                    playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.replay)
                    playbackControlsManager?.showControls(View.VISIBLE)
                }
            }
            progressBar?.setVisibility(View.GONE)
            if (!isPostrollAvailableInAdCuePoint() ||
                    IMADAIPlugin.factory.getName().equals(adCuePoints?.getAdPluginName()) ||
                    FBInstreamPlugin.factory.getName().equals(adCuePoints?.getAdPluginName())
            ) {
                if (player?.playlistController == null || !(player?.playlistController?.isAutoContinueEnabled ?: true)) {
                    playbackControlsManager?.showControls(View.VISIBLE)
                }
            }
        }

        player?.addListener(this, PlaylistEvent.playListLoaded) { event ->
            log.d("PLAYLIST playListLoaded")
        }

        player?.addListener(this, PlaylistEvent.playListStarted) { event ->
            log.d("PLAYLIST playListStarted")
            playbackControlsManager?.updatePrevNextImgBtnFunctionality(currentPlayedMediaIndex, event.playlist.mediaListSize)
            playbackControlsManager?.addPlaylistButtonsListener()
        }

        player?.addListener(this, PlaylistEvent.playlistLoopStateChanged) { event ->
            log.d("PLAYLIST playlistLoopStateChanged " + event.mode)
        }

        player?.addListener(this, PlaylistEvent.playlistAutoContinueStateChanged) { event ->
            log.d("PLAYLIST playlistAutoContinueStateChanged " + event.mode)
        }

        player?.addListener(this, PlaylistEvent.playListEnded) { event ->
            log.d("PLAYLIST playListEnded")
            var loopEnabld = player?.playlistController?.isLoopEnabled ?: false
            if (!loopEnabld) {
                playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.replay)
                progressBar?.setVisibility(View.GONE)
                playbackControlsManager?.showControls(View.VISIBLE)

            }
        }

        player?.addListener(this, PlaylistEvent.playListError) { event ->
            log.d("PLAYLIST playListError")
            Toast.makeText(this, event.error.message, Toast.LENGTH_SHORT).show()
        }

        player?.addListener(this, PlaylistEvent.playListLoadMediaError) { event ->
            log.d("PLAYLIST PlaylistLoadMediaError")
            Toast.makeText(this, event.error.message, Toast.LENGTH_SHORT).show()
            if (playbackControlsManager != null) {
                playbackControlsManager?.setContentPlayerState(PlayerEvent.Type.ERROR)
                playbackControlsManager?.handleContainerClick()
            }
        }

        player?.addListener(this, PlaylistEvent.playlistCountDownStart) { event ->
            var message = "playlistCountDownStart index = ${event.currentPlayingIndex} durationMS = ${event.playlistCountDownOptions?.durationMS}"
            log.d("PLAYLIST $message")
            showMessage(message)
            //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        player?.addListener(this, PlaylistEvent.playlistCountDownEnd) { event ->
            var message = "playlistCountDownEnd index = ${event.currentPlayingIndex} durationMS = ${event.playlistCountDownOptions?.durationMS}"
            log.d("PLAYLIST $message" )
            showMessage(message)
            //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        player?.addListener(this, PlayerEvent.textTrackChanged) { event ->
            log.d("PLAYER textTrackChanged")
            tracksSelectionController?.tracks?.let {
                for (i in 0 .. it.textTracks.size - 1) {
                    log.d(it.textTracks.size.toString() + ", PLAYER textTrackChanged " + it.textTracks[i].uniqueId + "/" + event.newTrack.getUniqueId())
                    if (event.newTrack.getUniqueId() == it.textTracks[i].uniqueId) {
                        tracksSelectionController?.setTrackLastSelectionIndex(TRACK_TYPE_TEXT, i)
                        break
                    }
                }
            }
        }

        player?.addListener(this, PlayerEvent.audioTrackChanged) { event ->
            log.d("PLAYER audioTrackChanged")
            tracksSelectionController?.tracks?.let {
                for (i in 0 .. it.audioTracks.size - 1) {
                    if (event.newTrack.getUniqueId() == it.audioTracks[i].uniqueId) {
                        tracksSelectionController?.setTrackLastSelectionIndex(TRACK_TYPE_AUDIO, i)
                        break
                    }
                }
            }
        }

        player?.addListener(this, PlayerEvent.videoTrackChanged) { event ->
            log.d("PLAYER videoTrackChanged")
            tracksSelectionController?.tracks?.let {
                for (i in 0 .. it.videoTracks.size - 1) {
                    if (event.newTrack.getUniqueId() == it.videoTracks[i].uniqueId) {
                        tracksSelectionController?.setTrackLastSelectionIndex(TRACK_TYPE_VIDEO, i)
                        break
                    }
                }
            }
        }

        player?.addListener(this, PlayerEvent.tracksAvailable) { event ->
            log.d("PLAYER tracksAvailable")
            updateEventsLogsList("player:\n" + event.eventType().name)
            //Obtain the actual tracks info from it.
            val tracks = event.tracksInfo
            tracksSelectionController = TracksSelectionController(this@PlayerActivity, player, tracks)
            playbackControlsManager?.setTracksSelectionController(tracksSelectionController)
            val defaultAudioTrackIndex = tracks.getDefaultAudioTrackIndex()
            val defaultTextTrackIndex = tracks.getDefaultTextTrackIndex()
            if (tracks.getAudioTracks().size > 0) {
                var labelAudio = tracks.getAudioTracks().get(defaultAudioTrackIndex).getLabel() ?: "";
                log.d("Default Audio lang = " + labelAudio)
            }
            if (tracks.getTextTracks().size > 0) {
                var labelText = tracks.getTextTracks().get(defaultTextTrackIndex).getLabel() ?: ""
                log.d("Default Text lang = " + labelText)
            }
            if (tracks.getVideoTracks().size > 0) {
                log.d("Default video isAdaptive = " + tracks.getVideoTracks().get(tracks.getDefaultAudioTrackIndex()).isAdaptive() + " bitrate = " + tracks.getVideoTracks().get(tracks.getDefaultAudioTrackIndex()).getBitrate())
            }
        }

        player?.addListener(this, PlayerEvent.error) { event ->
            log.d("PLAYER ERROR")
            if (event.error.isFatal()) {
                showMessage(getFullPlayerError(event))
                if (!(player?.playlistController?.isAutoContinueEnabled ?: true)) {
                    progressBar?.setVisibility(View.GONE)
                    playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.play)
                    playbackControlsManager?.showControls(View.VISIBLE)
                }
                progressBar?.setVisibility(View.INVISIBLE)
                //playbackControlsView?.getPlayPauseToggle()?.visibility = View.INVISIBLE
            }
            //playbackControlsManager?.setContentPlayerState(PlayerEvent.Type.ERROR)
        }

        player?.addListener(this, PlayerEvent.sourceSelected) { event ->
            log.d("PLAYER SOURCE SELECTED")
            updateEventsLogsList("player:\n" + event.eventType().name)
            log.d("Selected Source = " + event.source.getUrl())
        }

        player?.addListener(this, PlayerEvent.canPlay) { event ->
            log.d("PLAYER CAN PLAY")
            val vrController = player?.getController(VRController::class.java)
            if (vrController != null) {
                vrController.setOnClickListener { v ->
                    //application code for handaling ui operations
                    if (playbackControlsManager != null) {
                        playbackControlsManager?.handleContainerClick()
                    }
                }
            } else {
                if (adCuePoints != null && IMADAIPlugin.factory.name == adCuePoints?.getAdPluginName()) {
                    if (!initOptions.autoplay) {
                        playbackControlsManager?.showControls(View.VISIBLE)
                    } else {
                        playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.pause)
                    }
                }
            }
            updateEventsLogsList("player:\n" + event.eventType().name)
        }

        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("PLAYER stateChangeEvent " + event.eventType().name + " = " + event.newState)
            updateEventsLogsList("player:\n" + event.eventType().name + ":" + event.newState)

            when (event.newState) {
                PlayerState.IDLE -> log.d("StateChange Idle")
                PlayerState.LOADING -> log.d("StateChange Loading")
                PlayerState.READY -> {
                    log.d("StateChange Ready")
                    progressBar?.setVisibility(View.INVISIBLE)
                }
                PlayerState.BUFFERING -> {
                    log.d("StateChange Buffering")
                    val adController = player?.getController(AdController::class.java)
                    if (adController == null || adController != null && !adController.isAdDisplayed) {
                        progressBar?.setVisibility(View.VISIBLE)
                    }
                }
            }
        }

        player?.addListener(this, PlayerEvent.seeking) { event ->
            log.d("PLAYER SEEKING $event")
            val playerDuration = player?.duration ?: -1L
            val isPlayerPlaying = player?.isPlaying ?: false
            if (event.targetPosition < playerDuration)
                if (isPlayerPlaying) {
                    playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.pause)
                } else {
                    playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.play)
                    playbackControlsManager?.showControls(View.VISIBLE)
                }
            this@PlayerActivity.updateEventsLogsList("player:\n" + event.eventType().name)
        }

        player?.addListener(this, PlayerEvent.seeked) { event ->
            log.d("PLAYER SEEKED")
            updateEventsLogsList("player:\n" + event.eventType().name)
        }

        player?.addListener(this, KavaAnalyticsEvent.reportSent) { event ->
            val reportedEventName = event.reportedEventName
            if (PlayerEvent.Type.PLAYHEAD_UPDATED.name != reportedEventName) {
                updateEventsLogsList("kava:\n$reportedEventName")
            }
        }

        player?.addListener(this, YouboraEvent.reportSent) { event ->
            val reportedEventName = event.reportedEventName
            if (PlayerEvent.Type.PLAYHEAD_UPDATED.name != reportedEventName) {
                updateEventsLogsList("youbora:\n$reportedEventName")
            }

        }

        player?.addListener(this, PhoenixAnalyticsEvent.reportSent) { event ->
            val reportedEventName = event.reportedEventName
            if (PlayerEvent.Type.PLAYHEAD_UPDATED.name != reportedEventName) {
                updateEventsLogsList("phoenix:\n$reportedEventName")
            }
        }
    }

    private fun getFullPlayerError(event: PlayerEvent.Error): String? {
        try {
            val playerError = event.error
            val playerErrorException = playerError.exception as Exception?
            var errorMetadata = "Player error occurred."
            var exceptionClass : String? = null
            var exceptionCause = ""
            if (playerErrorException != null && playerErrorException.cause != null && playerErrorException.cause?.javaClass != null) {
                exceptionClass = playerErrorException.cause?.javaClass?.name
                errorMetadata = if (playerErrorException.cause.toString() != null) playerErrorException.cause.toString() else errorMetadata

                if (playerErrorException.cause is HttpDataSource.InvalidResponseCodeException) {
                    log.e("InvalidResponseCodeException " + (playerErrorException.cause as HttpDataSource.InvalidResponseCodeException).responseCode)
                }

                if (playerErrorException != null && playerErrorException.cause is UnknownHostException) {
                    log.e("UnknownHostException")
                }
            } else {
                exceptionCause = event.error.errorType.name + " - " + event.error.message
            }

            var msg = exceptionCause
            if (!TextUtils.isEmpty(msg)) {
                msg += "-"
            }
            return "$msg$errorMetadata $exceptionClass"
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return null
    }

    private fun updateEventsLogsList(eventMsg: String) {
        var eventMsg = eventMsg
        val date = Date()
        eventMsg = dateFormat.format(date) + " " + eventMsg
        eventsList.add(eventMsg)
        if (!TextUtils.isEmpty(searchLogPattern)) {
            if (eventMsg.toLowerCase().contains(searchLogPattern)) {
                searchedEventsList.add(eventMsg)
                eventsListRecyclerAdapter?.notifyData(searchedEventsList)
            }
        } else {
            eventsListRecyclerAdapter?.notifyData(eventsList)
        }
    }

    private fun convertPluginsJsonArrayToPKPlugins(pluginConfigs: JsonArray?): PKPluginConfigs {
        val pkPluginConfigs = PKPluginConfigs()
        val pluginDescriptors = gson.fromJson(pluginConfigs, Array<PluginDescriptor>::class.java)

        if (pluginDescriptors != null) {
            for (pluginDescriptor in pluginDescriptors) {
                val pluginName = pluginDescriptor.pluginName
                if (YouboraPlugin.factory.name.equals(pluginName, ignoreCase = true)) {
                    val youboraPlugin = gson.fromJson(pluginDescriptor.params?.get("options"), YouboraConfig::class.java)
                    pkPluginConfigs.setPluginConfig(YouboraPlugin.factory.name, youboraPlugin.toJson())
                } else if (KavaAnalyticsPlugin.factory.name.equals(pluginName, ignoreCase = true)) {
                    val kavaPluginConfig = gson.fromJson(pluginDescriptor.params, KavaAnalyticsConfig::class.java)
                    pkPluginConfigs.setPluginConfig(KavaAnalyticsPlugin.factory.name, kavaPluginConfig.toJson())
                } else if (IMAPlugin.factory.name.equals(pluginName, ignoreCase = true)) {
                    val imaPluginConfig = gson.fromJson(pluginDescriptor.params, UiConfFormatIMAConfig::class.java)
                    pkPluginConfigs.setPluginConfig(IMAPlugin.factory.name, imaPluginConfig.toJson())
                } else if (IMADAIPlugin.factory.name.equals(pluginName, ignoreCase = true)) {
                    val imaPluginConfig = gson.fromJson(pluginDescriptor.params, UiConfFormatIMADAIConfig::class.java)
                    pkPluginConfigs.setPluginConfig(IMADAIPlugin.factory.name, imaPluginConfig.toJson())
                } else if (PhoenixAnalyticsPlugin.factory.name.equals(pluginName, ignoreCase = true)) {
                    val phoenixAnalyticsConfig = gson.fromJson(pluginDescriptor.params, PhoenixAnalyticsConfig::class.java)
                    pkPluginConfigs.setPluginConfig(PhoenixAnalyticsPlugin.factory.name, phoenixAnalyticsConfig.toJson())
                } else if (FBInstreamPlugin.factory.name.equals(pluginName)) {
                    val fbInstreamConfig = gson.fromJson(pluginDescriptor.params, FBInstreamConfig::class.java);
                    pkPluginConfigs.setPluginConfig(FBInstreamPlugin.factory.getName(), fbInstreamConfig);
                }
            }
        }
        return pkPluginConfigs
    }

    override fun onSupportNavigateUp(): Boolean {
        backButtonPressed = true
        onBackPressed()
        return true
    }

    internal fun initDrm() {
        MediaSupport.initializeDrm(this) { supportedDrmSchemes, provisionPerformed, provisionError ->
            if (provisionPerformed) {
                if (provisionError != null) {
                    log.e("DRM Provisioning failed", provisionError)
                } else {
                    log.d("DRM Provisioning succeeded")
                }
            }
            log.d("DRM initialized; supported: $supportedDrmSchemes")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (player != null) {
            player?.removeListeners(this)
            player?.stop()
            player?.destroy()
            player = null
            eventsList.clear()
        }

        playbackControlsView?.let {
            it.destroy()
            playbackControlsView = null
        }
        networkChangeReceiver = null
    }

    fun setPlayer(player: KalturaPlayer?) {
        if (playbackControlsManager != null) {
            playbackControlsManager?.setAdPlayerState(null)
            playbackControlsManager?.setContentPlayerState(null)
        }
        this.player = player
        if (player == null) {
            log.e("Player is null")
            return
        }
        setPlayerListeners()
        val container = findViewById<ViewGroup>(R.id.player_container_layout)
        playbackControlsView = findViewById(R.id.player_controls)
        playbackControlsView?.setVisibility(View.INVISIBLE)
        container.viewTreeObserver.addOnGlobalLayoutListener(object: ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                container.viewTreeObserver.removeOnGlobalLayoutListener(this)
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    player.setPlayerView(ViewGroup.LayoutParams.MATCH_PARENT, 600)
                } else {
                    player.setPlayerView(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                }
                container.setOnClickListener { view ->
                    if (playbackControlsManager != null) {
                        playbackControlsManager?.handleContainerClick()
                    }
                }
                container.addView(player.playerView)
                playbackControlsView?.setPlayer(player)
            }
        })
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checking the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            supportActionBar?.hide()
            searchView?.setVisibility(View.GONE)
            eventsListView?.setVisibility(View.GONE)
            //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            player?.setPlayerView(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)

        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
            //unhide your objects here.
            supportActionBar?.show()
            searchView?.setVisibility(View.VISIBLE)
            eventsListView?.setVisibility(View.VISIBLE)
            player?.setPlayerView(ViewGroup.LayoutParams.MATCH_PARENT, 600)
        }
    }

    private fun isPlaybackEndedState(): Boolean {

        return playbackControlsManager?.playerState === PlayerEvent.Type.ENDED || allAdsCompeted && isPostrollAvailableInAdCuePoint() && ((player?.currentPosition ?: -1) >= (player?.duration ?: 0))
    }

    private fun isPostrollAvailableInAdCuePoint(): Boolean {
        return adCuePoints?.hasPostRoll() ?: false
    }

    override fun onResume() {
        log.d("Player Activity onResume")
        super.onResume()
        NetworkChangeReceiver.getObservable().addObserver(this)
        this.registerReceiver(networkChangeReceiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        if (isFirstOnResume) {
            isFirstOnResume = false
            return
        }
        if (player != null) {
            log.d("onResume -> Player Activity onResume")
            player?.onApplicationResumed()
            setPlayerListeners()
            if (isPlayingOnPause) {
                isPlayingOnPause = false
                if (playbackControlsView != null && playbackControlsView?.getPlayPauseToggle() != null) {
                    playbackControlsView?.getPlayPauseToggle()?.setBackgroundResource(R.drawable.play)
                }
            }
        }
    }

    override fun onPause() {
        player?.let {
            if (it.isPlaying()) {
                isPlayingOnPause = true
            }
            it.removeListeners(this)
            it.onApplicationPaused()
        }

        super.onPause()
        unregisterReceiver(networkChangeReceiver)
        NetworkChangeReceiver.getObservable().deleteObserver(this)
        adCuePoints?.let {
            if (FBInstreamPlugin.factory.getName().equals(it.getAdPluginName())) {
                return;
            }
        }

        if (!backButtonPressed && playbackControlsManager != null) {
            playbackControlsManager?.showControls(View.VISIBLE)
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            backButtonPressed = true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun update(observable: Observable, objectStatus: Any) {
        val isConnected = objectStatus as Boolean
        if (isConnected) {
            onNetworkConnected()
        } else {
            onNetworkDisConnected()
        }
    }

    protected fun onNetworkConnected() {
        showMessage(R.string.network_connected)
        if (player != null) {
            player?.onApplicationResumed()
            player?.play()
        }
    }

    protected fun onNetworkDisConnected() {
        showMessage(R.string.network_disconnected)
        if (player != null) {
            player?.onApplicationPaused()
        }
    }

    private fun showMessage(string: Int) {
        val itemView = findViewById<RelativeLayout>(R.id.player_container)
        val snackbar = Snackbar.make(itemView, string, Snackbar.LENGTH_LONG)
        snackbar.show()
    }

    private fun showMessage(string: String?) {
        val itemView = findViewById<RelativeLayout>(R.id.player_container)
        val snackbar = Snackbar.make(itemView, string ?: "", Snackbar.LENGTH_LONG)
        snackbar.show()
    }
}