package com.miassolutions.milkledger

import android.app.Application
import androidx.core.os.BuildCompat
import com.miassolutions.milkledger.core.util.Logger
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MilkLedgerApp : Application() {
    override fun onCreate() {
        super.onCreate()


//        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}