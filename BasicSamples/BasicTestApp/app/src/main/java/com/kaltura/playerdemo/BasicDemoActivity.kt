package com.kaltura.playerdemo

import android.content.Intent
import android.net.Uri
import android.widget.FrameLayout
import com.google.gson.JsonObject
import com.kaltura.netkit.utils.GsonParser
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKRequestConfig
import com.kaltura.playkit.Utils
import com.kaltura.tvplayer.KalturaBasicPlayer
import com.kaltura.tvplayer.PlayerInitOptions
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.net.URL

class BasicDemoActivity : BaseDemoActivity() {
    private val log = PKLog.get("BasicDemoActivity")
    private var currentItem: DemoItem? = null

    override fun items(): Array<DemoItem> {
        return items!!
    }

    override fun loadConfigFile() {

        val url = intent.data
        var jsonString: String? = null

        if (url != null) {
            jsonString = readUrlToString(url)
        } else {
            jsonString = Utils.readAssetToString(this, "basic/main.json")
        }

        val json = GsonParser.toJson(jsonString!!).asJsonObject

        parseBasicCommonOptions(json)
    }

    private fun readUrlToString(url: Uri): String? {
        try {
            return Utils.fullyReadInputStream(URL(url.toString()).openStream(), 1024 * 1024).toString()
        } catch (e: IOException) {
            return null
        }

    }

    override fun parseItem(`object`: JsonObject): DemoItem {
        return DemoItem(`object`.get("name").asString, `object`.get("entryId").asString, currentItem?.pkMediaEntry)
    }

    override fun loadItem(item: DemoItem) {
        this.currentItem = item
        startActivity(Intent(this, PlayerActivity::class.java))
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun playerActivityLoaded(playerActivity: PlayerActivity) {
        val updatedInitOptions = PlayerInitOptions()
        updatedInitOptions.setLicenseRequestAdapter(initOptions?.licenseRequestAdapter)
        updatedInitOptions.setContentRequestAdapter(initOptions?.contentRequestAdapter)
        updatedInitOptions.setVrPlayerEnabled(initOptions?.vrPlayerEnabled)
        updatedInitOptions.setVRSettings(initOptions?.vrSettings)
        updatedInitOptions.setAdAutoPlayOnResume(initOptions?.adAutoPlayOnResume)
        updatedInitOptions.setSubtitleStyle(initOptions?.setSubtitleStyle)
        updatedInitOptions.setLoadControlBuffers(initOptions?.loadControlBuffers)
        updatedInitOptions.setMulticastSettings(initOptions?.multicastSettings)
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

        val player = KalturaBasicPlayer.create(playerActivity, updatedInitOptions)
        player.setMedia(currentItem?.pkMediaEntry, 0L)
        player.setPlayerView(FrameLayout.LayoutParams.WRAP_CONTENT, 600)

        playerActivity.setPlayer(player)
    }

    override fun demoName(): String {
        return "BASIC Player Demo"
    }

    public override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    public override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}
