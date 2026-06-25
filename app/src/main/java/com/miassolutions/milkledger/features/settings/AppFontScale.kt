package com.miassolutions.milkledger.features.settings

enum class AppFontScale(
    val scale: Float,
    val title: String,
    val subtitle: String
) {
    SMALL(
        scale = 0.90f,
        title = "Small",
        subtitle = "Compact text"
    ),

    NORMAL(
        scale = 1.00f,
        title = "Default",
        subtitle = "Recommended"
    ),

    LARGE(
        scale = 1.10f,
        title = "Large",
        subtitle = "Easier to read"
    ),

    EXTRA_LARGE(
        scale = 1.30f,
        title = "Extra Large",
        subtitle = "Maximum readability"
    )
}