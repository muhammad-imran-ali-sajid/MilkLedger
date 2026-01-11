package com.miassolutions.milkledger.core.ui

import android.app.Activity
import android.view.WindowInsets
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

class SystemUiController(private val activity: Activity) {

    fun enableEdgeToEdge() {
        WindowInsetsControllerCompat(
            activity.window,
            activity.window.decorView
        ).show(WindowInsetsCompat.Type.systemBars())
    }

    fun hideSystemBars() {
        WindowInsetsControllerCompat(activity.window, activity.window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }
}