package com.miassolutions.milkledger.features.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.appSettingsDataStore by preferencesDataStore(
    name = "app_settings"
)

class AppSettingsPreferences(
    private val context: Context
) {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val DYNAMIC_COLORS_ENABLED = booleanPreferencesKey("dynamic_colors_enabled")
        val FONT_SCALE = stringPreferencesKey("font_scale")
    }

    val settingsFlow: Flow<AppSettings> =
        context.appSettingsDataStore.data.map { prefs ->
            AppSettings(
                theme = enumValueOrDefault(
                    value = prefs[Keys.THEME],
                    default = AppTheme.SYSTEM
                ),
                dynamicColorsEnabled = prefs[Keys.DYNAMIC_COLORS_ENABLED] ?: false,
                fontScale = enumValueOrDefault(
                    value = prefs[Keys.FONT_SCALE],
                    default = AppFontScale.NORMAL
                )
            )
        }

    suspend fun setTheme(theme: AppTheme) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[Keys.THEME] = theme.name
        }
    }

    suspend fun setDynamicColorsEnabled(enabled: Boolean) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[Keys.DYNAMIC_COLORS_ENABLED] = enabled
        }
    }

    suspend fun setFontScale(fontScale: AppFontScale) {
        context.appSettingsDataStore.edit { prefs ->
            prefs[Keys.FONT_SCALE] = fontScale.name
        }
    }

    suspend fun reset() {
        context.appSettingsDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    fun readSettingsBlocking(): AppSettings {
        return runBlocking {
            settingsFlow.first()
        }
    }

    private inline fun <reified T : Enum<T>> enumValueOrDefault(
        value: String?,
        default: T
    ): T {
        return runCatching {
            if (value == null) default else enumValueOf<T>(value)
        }.getOrDefault(default)
    }
}