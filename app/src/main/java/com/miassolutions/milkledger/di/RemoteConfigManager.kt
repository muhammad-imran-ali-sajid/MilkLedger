package com.miassolutions.milkledger.di

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigManager @Inject constructor() {
    private val rc : FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    companion object {
        private const val IS_PDF_FEATURE_ENABLED = "is_pdf_feature_enabled"
    }

    init {
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 0
        }

        rc.setConfigSettingsAsync(configSettings)

        val defaults = mapOf(
            IS_PDF_FEATURE_ENABLED to false
        )

        rc.setDefaultsAsync(defaults)
    }

    fun fetchAndActivate(){
        rc.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful){
                    val updated = task.result
                    Log.d("RemoteConfigManager", "Result : $updated")
                }
            }
    }

    fun isPdfFeatureEnabled(): Boolean {
        return  rc.getBoolean(IS_PDF_FEATURE_ENABLED)
    }
}