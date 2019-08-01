package com.kaltura.player.offlinedemo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu
import android.view.MenuItem
import android.view.View
import com.google.android.material.snackbar.Snackbar
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

//        findViewById<>()
        contentLayout
        val manager = OfflineManager.getInstance(this)


        val options = OVPMediaOptions(entryId)

        manager.setKalturaPartnerId(partnerId)
        var myAssetInfo: OfflineManager.AssetInfo? = null

        startButton.setOnClickListener {
            manager.prepareAsset(options, null, object: OfflineManager.PrepareCallback {
                override fun onPrepared(
                    assetInfo: OfflineManager.AssetInfo?,
                    selected: MutableMap<OfflineManager.TrackType, MutableList<OfflineManager.Track>>?
                ) {
                    myAssetInfo = assetInfo
                    manager.addAsset(assetInfo)
                }

                override fun onPrepareError(error: Exception?) {
                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }
            })
        }

        registerButton.setOnClickListener {
            manager.registerDrmAsset(myAssetInfo, object : OfflineManager.DrmRegisterListener {
                override fun onRegistered(assetId: String?, drmInfo: OfflineManager.DrmInfo?) {
                    contentLayout.post {
                        Snackbar.make(contentLayout, "Registered!", Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onRegisterError(assetId: String?, error: Exception?) {
                    contentLayout.post {
                        Snackbar.make(contentLayout, "Register error: $error", Snackbar.LENGTH_INDEFINITE).show()
                    }
                }
            })
        }

        playButton.setOnClickListener {
            startActivity(Intent(this, PlayerActivity::class.java).apply {
                data = Uri.parse(entryId)
            })
        }

        removeButton.setOnClickListener {
            manager.removeAsset(entryId)
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
}
