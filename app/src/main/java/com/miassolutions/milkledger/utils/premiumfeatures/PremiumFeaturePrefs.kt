package com.miassolutions.milkledger.utils.premiumfeatures

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.miassolutions.milkledger.core.contstants.Constants.FEATURE_FLAGS
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(FEATURE_FLAGS)

class PremiumFeaturePrefs(private val context: Context) {

    private val IS_PREMIUM = booleanPreferencesKey("is_premium_enabled")

    val isPremiumEnabled: Flow<Boolean> = context.dataStore.data.map { it[IS_PREMIUM] ?: false }

    suspend fun setIsPremiumEnabled(value: Boolean) {
        context.dataStore.edit { it[IS_PREMIUM] = value }
    }

}