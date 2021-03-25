package com.kaltura.player.offlinedemo

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.kaltura.playkit.*
import com.kaltura.tvplayer.KalturaPlayer
import com.kaltura.tvplayer.MediaOptions
import com.kaltura.tvplayer.OfflineManager
import com.kaltura.tvplayer.offline.OfflineManagerSettings
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

fun String.fmt(vararg args: Any?): String = java.lang.String.format(Locale.ROOT, this, *args)

@SuppressLint("ParcelCreator")
object NULL : KalturaItem(0, "", null, null) {
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


        val itemsJson = Utils.readAssetToString(this, "items.json")

        val gson = Gson()
        val items = gson.fromJson(itemsJson, Array<ItemJSON>::class.java)

        val testItems = items.map { it.toItem() }

        manager = OfflineManager.getInstance(this)
//        manager.setPreferredMediaFormat(PKMediaFormat.hls)
        manager.setOfflineManagerSettings(OfflineManagerSettings().setDefaultHlsAudioBitrateEstimation(64000))

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
            else -> "Expired or Error"
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
            if (!TextUtils.isEmpty(item.serverUrl)) {
                manager.setKalturaServerUrl(item.serverUrl);
            }
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

    override fun onDestroy() {
        super.onDestroy()
        manager.let {
            // Removing listeners by setting it to null
            manager.setAssetStateListener(null)
            manager.setDownloadProgressListener(null)
            manager.stop()
        }
    }

    private companion object {
        val log = PKLog.get("MainActivity")
    }
}
