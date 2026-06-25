package com.miassolutions.milkledger.features.settings

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val dynamicColorsEnabled: Boolean = false,
    val fontScale: AppFontScale = AppFontScale.NORMAL
)

enum class AppTheme {
    SYSTEM, LIGHT, DARK
}