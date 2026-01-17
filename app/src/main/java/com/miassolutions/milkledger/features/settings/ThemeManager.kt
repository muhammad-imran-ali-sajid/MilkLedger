package com.miassolutions.milkledger.features.settings


import androidx.appcompat.app.AppCompatDelegate

object ThemeManager {

    fun apply(theme: AppTheme) {
        when (theme) {
            AppTheme.SYSTEM ->
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                )

            AppTheme.LIGHT ->
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_NO
                )

            AppTheme.DARK ->
                AppCompatDelegate.setDefaultNightMode(
                    AppCompatDelegate.MODE_NIGHT_YES
                )
        }
    }
}
