package com.kaltura.kalturaplayertestapp.network

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import java.util.*

object NetworkChangeReceiver : BroadcastReceiver() {

    private var isConnected = true

    override fun onReceive(context: Context, intent: Intent) {
        getObservable().connectionChanged(isInternetOn(context))
    }

    class NetworkObservable private constructor()// Exist to defeat instantiation.
        : Observable() {

        fun connectionChanged(connected: Boolean?) {
            if (isConnected != connected) {
                setChanged()
                notifyObservers(connected)
                isConnected = connected!!
            }
        }

        override fun countObservers(): Int {
            return super.countObservers()
        }

        companion object {
            private var instance: NetworkObservable? = null

            fun getInstance(): NetworkObservable {
                if (instance == null) {
                    instance = NetworkObservable()
                }
                return instance!!
            }
        }
    }

    fun getObservable(): NetworkObservable {
        return NetworkObservable.getInstance()
    }

    fun isInternetOn(context: Context): Boolean {
        val conn = context
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = conn.activeNetworkInfo
        return networkInfo?.isConnected ?: false
    }
}