package com.kaltura.playkit.samples.prefetchsample

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.*
import com.kaltura.playkit.providers.api.phoenix.APIDefines
import com.kaltura.playkit.providers.ott.OTTMediaAsset
import com.kaltura.playkit.providers.ott.PhoenixMediaProvider
import com.kaltura.playkit.samples.prefetchsample.data.OfflineConfig
import com.kaltura.playkit.samples.prefetchsample.ui.PlayActivity
import com.kaltura.playkit.samples.prefetchsample.ui.adapter.RvOfflineAssetsAdapter
import com.kaltura.tvplayer.*
import com.kaltura.tvplayer.offline.OfflineManagerSettings
import com.kaltura.tvplayer.offline.dtg.DTGOfflineManager
import com.kaltura.tvplayer.offline.exo.ExoOfflineManager
import com.kaltura.tvplayer.offline.exo.PrefetchConfig
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.layout_provider_chooser.*
import kotlinx.android.synthetic.main.view_item.*
import kotlinx.android.synthetic.main.view_prefetch_config.*
import java.util.*

class MainActivity : AppCompatActivity() {

    val log = PKLog.get("MainActivity")

    private lateinit var rvOfflineAssetsAdapter: RvOfflineAssetsAdapter
    private val itemMap = mutableMapOf<String, Item>()
    private var offlineManager: OfflineManager? = null
    private lateinit var offlineSharePref: SharedPreferences
    private val KEY_PROVIDER: String = "KEY_OFFLINE_PROVIDER"
    private val dtgOfflineProvider: Int = 1
    private val exoOfflineProvider: Int = 2

    private var prefetchSettingMaxItemCountInCache: Int = 20
    private var prefetchSettingAssetPrefetchSize: Int = 2 // IN MB
    private var prefetchSettingRemoveCacheOnDestroy: Boolean = true

    var startTime = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        offlineSharePref = getPreferences(Context.MODE_PRIVATE)

        val appConfig = loadItemsFromJson(this)
        val testItems = appConfig.items.map { it.toItem() }

        testItems.filter { it != NULL }.forEach {
            itemMap[it.id()] = it
        }
        rvOfflineAssetsAdapter = RvOfflineAssetsAdapter(testItems) {
            showActionsDialog(
                    rvOfflineAssetsAdapter.getItemAtPosition(it).apply { position = it },
                    it
            )
        }

        btn_sumbit_prefetch_settings.setOnClickListener {
            (et_ps_item_in_cache.text).toString()?.let {
                if (it.isNotEmpty()) {
                    prefetchSettingMaxItemCountInCache = it.toInt()
                }
            }

            (et_ps_asset_size.text).toString()?.let {
                if (it.isNotEmpty()) {
                    prefetchSettingAssetPrefetchSize = it.toInt()
                }
            }

            prefetchSettingRemoveCacheOnDestroy = cb_ps_remove_on_destroy.isChecked

            fl_prefetch_settings.visibility = View.GONE

            toast(getString(R.string.prefetch_settings))
        }

        rvAssetList.adapter = rvOfflineAssetsAdapter
        rvAssetList.isNestedScrollingEnabled = false
        rvAssetList.setHasFixedSize(true)
        rvAssetList.itemAnimator = null

        val provider = getOfflineProvider(KEY_PROVIDER)
        if (provider > 0) {
            provider_frame.visibility = View.GONE
            offlineManager = if (provider == exoOfflineProvider) {
                offlineProvider = OfflineManager.OfflineProvider.EXO
                OfflineManager.getInstance(this, offlineProvider)
            } else {
                offlineProvider = OfflineManager.OfflineProvider.DTG
                OfflineManager.getInstance(this, offlineProvider)
            }
            setupManager(offlineManager, appConfig.offlineConfig)
        } else {
            provider_frame.visibility = View.VISIBLE
        }

