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
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.tvplayer.*
import com.kaltura.tvplayer.offline.OfflineManagerSettings
import com.kaltura.tvplayer.offline.Prefetch
import com.kaltura.tvplayer.offline.exo.PrefetchConfig
import com.kaltura.tvplayer.offline.exo.PrefetchManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
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

        manager = OfflineManager.getInstance(this, OfflineManager.OfflineProvider.EXO)
        var offlineSettings = OfflineManagerSettings()
        offlineSettings.setHlsAudioBitrateEstimation(64000)

//        val customAdapterData =
//            "PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48S2V5T1NBdXRoZW50aWNhdGlvblhNTD48RGF0YT48V2lkZXZpbmVQb2xpY3kgZmxfQ2FuUGVyc2lzdD0idHJ1ZSIgZmxfQ2FuUGxheT0idHJ1ZSI+PFBsYXliYWNrRHVyYXRpb24+MTcyODAwPC9QbGF5YmFja0R1cmF0aW9uPjxMaWNlbnNlRHVyYXRpb24+MTcyODAwPC9MaWNlbnNlRHVyYXRpb24+PC9XaWRldmluZVBvbGljeT48V2lkZXZpbmVDb250ZW50S2V5U3BlYyBUcmFja1R5cGU9IkhEIj48U2VjdXJpdHlMZXZlbD4xPC9TZWN1cml0eUxldmVsPjwvV2lkZXZpbmVDb250ZW50S2V5U3BlYz48RmFpclBsYXlQb2xpY3kgcGVyc2lzdGVudD0idHJ1ZSI+PFBlcnNpc3RlbmNlU2Vjb25kcz4xNzI4MDA8L1BlcnNpc3RlbmNlU2Vjb25kcz48L0ZhaXJQbGF5UG9saWN5PjxMaWNlbnNlIHR5cGU9InNpbXBsZSI+PFBvbGljeT48aWQ+NzA0YzlmYjAxMThhNDAzOWJjNzFlODU4MmUzNGE2ODU8L2lkPjwvUG9saWN5PjxQbGF5PjxpZD44OWE4YTBmNGUzNGY0NmFhOGE4ZjdiMzc5ZDM3N2ZiODwvaWQ+PC9QbGF5PjwvTGljZW5zZT48UG9saWN5IGlkPSI3MDRjOWZiMDExOGE0MDM5YmM3MWU4NTgyZTM0YTY4NSIgcGVyc2lzdGVudD0idHJ1ZSI+PE1pbmltdW1TZWN1cml0eUxldmVsPjIwMDA8L01pbmltdW1TZWN1cml0eUxldmVsPjxFeHBpcmF0aW9uQWZ0ZXJGaXJzdFBsYXk+MTcyODAwPC9FeHBpcmF0aW9uQWZ0ZXJGaXJzdFBsYXk+PC9Qb2xpY3k+PFBsYXkgaWQ9Ijg5YThhMGY0ZTM0ZjQ2YWE4YThmN2IzNzlkMzc3ZmI4Ij48RW5hYmxlcnM+PGlkPjc4NjYyN0Q4LUMyQTYtNDRCRS04Rjg4LTA4QUUyNTVCMDFBNzwvaWQ+PGlkPkQ2ODUwMzBCLTBGNEYtNDNBNi1CQkFELTM1NkYxRUEwMDQ5QTwvaWQ+PGlkPjAwMkY5NzcyLTM4QTAtNDNFNS05Rjc5LTBGNjM2MURDQzYyQTwvaWQ+PC9FbmFibGVycz48T3V0cHV0UHJvdGVjdGlvbnM+PE9QTD48Q29tcHJlc3NlZERpZ2l0YWxBdWRpbz4zMDA8L0NvbXByZXNzZWREaWdpdGFsQXVkaW8+PFVuY29tcHJlc3NlZERpZ2l0YWxBdWRpbz4zMDA8L1VuY29tcHJlc3NlZERpZ2l0YWxBdWRpbz48Q29tcHJlc3NlZERpZ2l0YWxWaWRlbz41MDA8L0NvbXByZXNzZWREaWdpdGFsVmlkZW8+PFVuY29tcHJlc3NlZERpZ2l0YWxWaWRlbz4zMDA8L1VuY29tcHJlc3NlZERpZ2l0YWxWaWRlbz48QW5hbG9nVmlkZW8+MjAwPC9BbmFsb2dWaWRlbz48L09QTD48L091dHB1dFByb3RlY3Rpb25zPjwvUGxheT48S2V5SURMaXN0PjxLZXlJRD5lYjkxNDg4MS1kMjM3LTQ2YzYtYjVkMC1mNjY0MjM1ZjJkNDg8L0tleUlEPjwvS2V5SURMaXN0PjxHZW5lcmF0aW9uVGltZT4yMDIxLTA2LTIzIDEzOjU2OjQ1LjcxNzwvR2VuZXJhdGlvblRpbWU+PEV4cGlyYXRpb25UaW1lPjIwMjEtMDctMDMgMTM6NTY6NDUuNzE3PC9FeHBpcmF0aW9uVGltZT48VW5pcXVlSWQ+Zjk1YTI2YjQ4YmFiNDliY2IyNmZkZmYwNTlhZjU2MjI8L1VuaXF1ZUlkPjxSU0FQdWJLZXlJZD5iYTc4ZmFjNGM3NmNlMDAzNzAwY2FhYWFlODAwNTc4MjwvUlNBUHViS2V5SWQ+PC9EYXRhPjxTaWduYXR1cmU+czNVSm1uMDRnUU4yMXdzK1ZwNWVrUzNTOXdnY21RQlkwNEVxQnhNaEE1Mmxmb3RjYVh6Y2xPZVhWdWprRUtpcFJYS1N3eStwQ1pwYjZXeFo2QndsNVptcThMeDJkQTBpQWd5TFk5N1ljSWhSMm1RYjVrVWQ5SDZzMSt6NTRlMDBhWG5mODU0U1hjSGd6U0dhQW4vZDh2bUIvQ0lZUWI1b3pVTGdoTFl5MHJ1WCtyb253dmwzYzBzTW5nVFIySXVvam5tWWc1TUw0N0thL1Z4YkYxRVAzV0FDZTh6eUdDb1ozVlNCbGtwRmEvRkNrQWRuc0ZhRVR6dGpBZ0VzRCtZSlI0cE82MVZpTlpqcFRyR1FKaVJDR3ByOW1aOS9PMXBtb1pKbWh2ODVnbHZFa1lneXB1cUlvRnlDTVR3MXFpYlpORkkvTHpyRHVXelJmRFdnbDA4Tzd3PT08L1NpZ25hdHVyZT48L0tleU9TQXV0aGVudGljYXRpb25YTUw+"
//        var licenseRequestAdapter = DRMAdapter()
//        DRMAdapter.customData = customAdapterData
//        manager.setLicenseRequestAdapter(licenseRequestAdapter)

        manager.setOfflineManagerSettings(offlineSettings)

        val offlineAssets = manager.getAllAssets() // manager.getAssetsInState(OfflineManager.AssetDownloadState.started)

        for (testItem in testItems) {
            for (offlineAsset in offlineAssets) {
                if (offlineAsset != null && testItem.id() == offlineAsset.assetId) {
                    testItem.assetInfo = offlineAsset
                    break
                }
            }
        }

        manager.setAssetStateListener(object : OfflineManager.AssetStateListener {

            override fun onAssetDownloadFailed(assetId: String, downloadType: OfflineManager.DownloadType, error: Exception) {
                toastLong("Download of $assetId, ${downloadType.name} failed: $error")
                updateItemStatus(assetId)
            }

            override fun onAssetDownloadComplete(assetId: String, downloadType: OfflineManager.DownloadType) {
                log.d("onAssetDownloadComplete $assetId totalDownloadTime: ${SystemClock.elapsedRealtimeNanos() - startTime}")

                if (downloadType == OfflineManager.DownloadType.FULL) {
                    toast("Complete " + assetId)
                } else {
                    toast("Prefetched" + assetId)
                }
                updateItemStatus(assetId)
            }

            override fun onAssetPrefetchComplete(
                assetId: String,
                downloadType: OfflineManager.DownloadType
            ) {
                log.d("onAssetPrefetchComplete $assetId totalDownloadTime: ${SystemClock.elapsedRealtimeNanos() - startTime}")

                if (downloadType == OfflineManager.DownloadType.FULL) {
                    toast("Complete")
                } else {
                    toast("Prefetched id =  $assetId")
                }
                updateItemStatus(assetId)
            }

            override fun onAssetDownloadPending(assetId: String, downloadType: OfflineManager.DownloadType) {
                //toast("Pending - onAssetDownloadPending")
                updateItemStatus(assetId)
            }

            override fun onAssetDownloadPaused(assetId: String, downloadType: OfflineManager.DownloadType) {
                toast("Paused - onAssetDownloadPaused")
                updateItemStatus(assetId)
            }

            override fun onRegistered(assetId: String, drmStatus: OfflineManager.DrmStatus) {
                toast("onRegistered: ${drmStatus.currentRemainingTime} seconds left")
                updateItemStatus(assetId)
            }

            override fun onRegisterError(assetId: String, downloadType: OfflineManager.DownloadType, error: Exception) {
                toastLong("onRegisterError: $assetId, ${downloadType.name}, $error ")
                updateItemStatus(assetId)
            }

            override fun onStateChanged(assetId: String, downloadType: OfflineManager.DownloadType, assetInfo: OfflineManager.AssetInfo) {
                toast("onStateChanged state = " + assetInfo.state.name)
                updateItemStatus(assetId)
            }

            override fun onAssetRemoved(assetId: String, downloadType: OfflineManager.DownloadType) {
                toast("onAssetRemoved")
                updateItemStatus(assetId)
            }

            override fun onAssetRemoveError(
                assetId: String,
                downloadType: OfflineManager.DownloadType,
                error: java.lang.Exception
            ) {
                toast("Error Asset Was Not Removed")
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
            showActionsDialog(av.getItemAtPosition(pos) as Item, pos)
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


    private fun showActionsDialog(item: Item, position: Int) {
        val items = arrayOf("Prepare", "Start", "Pause", "Play-Offline", "Play-Online", "Remove", "Status", "Update")
        AlertDialog.Builder(this).setItems(items) { _, i ->
            when (i) {
                0 -> if (item.isPrefetch) {
                      doPrefetch(item)
                    } else {
                        doPrepare(item)
                    }
                1 -> doStart(item)
                2 -> doPause(item)
                3 -> doOfflinePlayback(item)
                4 -> doOnlinePlayback(item, position)
                5 -> doRemove(item)
                6 -> doStatus(item)
                7 -> updateItemStatus(item)
            }
        }.show()
    }

    private fun updateItemStatus(item: Item) {
        item.assetInfo = manager.getAssetInfo(item.id())
        runOnUiThread {
            assetList.invalidateViews()
        }
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
                    // reduceLicenseDuration(mediaEntry, 300)
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

    private fun doOfflinePlayback(item: Item) {
        startActivity(Intent(this, PlayActivity::class.java).apply {
            data = Uri.parse(item.assetInfo?.assetId ?: return)
        })
    }

    private fun doOnlinePlayback(item: Item, position: Int) {
        val intent = Intent(this, PlayActivity::class.java)

        val bundle = Bundle()
        bundle.putBoolean("isOnlinePlayback", true)
        bundle.putInt("position", position)
        if (item is OTTItem) {
            bundle.putInt("partnerId", item.partnerId)
        }

        if (item is OVPItem) {
            bundle.putInt("partnerId", item.partnerId)
        }

        intent.putExtra("assetBundle", bundle)
        val assetId = item.assetInfo?.assetId ?: ""
        if (!assetId.isEmpty()) {
            intent.data = Uri.parse(assetId)
        }
        startActivity(intent)
    }

    private fun doPause(item: Item?) {
        item?.let { it ->
            it.id()?.let { itemId ->
                manager.pauseAssetDownload(itemId)
                updateItemStatus(it)
            }
        }
    }

    private fun doStart(item: Item) {
        log.d("doStart")
        val assetInfo = item.assetInfo ?: return

        startTime = SystemClock.elapsedRealtime()
        manager.startAssetDownload(assetInfo)
        updateItemStatus(item)
    }

    private fun buildOttMediaOptions(): OTTMediaOptions {
        val ottMediaAsset = OTTMediaAsset()
        ottMediaAsset.assetId = ASSET_ID
        ottMediaAsset.assetType = APIDefines.KalturaAssetType.Media
        ottMediaAsset.contextType = APIDefines.PlaybackContextType.Playback
        ottMediaAsset.assetReferenceType = APIDefines.AssetReferenceType.Media
        ottMediaAsset.protocol = PhoenixMediaProvider.HttpProtocol.Http
        ottMediaAsset.ks = null
        ottMediaAsset.formats = listOf("Mobile_Main")
        val ottMediaOptions = OTTMediaOptions(ottMediaAsset)

        ottMediaOptions.startPosition = 0L

        return ottMediaOptions
    }

    private fun doPrepare(item: Item) {
        if (item is OTTItem) {
            manager.setKalturaParams(KalturaPlayer.Type.ott, item.partnerId)
            manager.setKalturaServerUrl(item.serverUrl)
        }

        if (item is OVPItem) {
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
                toastLong("onMediaEntryLoaded: ${mediaEntry.name}")
                // reduceLicenseDuration(mediaEntry, 300)
            }

            override fun onSourceSelected(
                assetId: String,
                source: PKMediaSource,
                drmParams: PKDrmParams?
            ) {
                toastLong("onSourceSelected ")
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

    private fun doPrefetch(item: Item) {

        if (item is KalturaItem) {
            manager.setKalturaParams(KalturaPlayer.Type.ovp, item.partnerId)
            manager.setKalturaServerUrl(item.serverUrl)
        }

        val prefetchCallback = object : Prefetch.PrefetchCallback {
            override fun onPrefetched(
                assetId: String,
                assetInfo: OfflineManager.AssetInfo,
                selected: MutableMap<OfflineManager.TrackType, MutableList<OfflineManager.Track>>?
            ) {

            }

            override fun onPrepared(
                assetId: String,
                assetInfo: OfflineManager.AssetInfo,
                selected: MutableMap<OfflineManager.TrackType, MutableList<OfflineManager.Track>>?
            ) {
                item.assetInfo = assetInfo
                assetList.invalidateViews()
            }

            override fun onPrefetchError(assetId: String, error: Exception) {
                toastLong("onPrefetchError: $error")
            }

            override fun onPrepareError(assetId: String, error: java.lang.Exception) {
                TODO("Not yet implemented")
            }

            override fun onMediaEntryLoadError(error: Exception) {
                toastLong("onMediaEntryLoadError: $error")
            }
        }

        //val ottMediaOptions = buildOttMediaOptions();
        //manager.prefetchAsset(ottMediaOptions, PrefetchConfig(), prefetchCallback)

        val defaultPrefs = OfflineManager.SelectionPrefs().apply {
            //videoHeight = 300
            videoBitrate = 600000
            //videoWidth = 400
            allAudioLanguages = true
            allTextLanguages = true
            allowInefficientCodecs = false
        }

        var pm = manager.getPrefetchManager()
        if (item is KalturaItem) {
            if (!TextUtils.isEmpty(item.serverUrl)) {
                manager.setKalturaServerUrl(item.serverUrl);
            }

            manager.prefetchManager.prefetchAsset(item.mediaOptions(), PrefetchConfig().setSelectionPrefs(defaultPrefs), prefetchCallback)

//            var ms1 = OTTMediaAsset()
//            ms1.assetId = "610715"
//            ms1.formats = Collections.singletonList("Tablet Main")
//            ms1.protocol = PhoenixMediaProvider.HttpProtocol.Https
//            var mo1 = OTTMediaOptions(ms1)
//
//            var ms2 = OTTMediaAsset()
//            ms2.assetId = "924187"
//            ms2.formats = Collections.singletonList("Tablet Main")
//            ms2.protocol = PhoenixMediaProvider.HttpProtocol.Https
//            var mo2 = OTTMediaOptions(ms2)
//
//            var entries = mutableListOf<MediaOptions>()
//            entries.add(mo1)
//            entries.add(mo2)
//            entries.add(item.mediaOptions())
//            pm.prefetchByMediaOptionsList(entries, PrefetchConfig())
        } else {
            item.entry?.let { entry ->
                manager.prefetchManager.prefetchAsset(entry, PrefetchConfig().setSelectionPrefs(defaultPrefs), prefetchCallback)

//                var m1 = PKMediaEntry()
//                m1.duration = 10000
//                m1.id = "m1"
//                m1.mediaType = PKMediaEntry.MediaEntryType.Vod
//                m1.name = "m1"
//                var ms1 = PKMediaSource()
//                ms1.id = "m1"
//                ms1.mediaFormat = PKMediaFormat.hls
//                ms1.url = "https://rest-as.ott.kaltura.com/api_v3/service/assetFile/action/playManifest/partnerId/225/assetId/853309/assetType/media/assetFileId/9547693/contextType/PLAYBACK/a.m3u8"
//                m1.sources = Collections.singletonList(ms1)
//
//
//                var m2 = PKMediaEntry()
//                m2.duration = 10000
//                m2.id = "m2"
//                m2.mediaType = PKMediaEntry.MediaEntryType.Vod
//                m2.name = "m2"
//                var ms2 = PKMediaSource()
//                ms2.id = "m2"
//                ms2.mediaFormat = PKMediaFormat.hls
//                ms2.url = "https://rest-as.ott.kaltura.com/api_v3/service/assetFile/action/playManifest/partnerId/225/assetId/853309/assetType/media/assetFileId/9547693/contextType/PLAYBACK/a.m3u8"
//                m2.sources = Collections.singletonList(ms2)
//
//                var entries = mutableListOf<PKMediaEntry>()
//                entries.add(m1)
//                entries.add(m2)
//                entries.add(entry)
//                pm.prefetchByMediaEntryList(entries, PrefetchConfig())
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

    private fun toastLong(msg: String) = runOnUiThread {
        log.d("$msg")
        runOnUiThread() {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

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
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        private val ASSET_ID = "548576"
        val PARTNER_ID = 3009
    }
}
