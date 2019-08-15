package com.kaltura.player.offlinedemo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PKDrmParams
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaSource
import com.kaltura.tvplayer.MediaOptions
import com.kaltura.tvplayer.OfflineManager

import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.util.*

fun String.fmt(vararg args: Any?): String = java.lang.String.format(Locale.ROOT, this, *args)


val testItems = listOf(
    OVPItem(1851571, "0_pl5lbfo0"),
    OVPItem(2222401, "0_vcggu66e"),
    OVPItem(2222401, "1_2hsw7gwj"),
    OVPItem(2215841, "1_9bwuo813"),

    OTTItem(2250, "3817050", "https://rest-as.ott.kaltura.com/v5_0_3/api_v3"),

    NULL    // to avoid moving commas :-)
)

object NULL : Item(0, "") {
    override fun id(): String = TODO()
    override fun mediaOptions(): MediaOptions = TODO()
}

class MainActivity : AppCompatActivity() {

    private lateinit var itemArrayAdapter: ArrayAdapter<Item>
    private lateinit var manager: OfflineManager
    private val itemMap = mutableMapOf<String, Item>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//        setSupportActionBar(toolbar)



        manager = OfflineManager.getInstance(this)

        manager.setAssetStateListener(object : OfflineManager.AssetStateListener {
            override fun onAssetDownloadFailed(assetId: String, error: OfflineManager.AssetDownloadException?) {
                toastLong("Download of $error failed: $error")
                updateItemStatus(assetId)
            }

            override fun onAssetDownloadComplete(assetId: String) {
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

            override fun onRegistered(assetId: String, drmStatus: OfflineManager.DrmStatus?) {
                toast("onRegistered: ${drmStatus?.currentRemainingTime} seconds left")
                updateItemStatus(assetId)
            }

            override fun onRegisterError(assetId: String, error: Exception?) {
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

            it.assetInfo = manager.getAssetInfo(it.id())
            itemArrayAdapter.add(it)
            itemMap[it.id()] = it
        }

        assetList.adapter = this.itemArrayAdapter

        assetList.setOnItemClickListener { av: AdapterView<*>, v: View, pos: Int, id: Long ->
            showActionsDialog(av.getItemAtPosition(pos) as Item)
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
            manager.setKalturaPartnerId(item.partnerId)
            manager.setKalturaServerUrl(item.serverUrl)
            manager.renewDrmAsset(item.id(), item.mediaOptions(), object: OfflineManager.MediaEntryCallback {
                override fun onMediaEntryLoaded(assetId: String, mediaEntry: PKMediaEntry) {
                    reduceLicenseDuration(mediaEntry, 300)
                }

                override fun onMediaEntryLoadError(error: Exception?) {
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
        manager.startAssetDownload(item.assetInfo)
        updateItemStatus(item)
    }

    private fun doPrepare(item: Item) {
        manager.setKalturaPartnerId(item.partnerId)
        manager.setKalturaServerUrl(item.serverUrl)

        manager.prepareAsset(item.mediaOptions(), null, object: OfflineManager.PrepareCallback {
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

            override fun onSourceSelected(assetId: String, source: PKMediaSource, drmParams: PKDrmParams?) {

            }
        })
    }

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
        Snackbar.make(assetList, msg, Snackbar.LENGTH_LONG).setAction(next) {
            nextAction()
        }.show()
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

