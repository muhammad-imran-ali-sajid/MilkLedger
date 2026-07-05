package com.miassolutions.milkledger.features.remoteconfig.model

data class FeatureFlags(
    val driveBackupEnabled: Boolean = false,
    val loaded: Boolean = false
)