package com.kaltura.playkit.samples.basicsample

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import com.kaltura.playkit.samples.basicsample.databinding.ActivityConfigurationBinding

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
            etKsToken.setText(ConfigurationProvider.getKsToken())
            cbIsDrm.isChecked = ConfigurationProvider.getIsDrm()
            etFileFormats.setText(ConfigurationProvider.getFileFormatsString())
            btApply.setOnClickListener {
                ConfigurationProvider.setBaseUrl(etBaseUrl.text.toString())
                ConfigurationProvider.setPartnerId(etPartnerId.text.toString().toInt())
                ConfigurationProvider.setAssetId(etAssetId.text.toString())
                ConfigurationProvider.setKsToken(etKsToken.text.toString())
                ConfigurationProvider.setIsDrm(cbIsDrm.isChecked)
                ConfigurationProvider.setFileFormats(etFileFormats.text.toString())

                startActivity(Intent(this@ConfigurationActivity, MainActivity::class.java))
            }
        }
    }
}