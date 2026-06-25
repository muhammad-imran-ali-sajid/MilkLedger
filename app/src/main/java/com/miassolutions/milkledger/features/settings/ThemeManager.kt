package com.miassolutions.milkledger.features.settings

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors

object ThemeManager {

    fun applyTheme(theme: AppTheme) {
        val mode = when (theme) {
            AppTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }

        AppCompatDelegate.setDefaultNightMode(mode)
    }

    fun applyDynamicColorsIfEnabled(
        activity: Activity,
        enabled: Boolean
    ) {
        if (enabled && DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyToActivityIfAvailable(activity)
        }
    }

    fun wrapFontScale(
        baseContext: Context,
        fontScale: Float
    ): Context {
        val config = Configuration(baseContext.resources.configuration)
        config.fontScale = fontScale
        return baseContext.createConfigurationContext(config)
    }
}