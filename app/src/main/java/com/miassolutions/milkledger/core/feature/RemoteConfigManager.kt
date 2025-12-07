package com.miassolutions.milkledger.core.feature

import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.tasks.await

class RemoteConfigManager {

    companion object {
        private const val IS_PREMIUM_ENABLED = "is_premium_enabled"
    }
    private val rc = FirebaseRemoteConfig.getInstance()

    init {
        rc.setDefaultsAsync(
            mapOf(
                IS_PREMIUM_ENABLED to false
            )
        )
    }

    suspend fun fetch() {
        rc.fetchAndActivate().await()
    }

    fun isPremiumRemote() : Boolean = rc.getBoolean(IS_PREMIUM_ENABLED)
}