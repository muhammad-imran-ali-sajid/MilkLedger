package com.miassolutions.milkledger.utils.premiumfeatures

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor() {

    companion object {
        private const val TAG = "RemoteConfig"

        private const val IS_PREMIUM_ENABLED = "is_premium_enabled"
        private const val MIN_SUPPORTED_VERSION = "min_supported_version"
        private const val APK_URL = "apk_url"
        private const val UPDATE_MESSAGE = "update_message"
        private const val IS_PDF_FEATURE_ENABLED = "is_pdf_feature_enabled"
    }

    private val rc = FirebaseRemoteConfig.getInstance()

    init {
        rc.setConfigSettingsAsync(
            remoteConfigSettings { minimumFetchIntervalInSeconds = 0 }
        )

        rc.setDefaultsAsync(
            mapOf(
                IS_PREMIUM_ENABLED to false,
                MIN_SUPPORTED_VERSION to 1,
                APK_URL to "",
                UPDATE_MESSAGE to "Please update the app",
                IS_PDF_FEATURE_ENABLED to false
            )
        )
    }

    suspend fun fetch() {
        val result = rc.fetchAndActivate().await()

        Log.d(TAG, "Fetch success: $result")
        Log.d(TAG, "isPremiumRemote: ${rc.getBoolean(IS_PREMIUM_ENABLED)}")
        Log.d(TAG, "minSupportedVersion: ${rc.getLong(MIN_SUPPORTED_VERSION)}")
        Log.d(TAG, "apkUrl: ${rc.getString(APK_URL)}")
        Log.d(TAG, "updateMessage: ${rc.getString(UPDATE_MESSAGE)}")
        Log.d(TAG, "pdfFeatureEnabled: ${rc.getBoolean(IS_PDF_FEATURE_ENABLED)}")
    }

    fun isPremiumRemote(): Boolean {
        val value = rc.getBoolean(IS_PREMIUM_ENABLED)
        Log.d(TAG, "isPremiumRemote() = $value")
        return value
    }

    fun getMinSupportedVersion(): Int {
        val value = rc.getLong(MIN_SUPPORTED_VERSION).toInt()
        Log.d(TAG, "getMinSupportedVersion() = $value")
        return value
    }

    fun getApkUrl(): String {
        val value = rc.getString(APK_URL)
        Log.d(TAG, "getApkUrl() = $value")
        return value
    }

    fun getUpdateMessage(): String {
        val value = rc.getString(UPDATE_MESSAGE)
        Log.d(TAG, "getUpdateMessage() = $value")
        return value
    }

    fun isPdfFeatureEnabled(): Boolean {
        val value = rc.getBoolean(IS_PDF_FEATURE_ENABLED)
        Log.d(TAG, "isPdfFeatureEnabled() = $value")
        return value
    }
}