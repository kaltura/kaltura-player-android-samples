package com.kaltura.player.offlinedemo

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcel
import android.os.SystemClock
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PKDrmParams
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaSource
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.MediaOptions
import com.kaltura.tvplayer.OfflineManager
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

fun String.fmt(vararg args: Any?): String = java.lang.String.format(Locale.ROOT, this, *args)

typealias Prefs = OfflineManager.SelectionPrefs

val testItems = listOf(

    BasicItem("apple1", "https://devstreaming-cdn.apple.com/videos/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8"),
    BasicItem("apple2", "https://devstreaming-cdn.apple.com/videos/streaming/examples/img_bipbop_adv_example_ts/master.m3u8"),

    OVPItem(2215841, "0_axrfacp3", prefs = Prefs().apply {
        videoBitrate = 800000
        videoHeight = 600
        audioLanguages = listOf("heb", "eng")
        textLanguages = listOf("rus")
//        allTextLanguages = true
    }),

    OVPItem(1091, "0_mskmqcit", "http://cdntesting.qa.mkaltura.com"),
    OVPItem(1851571, "0_pl5lbfo0"),
    OVPItem(2222401, "0_vcggu66e"),
    OVPItem(2222401, "1_2hsw7gwj"),
    OVPItem(2215841, "1_9bwuo813"),

    OTTItem(225, "381705", "https://rest-as.ott.kaltura.com/v5_0_3/api_v3", "Tablet Main"),
    OTTItem(225, "790460", "https://rest-as.ott.kaltura.com/v5_0_3/api_v3", "Tablet Main"),

    NULL    // to avoid moving commas :-)
)

@SuppressLint("ParcelCreator")
object NULL : KalturaItem(0, "", null) {
    override fun id(): String = TODO()
    override fun mediaOptions(): MediaOptions = TODO()
}

class MainActivity : AppCompatActivity() {

    private lateinit var itemArrayAdapter: ArrayAdapter<Item>
    private lateinit var manager: OfflineManager
    private val itemMap = mutableMapOf<String, Item>()

    var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)

        manager = OfflineManager.getInstance(this)
