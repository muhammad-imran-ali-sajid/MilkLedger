package com.miassolutions.milkledger

import android.app.Application
import com.google.firebase.FirebaseApp
import com.miassolutions.milkledger.core.helper.RemoteConfigHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MilkLedgerApp : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this)
        RemoteConfigHelper.init()

//        val channel = NotificationChannel(
//            "notes_channel",
//            "Note Reminders",
//            NotificationManager.IMPORTANCE_HIGH
//        ).apply {
//            description = "Reminders for saved notes"
//        }
//
//        val manager = getSystemService(NotificationManager::class.java)
//        manager.createNotificationChannel(channel)

//        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}