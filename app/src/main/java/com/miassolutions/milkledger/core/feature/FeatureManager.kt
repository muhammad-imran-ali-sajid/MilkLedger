package com.miassolutions.milkledger.core.feature

import kotlinx.coroutines.flow.Flow

class FeatureManager(
    private val prefs: FeaturePrefs,
    private val remote: RemoteConfigManager
) {

    suspend fun refreshFlags() {
        remote.fetch()
        prefs.setIsPremiumEnabled(remote.isPremiumRemote())
    }

    fun isPremiumEnabled(): Flow<Boolean> = prefs.isPremiumEnabled
}