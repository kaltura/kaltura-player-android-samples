package com.kaltura.playerdemo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import com.kaltura.netkit.utils.ErrorElement
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaEntry.MediaEntryType.Unknown
import com.kaltura.playkit.PKMediaFormat
import com.kaltura.playkit.PKPluginConfigs
import com.kaltura.playkit.player.MediaSupport
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.PlayerInitOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

abstract class BaseDemoActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, KalturaPlayer.OnEntryLoadListener {

    protected val context: Context = this
    protected var playerConfigUiConfJson: JsonObject? = null

    protected var initOptions: PlayerInitOptions? = null
    var ks: String? = ""
    var items: Array<DemoItem>? = null
    private var contentContainer: ViewGroup? = null
    private var itemListView: ListView? = null

    protected abstract fun items(): Array<DemoItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        initDrm()

        loadConfigFile()

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        nav_view.setNavigationItemSelectedListener(this)

        content_container.addView(getItemListView())
        nav_view.setCheckedItem(R.id.nav_gallery)
    }

    protected fun parseInitOptions(json: JsonObject?) {
        val partnerId = safeInteger(json, PARTNER_ID)
                ?: throw IllegalArgumentException("partnerId must not be null")
        if (json?.has(PLAYER_CONFIG)!!) {
            val playerConfigJasonObject = safeObject(json, PLAYER_CONFIG)
            val options = PlayerInitOptions(partnerId)
            if (initOptions == null) {
                options.setAutoPlay(safeBoolean(playerConfigJasonObject!!, AUTOPLAY))
                        .setPreload(safeBoolean(playerConfigJasonObject, PRELOAD))
                        .setKs(safeString(playerConfigJasonObject, KS))
                        .setPluginConfigs(parsePluginConfigs(json.get(PLUGINS)))
                        .setAllowCrossProtocolEnabled(safeBoolean(playerConfigJasonObject, ALLOW_CROSS_PROTOCOL_ENABLED))
                        .setReferrer(safeString(playerConfigJasonObject, REFERRER))

            }
            initOptions = options
        }
    }

    private fun parsePluginConfigs(json: JsonElement?): PKPluginConfigs {
        val configs = PKPluginConfigs()
        if (json != null && json.isJsonObject) {
            val obj = json.asJsonObject
            for ((pluginName, value) in obj.entrySet()) {
                configs.setPluginConfig(pluginName, value)
            }
        }
        return configs
    }

    protected fun safeObject(json: JsonObject?, key: String): JsonObject? {
        val jsonElement = json?.get(key)
        return if (jsonElement != null && jsonElement.isJsonObject) {
            jsonElement.asJsonObject
        } else null
    }

    protected fun safeString(json: JsonObject, key: String): String? {
        val jsonElement = json.get(key)
        return if (jsonElement != null && jsonElement.isJsonPrimitive) {
            jsonElement.asString
        } else null
    }

    protected fun safeBoolean(json: JsonObject, key: String): Boolean? {
        val jsonElement = json.get(key)
        return if (jsonElement != null && jsonElement.isJsonPrimitive) {
            jsonElement.asBoolean
        } else null
    }

    protected fun safeInteger(json: JsonObject?, key: String): Int? {
        val jsonElement = json?.get(key)
        return if (jsonElement != null && jsonElement.isJsonPrimitive) {
            jsonElement.asInt
        } else null
    }

    protected abstract fun loadConfigFile()

    private fun initDrm() {
        MediaSupport.initializeDrm(this) { pkDeviceSupportInfo, provisionError ->
            if (pkDeviceSupportInfo.isProvisionPerformed) {
                if (provisionError != null) {
                    log.e("DRM Provisioning failed", provisionError)
                } else {
                    log.d("DRM Provisioning succeeded")
                }
            }
            log.d("DRM initialized; supported: ${pkDeviceSupportInfo.supportedDrmSchemes} isHardwareDrmSupported: ${pkDeviceSupportInfo.isHardwareDrmSupported}")

            // Now it's safe to look at `supportedDrmSchemes`
        }
    }

    protected fun parseCommonOptions(json: JsonObject) {
        parseInitOptions(safeObject(json, "initOptions"))

        ks = initOptions?.ks

        val jsonItems = json.get("items").asJsonArray
        val itemList = ArrayList<DemoItem>(jsonItems.size())
        for (item in jsonItems) {
            val `object` = item.asJsonObject
            itemList.add(parseItem(`object`))
        }

        items = itemList.toTypedArray()
    }

    protected fun parseBasicCommonOptions(json: JsonObject) {
        parseInitOptions(safeObject(json, "initOptions"))

        ks = initOptions?.ks

        val jsonItems = json.get("items").asJsonArray
        val itemList = ArrayList<DemoItem>(jsonItems.size())
        for (item in jsonItems) {
            val `object` = item.asJsonObject
            val pkMediaEntry = MockMediaParser.parseMedia(`object`)
            itemList.add(DemoItem(pkMediaEntry.name, pkMediaEntry.id, pkMediaEntry))
        }

        items = itemList.toTypedArray()
    }

    internal object MockMediaParser {

        @Throws(JsonSyntaxException::class)
        fun parseMedia(mediaObject: JsonObject): PKMediaEntry {

            val mediaEntry = Gson().fromJson(mediaObject, PKMediaEntry::class.java)
            if (mediaEntry.mediaType == null) {
                mediaEntry.mediaType = Unknown
            }
            val mediaSources = mediaEntry.sources
            for (mediaSource in mediaSources) {
                var format = PKMediaFormat.valueOfUrl(mediaSource.url)
                if (format == null) {
                    val mimeType = getMimeTypeFromJson(mediaObject)
                    if (mimeType != null) {
                        if (mimeType == PKMediaFormat.dash.mimeType) {
                            format = PKMediaFormat.dash
                        } else if (mimeType == PKMediaFormat.hls.mimeType) {
                            format = PKMediaFormat.hls
                        } else if (mimeType == PKMediaFormat.wvm.mimeType) {
                            format = PKMediaFormat.wvm
                        } else if (mimeType == PKMediaFormat.mp4.mimeType) {
                            format = PKMediaFormat.mp4
                        } else if (mimeType == PKMediaFormat.mp3.mimeType) {
                            format = PKMediaFormat.mp3
                        }
                    }
                }
                mediaSource.mediaFormat = format
            }
            return mediaEntry
        }
    }

    protected abstract fun parseItem(`object`: JsonObject): DemoItem

    protected fun partnerId(): Int {
        initOptions?.let {
            it.tvPlayerParams?.let { tvPlayerParam ->
                return tvPlayerParam.partnerId
            }
        }
        return 0
    }

    protected abstract fun loadItem(item: DemoItem)

    override fun onEntryLoadComplete(entry: PKMediaEntry, error: ErrorElement?) {
        if (error != null) {
            Log.d("onEntryLoadComplete", " error: $error")
        }
    }

    override fun onBackPressed() {
        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)

        val partnerIdTextView = findViewById<TextView>(R.id.partnerIdTextView)
        partnerIdTextView.text = "Partner: " + partnerId()

        val demoNameTextView = findViewById<TextView>(R.id.demoNameTextView)
        demoNameTextView.text = demoName()

        val icon = findViewById<ImageView>(R.id.imageView)
        icon.setColorFilter(android.R.color.holo_green_dark)

        return true
    }

    protected abstract fun demoName(): String

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        contentContainer?.removeAllViews()

        when (item.itemId) {
            R.id.nav_gallery -> contentContainer?.addView(getItemListView())
            R.id.nav_downloads, R.id.nav_plugins -> {
            }
        }// TODO

        val drawer = findViewById<DrawerLayout>(R.id.drawer_layout)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun getItemListView(): ListView {
        itemListView?.let {
            return it
        }

        val itemArrayAdapter = ArrayAdapter<DemoItem>(this, android.R.layout.simple_list_item_1)
        itemArrayAdapter.addAll(*items())

        itemListView = ListView(this)

        itemListView?.adapter = itemArrayAdapter

        itemListView?.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            AlertDialog.Builder(context)
                    .setTitle(getString(R.string.select_action))
                    .setItems(arrayOf(getString(R.string.play_stream))) { dialog, which ->
                        if (which == 0) {
                            loadItem(parent.getItemAtPosition(position) as DemoItem)
                        }
                    }.show()
        }

        return itemListView!!
    }

    companion object {

        val PLAYER = "player"
        val AUDIO_LANG = "audioLanguage"
        val TEXT_LANG = "textLanguage"
        val PLAYBACK = "playback"
        val OFF = "off"
        val AUTOPLAY = "autoPlay"
        val PRELOAD = "preload"
        //public static final String START_TIME = "startTime";
        val CONFIG = "config"
        val PLUGINS = "plugins"
        val AUTO = "auto"
        val OPTIONS = "options"
        val UICONF_ID = "uiConfId"
        val PARTNER_ID = "partnerId"
        val REFERRER = "referrer"
        val KS = "ks"
        val SERVER_URL = "serverUrl"
        val ALLOW_CROSS_PROTOCOL_ENABLED = "allowCrossProtocolEnabled"
        val STREAM_PRIORITY = "streamPriority"


        private val log = PKLog.get("BaseDemoActivity")
        val PLAYER_CONFIG = "playerConfig"
        val UICONF = "uiConf"

        private fun getMimeTypeFromJson(mediaObject: JsonObject): String? {
            var mimeType: String? = null
            try {
                val jsonObj = JSONObject(mediaObject.toString())
                val sources = jsonObj.getJSONArray("sources")
                if (sources != null && sources.length() > 0) {
                    val sourcesJson = sources.getJSONObject(0)
                    mimeType = sourcesJson.getString("mimeType")
                    return mimeType
                }
            } catch (e: JSONException) {
                //e.printStackTrace();
                log.d("Sources does not contain mime type in it - hope url extension is valid...")
            }

            return mimeType
        }
    }
}
