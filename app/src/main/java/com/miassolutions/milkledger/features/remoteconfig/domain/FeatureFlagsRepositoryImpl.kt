package com.miassolutions.milkledger.features.remoteconfig.domain

import android.util.Log
import com.miassolutions.milkledger.features.remoteconfig.data.RemoteConfigDataSource
import com.miassolutions.milkledger.features.remoteconfig.model.FeatureFlags
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeatureFlagsRepositoryImpl @Inject constructor(
    private val remoteConfigDataSource: RemoteConfigDataSource
) : FeatureFlagsRepository {

    private val _featureFlags = MutableStateFlow(FeatureFlags())


    override val featureFlags: StateFlow<FeatureFlags> = _featureFlags.asStateFlow()


    override suspend fun refresh() {
        try {
            remoteConfigDataSource.initialize()
            remoteConfigDataSource.fetchAndActivate()
            publishCurrentValues()
        } catch (e: Exception) {
            Log.e("FeatureFlags", "RemoteConfig refresh failed", e)
            publishCurrentValues()
        }
    }

    private fun publishCurrentValues() {
        _featureFlags.value = FeatureFlags(
            driveBackupEnabled = remoteConfigDataSource.isDriveBackupEnabled(),
            loaded = true
        )
    }

    override fun isDriveBackupEnabledNow(): Boolean {
        return _featureFlags.value.driveBackupEnabled
    }
}