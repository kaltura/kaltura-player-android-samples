package com.kaltura.player.offlinedemo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import com.google.android.material.snackbar.Snackbar
import com.kaltura.playkit.PKMediaFormat
import com.kaltura.tvplayer.OVPMediaOptions
import com.kaltura.tvplayer.OfflineManager

import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*
import java.lang.Exception

//const val partnerId = 2215841
//const val entryId = "1_9bwuo813"
const val partnerId = 1851571
const val entryId = "0_pl5lbfo0"



class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val manager = OfflineManager.getInstance(this)

        manager.setAssetStateListener(object : OfflineManager.AssetStateListener {
            override fun onAssetDownloadFailed(assetId: String?, error: OfflineManager.AssetDownloadException?) {
                snackbarLong("Download of $error failed: $error")
            }

            override fun onAssetDownloadComplete(assetId: String?) {
                snackbar("Complete")
            }

            override fun onAssetDownloadPending(assetId: String?) {

            }

            override fun onAssetDownloadPaused(assetId: String?) {
                snackbar("Paused")
            }

            override fun onRegistered(assetId: String?, drmStatus: OfflineManager.DrmStatus?) {

                snackbar("onRegistered")
            }

            override fun onRegisterError(assetId: String?, error: Exception?) {
                snackbarLong("onRegisterError: $assetId $error")
            }

            override fun onStateChanged(assetId: String?, assetInfo: OfflineManager.AssetInfo?) {
                snackbar("onStateChanged")
            }

            override fun onAssetRemoved(assetId: String?) {
                snackbar("onAssetRemoved")
            }

        })

        val options = OVPMediaOptions(entryId)

        manager.setKalturaPartnerId(partnerId)
        manager.setPreferredMediaFormat(PKMediaFormat.hls)

        var myAssetInfo: OfflineManager.AssetInfo? = null

        prepareButton.setOnClickListener {
            manager.prepareAsset(options, null, object: OfflineManager.PrepareCallback {
                override fun onPrepared(
                    assetInfo: OfflineManager.AssetInfo?,
                    selected: MutableMap<OfflineManager.TrackType, MutableList<OfflineManager.Track>>?
                ) {
                    snackbar("Prepared", Snackbar.LENGTH_SHORT)
                    myAssetInfo = assetInfo
                }

                override fun onPrepareError(error: Exception?) {
                    snackbar("Prepare error: $error", Snackbar.LENGTH_LONG)
                }
            })
        }

        startButton.setOnClickListener {
            manager.startAssetDownload(myAssetInfo)
        }

        playButton.setOnClickListener {
            startActivity(Intent(this, PlayActivity::class.java).apply {
                data = Uri.parse(entryId)
            })
        }

        removeButton.setOnClickListener {
            manager.removeAsset(entryId)
        }

        statusButton.setOnClickListener {
            val drmStatus = manager.getDrmStatus(entryId)

            Snackbar.make(contentLayout, drmStatus.toString(), Snackbar.LENGTH_LONG).setAction("Renew") {
                manager.renewDrmAsset(entryId, options)
            }.show()
        }

    }

    private fun snackbar(msg: String, duration: Int) {
        runOnUiThread {
            Snackbar.make(contentLayout, msg, duration).show()
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
}
