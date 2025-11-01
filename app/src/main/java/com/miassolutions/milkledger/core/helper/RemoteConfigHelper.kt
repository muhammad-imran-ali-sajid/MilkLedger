package com.miassolutions.milkledger.core.helper

import android.util.Log
import android.view.ViewGroup
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
            .setMinimumFetchIntervalInSeconds(0) // 0 = debug, use 3600 for production
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(defaults)
    }

    /**
     * Lifecycle-safe fetch and activate.
     * Executes callback only if lifecycle is at least STARTED.
     */
    fun fetchAndActivate(
        lifecycleOwner: LifecycleOwner,
        onComplete: (Boolean) -> Unit
    ) {
        remoteConfig.fetchAndActivate()
            .addOnCompleteListener { task ->
                // Execute only if the fragment/activity is still active
                if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                    lifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
                        onComplete(task.isSuccessful)
                    }
                } else {
                    Log.w(TAG, "fetchAndActivate ignored: lifecycle not active")
                }
            }
    }

    /**
     * Apply enable/disable state to a button/card based on Remote Config value.
     */
    fun applyButtonState(button: ViewGroup, key: String = BUTTON_ENABLED_KEY): Boolean {
        val isEnabled = remoteConfig.getBoolean(key)
        button.isEnabled = isEnabled
        button.alpha = if (isEnabled) 1.0f else 0.5f
        Log.d(TAG, "RemoteConfig [$key] = $isEnabled")
        return isEnabled
    }
}
