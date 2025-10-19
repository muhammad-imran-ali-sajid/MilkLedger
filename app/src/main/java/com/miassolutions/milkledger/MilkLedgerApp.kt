package com.miassolutions.milkledger

import android.app.Application
import androidx.core.os.BuildCompat
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MilkLedgerApp : Application() {
    override fun onCreate() {
        super.onCreate()


//        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}