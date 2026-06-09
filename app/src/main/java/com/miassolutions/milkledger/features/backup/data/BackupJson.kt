package com.miassolutions.milkledger.features.backup.data

import kotlinx.serialization.json.Json

object BackupJson {
    val json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = true
    }
}