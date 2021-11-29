package com.kaltura.playkit.samples.imasample

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.*
import com.kaltura.playkit.ads.*
import com.kaltura.playkit.player.AudioCodecSettings
import com.kaltura.playkit.plugins.ads.AdCuePoints
import com.kaltura.playkit.plugins.ads.AdEvent
import com.kaltura.playkit.plugins.ima.IMAConfig
import com.kaltura.playkit.plugins.ima.IMAPlugin
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), PlaybackControlsView.ChangeMediaListener {

    private val log = PKLog.get("MainActivity")

    private val START_POSITION = 0L // position for start playback in msec.
    private val FIRST_ASSET_ID = "548576"
    private val SECOND_ASSET_ID = "548577"

    // Ads configuration constants.
    private var preSkipAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/single_ad_samples&ciu_szs=300x250&impl=s&gdfp_req=1&env=vp&output=vast&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ct%3Dskippablelinear&correlator="
    private var preMidPostSingleAdTagUrl = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&cmsid=496&vid=short_onecue&correlator="
    private var ads5AdsEvery10Secs = "https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostlongpod&cmsid=496&vid=short_tencue&correlator="
    private var adCuePoints: AdCuePoints? = null
    private var adsPosition: MutableList<Long>? = null
    private lateinit var playedAdsPosition: MutableList<Boolean>
    private var adMarkersHashMap: MutableMap<Long, Boolean> = mutableMapOf()

    // Player
    private var player: KalturaPlayer? = null
    private var playerState: PlayerState? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        progressBar.visibility = View.GONE
        loadPlaykitPlayer()
    }

    private fun addPlayerStateListener() {
        player?.addListener(this, PlayerEvent.stateChanged) { event ->
            log.d("State changed from " + event.oldState + " to " + event.newState)
            playerState = event.newState
        }
    }

    override fun onPause() {
        log.d("onPause")
        super.onPause()
        playerControls?.release()
        player?.onApplicationPaused()
    }

    override fun onResume() {
        log.d("onResume")
        super.onResume()
        playerControls?.resume()
        player?.let { player ->
            playerState?.let {
                player.onApplicationResumed()
            }
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
        //  playerInitOptions.mediaEntryCacheConfig = MediaEntryCacheConfig(true)
        playerInitOptions.setAudioCodecSettings(AudioCodecSettings().setAllowMixedCodecs(true))

        // IMA Configuration
        val pkPluginConfigs = PKPluginConfigs()
        val adsConfig = getAdsConfig(preMidPostSingleAdTagUrl)
        pkPluginConfigs.setPluginConfig(IMAPlugin.factory.name, adsConfig)

        playerInitOptions.setPluginConfigs(pkPluginConfigs)

        player = KalturaOttPlayer.create(this@MainActivity, playerInitOptions)

        // player?.advertisingController?.playAdNow()
       // player?.setAdvertisingConfig(createAdvertisingConfig())
        player?.setAdvertisingConfig(getAdvertisingConfigJson())
        player?.setPlayerView(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        subscribeToAdEvents()
        val container = findViewById<ViewGroup>(R.id.player_root)
        container.addView(player?.playerView)

        playerControls?.setPlayer(player)
        playerControls.setChangeMediaListener(this)
        buildOttMediaOptions(FIRST_ASSET_ID)
        addPlayerStateListener()
    }

    private fun getAdvertisingConfigJson(): String {
        return "{\n" +
                "   \"advertising\": [\n" +
                "      {\n" +
                "         \"adBreakPositionType\": \"POSITION\",\n" +
                "         \"position\": 0,\n" +
                "         \"ads\": [\n" +
                "            [\n" +
                "               \"https://kaltura.github.io/playkit-admanager-samplasdasdasdases/vast/pod-inline-someskip.xml\",\n" +
                "               \"https://pubaasdasdasdasdasdasdasdasdsg.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0\",\n" +
                "               \"https://pubaasndjknasjdnasds.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0\",\n" +
                "               \"https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0\"\n" +
                "            ],\n" +
                "            [\n" +
                "               \"https://kaltasdasdura.gasdasithub.io/playasdasddkit-admanager-samples/vast/psaod-inline-someskip.xml\",\n" +
                "               \"https://kaltura.github.io/playkit-admanager-samples/vast/pod-inline-someskip.xml\",\n" +
                "               \"https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0\"\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {\n" +
                "         \"adBreakPositionType\": \"POSITION\",\n" +
                "         \"position\": 30,\n" +
                "         \"ads\": [\n" +
                "            [\n" +
                "               \"https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml\",\n" +
                "               \"https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml\",\n" +
                "               \"https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&cue=15000&vad_type=linear&vpos=midroll&pod=2&mridx=1&rmridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0\"\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {\n" +
                "         \"adBreakPositionType\": \"POSITION\",\n" +
                "         \"position\": -1,\n" +
                "         \"ads\": [\n" +
                "            [\n" +
                "               \"https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=postroll&pod=3&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0\"\n" +
                "            ]\n" +
                "         ]\n" +
                "      },\n" +
                "      {\n" +
                "         \"adBreakPositionType\": \"POSITION\",\n" +
                "         \"position\": 15,\n" +
                "         \"ads\": [\n" +
                "            [\n" +
                "               \"https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml\",\n" +
                "               \"https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml\",\n" +
                "               \"https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&cue=15000&vad_type=linear&vpos=midroll&pod=2&mridx=1&rmridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0\"\n" +
                "            ]\n" +
                "         ]\n" +
                "      }\n" +
                "   ],\n" +
                "   \"adTimeUnit\": \"SECONDS\"\n" +
                "}"
    }

    private fun createAdvertisingConfig(): AdvertisingConfig {
//        val prerollVastUrlList = listOf("https://kaltura.github.io/playkit-admanager-samples/vast/single-inline-noskip.xml")
//        val midrollVastUrlList = listOf("https://kaltura.github.io/playkit-admanager-samples/vast/single-inline-noskip.xml")
//        val postrollVastUrlList = listOf("https://kaltura.github.io/playkit-admanager-samples/vast/single-inline-noskip.xml")

//        val prerollVastUrlList = listOf("https://pubads.g.doubleclick.net/gampad/live/ads?slotname=/21633895671/QA/Android_Native_App/COI&sz=640x360&ciu_szs=&cust_params=sample_ar%3Dskippablelinear%26Gender%3DM%26Age%3D33%26KidsPinEnabled%3DN%26distinct_id%3D42c92f17603e4ee2b4232666b9591134%26AppVersion%3D0.1.80%26DeviceModel%3Dmoto+g(6)%26OptOut%3DFalse%26OSVersion%3D9%26PackageName%3Dcom.tv.v18.viola%26first_time%3DFalse%26logintype%3DTraditional&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&video_url_to_fetch=https%253A%252F%252Fwww.voot.com&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=65000&vrid=1095418&ppid=42c92f17603e4ee2b4232666b9591134&correlator=10771&lpr=true&cmsid=2467608&video_doc_id=0_im5ianso&kfa=0&tfcd=0",
//            "https://pubadsdadsasdasa.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
//            "https://kaltura.github.io/playkit-admanager-samples/vast/pod-inline-someskip.xml")
        val prerollVastUrlListWithWaterfallingAd1 = listOf("https://kaltura.github.io/playkit-admanager-samplasdasdasdases/vast/pod-inline-someskip.xml",
            "https://pubaasdasdasdasdasdasdasdasdsg.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
        "https://pubaasndjknasjdnasds.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
        "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")

        val prerollVastUrlListWithoutWaterfallingAd2 = listOf("https://kaltasdasdura.gasdasithub.io/playasdasddkit-admanager-samples/vast/psaod-inline-someskip.xml",
            "https://kaltura.github.io/playkit-admanager-samples/vast/pod-inline-someskip.xml",
        "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")
        val prerollAdPod = listOf(prerollVastUrlListWithWaterfallingAd1, prerollVastUrlListWithoutWaterfallingAd2)

        val midrollVastUrlList = listOf("https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml",
            "https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml",
            "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&cue=15000&vad_type=linear&vpos=midroll&pod=2&mridx=1&rmridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")
        val postrollVastUrlList = listOf("https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=postroll&pod=3&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")

        val midrollVastUrlListForEVERY = listOf("https://kalasdasdasdtura.github.asdasdasio/playkisdasdasdasdt-admanager-samples/vast/pod-inline-someskip.xml",
        "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&cue=15000&vad_type=linear&vpos=midroll&pod=2&mridx=1&rmridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")

        val prerollAdBreak = AdBreak(AdBreakPositionType.POSITION, 0, prerollAdPod)
        val midrollAdBreak1 = AdBreak(AdBreakPositionType.POSITION, 15, listOf(midrollVastUrlList))
        val midrollAdBreak2 = AdBreak(AdBreakPositionType.POSITION, 30, listOf(midrollVastUrlList))

        //val midrollAdBreak1 = AdBreak(AdBreakPositionType.EVERY, 10, listOf(midrollVastUrlListForEVERY))
//        val midrollAdBreak3 = AdBreak(45, midrollVastUrlList)
//        val midrollAdBreak4 = AdBreak(60, midrollVastUrlList)
//        val midrollAdBreak5 = AdBreak(90, midrollVastUrlList)
//        val midrollAdBreak6 = AdBreak(120, midrollVastUrlList)
        val postrollAdBreak = AdBreak(AdBreakPositionType.POSITION, -1, listOf(postrollVastUrlList))
//15, 30, 60, 90, 100 -> 75
        return AdvertisingConfig(listOf(prerollAdBreak
            // , midrollAdBreak6
             , midrollAdBreak1
            // , midrollAdBreak4
            //  , midrollAdBreak3
            , midrollAdBreak2
            //  , midrollAdBreak5
            , postrollAdBreak), AdTimeUnit.SECONDS)
    }

    private fun buildOttMediaOptions(assetId: String) {
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = assetId
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaAsset.ks = null

        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)
        ottMediaOptions.startPosition = START_POSITION

        player?.loadMedia(ottMediaOptions) { mediaOptions, entry, loadError ->
            if (loadError != null) {
                Snackbar.make(findViewById(android.R.id.content), loadError.message, Snackbar.LENGTH_LONG).show()
            } else {
                log.d("OTTMedia onEntryLoadComplete  entry = " + entry.id)
            }
        }
    }

    private fun getAdsConfig(adTagUrl: String): IMAConfig {
        val videoMimeTypes = ArrayList<String>()
        videoMimeTypes.add("video/mp4")
        videoMimeTypes.add("application/x-mpegURL")
        videoMimeTypes.add("application/dash+xml")
        return IMAConfig().setAdTagUrl(adTagUrl).setVideoMimeTypes(videoMimeTypes).enableDebugMode(true).setAlwaysStartWithPreroll(true).setAdLoadTimeOut(8)
    }

    private fun subscribeToAdEvents() {

        player?.addListener(this, AdEvent.adWaterFalling) { event ->

        }

        player?.addListener(this, AdEvent.adWaterFallingFailed) { event ->

        }

        player?.addListener(this, AdEvent.started) { event ->
            //Some events holds additional data objects in them.
            //In order to get access to this object you need first cast event to
            //the object it belongs to. You can learn more about this kind of objects in
            //our documentation.

            //Then you can use the data object itself.
            val adInfo = event.adInfo
            //Print to log content type of this ad.
            log.d("ad event received: " + event.eventType().name
                    + ". Additional info: ad content type is: "
                    + adInfo.getAdContentType())

            val adPodTime = event.adInfo.adPodTimeOffset
            adMarkersHashMap[adPodTime] = true
        }

        player?.addListener(this, AdEvent.contentResumeRequested) {
            log.d("ADS_PLAYBACK_ENDED")
            playerControls?.visibility = View.VISIBLE
            playerControls?.setSeekBarStateForAd(false)
            playerControls?.setPlayerState(PlayerState.READY)

            player?.let { player ->
                adCuePoints?.let {
                    if (it.hasPostRoll() && adMarkersHashMap.containsKey(-1L)) {
                        adMarkersHashMap.remove(-1L)
                        adMarkersHashMap[player.duration] = false
                    }
                }
            }

            adsPosition = ArrayList(adMarkersHashMap.keys)
            playedAdsPosition = ArrayList(adMarkersHashMap.values)

            adCuePoints?.let { adCuePoints ->
                adsPosition?.let {
                    playerControls.setAdMarkers(it.toLongArray(), playedAdsPosition.toBooleanArray(),  adCuePoints.adCuePoints.size)
                }
            }
        }

        player?.addListener(this, AdEvent.adBufferStart) {
            log.d("AdEvent.adBufferStart")
            progressBar.visibility = View.VISIBLE
        }

        player?.addListener(this, AdEvent.adBufferEnd) {
            log.d("AdEvent.adBreakEnded")
            progressBar.visibility = View.GONE
        }

        player?.addListener(this, AdEvent.contentPauseRequested) {
            log.d("AD_CONTENT_PAUSE_REQUESTED")
            playerControls?.visibility = View.INVISIBLE
        }

        player?.addListener(this, AdEvent.adPlaybackInfoUpdated) { event ->
            log.d("AD_PLAYBACK_INFO_UPDATED  = " + event.width + "/" + event.height + "/" + event.bitrate)
        }

        player?.addListener(this, AdEvent.skippableStateChanged) { event -> log.d("SKIPPABLE_STATE_CHANGED") }

        player?.addListener(this, AdEvent.adRequested) { event ->
            log.d("AD_REQUESTED adtag = " + event.adTagUrl)
        }

        player?.addListener(this, AdEvent.playHeadChanged) { event ->
            val adEventProress = event
            //Log.d(TAG, "received AD PLAY_HEAD_CHANGED " + adEventProress.adPlayHead);
        }

        player?.addListener(this, AdEvent.adBreakStarted) { event -> log.d("AD_BREAK_STARTED") }

        player?.addListener(this, AdEvent.cuepointsChanged) { event ->
            log.d("AD_CUEPOINTS_UPDATED")
            adCuePoints = event.cuePoints
            adCuePoints?.let { cuePoints ->
                for (adTime: Long in cuePoints.adCuePoints) {
                    adMarkersHashMap[adTime] = false
                }
            }
        }

        player?.addListener(this, AdEvent.loaded) { event ->
            log.d("AD_LOADED " + event.adInfo.getAdIndexInPod() + "/" + event.adInfo.getTotalAdsInPod())
        }

        player?.addListener(this, AdEvent.resumed) { event -> log.d("AD_RESUMED") }

        player?.addListener(this, AdEvent.paused) { event -> log.d("AD_PAUSED") }

        player?.addListener(this, AdEvent.skipped) { event -> log.d("AD_SKIPPED") }

        player?.addListener(this, AdEvent.allAdsCompleted) {
                event -> log.d("AD_ALL_ADS_COMPLETED")
            if (adCuePoints != null && adCuePoints?.hasPostRoll()!!) {
                playerControls?.setPlayerState(PlayerState.IDLE)
            }
        }

        player?.addListener(this, AdEvent.completed) { event -> log.d("AD_COMPLETED") }

        player?.addListener(this, AdEvent.firstQuartile) { event -> log.d("FIRST_QUARTILE") }

        player?.addListener(this, AdEvent.midpoint) { event ->
            log.d("MIDPOINT")
            if (player != null) {
                val adController =  player?.getController(AdController::class.java)
                if (adController != null) {
                    if (adController.isAdDisplayed) {
                        log.d("AD CONTROLLER API: " + adController.adCurrentPosition + "/" + adController.adDuration)
                    }
                    //Log.d(TAG, "adController.getCuePoints().getAdCuePoints().size());
                    //Log.d(TAG, adController.getAdInfo().toString());
                    //adController.skip();
                }
            }
        }

        player?.addListener(this, AdEvent.thirdQuartile) { event -> log.d("THIRD_QUARTILE") }

        player?.addListener(this, AdEvent.adBreakEnded) { event -> log.d("AD_BREAK_ENDED") }

        player?.addListener(this, AdEvent.adClickedEvent) { event ->
            log.d("AD_CLICKED url = " + event.clickThruUrl)
        }

        player?.addListener(this, AdEvent.error) { event ->
            log.d("AD_ERROR : " + event.error.errorType.name)
            if (event?.error != null) {
                playerControls?.setSeekBarStateForAd(false)
                log.e("ERROR: " + event.error.errorType + ", " + event.error.message)
            }
        }

        player?.addListener(this, PlayerEvent.error) { event -> log.d("PLAYER ERROR " + event.error.message!!) }

        //player?.addListener(this, PlayerEvent.me)
    }

    override fun changeMediaOnClick() {
        player?.let {
            if (it.isPlaying && it.mediaEntry.id.equals(FIRST_ASSET_ID)) {
                buildOttMediaOptions(SECOND_ASSET_ID)
            } else {
                buildOttMediaOptions(FIRST_ASSET_ID)
            }
        }
    }

    companion object {
        //Media entry configuration constants.
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        val PARTNER_ID = 3009
    }
}