//        manager.setPreferredMediaFormat(PKMediaFormat.hls)
        manager.setEstimatedHlsAudioBitrate(64000)

        manager.setAssetStateListener(object : OfflineManager.AssetStateListener {
            override fun onAssetDownloadFailed(assetId: String, error: Exception) {
                toastLong("Download of $error failed: $error")
                updateItemStatus(assetId)
            }

            override fun onAssetDownloadComplete(assetId: String) {
                log.d("onAssetDownloadComplete")

                log.d("onAssetDownloadComplete: ${SystemClock.elapsedRealtimeNanos() - startTime}")
                toast("Complete")
                updateItemStatus(assetId)
            }

            override fun onAssetDownloadPending(assetId: String) {
                updateItemStatus(assetId)
            }

            override fun onAssetDownloadPaused(assetId: String) {
                toast("Paused")
                updateItemStatus(assetId)
            }

            override fun onRegistered(assetId: String, drmStatus: OfflineManager.DrmStatus) {
                toast("onRegistered: ${drmStatus.currentRemainingTime} seconds left")
                updateItemStatus(assetId)
            }

            override fun onRegisterError(assetId: String, error: Exception) {
                toastLong("onRegisterError: $assetId $error")
                updateItemStatus(assetId)
            }

            override fun onStateChanged(assetId: String, assetInfo: OfflineManager.AssetInfo) {
                toast("onStateChanged")
                updateItemStatus(assetId)
            }

            override fun onAssetRemoved(assetId: String) {
                toast("onAssetRemoved")
                updateItemStatus(assetId)
            }

        })

        manager.setDownloadProgressListener { assetId, bytesDownloaded, totalBytesEstimated, percentDownloaded ->
            log.d("[progress] $assetId: ${bytesDownloaded / 1000} / ${totalBytesEstimated / 1000}")

            val item = itemMap[assetId] ?: return@setDownloadProgressListener
            item.bytesDownloaded = bytesDownloaded
            item.percentDownloaded = percentDownloaded

            runOnUiThread {
                assetList.invalidateViews()
            }
        }

        itemArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        testItems.filter { it != NULL }.forEach {
            itemArrayAdapter.add(it)
            itemMap[it.id()] = it
        }

        assetList.adapter = this.itemArrayAdapter

        assetList.setOnItemClickListener { av: AdapterView<*>, _: View, pos: Int, _: Long ->
            showActionsDialog(av.getItemAtPosition(pos) as Item)
        }

        manager.start {
            log.d("manager started")
            itemMap.values.forEach {
                it.assetInfo = manager.getAssetInfo(it.id())
            }

            runOnUiThread(assetList::invalidateViews)
        }
    }

    private fun updateItemStatus(assetId: String) {
        updateItemStatus(itemMap[assetId] ?: return)
    }

    private fun showActionsDialog(item: Item) {
        val items = arrayOf("Prepare", "Start", "Pause", "Play", "Remove", "Status", "Update")
        AlertDialog.Builder(this).setItems(items) { _, i ->
            when (i) {
                0 -> doPrepare(item)
                1 -> doStart(item)
                2 -> doPause(item)
                3 -> doPlay(item)
                4 -> doRemove(item)
                5 -> doStatus(item)
                6 -> updateItemStatus(item)
            }
        }.show()
    }

    private fun updateItemStatus(item: Item) {
        item.assetInfo = manager.getAssetInfo(item.id())
        runOnUiThread { assetList.invalidateViews() }
    }

    private fun doStatus(item: Item) {

        if (item !is KalturaItem) {
            toastLong("Not applicable")
            return
        }

        val drmStatus = manager.getDrmStatus(item.id())

        if (drmStatus.isClear) {
            toastLong("Clear")
            return
        }

        val msg = when {
            drmStatus.isValid -> "Valid; will expire in " + drmStatus.currentRemainingTime + " seconds"
            else -> "Expired"
        }

        snackbar(msg, "Renew") {
            manager.setKalturaParams(KalturaPlayer.Type.ovp, item.partnerId)
            manager.renewDrmAssetLicense(item.id(), item.mediaOptions(), object: OfflineManager.MediaEntryCallback {
                override fun onMediaEntryLoaded(assetId: String, mediaEntry: PKMediaEntry) {
                    reduceLicenseDuration(mediaEntry, 300)
                }

                override fun onMediaEntryLoadError(error: Exception) {
                    toastLong("onMediaEntryLoadError: $error")
                }
            })
        }
    }

    private fun doRemove(item: Item) {
        manager.removeAsset(item.assetInfo?.assetId ?: return)
        updateItemStatus(item)
    }

    private fun doPlay(item: Item) {
        startActivity(Intent(this, PlayActivity::class.java).apply {
            data = Uri.parse(item.assetInfo?.assetId ?: return)
        })
    }

    private fun doPause(item: Item) {
        manager.pauseAssetDownload(item.id())
        updateItemStatus(item)
    }

    private fun doStart(item: Item) {
        log.d("doStart")
        val assetInfo = item.assetInfo ?: return

        startTime = SystemClock.elapsedRealtime()
        manager.startAssetDownload(assetInfo)
        updateItemStatus(item)
    }

    private fun doPrepare(item: Item) {

        if (item is KalturaItem) {
            manager.setKalturaParams(KalturaPlayer.Type.ovp, item.partnerId)
            manager.setKalturaServerUrl(item.serverUrl)
        }

        val prepareCallback = object : OfflineManager.PrepareCallback {
            override fun onPrepared(
                assetId: String,
                assetInfo: OfflineManager.AssetInfo,
                selected: MutableMap<OfflineManager.TrackType, MutableList<OfflineManager.Track>>?
            ) {
                item.assetInfo = assetInfo
                runOnUiThread {
                    snackbar("Prepared", "Start") {
                        doStart(item)
                    }
                    assetList.invalidateViews()
                }
            }

            override fun onPrepareError(assetId: String, error: Exception) {
                toastLong("onPrepareError: $error")
            }

            override fun onMediaEntryLoadError(error: Exception) {
                toastLong("onMediaEntryLoadError: $error")
            }

            override fun onMediaEntryLoaded(assetId: String, mediaEntry: PKMediaEntry) {
                reduceLicenseDuration(mediaEntry, 300)
            }

            override fun onSourceSelected(
                assetId: String,
                source: PKMediaSource,
                drmParams: PKDrmParams?
            ) {

            }
        }

        val defaultPrefs = OfflineManager.SelectionPrefs().apply {
            videoHeight = 300
            videoBitrate = 600000
            videoWidth = 400
            allAudioLanguages = true
            allTextLanguages = true
            allowInefficientCodecs = false
        }

        if (item is KalturaItem) {
            manager.prepareAsset(item.mediaOptions(), item.selectionPrefs ?: defaultPrefs, prepareCallback)
        } else {
            item.entry?.let {
                    entry -> manager.prepareAsset(entry, item.selectionPrefs ?: defaultPrefs, prepareCallback)
            }
        }
    }

    @Suppress("SameParameterValue")
    private fun reduceLicenseDuration(mediaEntry: PKMediaEntry, seconds: Int) {
        for (source in mediaEntry.sources) {
            if (source.hasDrmParams()) {
                for (params in source.drmData) {
                    params.licenseUri = params.licenseUri + "&rental_duration=" + seconds
                }
            }
        }
    }

    private fun snackbar(msg: String, next: String, nextAction: () -> Unit) = runOnUiThread {
        Snackbar.make(assetList, msg, Snackbar.LENGTH_LONG).apply {
            duration = 5000
            setAction(next) {
                nextAction()
            }
            show()
        }
    }

    private fun toast(msg: String) = runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }

    private fun toastLong(msg: String) = runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_LONG).show() }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    private companion object {
        val log = PKLog.get("MainActivity")
    }
}

