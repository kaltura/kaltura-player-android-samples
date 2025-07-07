package com.kaltura.playkit.samples.broadpeak

import androidx.preference.PreferenceManager

object ConfigurationProvider {
    private val BASE_URL_PREF_KEY = "BASE_URL_PREF_KEY"
    private val BASE_URL_PREF_DEFAULT_VALUE = "https://api.frp1.ott.kaltura.com/api_v3/"

    private val PARTNER_ID_PREF_KEY = "PARTNER_ID_PREF_KEY"
    private val PARTNER_ID_PREF_DEFAULT_VALUE = 5033

    private val FILE_FORMATS_PREF_KEY = "FILE_FORMATS_PREF_KEY"
    private val FILE_FORMATS_PREF_DEFAULT_VALUE = null

    private val ASSET_ID_PREF_KEY = "ASSET_ID_PREF_KEY"
    private val ASSET_ID_PREF_DEFAULT_VALUE = "3891900"

    private val VFAST_ASSET_ID_PREF_KEY = "VFAST_ASSET_ID_PREF_KEY"
    private val VFAST_ASSET_ID_PREF_DEFAULT_VALUE = ""

    private val IS_DRM_PREF_KEY = "IS_DRM_PREF_KEY"
    private val IS_DRM_PREF_DEFAULT_VALUE = false

    private val KS_TOKEN_PREF_KEY = "KS_TOKEN_PREF_KEY"
    private val KS_TOKEN_PREF_DEFAULT_VALUE = ""

    fun getBaseUrl(): String {
        return PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).getString(BASE_URL_PREF_KEY, BASE_URL_PREF_DEFAULT_VALUE)
            ?: BASE_URL_PREF_DEFAULT_VALUE
    }

    fun setBaseUrl(baseUrl: String) {
        with (PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).edit()) {
            putString(BASE_URL_PREF_KEY, baseUrl)
            apply()
        }
    }

    fun getPartnerId(): Int {
        return PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).getInt(PARTNER_ID_PREF_KEY, PARTNER_ID_PREF_DEFAULT_VALUE)
            ?: PARTNER_ID_PREF_DEFAULT_VALUE
    }

    fun setPartnerId(partnerId: Int) {
        with (PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).edit()) {
            putInt(PARTNER_ID_PREF_KEY, partnerId)
            apply()
        }
    }

    fun getFileFormats(): List<String>? {
        val fileFormatsString = PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).getString(FILE_FORMATS_PREF_KEY, FILE_FORMATS_PREF_DEFAULT_VALUE)
            ?: FILE_FORMATS_PREF_DEFAULT_VALUE
        return fileFormatsString?.split(",")
    }

    fun getFileFormatsString(): String? {
        return PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).getString(FILE_FORMATS_PREF_KEY, FILE_FORMATS_PREF_DEFAULT_VALUE)
            ?: FILE_FORMATS_PREF_DEFAULT_VALUE
    }

    fun setFileFormats(fileFormats: String?) {
        with (PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).edit()) {
            putString(FILE_FORMATS_PREF_KEY, fileFormats)
            apply()
        }
    }

    fun getVfastAssetId(): String {
        return PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).getString(VFAST_ASSET_ID_PREF_KEY, VFAST_ASSET_ID_PREF_DEFAULT_VALUE)
            ?: VFAST_ASSET_ID_PREF_DEFAULT_VALUE
    }

    fun setVfastAssetId(vfastAssetId: String) {
        with (PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).edit()) {
            putString(VFAST_ASSET_ID_PREF_KEY, vfastAssetId)
            apply()
        }
    }

    fun getAssetId(): String {
        return PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).getString(ASSET_ID_PREF_KEY, ASSET_ID_PREF_DEFAULT_VALUE)
            ?: ASSET_ID_PREF_DEFAULT_VALUE
    }

    fun setAssetId(assetId: String) {
        with (PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).edit()) {
            putString(ASSET_ID_PREF_KEY, assetId)
            apply()
        }
    }

    fun getIsDrm(): Boolean {
        return PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).getBoolean(IS_DRM_PREF_KEY, IS_DRM_PREF_DEFAULT_VALUE)
            ?: IS_DRM_PREF_DEFAULT_VALUE
    }

    fun setIsDrm(isDrm: Boolean) {
        with (PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).edit()) {
            putBoolean(IS_DRM_PREF_KEY, isDrm)
            apply()
        }
    }

    fun getKsToken(): String {
        return PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).getString(KS_TOKEN_PREF_KEY, KS_TOKEN_PREF_DEFAULT_VALUE)
            ?: KS_TOKEN_PREF_DEFAULT_VALUE
    }

    fun setKsToken(ksToken: String) {
        with (PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).edit()) {
            putString(KS_TOKEN_PREF_KEY, ksToken)
            apply()
        }
    }
}