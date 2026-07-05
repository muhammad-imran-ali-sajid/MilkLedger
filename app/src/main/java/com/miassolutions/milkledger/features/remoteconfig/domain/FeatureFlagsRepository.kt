package com.miassolutions.milkledger.features.remoteconfig.domain

import com.miassolutions.milkledger.features.remoteconfig.model.FeatureFlags
import kotlinx.coroutines.flow.StateFlow

interface FeatureFlagsRepository {
    val featureFlags: StateFlow<FeatureFlags>

    suspend fun refresh()

    fun isDriveBackupEnabledNow(): Boolean
}