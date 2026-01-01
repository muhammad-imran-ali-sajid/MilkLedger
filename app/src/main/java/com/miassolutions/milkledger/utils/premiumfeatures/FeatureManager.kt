package com.miassolutions.milkledger.utils.premiumfeatures

import kotlinx.coroutines.flow.Flow

class FeatureManager(
    private val prefs: PremiumFeaturePrefs,
    private val remote: RemoteConfigManager
) {

    suspend fun refreshFlags() {
        remote.fetch()
        prefs.setIsPremiumEnabled(remote.isPremiumRemote())

    }

    fun isPremiumEnabled(): Flow<Boolean> = prefs.isPremiumEnabled
}