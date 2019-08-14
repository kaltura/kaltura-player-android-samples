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
import androidx.appcompat.app.AlertDialog
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PKDrmParams
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaSource
import com.kaltura.tvplayer.OfflineManager

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.Exception
import java.util.*

//const val partnerId = 2215841
//const val entryId = "1_9bwuo813"
//const val partnerId = 1851571
//const val entryId = "0_pl5lbfo0"


public fun String.fmt(vararg args: Any?): String = java.lang.String.format(Locale.ROOT, this, *args)

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
                snackbarLong("Download of $error failed: $error")
            }

            override fun onAssetDownloadComplete(assetId: String) {
                snackbar("Complete")
            }

            override fun onAssetDownloadPending(assetId: String) {

            }

            override fun onAssetDownloadPaused(assetId: String) {
                snackbar("Paused")
            }

            override fun onRegistered(assetId: String, drmStatus: OfflineManager.DrmStatus?) {
                snackbar("onRegistered: ${drmStatus?.currentRemainingTime} seconds left")
            }

            override fun onRegisterError(assetId: String, error: Exception?) {
                snackbarLong("onRegisterError: $assetId $error")
            }

            override fun onStateChanged(assetId: String, assetInfo: OfflineManager.AssetInfo) {
                snackbar("onStateChanged")
            }

            override fun onAssetRemoved(assetId: String) {
                snackbar("onAssetRemoved")
            }

        })

        manager.setDownloadProgressListener { assetId, bytesDownloaded, totalBytesEstimated, percentDownloaded ->
            log.d("[progress] $assetId: ${bytesDownloaded / 1000} / ${totalBytesEstimated / 1000}")

            val item = itemMap[assetId] ?: return@setDownloadProgressListener
            item.bytesDownloaded = bytesDownloaded
            item.totalBytesEstimated = totalBytesEstimated
            item.percentDownloaded = percentDownloaded

            runOnUiThread {
                assetList.invalidateViews()
            }
        }


        itemArrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)

        OVPItem.values().forEach {
            itemArrayAdapter.add(it)
            itemMap[it.id()] = it
        }

        OTTItem.values().forEach {
            itemArrayAdapter.add(it)
            itemMap[it.id()] = it
        }

        assetList.adapter = this.itemArrayAdapter

        assetList.setOnItemClickListener { av: AdapterView<*>, v: View, pos: Int, id: Long ->
            showActionsDialog(av.getItemAtPosition(pos) as Item)
        }
    }

    private fun showActionsDialog(item: Item) {
        val items = arrayOf("Prepare", "Start", "Pause", "Play", "Remove", "Status")
        AlertDialog.Builder(this).setItems(items) { _, i ->
            when (i) {
                0 -> doPrepare(item)
                1 -> doStart(item)
                2 -> doPause(item)
                3 -> doPlay(item)
                4 -> doRemove(item)
                5 -> doStatus(item)
            }
        }.show()
    }

    private fun doStatus(item: Item) {
        val drmStatus = manager.getDrmStatus(item.id())

        val msg = if (drmStatus.isValid) "Valid; will expire in " + drmStatus.currentRemainingTime + " seconds" else "Expired"

        Snackbar.make(contentLayout, msg, Snackbar.LENGTH_LONG).setAction("Renew") {
            manager.setKalturaPartnerId(item.partnerId)
            manager.setKalturaServerUrl(item.serverUrl)
            manager.renewDrmAsset(item.id(), item.mediaOptions(), object: OfflineManager.MediaEntryCallback {
                override fun onMediaEntryLoaded(assetId: String, mediaEntry: PKMediaEntry) {
                    reduceLicenseDuration(mediaEntry, 300)
                }

                override fun onMediaEntryLoadError(error: Exception?) {
                    snackbar("onMediaEntryLoadError: $error", Snackbar.LENGTH_LONG)
                }
            })
        }.show()
    }

    private fun doRemove(item: Item) {
        manager.removeAsset(item.assetInfo?.assetId ?: return)
    }

    private fun doPlay(item: Item) {
        startActivity(Intent(this, PlayActivity::class.java).apply {
            data = Uri.parse(item.assetInfo?.assetId ?: return)
        })
    }

    private fun doPause(item: Item) {
        manager.pauseAssetDownload(item.id())
    }

    private fun doStart(item: Item) {
        manager.startAssetDownload(item.assetInfo)
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
                    Snackbar.make(assetList, "Prepared", Snackbar.LENGTH_LONG).setAction("START") {
                        doStart(item)
                    }.show()
                }
            }

            override fun onPrepareError(assetId: String, error: Exception) {
                snackbar("onPrepareError: $error", Snackbar.LENGTH_LONG)
            }

            override fun onMediaEntryLoadError(error: Exception) {
                snackbar("onMediaEntryLoadError: $error", Snackbar.LENGTH_LONG)
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

    private fun snackbar(msg: String, duration: Int) {
        runOnUiThread {
            Snackbar.make(assetList, msg, duration).show()
        }
    }

    private fun snackbar(msg: String) = snackbar(msg, Snackbar.LENGTH_SHORT)

    private fun snackbarLong(msg: String) = snackbar(msg, Snackbar.LENGTH_LONG)

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

    companion object {
        val log = PKLog.get("MainActivity")
    }
}

