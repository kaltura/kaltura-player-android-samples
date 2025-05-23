package com.kaltura.playkit.samples.basicsample

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.kaltura.playkit.player.PKHttpClientManager
import com.kaltura.playkit.samples.basicsample.databinding.ActivityConfigurationBinding
import com.kaltura.tvplayer.KalturaOttPlayer

class ConfigurationActivity : AppCompatActivity() {
    private lateinit var binding: ActivityConfigurationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfigurationBinding.inflate(LayoutInflater.from(this))
        setContentView(binding.root)

        binding.run {
            etBaseUrl.setText(ConfigurationProvider.getBaseUrl())
            etPartnerId.setText(ConfigurationProvider.getPartnerId().toString())
            etAssetId.setText(ConfigurationProvider.getAssetId())
            etVfastAssetId.setText(ConfigurationProvider.getVfastAssetId())
            etKsToken.setText(ConfigurationProvider.getKsToken())
            cbIsDrm.isChecked = ConfigurationProvider.getIsDrm()
            etFileFormats.setText(ConfigurationProvider.getFileFormatsString())
            btApply.setOnClickListener {
                ConfigurationProvider.setBaseUrl(etBaseUrl.text.toString())
                ConfigurationProvider.setPartnerId(etPartnerId.text.toString().toInt())
                ConfigurationProvider.setAssetId(etAssetId.text.toString())
                ConfigurationProvider.setVfastAssetId(etVfastAssetId.text.toString())
                ConfigurationProvider.setKsToken(etKsToken.text.toString())
                ConfigurationProvider.setIsDrm(cbIsDrm.isChecked)
                ConfigurationProvider.setFileFormats(etFileFormats.text.toString())

                KalturaOttPlayer.initialize(this@ConfigurationActivity,
                    ConfigurationProvider.getPartnerId(),
                    ConfigurationProvider.getBaseUrl()
                )
                doConnectionsWarmup()

                startActivity(Intent(this@ConfigurationActivity, MainActivity::class.java))
            }
        }
    }

    private fun doConnectionsWarmup() {
        PKHttpClientManager.setHttpProvider("okhttp")
        PKHttpClientManager.warmUp(
            "https://rest-as.ott.kaltura.com/crossdomain.xml",
            "https://api-preprod.ott.kaltura.com/crossdomain.xml",
            "https://cdnapisec.kaltura.com/favicon.ico",
            "https://cfvod.kaltura.com/favicon.ico"
        )
    }
}