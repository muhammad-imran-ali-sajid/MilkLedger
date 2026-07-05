package com.miassolutions.milkledger.features.remoteconfig.data

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig.VALUE_SOURCE_DEFAULT
import com.google.firebase.remoteconfig.FirebaseRemoteConfig.VALUE_SOURCE_REMOTE
import com.google.firebase.remoteconfig.FirebaseRemoteConfig.VALUE_SOURCE_STATIC
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.miassolutions.milkledger.BuildConfig
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigDataSource @Inject constructor(
    private val remoteConfig: FirebaseRemoteConfig
) {

    suspend fun initialize() {
        val minimumFetchIntervalSeconds =
            if (BuildConfig.DEBUG) {
                0L
            } else {
                60L * 60L
            }

        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(minimumFetchIntervalSeconds)
            .build()

        remoteConfig.setConfigSettingsAsync(settings).await()

        remoteConfig.setDefaultsAsync(
            mapOf(
                RemoteConfigKeys.DRIVE_BACKUP_ENABLED to false
            )
        ).await()
    }

    suspend fun fetchAndActivate(): Boolean {
        val activated =
            if (BuildConfig.DEBUG) {
                // Force fresh fetch in debug.
                remoteConfig.fetch(0).await()
                remoteConfig.activate().await()
            } else {
                remoteConfig.fetchAndActivate().await()
            }

        logDriveBackupFlag("After fetchAndActivate")

        return activated
    }

    fun isDriveBackupEnabled(): Boolean {
        return remoteConfig.getBoolean(RemoteConfigKeys.DRIVE_BACKUP_ENABLED)
    }

    fun logDriveBackupFlag(prefix: String) {
        val value = remoteConfig.getValue(RemoteConfigKeys.DRIVE_BACKUP_ENABLED)
        val source = when (value.source) {
            VALUE_SOURCE_REMOTE -> "REMOTE"
            VALUE_SOURCE_DEFAULT -> "DEFAULT"
            VALUE_SOURCE_STATIC -> "STATIC"
            else -> "UNKNOWN"
        }

        Log.d(
            "RemoteConfigFlags",
            "$prefix | ${RemoteConfigKeys.DRIVE_BACKUP_ENABLED}=${value.asBoolean()} | source=$source"
        )
    }
}