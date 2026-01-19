package com.miassolutions.milkledger.app

import android.app.Application
import com.google.android.material.color.DynamicColors
import com.google.firebase.FirebaseApp
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MilkLedgerApp : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        DynamicColors.applyToActivitiesIfAvailable(this)

    }
}