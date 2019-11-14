package com.kaltura.playerdemo

import android.content.Intent
import android.widget.FrameLayout

import com.google.gson.Gson
import com.google.gson.JsonObject

import com.kaltura.netkit.utils.GsonParser
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.Utils
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import com.kaltura.tvplayer.config.PhoenixTVPlayerParams

import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

import com.kaltura.playkit.providers.ott.PhoenixMediaProvider.HttpProtocol.Https

class OTTDemoActivity : BaseDemoActivity() {
    private val log = PKLog.get("OTTDemoActivity")
    private val gson = Gson()
    private var currentItem: TVItem? = null

    override fun items(): Array<DemoItem> {
        return items!!
    }

    override fun loadConfigFile() {
        val jsonString = Utils.readAssetToString(this, "ott/main.json")
        val json = GsonParser.toJson(jsonString!!).asJsonObject

        parseCommonOptions(json)

    }

    override fun parseItem(`object`: JsonObject): DemoItem {
        return if (`object`.has("protocol")) {
            TVItem(`object`.get("name").asString, `object`.get("assetId").asString, arrayOf("804398"), `object`.get("protocol").asString)
        } else {
            TVItem(`object`.get("name").asString, `object`.get("assetId").asString, arrayOf("804398"))
        }
    }

    override fun loadItem(item: DemoItem) {
        this.currentItem = item as TVItem
        startActivity(Intent(this, PlayerActivity::class.java))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun playerActivityLoaded(playerActivity: PlayerActivity) {
        val updatedInitOptions = PlayerInitOptions(initOptions!!.partnerId)
        updatedInitOptions.setLicenseRequestAdapter(initOptions!!.licenseRequestAdapter)
        updatedInitOptions.setContentRequestAdapter(initOptions!!.contentRequestAdapter)
        updatedInitOptions.setVrPlayerEnabled(initOptions!!.vrPlayerEnabled)
        updatedInitOptions.setVRSettings(initOptions!!.vrSettings)
        updatedInitOptions.setAdAutoPlayOnResume(initOptions!!.adAutoPlayOnResume)
        updatedInitOptions.setSubtitleStyle(initOptions!!.setSubtitleStyle)
        updatedInitOptions.setLoadControlBuffers(initOptions!!.loadControlBuffers)
        updatedInitOptions.setAbrSettings(initOptions!!.abrSettings)
        updatedInitOptions.setAspectRatioResizeMode(initOptions!!.aspectRatioResizeMode)
        updatedInitOptions.setPreferredMediaFormat(if (initOptions!!.preferredMediaFormat != null) initOptions!!.preferredMediaFormat else null)
        updatedInitOptions.setAllowClearLead(initOptions!!.allowClearLead)
        updatedInitOptions.setAllowCrossProtocolEnabled(initOptions!!.allowCrossProtocolEnabled)
        updatedInitOptions.setSecureSurface(initOptions!!.secureSurface)
        updatedInitOptions.setKs(initOptions!!.ks)
        updatedInitOptions.setAutoPlay(initOptions!!.autoplay)
        updatedInitOptions.setReferrer(initOptions!!.referrer)
        updatedInitOptions.forceSinglePlayerEngine(initOptions!!.forceSinglePlayerEngine)
        if (initOptions!!.audioLanguage != null && initOptions!!.audioLanguageMode != null) {
            updatedInitOptions.setAudioLanguage(initOptions!!.audioLanguage, initOptions!!.audioLanguageMode)
        }
        if (initOptions!!.textLanguage != null && initOptions!!.textLanguageMode != null) {
            updatedInitOptions.setTextLanguage(initOptions!!.textLanguage, initOptions!!.textLanguageMode)
        }

        if (initOptions!!.partnerId == 198) {
            val phoenixTVPlayerParams = PhoenixTVPlayerParams()
            phoenixTVPlayerParams.analyticsUrl = "https://analytics.kaltura.com"
            phoenixTVPlayerParams.ovpPartnerId = 1774581
            phoenixTVPlayerParams.partnerId = 198
            phoenixTVPlayerParams.serviceUrl = "https://api-preprod.ott.kaltura.com/v5_1_0/"
            phoenixTVPlayerParams.ovpServiceUrl = "http://cdnapi.kaltura.com/"
            updatedInitOptions.tvPlayerParams = phoenixTVPlayerParams
        }

        val player = KalturaOttPlayer.create(playerActivity, updatedInitOptions)

        val ottMediaOptions = OTTMediaOptions()
        ottMediaOptions.assetId = currentItem!!.id
        ottMediaOptions.protocol = currentItem!!.protocol
        player.loadMedia(ottMediaOptions) { entry, loadError -> log.d("onEntryLoadComplete; $entry; $loadError") }
        player.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, 600)

        playerActivity.setPlayer(player)
    }


    override fun demoName(): String {
        return "OTT Player Demo"
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    class TVItem : DemoItem {

        val fileIds: Array<String>
        val protocol: String

        constructor(name: String, id: String, fileIds: Array<String>) : super(name, id) {
            this.fileIds = fileIds
            this.protocol = Https
        }

        constructor(name: String, id: String, fileIds: Array<String>, protocol: String) : super(name, id) {
            this.fileIds = fileIds
            this.protocol = protocol
        }
    }
}