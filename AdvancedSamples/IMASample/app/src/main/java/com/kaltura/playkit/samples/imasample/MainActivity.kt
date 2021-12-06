package com.kaltura.playkit.samples.imasample

import android.os.Bundle
import android.text.TextUtils
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
    private var playedAdsPosition: MutableList<Boolean>? = null
    private var adMarkersHashMap: MutableMap<Long, Boolean> = mutableMapOf()

    // Player
    private var player: KalturaPlayer? = null
    private var playerState: PlayerState? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        PKLog.setGlobalLevel(PKLog.Level.verbose)
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
                "               \"<?xml version=\\\"1.0\\\" encoding=\\\"UTF-8\\\"?>\\r\\n<VAST xmlns:xsi=\\\"http://www.w3.org/2001/XMLSchema-instance\\\" xsi:noNamespaceSchemaLocation=\\\"vast4.xsd\\\" version=\\\"4.0\\\"><Ad id=\\\"ad1\\\" sequence=\\\"1\\\"><InLine><AdSystem>Kaltura</AdSystem><AdTitle>30 second skippable ad</AdTitle><Description><![CDATA[Single Inline]]></Description><Error><![CDATA[http://vast.example.com/track/error?code=[ERRORCODE]]]></Error><Impression><![CDATA[http://vast.example.com/track/impression]]></Impression><Creatives><Creative id=\\\"57859154776\\\" sequence=\\\"1\\\"><Linear skipoffset=\\\"00:00:05\\\"><Duration>00:00:30</Duration><TrackingEvents><Tracking event=\\\"acceptInvitationLinear\\\"><![CDATA[http://vast.example.com/track/acceptInvitationLinear]]></Tracking><Tracking event=\\\"acceptInvitation\\\"><![CDATA[http://vast.example.com/track/acceptInvitation]]></Tracking><Tracking event=\\\"playerCollapse\\\"><![CDATA[http://vast.example.com/track/playerCollapse]]></Tracking><Tracking event=\\\"playerExpand\\\"><![CDATA[http://vast.example.com/track/playerExpand]]></Tracking><Tracking event=\\\"closeLinear\\\"><![CDATA[http://vast.example.com/track/closeLinear]]></Tracking><Tracking event=\\\"complete\\\"><![CDATA[http://vast.example.com/track/complete]]></Tracking><Tracking event=\\\"creativeView\\\"><![CDATA[http://vast.example.com/track/creativeView]]></Tracking><Tracking event=\\\"firstQuartile\\\"><![CDATA[http://vast.example.com/track/firstQuartile]]></Tracking><Tracking event=\\\"midpoint\\\"><![CDATA[http://vast.example.com/track/midpoint]]></Tracking><Tracking event=\\\"mute\\\"><![CDATA[http://vast.example.com/track/mute]]></Tracking><Tracking event=\\\"otherAdInteraction\\\"><![CDATA[http://vast.example.com/track/otherAdInteraction]]></Tracking><Tracking event=\\\"pause\\\"><![CDATA[http://vast.example.com/track/pause]]></Tracking><Tracking event=\\\"playerCollapse\\\"><![CDATA[http://vast.example.com/track/playerCollapse]]></Tracking><Tracking event=\\\"playerExpand\\\"><![CDATA[http://vast.example.com/track/playerExpand]]></Tracking><Tracking event=\\\"progress\\\"><![CDATA[http://vast.example.com/track/progress]]></Tracking><Tracking event=\\\"resume\\\"><![CDATA[http://vast.example.com/track/resume]]></Tracking><Tracking event=\\\"rewind\\\"><![CDATA[http://vast.example.com/track/rewind]]></Tracking><Tracking event=\\\"skip\\\"><![CDATA[http://vast.example.com/track/skip]]></Tracking><Tracking event=\\\"start\\\"><![CDATA[http://vast.example.com/track/start]]></Tracking><Tracking event=\\\"thirdQuartile\\\"><![CDATA[http://vast.example.com/track/thirdQuartile]]></Tracking><Tracking event=\\\"timeSpentViewing\\\"><![CDATA[http://vast.example.com/track/timeSpentViewing]]></Tracking><Tracking event=\\\"unmute\\\"><![CDATA[http://vast.example.com/track/unmute]]></Tracking></TrackingEvents><MediaFiles><MediaFile id=\\\"1\\\" delivery=\\\"progressive\\\" width=\\\"640\\\" height=\\\"360\\\" type=\\\"video/mp4\\\" bitrate=\\\"227\\\" scalable=\\\"true\\\" maintainAspectRatio=\\\"true\\\"><![CDATA[https://cdnapisec.kaltura.com/p/2215841/playManifest/entryId/1_dx1anrn9/format/url/protocol/https/a.mp4]]></MediaFile><MediaFile id=\\\"1\\\" delivery=\\\"streaming\\\" width=\\\"640\\\" height=\\\"360\\\" type=\\\"application/x-mpegURL\\\" minBitrate=\\\"112\\\" maxBitrate=\\\"125\\\" scalable=\\\"true\\\" maintainAspectRatio=\\\"true\\\"><![CDATA[https://cdnapisec.kaltura.com/p/2215841/playManifest/entryId/1_dx1anrn9/format/applehttp/protocol/https/a.m3u8]]></MediaFile></MediaFiles></Linear></Creative></Creatives></InLine></Ad></VAST>\\r\\n\"\n" +
                "            ]\n" +
                "         ]\n" +
                "      }\n" +
                "   ],\n" +
                "   \"adType\": \"AD_RESPONSE\",\n" +
                "   \"adConfigUnit\": \"SECONDS\"\n" +
                "}"
    }

    private fun waterfallevery(): String {
        return "{\n" +
                "  \"advertising\": [\n" +
                "    {\n" +
                "      \"adBreakPositionType\": \"POSITION\",\n" +
                "      \"position\": 0,\n" +
                "      \"ads\": [\n" +
                "        [\n" +
                "          \"https://rrrrrexternaltests.dev.kaltura.com/standalonePlayer/Ads/adManager/customAdTags/vast/single_preroll_skip_terminator.xml\",\n" +
                "          \"https://rrrrrexternaltests.dev.kaltura.com/standalonePlayer/Ads/adManager/customAdTags/vast/single_preroll_skip_transformers.xml\",\n" +
                "          \"http://pubads.g.doubleclick.net/gampad/ads?sz=640x360&iu=/6062/iab_vast_samples/skippable&ciu_szs=300x250,728x90&impl=s&gdfp_req=1&env=vp&output=xml_vast2&unviewed_position_start=1&url=[referrer_url]&correlator=[timestamp]\"\n" +
                "        ]\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"adBreakPositionType\": \"EVERY\",\n" +
                "      \"position\": 20,\n" +
                "      \"ads\": [\n" +
                "        [\n" +
                "          \"https://kaltura.github.io/playkit-admanager-samples/vast/pod-inline-someskip.xml\"\n" +
                "        ]\n" +
                "      ]\n" +
                "    },\n" +
                "    {\n" +
                "      \"adBreakPositionType\": \"POSITION\",\n" +
                "      \"position\": -1,\n" +
                "      \"ads\": [\n" +
                "        [\n" +
                "          \"https://rrrrrexternaltests.dev.kaltura.com/standalonePlayer/Ads/adManager/customAdTags/vast/single_preroll_skip_terminator.xml\",\n" +
                "          \"https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=postroll&pod=3&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0\"\n" +
                "        ]\n" +
                "      ]\n" +
                "    }\n" +
                "  ],\n" +
                "  \"adTimeUnit\": \"SECONDS\"\n" +
                "}"
    }

    private fun getPlayAdNowConfigAdBreak(): AdBreak {
        val vastUrlList = listOf("https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml",
            "https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml",
            "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&cue=15000&vad_type=linear&vpos=midroll&pod=2&mridx=1&rmridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")
        val playAdNowVastAdBreak = AdBreak(AdBreakPositionType.POSITION, 15, listOf(vastUrlList))

        return playAdNowVastAdBreak
    }

    private fun createAdvertisingConfig1(): AdvertisingConfig {
//        val prerollVastUrlList = listOf("https://kaltura.github.io/playkit-admanager-samples/vast/single-inline-noskip.xml")
//        val midrollVastUrlList = listOf("https://kaltura.github.io/playkit-admanager-samples/vast/single-inline-noskip.xml")
//        val postrollVastUrlList = listOf("https://kaltura.github.io/playkit-admanager-samples/vast/single-inline-noskip.xml")

//        val prerollVastUrlList = listOf("https://pubads.g.doubleclick.net/gampad/live/ads?slotname=/21633895671/QA/Android_Native_App/COI&sz=640x360&ciu_szs=&cust_params=sample_ar%3Dskippablelinear%26Gender%3DM%26Age%3D33%26KidsPinEnabled%3DN%26distinct_id%3D42c92f17603e4ee2b4232666b9591134%26AppVersion%3D0.1.80%26DeviceModel%3Dmoto+g(6)%26OptOut%3DFalse%26OSVersion%3D9%26PackageName%3Dcom.tv.v18.viola%26first_time%3DFalse%26logintype%3DTraditional&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&video_url_to_fetch=https%253A%252F%252Fwww.voot.com&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=65000&vrid=1095418&ppid=42c92f17603e4ee2b4232666b9591134&correlator=10771&lpr=true&cmsid=2467608&video_doc_id=0_im5ianso&kfa=0&tfcd=0",
//            "https://pubadsdadsasdasa.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
//            "https://kaltura.github.io/playkit-admanager-samples/vast/pod-inline-someskip.xml")
        val prerollVastUrlListWithWaterfallingAdErrored = listOf("https://kaltura.test.1.github.io/playkit-admanager-samplasdasdasdases/vast/pod-inline-someskip.xml",
            "https://kaltura.github.test.io/playkit-admanager-samplasdasdasdases/vast/pod-inline-someskip.xml",
            "https://pubaasdasdasdasdasdasdasdasdsg.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
            "https://pubaasndjknasjdnasds.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
            "https://ssssssss.pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")
        val prerollVastUrlListWithWaterfallingAd1 = listOf("https://kaltura.github.io/playkit-admanager-samplasdasdasdases/vast/pod-inline-someskip.xml",
            "https://pubaasdasdasdasdasdasdasdasdsg.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
        "https://pubaasndjknasjdnasds.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
        "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")

        val prerollVastUrlListWithoutWaterfallingAd2 = listOf("https://kaltasdasdura.gasdasithub.io/playasdasddkit-admanager-samples/vast/psaod-inline-someskip.xml",
            "https://kaltura.github.io/playkit-admanager-samples/vast/pod-inline-someskip.xml",
        "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")

        val prerollVastUrlListWithWaterfallingAd3 = listOf("https://kaltura.github.io/playkit-admanager-samplasdasdasdases/vast/pod-inline-someskip.xml",
            "https://pubaasdasdasdasdasdasdasdasdsg.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
            "https://pubaasndjknasjdnasds.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
            "https://kaltura.github.io/playkit-admanager-samples/vast/pod-inline-single-ad-skip-no-learnmore.xml")

        val prerollAdPod = listOf(prerollVastUrlListWithWaterfallingAdErrored)
      //  val prerollAdPod = listOf(prerollVastUrlListWithWaterfallingAd1, prerollVastUrlListWithoutWaterfallingAd2, prerollVastUrlListWithWaterfallingAd3)

        val adBreakXML = listOf("<VAST xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"vast4.xsd\" version=\"4.0\">\n" +
                "<Ad id=\"ad1\" sequence=\"1\">\n" +
                "<InLine>\n" +
                "<AdSystem>Kaltura</AdSystem>\n" +
                "<AdTitle>30 second skippable ad</AdTitle>\n" +
                "<Description>\n" +
                "<![CDATA[ Single Inline ]]>\n" +
                "</Description>\n" +
                "<Error>\n" +
                "<![CDATA[ http://vast.example.com/track/error?code=[ERRORCODE] ]]>\n" +
                "</Error>\n" +
                "<Impression>\n" +
                "<![CDATA[ http://vast.example.com/track/impression ]]>\n" +
                "</Impression>\n" +
                "<Creatives>\n" +
                "<Creative id=\"57859154776\" sequence=\"1\">\n" +
                "<Linear skipoffset=\"00:00:05\">\n" +
                "<Duration>00:00:30</Duration>\n" +
                "<TrackingEvents>\n" +
                "<Tracking event=\"acceptInvitationLinear\">\n" +
                "<![CDATA[ http://vast.example.com/track/acceptInvitationLinear ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"acceptInvitation\">\n" +
                "<![CDATA[ http://vast.example.com/track/acceptInvitation ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"playerCollapse\">\n" +
                "<![CDATA[ http://vast.example.com/track/playerCollapse ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"playerExpand\">\n" +
                "<![CDATA[ http://vast.example.com/track/playerExpand ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"closeLinear\">\n" +
                "<![CDATA[ http://vast.example.com/track/closeLinear ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"complete\">\n" +
                "<![CDATA[ http://vast.example.com/track/complete ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"creativeView\">\n" +
                "<![CDATA[ http://vast.example.com/track/creativeView ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"firstQuartile\">\n" +
                "<![CDATA[ http://vast.example.com/track/firstQuartile ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"midpoint\">\n" +
                "<![CDATA[ http://vast.example.com/track/midpoint ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"mute\">\n" +
                "<![CDATA[ http://vast.example.com/track/mute ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"otherAdInteraction\">\n" +
                "<![CDATA[ http://vast.example.com/track/otherAdInteraction ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"pause\">\n" +
                "<![CDATA[ http://vast.example.com/track/pause ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"playerCollapse\">\n" +
                "<![CDATA[ http://vast.example.com/track/playerCollapse ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"playerExpand\">\n" +
                "<![CDATA[ http://vast.example.com/track/playerExpand ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"progress\">\n" +
                "<![CDATA[ http://vast.example.com/track/progress ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"resume\">\n" +
                "<![CDATA[ http://vast.example.com/track/resume ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"rewind\">\n" +
                "<![CDATA[ http://vast.example.com/track/rewind ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"skip\">\n" +
                "<![CDATA[ http://vast.example.com/track/skip ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"start\">\n" +
                "<![CDATA[ http://vast.example.com/track/start ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"thirdQuartile\">\n" +
                "<![CDATA[ http://vast.example.com/track/thirdQuartile ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"timeSpentViewing\">\n" +
                "<![CDATA[ http://vast.example.com/track/timeSpentViewing ]]>\n" +
                "</Tracking>\n" +
                "<Tracking event=\"unmute\">\n" +
                "<![CDATA[ http://vast.example.com/track/unmute ]]>\n" +
                "</Tracking>\n" +
                "</TrackingEvents>\n" +
                "<MediaFiles>\n" +
                "<MediaFile id=\"1\" delivery=\"progressive\" width=\"640\" height=\"360\" type=\"video/mp4\" bitrate=\"227\" scalable=\"true\" maintainAspectRatio=\"true\">\n" +
                "<![CDATA[ https://cdnapisec.kaltura.com/p/2215841/playManifest/entryId/1_dx1anrn9/format/url/protocol/https/a.mp4 ]]>\n" +
                "</MediaFile>\n" +
                "<MediaFile id=\"1\" delivery=\"streaming\" width=\"640\" height=\"360\" type=\"application/x-mpegURL\" minBitrate=\"112\" maxBitrate=\"125\" scalable=\"true\" maintainAspectRatio=\"true\">\n" +
                "<![CDATA[ https://cdnapisec.kaltura.com/p/2215841/playManifest/entryId/1_dx1anrn9/format/applehttp/protocol/https/a.m3u8 ]]>\n" +
                "</MediaFile>\n" +
                "</MediaFiles>\n" +
                "</Linear>\n" +
                "</Creative>\n" +
                "</Creatives>\n" +
                "</InLine>\n" +
                "</Ad>\n" +
                "</VAST>")
        val prerollAdPodXML = listOf(adBreakXML)

        val midrollVastUrlListError = listOf("https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml",
            "https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml",
            "https://pubadsdasdasdasdasdasd.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&cue=15000&vad_type=linear&vpos=midroll&pod=2&mridx=1&rmridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")
        val postrollVastUrlList = listOf("https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=postroll&pod=3&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")

        val midrollVastUrlListPlayabale = listOf("https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml",
            "https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml",
            "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&cue=15000&vad_type=linear&vpos=midroll&pod=2&mridx=1&rmridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")


        val midrollVastUrlListForEVERY = listOf("https://kalasdasdasdtura.github.asdasdasio/playkisdasdasdasdt-admanager-samples/vast/pod-inline-someskip.xml",
            "https://puasdasdasbads.g.doubleasdasdasdasdclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&cue=15000&vad_type=linear&vpos=midroll&pod=2&mridx=1&rmridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")

        val prerollAdBreak = AdBreak(AdBreakPositionType.POSITION, 0, prerollAdPod)
      //  val prerollAdBreak = AdBreak(AdBreakPositionType.POSITION, 0, prerollAdPodXML)
        val midrollAdBreak1 = AdBreak(AdBreakPositionType.POSITION, 10, listOf(midrollVastUrlListPlayabale))
        val midrollAdBreak2 = AdBreak(AdBreakPositionType.POSITION, 20, listOf(midrollVastUrlListPlayabale))
//        val midrollAdBreak3 = AdBreak(AdBreakPositionType.POSITION, 30, listOf(midrollVastUrlListForEVERY))
//        val midrollAdBreak4 = AdBreak(AdBreakPositionType.POSITION, 40, listOf(midrollVastUrlListPlayabale))
//        val midrollAdBreak5 = AdBreak(AdBreakPositionType.POSITION, 50, listOf(midrollVastUrlListError))
//        val midrollAdBreak6 = AdBreak(AdBreakPositionType.POSITION, 60, listOf(midrollVastUrlListForEVERY))
//        val midrollAdBreak7 = AdBreak(AdBreakPositionType.POSITION, 70, listOf(midrollVastUrlListPlayabale))




       //   val midrollAdBreak1 = AdBreak(AdBreakPositionType.EVERY, 10, listOf(midrollVastUrlListForEVERY))
//        val midrollAdBreak3 = AdBreak(45, midrollVastUrlList)
//        val midrollAdBreak4 = AdBreak(60, midrollVastUrlList)
//        val midrollAdBreak5 = AdBreak(90, midrollVastUrlList)
//        val midrollAdBreak6 = AdBreak(120, midrollVastUrlList)
        val postrollAdBreak = AdBreak(AdBreakPositionType.POSITION, -1, listOf(postrollVastUrlList))
//15, 30, 60, 90, 100 -> 75
        return AdvertisingConfig(listOf(
            prerollAdBreak
           //  , midrollAdBreak6
           ,   midrollAdBreak1
//             , midrollAdBreak4
//              , midrollAdBreak3
                , midrollAdBreak2
//              , midrollAdBreak5
//              , midrollAdBreak7
           ,  postrollAdBreak
        ), AdTimeUnit.SECONDS, AdType.AD_URL, -1)
    }

    private fun createAdvertisingConfig2(): AdvertisingConfig {
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

        val prerollVastUrlListWithWaterfallingAd3 = listOf("https://kaltura.github.io/playkit-admanager-samplasdasdasdases/vast/pod-inline-someskip.xml",
            "https://pubaasdasdasdasdasdasdasdasdsg.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
            "https://pubaasndjknasjdnasds.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=preroll&pod=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0",
            "https://kaltura.github.io/playkit-admanager-samples/vast/pod-inline-single-ad-skip-no-learnmore.xml")

        val prerollAdPod = listOf(prerollVastUrlListWithWaterfallingAd1)

        val midrollVastUrlList = listOf("https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml",
            "https://kalasdasdtura.gasdasdasithub.io/playkit-adsasdadmanager-samples/vast/pod-inline-someskip.xml",
            "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&cue=15000&vad_type=linear&vpos=midroll&pod=2&mridx=1&rmridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")
        val postrollVastUrlList = listOf("https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&vad_type=linear&vpos=postroll&pod=3&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")

        val midrollVastUrlListForEVERY = listOf("https://kalasdasdasdtura.github.asdasdasio/playkisdasdasdasdt-admanager-samples/vast/pod-inline-someskip.xml",
            "https://pubads.g.doubleclick.net/gampad/ads?slotname=/124319096/external/ad_rule_samples&sz=640x480&ciu_szs=300x250&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpost&url=&unviewed_position_start=1&output=xml_vast3&impl=s&env=vp&gdfp_req=1&ad_rule=0&cue=15000&vad_type=linear&vpos=midroll&pod=2&mridx=1&rmridx=1&ppos=1&lip=true&min_ad_duration=0&max_ad_duration=30000&vrid=6256&cmsid=496&video_doc_id=short_onecue&kfa=0&tfcd=0")

        val prerollAdBreak = AdBreak(AdBreakPositionType.POSITION, 0, prerollAdPod)
      //  val midrollAdBreak1 = AdBreak(AdBreakPositionType.POSITION, 15, prerollAdPod)
        val midrollAdBreak2 = AdBreak(AdBreakPositionType.POSITION, 13, listOf(midrollVastUrlList))

        //val midrollAdBreak1 = AdBreak(AdBreakPositionType.EVERY, 10, listOf(midrollVastUrlListForEVERY))
//        val midrollAdBreak3 = AdBreak(45, midrollVastUrlList)
//        val midrollAdBreak4 = AdBreak(60, midrollVastUrlList)
//        val midrollAdBreak5 = AdBreak(90, midrollVastUrlList)
//        val midrollAdBreak6 = AdBreak(120, midrollVastUrlList)
        val postrollAdBreak = AdBreak(AdBreakPositionType.POSITION, -1, listOf(postrollVastUrlList))
//15, 30, 60, 90, 100 -> 75
        return AdvertisingConfig(listOf(prerollAdBreak
            // , midrollAdBreak6
           // , midrollAdBreak1
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

        if (TextUtils.equals(assetId, FIRST_ASSET_ID)) {
            player?.setAdvertisingConfig(getAdvertisingConfigJson())
            //  player?.setAdvertisingConfig(createAdvertisingConfig1())
        } else {
           // player?.setAdvertisingConfig(null as AdvertisingConfig?)
              player?.setAdvertisingConfig(createAdvertisingConfig1())
        }

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
            log.d("adInfo callback from IMAPlugin: AdEvent.adWaterFalling \n " +
                    "${event.adBreakConfig}")
        }

        player?.addListener(this, AdEvent.adWaterFallingFailed) { event ->
            log.w("adInfo callback from IMAPlugin: AdEvent.adWaterFallingFailed \n" +
                    "${event.adBreakConfig}")
        }

        player?.addListener(this, AdEvent.error) { event ->
            log.e("adInfo callback from IMAPlugin: AdEvent.error \n" +
                    "${event.error}")
        }

        player?.addListener(this, AdEvent.adBreakFetchError) { event ->
            log.e("adInfo callback from IMAPlugin: AdEvent.adBreakFetchError \n" +
                    "${event.eventType()}")
        }

        player?.addListener(this, AdEvent.started) { event ->
            //Some events holds additional data objects in them.
            //In order to get access to this object you need first cast event to
            //the object it belongs to. You can learn more about this kind of objects in
            //our documentation.

            //Then you can use the data object itself.
            val adInfo = event.adInfo
            //Print to log content type of this ad.
            log.d("adInfo callback from IMAPlugin adInfo: $adInfo")

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
                    playerControls.setAdMarkers(it.toLongArray(), playedAdsPosition?.toBooleanArray(),  adCuePoints.adCuePoints.size)
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

            adsPosition = ArrayList(adMarkersHashMap.keys)
            playedAdsPosition = ArrayList(adMarkersHashMap.values)
        }

        player?.addListener(this, AdEvent.loaded) { event ->
            log.d("AD_LOADED " + event.adInfo.getAdIndexInPod() + "/" + event.adInfo.getTotalAdsInPod())
         //   player?.advertisingController?.playAdNow(getPlayAdNowConfigAdBreak())
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

        player?.addListener(this, PlayerEvent.loadedMetadata) {
            log.d("PLAYER loadedMetadata ")
            adCuePoints?.let { adCuePoints ->
                adsPosition?.let {
                    playerControls.setAdMarkers(it.toLongArray(), playedAdsPosition?.toBooleanArray(),  adCuePoints.adCuePoints.size)
                }
            }
        }

        //player?.addListener(this, PlayerEvent.me)
    }

    override fun changeMediaOnClick() {
        adMarkersHashMap.clear()
        adCuePoints = null
        adsPosition = null
        playedAdsPosition = null

        player?.let {
            if (it.isPlaying && it.mediaEntry.id.equals(FIRST_ASSET_ID)) {
                buildOttMediaOptions(SECOND_ASSET_ID)
            } else {
                buildOttMediaOptions(FIRST_ASSET_ID)
            }
        }
    }

    override fun playAdNowApi() {
        player?.advertisingController?.playAdNow(getPlayAdNowConfigAdBreak())
    }

    companion object {
        //Media entry configuration constants.
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        val PARTNER_ID = 3009
    }
}
