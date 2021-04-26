package com.kaltura.kalturaplayertestapp

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.gson.Gson
import com.kaltura.playkit.PKLog
import com.kaltura.playkit.player.PKHttpClientManager
import com.kaltura.tvplayer.KalturaOttPlayer
import com.kaltura.tvplayer.KalturaOvpPlayer

class SplashScreen: Activity() {

    private val log = PKLog.get("SplashScreen")
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        val isPlayServicesAvailable = isGooglePlayServicesAvailable()

        if (isPlayServicesAvailable) {
            KalturaOvpPlayer.initialize(this, 4171, "http://qa-apache-php7.dev.kaltura.com/")
            KalturaOvpPlayer.initialize(this, 1091, "http://qa-apache-php7.dev.kaltura.com/")
            KalturaOvpPlayer.initialize(this, 2506752, "https://cdnapisec.kaltura.com/")
            KalturaOvpPlayer.initialize(this, 27017, "https://cdnapisec.kaltura.com/")
            KalturaOvpPlayer.initialize(this, 243342, "https://cdnapisec.kaltura.com/")
            KalturaOvpPlayer.initialize(this, 1804331, "https://cdnapisec.kaltura.com/")
            KalturaOvpPlayer.initialize(this, 2267831, "https://cdnapisec.kaltura.com/")
            KalturaOvpPlayer.initialize(this, 2215841, "https://cdnapisec.kaltura.com/")
            KalturaOvpPlayer.initialize(this, 2222401, "https://cdnapisec.kaltura.com/")
            KalturaOvpPlayer.initialize(this, 1740481, "https://cdnapisec.kaltura.com/")
            KalturaOvpPlayer.initialize(this, 2093031, "https://cdnapisec.kaltura.com/")
            KalturaOvpPlayer.initialize(this, 1068292, "https://cdnapisec.kaltura.com/")
            KalturaOvpPlayer.initialize(this, 1281471, "https://cdnapisec.kaltura.com/")
            KalturaOvpPlayer.initialize(this, 2068231, "https://cdnapisec.kaltura.com/")
            KalturaOttPlayer.initialize(this, 3009, "https://rest-us.ott.kaltura.com/v4_5/")
            doConnectionsWarmup()
            val i = Intent(this@SplashScreen, SignInActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    private fun doConnectionsWarmup() {
        PKHttpClientManager.setHttpProvider("okhttp")
        PKHttpClientManager.warmUp(
                "https://rest-us.ott.kaltura.com/crossdomain.xml",
                "https://rest-as.ott.kaltura.com/crossdomain.xml",
                "https://api-preprod.ott.kaltura.com/crossdomain.xml",
                "https://cdnapisec.kaltura.com/favicon.ico",
                "https://cfvod.kaltura.com/favicon.ico"
        )
    }

    fun isGooglePlayServicesAvailable(): Boolean {
        val googlePlayServicesCheck = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this@SplashScreen)
        when (googlePlayServicesCheck) {
            ConnectionResult.SUCCESS -> return true
            ConnectionResult.SERVICE_DISABLED, ConnectionResult.SERVICE_INVALID, ConnectionResult.SERVICE_MISSING, ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED -> {
                val dialog = GoogleApiAvailability.getInstance().getErrorDialog(this@SplashScreen, googlePlayServicesCheck, 0)
                dialog.setOnCancelListener {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "com.google.android.gms"))
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                        this@SplashScreen.startActivity(intent)
                        this@SplashScreen.finish()
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }
                dialog.show()
            }
        }
        return false
    }
}