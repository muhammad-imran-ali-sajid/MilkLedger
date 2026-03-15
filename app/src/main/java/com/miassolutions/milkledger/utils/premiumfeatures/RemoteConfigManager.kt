package com.miassolutions.milkledger.utils.premiumfeatures

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor(){

    companion object {
        private const val IS_PREMIUM_ENABLED = "is_premium_enabled"
        private const val MIN_SUPPORTED_VERSION = "min_supported_version"
        private const val APK_URL = "apk_url"
        private const val UPDATE_MESSAGE = "update_message"
        private const val IS_PDF_FEATURE_ENABLED = "is_pdf_feature_enabled"
    }

    private val rc = FirebaseRemoteConfig.getInstance()

    init {
        rc.setDefaultsAsync(
            mapOf(
                IS_PREMIUM_ENABLED to false,
                MIN_SUPPORTED_VERSION to 1,
                APK_URL to "",
                UPDATE_MESSAGE to "Please update the app"
            )
        )
    }

    suspend fun fetch() {
        rc.fetchAndActivate().await()
    }

    fun isPremiumRemote(): Boolean = rc.getBoolean(IS_PREMIUM_ENABLED)

    fun getMinSupportedVersion(): Int = rc.getLong(MIN_SUPPORTED_VERSION).toInt()

    fun getApkUrl(): String = rc.getString(APK_URL)

    fun getUpdateMessage(): String = rc.getString(UPDATE_MESSAGE)

    fun isPdfFeatureEnabled(): Boolean {
        return  rc.getBoolean(IS_PDF_FEATURE_ENABLED)
    }
}