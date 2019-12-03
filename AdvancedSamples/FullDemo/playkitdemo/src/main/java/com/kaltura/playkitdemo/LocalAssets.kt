package com.kaltura.playkitdemo

import android.content.Context
import android.content.DialogInterface
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast

import androidx.appcompat.app.AlertDialog

import com.kaltura.netkit.connect.response.ResultElement
import com.kaltura.netkit.utils.ErrorElement
import com.kaltura.playkit.LocalAssetsManager
import com.kaltura.playkit.LocalDataStore
import com.kaltura.playkit.PKDrmParams
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.PKMediaEntry
import com.kaltura.playkit.PKMediaFormat
import com.kaltura.playkit.PKMediaSource
import com.kaltura.playkit.providers.base.OnMediaLoadCompletion

import java.io.File
import java.util.Collections

/**
 * Created by Noam Tamim @ Kaltura on 14/02/2017.
 */

class LocalAssets(context: Context) {

    private val drmParams = PKDrmParams("https://udrm.kaltura.com/widevine/license?custom_data=eyJjYV9zeXN0ZW0iOiJPVlAiLCJ1c2VyX3Rva2VuIjoiZGpKOE1UZzFNVFUzTVh4NThBVkFQOXo1R0lvU3BXWE95emEtdWlQNnk5cXpBdkpkMzZfUFZOcUNfT0NZWWhLRVh5LThqcGFMRktGcU15d3VTN2ZLZmxibTd1VVJGQkVtSGxsNkc4NEU2LUxrcnFXbVV1ZWEtZnFqdXc9PSIsImFjY291bnRfaWQiOiIxODUxNTcxIiwiY29udGVudF9pZCI6IjBfcGw1bGJmbzAiLCJmaWxlcyI6IjBfendxM2w0NHIsMF91YTYycms2cywwX290bWFxcG5mLDBfeXdrbXFua2csMV9lMHF0YWoxaiwxX2IycXp5dmE3In0%3D&signature=LFiNPZL8%2BNevsZ8cNhrmSDM4SDQ%3D", PKDrmParams.Scheme.WidevineClassic)
    private val widevineClassicSource = PKMediaSource()
            .setId("wvc")
            .setDrmData(listOf(drmParams))
            .setMediaFormat(PKMediaFormat.wvm)
            .setUrl("http://cdnapi.kaltura.com/p/1851571/playManifest/entryId/0_pl5lbfo0/format/url/tags/widevine/protocol/http/a.wvm")
    private val localAssetPath: String

    private val widevineClassicEntry = PKMediaEntry().setId("e1").setMediaType(PKMediaEntry.MediaEntryType.Vod)
            .setSources(listOf(widevineClassicSource))
    private val localAssetsManager: LocalAssetsManager

    init {

        val localDataStore = LocalAssetsManager.DefaultLocalDataStore(context)
        localAssetsManager = LocalAssetsManager(context, localDataStore)

        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, "0_pl5lbfo0.wvm")
        localAssetPath = file.absolutePath
    }

    fun registerAsset(listener: LocalAssetsManager.AssetRegistrationListener) {
        localAssetsManager.registerAsset(widevineClassicSource, localAssetPath, widevineClassicSource.id, listener)
    }

    fun getLocalMediaEntry(completion: OnMediaLoadCompletion) {
        val localMediaSource = localAssetsManager.getLocalMediaSource(widevineClassicSource.id, localAssetPath)
        completion.onComplete(object : ResultElement<PKMediaEntry> {

            override fun getResponse(): PKMediaEntry {
                return PKMediaEntry()
                        .setMediaType(PKMediaEntry.MediaEntryType.Vod)
                        .setId(widevineClassicEntry.id)
                        .setSources(listOf(localMediaSource))
            }

            override fun isSuccess(): Boolean {
                return true
            }

            override fun getError(): ErrorElement? {
                return null
            }
        })
    }

    fun checkAssetStatus(assetStatusListener: LocalAssetsManager.AssetStatusListener) {
        localAssetsManager.checkAssetStatus(localAssetPath, widevineClassicSource.id, assetStatusListener)
    }

    fun unregisterAsset(assetRemovalListener: LocalAssetsManager.AssetRemovalListener) {
        localAssetsManager.unregisterAsset(localAssetPath, widevineClassicSource.id, assetRemovalListener)
    }

    companion object {
        private val log = PKLog.get("LocalAssets")

        internal fun start(context: Context, completion: OnMediaLoadCompletion) {

            val localAssets = LocalAssets(context)

            val alertBuilder = AlertDialog.Builder(context)
            alertBuilder.setTitle("Local Assets")
                    .setNegativeButton("Hide") { dialog, which -> Handler(Looper.getMainLooper()).postDelayed({ alertBuilder.show() }, 5000) }
                    .setItems(arrayOf("Register", "Check Status", "Play", "Unregister")) { dialog, which ->
                        log.d("clicked: $which")
                        when (which) {
                            0 -> localAssets.registerAsset(object : LocalAssetsManager.AssetRegistrationListener {
                                override fun onRegistered(localAssetPath: String) {
                                    Toast.makeText(context, "Registered", Toast.LENGTH_LONG).show()
                                    alertBuilder.show()
                                }

                                override fun onFailed(localAssetPath: String, error: Exception) {
                                    Toast.makeText(context, "Failed: $error", Toast.LENGTH_LONG).show()
                                    alertBuilder.show()
                                }
                            })
                            1 -> localAssets.checkAssetStatus(LocalAssetsManager.AssetStatusListener { localAssetPath, expiryTimeSeconds, availableTimeSeconds, isRegistered ->
                                Toast.makeText(context, "Status: $isRegistered, $expiryTimeSeconds, $availableTimeSeconds", Toast.LENGTH_LONG).show()
                                alertBuilder.show()
                            })
                            2 -> {
                                localAssets.getLocalMediaEntry(completion)
                                alertBuilder.show()
                            }
                            3 -> {
                                localAssets.unregisterAsset(LocalAssetsManager.AssetRemovalListener { Toast.makeText(context, "Unregistered:", Toast.LENGTH_LONG).show() })
                                alertBuilder.show()
                            }
                        }
                    }

            alertBuilder.show()
        }
    }
}
