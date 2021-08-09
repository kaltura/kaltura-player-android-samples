package com.kaltura.playkit.samples.prefetchsample.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_LONG
import androidx.appcompat.app.AlertDialog.Builder
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.kaltura.dtg.DownloadItem
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKPluginConfigs
import com.kaltura.playkit.PlayerEvent.*
import com.kaltura.playkit.Utils
import com.kaltura.playkit.player.AudioTrack
import com.kaltura.playkit.player.PKTracks
import com.kaltura.playkit.player.TextTrack
import com.kaltura.playkit.player.VideoTrack
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.playkit.plugins.kava.KavaAnalyticsConfig
import com.kaltura.playkit.plugins.kava.KavaAnalyticsPlugin
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsConfig
import com.kaltura.playkit.plugins.ott.PhoenixAnalyticsPlugin
import com.kaltura.playkit.plugins.youbora.YouboraPlugin
import com.kaltura.playkit.plugins.youbora.pluginconfig.YouboraConfig
import com.kaltura.playkit.samples.prefetchsample.*
import com.kaltura.playkit.samples.prefetchsample.R
import com.kaltura.playkit.samples.prefetchsample.data.AppConfig
import com.kaltura.playkit.samples.prefetchsample.data.PluginDescriptor
import com.kaltura.playkit.samples.prefetchsample.data.UiConfFormatIMAConfig
import com.kaltura.tvplayer.*
import com.kaltura.tvplayer.config.PhoenixTVPlayerParams
import com.npaw.youbora.lib6.plugin.Options
import kotlinx.android.synthetic.main.activity_play.*
import kotlinx.android.synthetic.main.content_play.*

private val log = PKLog.get("PlayActivity")

class PlayActivity : AppCompatActivity() {

    private var manager: OfflineManager? = null
    private var player: KalturaPlayer? = null
    private lateinit var playDrawable: Drawable
    private lateinit var pauseDrawable: Drawable

    private var audioTracks: List<AudioTrack>? = null
    private var textTracks: List<TextTrack>? = null
    private var videoTracks: List<VideoTrack>? = null

    private var currentTextTrack: TextTrack? = null
    private var currentAudioTrack: AudioTrack? = null
    private var currentVideoTrack: VideoTrack? = null

