package com.miassolutions.milkledger.core.helper

import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object RemoteConfigHelper {
    private const val TAG = "RemoteConfigHelper"
    private const val BUTTON_ENABLED_KEY = "isTrialVersion"

    private val remoteConfig: FirebaseRemoteConfig by lazy { FirebaseRemoteConfig.getInstance() }

    /**
     * Initialize Remote Config with defaults and fetch interval settings.
     */
    fun init(defaults: Map<String, Any> = mapOf(BUTTON_ENABLED_KEY to true)) {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(0) // 0 for debug; increase for production
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(defaults)
    }

    /**
     * Fetch and activate the Remote Config values lifecycle-safely.
     * Returns the value of the specified key via callback.
     */
    fun fetchValue(
        lifecycleOwner: LifecycleOwner,
        key: String = BUTTON_ENABLED_KEY,
        onResult: (Boolean) -> Unit
    ) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        val value = remoteConfig.getBoolean(key)
                        Log.d(TAG, "RemoteConfig [$key] = $value (fetch success: ${task.isSuccessful})")
                        onResult(value)
                    }
                } else {
                    Log.w(TAG, "fetchValue ignored: lifecycle not active")
                }
            }
    }
}
