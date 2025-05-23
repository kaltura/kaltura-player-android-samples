package com.kaltura.playkit.samples.basicsample

import androidx.core.content.edit
import androidx.preference.PreferenceManager

object ConfigurationProvider {
    private val BASE_URL_PREF_KEY = "BASE_URL_PREF_KEY"
    private val BASE_URL_PREF_DEFAULT_VALUE = "https://3201.frp1.ott.kaltura.com"

    private val PARTNER_ID_PREF_KEY = "PARTNER_ID_PREF_KEY"
    private val PARTNER_ID_PREF_DEFAULT_VALUE = 3201

    private val FILE_FORMATS_PREF_KEY = "FILE_FORMATS_PREF_KEY"
    private val FILE_FORMATS_PREF_DEFAULT_VALUE = ""

    private val ASSET_ID_PREF_KEY = "ASSET_ID_PREF_KEY"
    private val ASSET_ID_PREF_DEFAULT_VALUE = "1012948"

    private val VFAST_ASSET_ID_PREF_KEY = "VFAST_ASSET_ID_PREF_KEY"
    private val VFAST_ASSET_ID_PREF_DEFAULT_VALUE = ""

    private val IS_DRM_PREF_KEY = "IS_DRM_PREF_KEY"
    private val IS_DRM_PREF_DEFAULT_VALUE = false

    private val KS_TOKEN_PREF_KEY = "KS_TOKEN_PREF_KEY"
    private val KS_TOKEN_PREF_DEFAULT_VALUE = "djJ8MzIwMXzC_WsfHIOHhFzkDoeJ9k8aZ_OI7bcVmVmwc6rVkWEk_nxFNVWmEYA9XusnlpY4AQY5Fyv1fy5pARic2j_E7JID7Q6ERzTta4AW6G-o_NYzoBNGf8zTenK8JO4bA_l3a2xTv3a1LC5qwaCd4M9j9FLrh_p9NFWSeaOHVO6FfqsQjUTvnwvJelbNtMQoH6LRWWS2YvXCdm-RkTz7bPHyR7beSfFMWAu_pLfcLW_AP4EPPO_KIhdpWeidNJV8wp8FsP_jJfck6CqjIBErvGYxyjHayeYTforSHUozJH2G3PFNYEpOwgPX_HxdAAqNWHOQV3Cct2IY7UCDVrdNYbxckwJAinvH1jI-IJ6nm8dSodjWoVqZ15sqDMnWs8fUdJkpsGB7sDxWAP8ciHTV0aVXQHYswakLYRL9KVQW9G-yMz-0Pg=="

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

    fun getFileFormats(): List<String> {
        val fileFormatsString = PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).getString(FILE_FORMATS_PREF_KEY, FILE_FORMATS_PREF_DEFAULT_VALUE)
            ?: FILE_FORMATS_PREF_DEFAULT_VALUE
        return fileFormatsString.split(",")
    }

    fun getFileFormatsString(): String {
        return PreferenceManager.getDefaultSharedPreferences(DemoApplication.getApplicationContext()).getString(FILE_FORMATS_PREF_KEY, FILE_FORMATS_PREF_DEFAULT_VALUE)
            ?: FILE_FORMATS_PREF_DEFAULT_VALUE
    }

    fun setFileFormats(fileFormats: String) {
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