        cb_is_exo_enable.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                offlineProvider =  OfflineManager.OfflineProvider.EXO
                offlineManager = OfflineManager.getInstance(
                        this,
                        offlineProvider
                )
                saveOfflineProvider(exoOfflineProvider)
            }

            // Show the custom notification
//            offlineManager?.setForegroundNotification(
//                OfflineCustomNotification(this, Consts.EXO_DOWNLOAD_CHANNEL_ID)
//            )
            hideProviderFrame()
            setupManager(offlineManager, appConfig.offlineConfig)
        }

        cb_is_dtg_enable.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                offlineProvider =  OfflineManager.OfflineProvider.DTG
                offlineManager = OfflineManager.getInstance(
                        this,
                        offlineProvider
                )
                saveOfflineProvider(dtgOfflineProvider)
            }
            hideProviderFrame()
            setupManager(offlineManager, appConfig.offlineConfig)
        }
    }

    private fun setupManager(manager: OfflineManager?, offlineConfig: OfflineConfig?) {
        offlineManager = manager

        offlineConfig?.let {
            offlineManager?.setPreferredMediaFormat(it.preferredFormat)
        }

        runOnUiThread {
            if (offlineManager is ExoOfflineManager) {
                rvOfflineAssetsAdapter.isOfflineProviderExo(true)
                rvOfflineAssetsAdapter.notifyDataSetChanged()
            }
            toastLong(getString(R.string.message_enable_prefetch))
        }

        val offlineSettings = OfflineManagerSettings()
        offlineSettings.hlsAudioBitrateEstimation = 64000

//        val customAdapterData = "CUSTOM_DATA"
//        var licenseRequestAdapter = DRMAdapter()
//        DRMAdapter.customData = customAdapterData
//        manager.setLicenseRequestAdapter(licenseRequestAdapter)

        //   offlineSettings.downloadRequestAdapter = DownloadRequestAdapter()

        offlineManager?.setOfflineManagerSettings(offlineSettings)

        addAssetStateListener(offlineManager)

        offlineManager?.setDownloadProgressListener { assetId, bytesDownloaded, totalBytesEstimated, percentDownloaded ->
            log.d("[progress] $assetId: ${bytesDownloaded / 1000} / ${totalBytesEstimated / 1000}")
            val item = itemMap[assetId] ?: return@setDownloadProgressListener
            item.bytesDownloaded = bytesDownloaded
            item.percentDownloaded = percentDownloaded
            if (item.position == -1) {
                item.position = rvOfflineAssetsAdapter.getPositionOfItem(assetId)
            }
            if (item.position != -1) {
                updateRecyclerViewAdapter(item.position)
            } else {
                // In worst case, update the whole recycler view.
                rvOfflineAssetsAdapter.notifyDataSetChanged()
            }
        }

        offlineManager?.start {
            log.d("manager started")
            itemMap.values.forEach {
                it.assetInfo = offlineManager?.getAssetInfo(it.id())
                if (it.position == -1) {
                    it.position = rvOfflineAssetsAdapter.getPositionOfItem(it.id())
                }
                updateRecyclerViewAdapter(it.position)
            }
        }
    }

    private fun saveOfflineProvider(value: Int) {
        saveToPref(KEY_PROVIDER, value)
    }

    private fun updateRecyclerViewAdapter(position: Int) {
        runOnUiThread {
            rvOfflineAssetsAdapter.notifyItemChanged(position)
        }
    }

    private fun showProgressBar() {
        runOnUiThread {
            pbLoader.visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        runOnUiThread {
            if (pbLoader.isVisible) {
                pbLoader.visibility = View.GONE
            }
        }
    }

    private fun showActionsDialog(item: Item, position: Int) {
        if (offlineManager == null) {
            toast(getString(R.string.no_offline_provider))
            return
        }

        val items = arrayOf(
                if (item.isPrefetch && offlineManager is ExoOfflineManager) "Prefetch" else "Prepare",
                "Start",
                "Pause",
                "Play-Offline",
                "Play-Online",
                "Remove",
                "Status"
        )
        AlertDialog.Builder(this).setItems(items) { _, i ->
            when (i) {
                0 -> {
                    showProgressBar()
                    if (item.isPrefetch && offlineManager is ExoOfflineManager) {
                        doPrefetch(item)
                    } else {
                        doPrepare(item)
                    }
                }
                1 -> doStart(item)
                2 -> doPause(item)
                3 -> doOfflinePlayback(item, position)
                4 -> doOnlinePlayback(item, position)
                5 -> {
                    doRemove(item)
                }
                6 -> doStatus(item)
            }
        }.show()
    }

    private fun doStatus(item: Item) {
        if (item !is KalturaItem) {
            toastLong("Not applicable")
            return
        }

        val drmStatus = offlineManager?.getDrmStatus(item.id())

        if (drmStatus?.status == OfflineManager.DrmStatus.Status.unknown) {
            toastLong("To check the status is invalid for this asset")
            return
        }

        if (drmStatus?.isClear == true) {
            toastLong("Clear")
            return
        }

        val msg = when {
            drmStatus?.isValid == true -> "Valid; will expire in " + drmStatus.currentRemainingTime + " seconds"
            else -> "Expired or Error"
        }

        snackbar(msg, "Renew") {
            offlineManager?.setKalturaParams(KalturaPlayer.Type.ovp, item.partnerId)
            offlineManager?.renewDrmAssetLicense(
                    item.id(),
                    item.mediaOptions(),
                    object : OfflineManager.MediaEntryCallback {
                        override fun onMediaEntryLoaded(
                                assetId: String,
                                downloadType: OfflineManager.DownloadType,
                                mediaEntry: PKMediaEntry
                        ) {
                            //   reduceLicenseDuration(mediaEntry, 300)
                        }

                        override fun onMediaEntryLoadError(
                                downloadType: OfflineManager.DownloadType,
                                error: Exception
                        ) {
                            toastLong("onMediaEntryLoadError: $error")
                        }
                    })
        }
    }

    private fun doRemove(item: Item) {
        item.assetInfo?.assetId?.let {
            showProgressBar()
            offlineManager?.removeAsset(it)
            updateItemStatus(item)
            return
        }
        toast("This asset is not prepared.")
    }

    private fun doOfflinePlayback(item: Item, position: Int) {
        item.assetInfo?.assetId?.let {
            val intent = Intent(this, PlayActivity::class.java)
            val bundle = Bundle()
            bundle.putInt("position", position)
            bundle.putLong("startPosition", item.startPosition ?: -1)
            intent.putExtra("assetBundle", bundle)
            intent.data = Uri.parse(it)
            startActivity(intent)
            return
        }
        toast("This asset is not downloaded.")
    }

    private fun doOnlinePlayback(item: Item, position: Int) {
        val intent = Intent(this, PlayActivity::class.java)

        val bundle = Bundle()
        bundle.putBoolean("isOnlinePlayback", true)
        bundle.putInt("position", position)
        bundle.putLong("startPosition", item.startPosition ?: -1)
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
                offlineManager?.pauseAssetDownload(itemId)
                updateItemStatus(it)
            }
        }
    }

    private fun doStart(item: Item) {
        log.d("doStart")
        val assetInfo = item.assetInfo ?: return

        startTime = SystemClock.elapsedRealtime()
        try {
            offlineManager?.startAssetDownload(assetInfo)
        } catch (e: IllegalArgumentException){
            hideProgressBar()
        }

        updateItemStatus(item)
    }

    private fun updateItemStatus(assetId: String) {
        hideProgressBar()
        updateItemStatus(itemMap[assetId] ?: return)
    }

    private fun updateItemStatus(item: Item) {
        item.assetInfo = offlineManager?.getAssetInfo(item.id())
        updateRecyclerViewAdapter(item.position)
    }

    private fun doPrepare(item: Item) {

        val assetInfo = offlineManager?.getAssetInfo(item.id())
        if (assetInfo?.state == OfflineManager.AssetDownloadState.completed) {
            offlineManager?.getDrmStatus(item.id())?.let {
                if (it.isValid || it.status == OfflineManager.DrmStatus.Status.unknown){
                    hideProgressBar()
                    toast("Asset already downloaded")
                    return
                }
            }
        }

        if (item is OTTItem) {
            offlineManager?.setKalturaParams(KalturaPlayer.Type.ott, item.partnerId)
            offlineManager?.setKalturaServerUrl(item.serverUrl)
        }

        if (item is OVPItem) {
            offlineManager?.setKalturaParams(KalturaPlayer.Type.ovp, item.partnerId)
            offlineManager?.setKalturaServerUrl(item.serverUrl)
        }

        val prepareCallback = object : OfflineManager.PrepareCallback {

            override fun onPrepared(
                    assetId: String,
                    assetInfo: OfflineManager.AssetInfo,
                    selected: MutableMap<OfflineManager.TrackType, MutableList<OfflineManager.Track>>?
            ) {
                item.assetInfo = assetInfo
                runOnUiThread {
                    hideProgressBar()
                    snackbar("Prepared", "Start") {
                        doStart(item)
                    }
                    updateRecyclerViewAdapter(item.position)
                }
            }

            override fun onPrepareError(
                    assetId: String,
                    downloadType: OfflineManager.DownloadType,
                    error: Exception
            ) {
                hideProgressBar()
                toastLong("onPrepareError: $error")
            }

            override fun onMediaEntryLoadError(
                    downloadType: OfflineManager.DownloadType,
                    error: Exception
            ) {
                hideProgressBar()
                toastLong("onMediaEntryLoadError: $error")
            }

            override fun onMediaEntryLoaded(
                    assetId: String,
                    downloadType: OfflineManager.DownloadType,
                    mediaEntry: PKMediaEntry
            ) {
                hideProgressBar()
                toastLong("onMediaEntryLoaded: ${mediaEntry.name}")
                // reduceLicenseDuration(mediaEntry, 300)
            }

            override fun onSourceSelected(
                    assetId: String,
                    source: PKMediaSource,
                    drmParams: PKDrmParams?
            ) {
                // hideProgressBar()
                toastLong("onSourceSelected ")
            }
        }

        val defaultPrefs = OfflineManager.SelectionPrefs().apply {
            //  videoHeight = 500
            // videoBitrate = 300000
            // videoWidth = 3000
            allAudioLanguages = true
            allTextLanguages = true
            // videoCodecs = mutableListOf()
            //  videoCodecs.also { it?.add(OfflineManager.TrackCodec.AVC1) }
            //allowInefficientCodecs = false
        }

        if (item is KalturaItem) {
            if (!TextUtils.isEmpty(item.serverUrl)) {
                offlineManager?.setKalturaServerUrl(item.serverUrl);
            }
            offlineManager?.prepareAsset(
                    item.mediaOptions(),
                    item.selectionPrefs ?: defaultPrefs,
                    prepareCallback
            )
        } else {
            item.entry?.let { entry ->
                offlineManager?.prepareAsset(
                        entry,
                        item.selectionPrefs ?: defaultPrefs,
                        prepareCallback
                )
            }

            /*val mediaEntries = mutableListOf<PKMediaEntry?>()
            for (i:Int in 0 until 5) {
                val adapterItem = rvOfflineAssetsAdapter.getItemAtPosition(i).apply { position = i }
               // mediaEntries.add(adapterItem.entry)
                adapterItem.entry?.let {
                    offlineManager?.prepareAsset(it, defaultPrefs, prepareCallback)
                }
            }*/
        }
    }

    private fun doPrefetch(item: Item) {
        val prefetchManager = offlineManager?.prefetchManager
        prefetchManager?.setPrefetchConfig(PrefetchConfig().apply {
            isRemoveCacheOnDestroy = prefetchSettingRemoveCacheOnDestroy
            maxItemCountInCache = prefetchSettingMaxItemCountInCache
            assetPrefetchSize = prefetchSettingAssetPrefetchSize
        })

        if (prefetchManager?.isPrefetched(item.id()) == true) {
            offlineManager?.getDrmStatus(item.id())?.let {
                if (it.isValid || it.status == OfflineManager.DrmStatus.Status.unknown) {
                    hideProgressBar()
                    toast("Asset already prefetched")
                    return
                }
            }
        }

        if (item is KalturaItem) {
            offlineManager?.setKalturaParams(KalturaPlayer.Type.ovp, item.partnerId)
            offlineManager?.kalturaServerUrl = item.serverUrl
        }

        val prefetchCallback = addPrefetchCallback(item)

        //val ottMediaOptions = buildOttMediaOptions();
        //manager.prefetchAsset(ottMediaOptions, PrefetchConfig(), prefetchCallback)

        val defaultPrefs = OfflineManager.SelectionPrefs().apply {
            //videoHeight = 300
            // videoBitrate = 600000
            //videoWidth = 400
            allAudioLanguages = true
            allTextLanguages = true
            allowInefficientCodecs = false
        }

        if (item is KalturaItem) {
            if (!TextUtils.isEmpty(item.serverUrl)) {
                offlineManager?.setKalturaServerUrl(item.serverUrl);
            }

            prefetchManager?.prefetchAsset(item.mediaOptions(), defaultPrefs, prefetchCallback)
            //prefetchManager?.prefetchByMediaEntryList(mediaOptions, defaultPrefs, prefetchCallback)
        } else {

            item.entry?.let {
                prefetchManager?.prefetchAsset(it, defaultPrefs, prefetchCallback)
            }

            // Pass a list of mediaEntries
            /* val mediaEntries = mutableListOf<PKMediaEntry?>()
             item.entry?.let { entry ->
                 prefetchManager?.prefetchByMediaEntryList(mediaEntries, defaultPrefs, prefetchCallback)
             }*/
        }
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

    private fun addAssetStateListener(manager: OfflineManager?) {

        manager?.setAssetStateListener(object : OfflineManager.AssetStateListener {

            override fun onAssetDownloadFailed(
                    assetId: String,
                    downloadType: OfflineManager.DownloadType,
                    error: Exception
            ) {
                toastLong("Download of $assetId, ${downloadType.name} failed: $error")
                updateItemStatus(assetId)
            }

            override fun onAssetDownloadComplete(
                    assetId: String,
                    downloadType: OfflineManager.DownloadType
            ) {
                log.d("onAssetDownloadComplete $assetId totalDownloadTime: ${SystemClock.elapsedRealtimeNanos() - startTime}")

                if (downloadType == OfflineManager.DownloadType.FULL) {
                    toast("Complete $assetId")
                } else {
                    toast("Prefetched $assetId")
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

            override fun onAssetDownloadPending(
                    assetId: String,
                    downloadType: OfflineManager.DownloadType
            ) {
                //toast("Pending - onAssetDownloadPending")
                updateItemStatus(assetId)
            }

            override fun onAssetDownloadPaused(
                    assetId: String,
                    downloadType: OfflineManager.DownloadType
            ) {
                toast("Paused - onAssetDownloadPaused")
                updateItemStatus(assetId)
            }

            override fun onRegistered(assetId: String, drmStatus: OfflineManager.DrmStatus) {
                toast("onRegistered: ${drmStatus.currentRemainingTime} seconds left")
                updateItemStatus(assetId)
            }

            override fun onRegisterError(
                    assetId: String,
                    downloadType: OfflineManager.DownloadType,
                    error: Exception
            ) {
                toastLong("onRegisterError: $assetId, ${downloadType.name}, $error ")
                //   updateItemStatus(assetId)
                val item = itemMap[assetId] ?: return
                item.drmNotRegistered = true
                updateRecyclerViewAdapter(item.position)
            }

            override fun onStateChanged(
                    assetId: String,
                    downloadType: OfflineManager.DownloadType,
                    assetInfo: OfflineManager.AssetInfo
            ) {
                toast("onStateChanged state = " + assetInfo.state.name)
                updateItemStatus(assetId)
            }

            override fun onAssetRemoved(
                    assetId: String,
                    downloadType: OfflineManager.DownloadType
            ) {
                hideProgressBar()
                toast("onAssetRemoved")
                updateItemStatus(assetId)
            }

            override fun onAssetRemoveError(
                    assetId: String,
                    downloadType: OfflineManager.DownloadType,
                    error: java.lang.Exception
            ) {
                hideProgressBar()
                toast("Error Asset Was Not Removed")
            }
        })
    }

    private fun addPrefetchCallback(item: Item): OfflineManager.PrepareCallback {

        return object : OfflineManager.PrepareCallback {

            override fun onPrepared(
                    assetId: String,
                    assetInfo: OfflineManager.AssetInfo,
                    selected: MutableMap<OfflineManager.TrackType, MutableList<OfflineManager.Track>>?
            ) {
                hideProgressBar()
                item.assetInfo = assetInfo
                updateRecyclerViewAdapter(item.position)
            }

            override fun onPrepareError(
                    assetId: String,
                    downloadType: OfflineManager.DownloadType,
                    error: java.lang.Exception
            ) {
                hideProgressBar()
                toastLong("onPrepareError: $error")
            }

            override fun onMediaEntryLoadError(
                    downloadType: OfflineManager.DownloadType,
                    error: Exception
            ) {
                hideProgressBar()
                toastLong("onMediaEntryLoadError: $error")
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
        Snackbar.make(rvAssetList, msg, BaseTransientBottomBar.LENGTH_LONG).apply {
            duration = 5000
            setAction(next) {
                nextAction()
            }
            show()
        }
    }

    private fun toast(msg: String) = runOnUiThread { Toast.makeText(this, msg, Toast.LENGTH_SHORT).show() }

    private fun toastLong(msg: String) = runOnUiThread {
        log.d(msg)
        runOnUiThread() {
            Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        }
    }

    private fun hideProviderFrame() {
        val anim = TranslateAnimation(0f, 800f, 0f, 0f)
        anim.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation?) {}
            override fun onAnimationEnd(animation: Animation?) {
                provider_frame.visibility = View.GONE
            }

            override fun onAnimationRepeat(animation: Animation?) {}
        })
        anim.duration = 500
        provider_frame.startAnimation(anim)
    }

    private fun saveToPref(key: String, value: Int) {
        with (offlineSharePref.edit()) {
            putInt(key, value)
            apply()
        }
    }

    private fun getOfflineProvider(key: String): Int {
        return offlineSharePref.getInt(key, 0)
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
            R.id.action_settings -> {
                if (offlineManager == null) {
                    toastLong(getString(R.string.no_offline_provider))
                    return false
                }

                if (offlineManager is DTGOfflineManager) {
                    toastLong(getString(R.string.no_prefetch_for_dtg))
                    return false
                }

                if (fl_prefetch_settings.visibility == View.VISIBLE)
                    fl_prefetch_settings.visibility = View.GONE
                else {
                    if (offlineManager is ExoOfflineManager) {
                        et_ps_item_in_cache.setText(prefetchSettingMaxItemCountInCache.toString())
                        et_ps_asset_size.setText(prefetchSettingAssetPrefetchSize.toString())
                        cb_ps_remove_on_destroy.isChecked = prefetchSettingRemoveCacheOnDestroy
                    }
                    fl_prefetch_settings.visibility = View.VISIBLE
                }

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        offlineManager?.let {
            // Removing listeners by setting it to null
            it.setAssetStateListener(null)
            it.setDownloadProgressListener(null)
            it.stop()
        }
    }

    companion object {
        val SERVER_URL = "https://rest-us.ott.kaltura.com/v4_5/api_v3/"
        private val ASSET_ID = "548576"
        val PARTNER_ID = 3009
        var offlineProvider: OfflineManager.OfflineProvider? = null
    }
}
