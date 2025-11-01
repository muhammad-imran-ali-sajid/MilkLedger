package com.miassolutions.milkledger.core.helper

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

object RemoteConfigHelper {
    private val tag = "RemoteConfigHelper"
    private const val BUTTON_ENABLED_KEY = "isTrialVersion" // Change key as needed
    private val remoteConfig: FirebaseRemoteConfig by lazy { FirebaseRemoteConfig.getInstance() }


    /**
     * Initialize Remote Config with defaults and fetch new values.
     */
    fun init(defaults: Map<String, Any> = mapOf(BUTTON_ENABLED_KEY to true)) {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0) // 1 hour for production, 0 for debug
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(defaults)
    }


    /**
     * Fetch and activate remote config values.
     */
    fun fetchAndActivate(onComplete: (() -> Unit)? = null) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onComplete?.invoke()
                } else {
                    onComplete?.invoke()
                }
            }
    }


    /**
     * Apply the button enable/disable state based on Remote Config value.
     */
    fun applyButtonState(button: ViewGroup, key: String = BUTTON_ENABLED_KEY): Boolean {
        val isEnabled = remoteConfig.getBoolean(key)
        button.isEnabled = isEnabled
        button.alpha = if (isEnabled) 1.0f else 0.5f // Optional: dim the button when disabled
        Log.d(tag, "Value : $isEnabled")
        return isEnabled
    }
}