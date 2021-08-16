package com.kaltura.playerdemo

import android.content.Intent
import android.widget.FrameLayout
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.kaltura.netkit.utils.GsonParser
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKRequestConfig
import com.kaltura.playkit.Utils
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider.HttpProtocol.Https
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.OTTMediaOptions
import com.kaltura.tvplayer.PlayerInitOptions
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class OTTDemoActivity : BaseDemoActivity() {
    private val log = PKLog.get("OTTDemoActivity")
    private val gson = Gson()
    private var currentItem: TVItem? = null

    override fun items(): Array<DemoItem> {
        return items!!
    }

    override fun loadConfigFile() {
        val jsonString = Utils.readAssetToString(this, "ott/main.json")
        val json = GsonParser.toJson(jsonString).asJsonObject

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
        val updatedInitOptions = PlayerInitOptions(initOptions?.partnerId)
        updatedInitOptions.setLicenseRequestAdapter(initOptions?.licenseRequestAdapter)
        updatedInitOptions.setContentRequestAdapter(initOptions?.contentRequestAdapter)
        updatedInitOptions.setVrPlayerEnabled(initOptions?.vrPlayerEnabled)
        updatedInitOptions.setVRSettings(initOptions?.vrSettings)
        updatedInitOptions.setAdAutoPlayOnResume(initOptions?.adAutoPlayOnResume)
        updatedInitOptions.setSubtitleStyle(initOptions?.setSubtitleStyle)
        updatedInitOptions.setLoadControlBuffers(initOptions?.loadControlBuffers)
        updatedInitOptions.setAbrSettings(initOptions?.abrSettings)
        updatedInitOptions.setAspectRatioResizeMode(initOptions?.aspectRatioResizeMode)
        updatedInitOptions.setPreferredMediaFormat(initOptions?.preferredMediaFormat)
        updatedInitOptions.setAllowClearLead(initOptions?.allowClearLead)
        updatedInitOptions.setPKRequestConfig(PKRequestConfig(initOptions?.allowCrossProtocolEnabled ?: false))
        updatedInitOptions.setSecureSurface(initOptions?.secureSurface)
        updatedInitOptions.setKs(initOptions?.ks)
        updatedInitOptions.setAutoPlay(initOptions?.autoplay)
        updatedInitOptions.setReferrer(initOptions?.referrer)
        updatedInitOptions.forceSinglePlayerEngine(initOptions?.forceSinglePlayerEngine)

        initOptions?.let {
            it.audioLanguage?.let { audioLanguage ->
                it.audioLanguageMode?.let { audioLanguageMode ->
                    updatedInitOptions.setAudioLanguage(audioLanguage, audioLanguageMode)
                }
            }
        }

        initOptions?.let {
            it.textLanguage?.let { textLanguage ->
                it.textLanguageMode?.let { textLanguageMode ->
                    updatedInitOptions.setAudioLanguage(textLanguage, textLanguageMode)
                }
            }
        }

        val player = KalturaOttPlayer.create(playerActivity, updatedInitOptions)

        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = currentItem?.id
        ottMediaAsset.protocol = currentItem?.protocol
        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)

        player.loadMedia(ottMediaOptions) { mediaOptions, entry, loadError ->
            log.d("onEntryLoadComplete; $entry; $loadError")
        }
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