    private var testItems: List<Item>? = null
    private var currentItemPlayingPosition: Int = 0
    private var isOnlinePlayback: Boolean = true
    private var partnerId: Int = 0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play)

        playerRoot.systemUiVisibility = SYSTEM_UI_FLAG_FULLSCREEN

        playDrawable = ContextCompat.getDrawable(this@PlayActivity, R.drawable.exo_ic_play_circle_filled)!!
        pauseDrawable = ContextCompat.getDrawable(this@PlayActivity, R.drawable.ic_pause_white_24dp)!!

        val bundle = intent.getBundleExtra("assetBundle")
        isOnlinePlayback = bundle?.getBoolean("isOnlinePlayback") ?: false
        var itemIndexPosition = bundle?.getInt("position") ?: -1
        currentItemPlayingPosition = itemIndexPosition
        var startPosition = bundle?.getLong("startPosition") ?: -1
        partnerId = bundle?.getInt("partnerId") ?: 0

        val itemsJson = Utils.readAssetToString(this, "appConfig.json")
        val gson = Gson()
        val appConfig = gson.fromJson(itemsJson, AppConfig::class.java)
        testItems = appConfig.items.map { it.toItem() }
        var testItem = testItems?.get(itemIndexPosition)

        val options = PlayerInitOptions(partnerId).apply {
            autoplay = true
            allowCrossProtocolEnabled = true
            offlineProvider = MainActivity.offlineProvider
        }
        manager = OfflineManager.getInstance(this, options.offlineProvider)

        if (isOnlinePlayback) {
            if (testItem?.id() != null) {
                    val entry = manager?.getLocalPlaybackEntry(testItem.id())
                    if (entry?.sources != null) {
                        playAssetOffline(isOnlinePlayback, testItem?.id(), options, startPosition, testItem)
                    } else {
                        testItems?.let { itemList ->
                            playAssetOnline(itemList, itemIndexPosition, options)
                        }
                    }
                } else {
                Toast.makeText(this, "No asset id given", LENGTH_LONG).show()
            }
        } else {

                var testItem = testItems?.get(itemIndexPosition)
                if (testItem?.id() != null) {
                    playAssetOffline(isOnlinePlayback, testItem?.id(), options, startPosition, testItem)
                } else {
                    Toast.makeText(this, "No asset id given", LENGTH_LONG).show()
                }
        }

        player?.setPlayerView(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        playerRoot.addView(player?.playerView)

        fab_playpause.setOnClickListener {
            togglePlayPause()
        }

        fab_replay.setOnClickListener {
            player?.replay()
        }

        fab_change_media.setOnClickListener {

            val itemsJson = Utils.readAssetToString(this, "appConfig.json")
            val gson = Gson()
            val appConfig = gson.fromJson(itemsJson, AppConfig::class.java)
            testItems = appConfig.items.map { it.toItem() }
            if (testItems != null) {

                if (currentItemPlayingPosition + 1 == testItems?.size) {
                    currentItemPlayingPosition = 0
                } else {
                    currentItemPlayingPosition += 1
                }

                val options = PlayerInitOptions(partnerId).apply {
                    autoplay = true
                    allowCrossProtocolEnabled = true
                    offlineProvider = MainActivity.offlineProvider
                }

                var testItem = testItems?.get(currentItemPlayingPosition)
                if (testItem?.id() != null) {
                    val entry = manager?.getLocalPlaybackEntry(testItem.id())
                    if (entry?.sources != null) {
                        playAssetOffline(
                            isOnlinePlayback,
                            testItem.id(),
                            options,
                            startPosition,
                            testItem
                        )
                    } else {
                        testItems?.let { itemList ->
                            playAssetOnline(itemList, currentItemPlayingPosition, options)
                        }
                    }
                } else {
                    Toast.makeText(this, "No asset id given", LENGTH_LONG).show()
                }
            }
        }

        fab_replay_10.setOnClickListener {
            if (player != null) {
                var pos = player?.currentPosition ?: 10000
                player?.seekTo(pos - 10000)
            }
        }

        fab_forward_10.setOnClickListener {
            if (player != null) {
                var pos = player?.currentPosition ?: -10000
                player?.seekTo(pos + 10000)
            }
        }

        fab_video_track.setOnClickListener {
            selectPlayerTrack(DownloadItem.TrackType.VIDEO)
        }

        fab_audio_track.setOnClickListener {
            selectPlayerTrack(DownloadItem.TrackType.AUDIO)
        }

        fab_text_track.setOnClickListener {
            selectPlayerTrack(DownloadItem.TrackType.TEXT)
        }

        addPlayerEventListeners()
    }

    private fun convertPluginsJsonArrayToPKPlugins(pluginConfigs: JsonArray?, setPlugin: Boolean): PKPluginConfigs {
        val pkPluginConfigs = PKPluginConfigs()
        val pluginDescriptors = Gson().fromJson(pluginConfigs, Array<PluginDescriptor>::class.java)
        val errorMessage = "plugin list size is less than the requested played media index."

        if (pluginDescriptors != null) {
            for (pluginDescriptor in pluginDescriptors) {
                val pluginName = pluginDescriptor.pluginName
                if (YouboraPlugin.factory.name.equals(pluginName, ignoreCase = true)) {
                    if (pluginDescriptor.params != null) {
                        var youboraPluginConfig: YouboraConfig? = null
                        var youboraPluginBundle: Bundle? = null
                        val isBundle = pluginDescriptor.isBundle
                        when (pluginDescriptor.params) {
                            is JsonObject -> {
                                youboraPluginConfig = Gson().fromJson((pluginDescriptor.params as JsonObject).get("options"), YouboraConfig::class.java)
                                youboraPluginConfig?.let {
                                    if (isBundle == true) {
                                        youboraPluginBundle = getYouboraBundle(it)
                                    }
                                }
                            }

                            is JsonArray -> {
                                var config: JsonElement? = null
                                val pluginValue: JsonArray? = (pluginDescriptor.params as JsonArray)
                                pluginValue?.let {
                                    if (pluginValue.size() > 0) {
                                        config = (pluginDescriptor.params as JsonArray).get(0).asJsonObject.get("config").asJsonObject.get("options")
                                    } else {
                                        config = null
                                        log.e("$pluginName  $errorMessage")
                                    }
                                }

                                config?.let {
                                    youboraPluginConfig = Gson().fromJson(config, YouboraConfig::class.java)
                                }

                                youboraPluginConfig?.let {
                                    if (isBundle == true) {
                                        youboraPluginBundle = getYouboraBundle(it)
                                    }
                                }
                            }
                        }
                        youboraPluginConfig?.let {
                            if (setPlugin) {
                                pkPluginConfigs.setPluginConfig(YouboraPlugin.factory.name, if (isBundle == true) youboraPluginBundle else it.toJson())
                            } else {
                                player?.updatePluginConfig(YouboraPlugin.factory.name, if (isBundle == true) youboraPluginBundle else it.toJson())
                            }
                        }
                    }
                } else if (KavaAnalyticsPlugin.factory.name.equals(pluginName, ignoreCase = true)) {
                    val kavaPluginConfig = Gson().fromJson(pluginDescriptor.params as JsonObject, KavaAnalyticsConfig::class.java)
                    pkPluginConfigs.setPluginConfig(KavaAnalyticsPlugin.factory.name, kavaPluginConfig.toJson())
                } else if (IMAPlugin.factory.name.equals(pluginName, ignoreCase = true)) {
                    if (pluginDescriptor.params != null) {
                        var imaPluginConfig: UiConfFormatIMAConfig? = null
                        when (pluginDescriptor.params) {
                            is JsonObject -> {
                                imaPluginConfig = Gson().fromJson(pluginDescriptor.params as JsonObject, UiConfFormatIMAConfig::class.java)
                            }

                            is JsonArray -> {
                                var config: JsonElement? = null
                                val pluginValue: JsonArray? = (pluginDescriptor.params as JsonArray)
                                pluginValue?.let {
                                    if (pluginValue.size() > 0) {
                                        config = (pluginDescriptor.params as JsonArray).get(0).asJsonObject.get("config")
                                    } else {
                                        config = null
                                        log.e("$pluginName  $errorMessage")
                                    }
                                }

                                config?.let {
                                    imaPluginConfig = Gson().fromJson(config, UiConfFormatIMAConfig::class.java)
                                }
                            }
                        }

                        imaPluginConfig?.let {
                            if (setPlugin) {
                                pkPluginConfigs.setPluginConfig(IMAPlugin.factory.name, it.toJson())
                            } else {
                                player?.updatePluginConfig(IMAPlugin.factory.name, it.toJson())
                            }
                        }
                    }
                } else if (PhoenixAnalyticsPlugin.factory.name.equals(pluginName, ignoreCase = true)) {
                    if (pluginDescriptor.params != null) {
                        var phoenixAnalyticsConfig: PhoenixAnalyticsConfig? = null
                        when (pluginDescriptor.params) {
                            is JsonObject -> {
                                phoenixAnalyticsConfig = Gson().fromJson(pluginDescriptor.params as JsonObject, PhoenixAnalyticsConfig::class.java)
                            }

                            is JsonArray -> {
                                var config: JsonElement? = null
                                val pluginValue: JsonArray? = (pluginDescriptor.params as JsonArray)
                                pluginValue?.let {
                                    if (pluginValue.size() > 0) {
                                        config = (pluginDescriptor.params as JsonArray).get(0)
                                    } else {
                                        config = null
                                        log.e("$pluginName  $errorMessage")
                                    }
                                }

                                config?.let {
                                    phoenixAnalyticsConfig = Gson().fromJson(config, PhoenixAnalyticsConfig::class.java)
                                }
                            }
                        }
                        phoenixAnalyticsConfig?.let {
                            if (setPlugin) {
                                pkPluginConfigs.setPluginConfig(PhoenixAnalyticsPlugin.factory.name, it.toJson())
                            } else {
                                player?.updatePluginConfig(PhoenixAnalyticsPlugin.factory.name, it.toJson())
                            }
                        }
                    }
                }
            }
        }
        return pkPluginConfigs
    }

    private fun getYouboraBundle(youboraPluginConfig: YouboraConfig): Bundle {

        val optBundle = Bundle()

        //Youbora config bundle. Main config goes here.
        optBundle.putString(Options.KEY_ACCOUNT_CODE, youboraPluginConfig.accountCode)
        optBundle.putString(Options.KEY_USERNAME, youboraPluginConfig.username)
        optBundle.putBoolean(Options.KEY_ENABLED, true)
        optBundle.putString(Options.KEY_APP_NAME, "TestApp");
        optBundle.putString(Options.KEY_APP_RELEASE_VERSION, "v1.0");

        //Media entry bundle.
        optBundle.putString(Options.KEY_CONTENT_TITLE, youboraPluginConfig.content?.contentTitle)

        //Optional - Device bundle o/w youbora will decide by its own.
        optBundle.putString(Options.KEY_DEVICE_CODE, youboraPluginConfig.device?.deviceCode)
        optBundle.putString(Options.KEY_DEVICE_BRAND, youboraPluginConfig.device?.deviceBrand)
        optBundle.putString(Options.KEY_DEVICE_MODEL, youboraPluginConfig.device?.deviceModel)
        optBundle.putString(Options.KEY_DEVICE_TYPE, youboraPluginConfig.device?.deviceType)
        optBundle.putString(Options.KEY_DEVICE_OS_NAME, youboraPluginConfig.device?.deviceOsName)
        optBundle.putString(Options.KEY_DEVICE_OS_VERSION, youboraPluginConfig.device?.deviceOsVersion)

        //Youbora ads configuration bundle.
        optBundle.putString(Options.KEY_AD_CAMPAIGN, youboraPluginConfig.ads?.adCampaign)

        //Configure custom properties here:
        optBundle.putString(Options.KEY_CONTENT_GENRE, youboraPluginConfig.properties?.genre)
        optBundle.putString(Options.KEY_CONTENT_TYPE, youboraPluginConfig.properties?.type)
        optBundle.putString(Options.KEY_CONTENT_TRANSACTION_CODE, youboraPluginConfig.properties?.transactionType)
        optBundle.putString(Options.KEY_CONTENT_CDN, youboraPluginConfig.properties?.contentCdnCode)

        optBundle.putString(Options.KEY_CONTENT_PRICE, youboraPluginConfig.properties?.price)
        optBundle.putString(Options.KEY_CONTENT_ENCODING_AUDIO_CODEC, youboraPluginConfig.properties?.audioType)
        optBundle.putString(Options.KEY_CONTENT_CHANNEL, youboraPluginConfig.properties?.audioChannels)

        val contentMetadataBundle = Bundle()

        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_YEAR, youboraPluginConfig.properties?.year)
        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_CAST, youboraPluginConfig.properties?.cast)
        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_DIRECTOR, youboraPluginConfig.properties?.director)
        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_OWNER, youboraPluginConfig.properties?.owner)
        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_PARENTAL, youboraPluginConfig.properties?.parental)
        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_RATING, youboraPluginConfig.properties?.rating)
        contentMetadataBundle.putString(YouboraConfig.KEY_CONTENT_METADATA_QUALITY, youboraPluginConfig.properties?.quality)

        optBundle.putBundle(Options.KEY_CONTENT_METADATA, contentMetadataBundle)

        //You can add some extra params here:
        optBundle.putString(Options.KEY_CUSTOM_DIMENSION_1, youboraPluginConfig.contentCustomDimensions?.contentCustomDimension1)
        optBundle.putString(Options.KEY_CUSTOM_DIMENSION_2, youboraPluginConfig.contentCustomDimensions?.contentCustomDimension2)

        return optBundle
    }

    private fun addPlugins(item: Item?, playerInitOptions: PlayerInitOptions) {
        if (item == null) {
            return
        }
        val appPluginConfigJsonArray = item.plugins
        if (item.plugins != null) {
            val pkPluginConfigs = convertPluginsJsonArrayToPKPlugins(appPluginConfigJsonArray, true)
            playerInitOptions.setPluginConfigs(pkPluginConfigs)
        }
    }

    private fun playAssetOffline(isOnlinePlayback: Boolean, assetId: String, options: PlayerInitOptions, startPosition: Long?, item: Item?) {
        if (isOnlinePlayback) {
            addPlugins(item, options)
        }
        if (player == null) {
            if (item?.playerType == KalturaPlayer.Type.ovp) {
                player = KalturaOvpPlayer.create(this, options)
            } else if (item?.playerType == KalturaPlayer.Type.ott) {
                player = KalturaOttPlayer.create(this, options)
            }else {
                player = KalturaBasicPlayer.create(this, options)
            }
        }
        val entry = manager?.getLocalPlaybackEntry(assetId)
        player?.setMedia(entry, startPosition)
    }

    private fun playAssetOnline(itemList: List<Item>, position: Int, options: PlayerInitOptions) {
        when (val item: Item = itemList[position]) {
            is OTTItem -> {
                addPlugins(item, options)
                if (player == null) {
                    player = KalturaOttPlayer.create(this, options)
                }
                player?.loadMedia(item.mediaOptions()) { mediaOptions, entry, error ->
                    if (error != null) {
                        log.d("OTTMedia Error error = " + error.message + " Extra = " + error.extra)
                        runOnUiThread {
                            Snackbar.make(
                                findViewById<View>(android.R.id.content),
                                error.message,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        log.d("OTTMediaAsset onEntryLoadComplete entry =" + entry.id)
                    }
                }
            }
            is OVPItem -> {
                addPlugins(item, options)
                if (player == null) {
                    player = KalturaOvpPlayer.create(this, options)
                }

                player?.loadMedia(item.mediaOptions()) { mediaOptions, entry, error ->
                    if (error != null) {
                        log.d("OVPMedia Error error = " + error.message + " Extra = " + error.extra)
                        runOnUiThread {
                            Snackbar.make(
                                findViewById<View>(android.R.id.content),
                                error.message,
                                Snackbar.LENGTH_LONG
                            ).show()
                        }
                    } else {
                        log.d("OVPMediaAsset onEntryLoadComplete entry =" + entry.id)
                    }
                }
            }
            is BasicItem -> {
                item.entry?.let {
                    addPlugins(item, options)
                    if (player == null) {
                        player = KalturaBasicPlayer.create(this, options)
                    }
                    player?.setMedia(it, item.startPosition)
                }
            }
            else -> {
                Toast.makeText(this, "No Player Type found", LENGTH_LONG).show()
            }
        }
    }

    private fun selectPlayerTrack(trackType: DownloadItem.TrackType) {
        val trackTitles = arrayListOf<String>()
        val trackIds = arrayListOf<String>()

        when (trackType) {
            DownloadItem.TrackType.AUDIO -> {
                val tracks = audioTracks
                val trackTitles = arrayListOf<String>()
                val trackIds = arrayListOf<String>()

                if (tracks != null) {
                    for (track in tracks) {
                        val language = track.language
                        if (language != null) {
                            trackIds.add(track.uniqueId)
                            trackTitles.add(language)
                        }
                    }
                }
                if (trackIds.size < 1) {
                    Toast.makeText(this, "No tracks to select from", LENGTH_LONG).show()
                    return
                }

                val currentTrack = currentAudioTrack
                val currentIndex =
                    if (currentTrack != null) trackIds.indexOf(currentTrack.uniqueId) else -1
                val selected = intArrayOf(currentIndex)
                Builder(this)
                    .setTitle("Select track")
                    .setSingleChoiceItems(trackTitles.toTypedArray(), selected[0]) { _, i ->
                        selected[0] = i
                    }
                    .setPositiveButton("OK") { _, _ ->
                        if (selected[0] >= 0) {
                            player?.changeTrack(trackIds[selected[0]])
                        }
                    }.show()
            }
            DownloadItem.TrackType.TEXT -> {
                val tracks = textTracks
                val trackTitles = arrayListOf<String>()
                val trackIds = arrayListOf<String>()
                if (tracks != null) {
                    for (track in tracks) {
                        val language = track.language
                        if (language != null) {
                            trackIds.add(track.uniqueId)
                            trackTitles.add(language)
                        }
                    }
                }
                if (trackIds.size < 1) {
                    Toast.makeText(this, "No tracks to select from", LENGTH_LONG).show()
                    return
                }

                val currentTrack = currentTextTrack
                val currentIndex =
                    if (currentTrack != null) trackIds.indexOf(currentTrack.uniqueId) else -1
                val selected = intArrayOf(currentIndex)
                Builder(this)
                    .setTitle("Select track")
                    .setSingleChoiceItems(trackTitles.toTypedArray(), selected[0]) { _, i ->
                        selected[0] = i
                    }
                    .setPositiveButton("OK") { _, _ ->
                        if (selected[0] >= 0) {
                            player?.changeTrack(trackIds[selected[0]])
                        }
                    }.show()
            }
            DownloadItem.TrackType.VIDEO -> {
                val tracks = videoTracks
                val trackTitles = arrayListOf<String>()
                val trackIds = arrayListOf<String>()
                if (tracks != null) {
                    for (track in tracks) {
                        val bitrate = track.bitrate
                        if (bitrate != null) {
                            trackIds.add(track.uniqueId)
                            if (bitrate == 0L) {
                                trackTitles.add("Auto")
                            } else {
                                trackTitles.add(bitrate.toString())
                            }
                        }
                    }
                }
                if (trackIds.size < 1) {
                    Toast.makeText(this, "No tracks to select from", LENGTH_LONG).show()
                    return
                }

                val currentTrack = currentVideoTrack
                val currentIndex =
                    if (currentTrack != null) trackIds.indexOf(currentTrack.uniqueId) else -1
                val selected = intArrayOf(currentIndex)
                Builder(this)
                    .setTitle("Select track")
                    .setSingleChoiceItems(trackTitles.toTypedArray(), selected[0]) { _, i ->
                        selected[0] = i
                    }
                    .setPositiveButton("OK") { _, _ ->
                        if (selected[0] >= 0) {
                            player?.changeTrack(trackIds[selected[0]])
                        }
                    }.show()
            }
        }
    }

    private fun addPlayerEventListeners() {

        player?.addListener(this, playing) {
            updatePlayPauseButton(true)
        }

        player?.addListener(this, tracksAvailable) {
            val tracksInfo: PKTracks = it.tracksInfo
            audioTracks = tracksInfo.audioTracks
            textTracks = tracksInfo.textTracks
            videoTracks = tracksInfo.videoTracks
            if (currentAudioTrack == null && audioTracks!!.isNotEmpty()) {
                currentAudioTrack = audioTracks!![tracksInfo.defaultAudioTrackIndex]
            }
            if (currentTextTrack == null && textTracks!!.isNotEmpty()) {
                currentTextTrack = textTracks!![tracksInfo.defaultTextTrackIndex]
            }
            if (currentVideoTrack == null && videoTracks!!.isNotEmpty()) {
                currentVideoTrack = videoTracks!![tracksInfo.defaultVideoTrackIndex]
            }
        }

        player?.addListener(this, audioTrackChanged) {
            currentAudioTrack = it.newTrack
            currentAudioTrack?.let { track ->
                log.d("currentAudioTrack: ${track.uniqueId} ${track.language}")
            }
        }

        player?.addListener(this, textTrackChanged) {
            currentTextTrack = it.newTrack
            log.d("currentTextTrack: $currentTextTrack")
        }

        player?.addListener(this, videoTrackChanged) {
            currentVideoTrack = it.newTrack
            log.d("currentVideoTrack: $currentVideoTrack")
        }

        player?.addListener(this, error) {
            var message: String? = it.error.message
            log.e("error: ${it.error.errorType} $message")
        }
    }

    private fun togglePlayPause() {
        val playing = player?.isPlaying ?: false

        if (playing) {
            player?.pause()
        } else {
            player?.play()
        }

        updatePlayPauseButton(!playing)
    }

    private fun updatePlayPauseButton(isPlaying: Boolean) {
        val next = if (isPlaying) pauseDrawable else playDrawable
        fab_playpause.setImageDrawable(next)
    }

    override fun onPause() {
        player?.let {
            if (it.isPlaying) {
                updatePlayPauseButton(it.isPlaying)
                player?.onApplicationPaused()
            }
        }
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        player?.let {
            updatePlayPauseButton(it.isPlaying)
            player?.onApplicationResumed()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.destroy()
    }
}
