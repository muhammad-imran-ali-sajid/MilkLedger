package com.miassolutions.milkledger

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.os.BuildCompat
import com.miassolutions.milkledger.core.helper.RemoteConfigHelper
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MilkLedgerApp : Application() {
    override fun onCreate() {
        super.onCreate()
        RemoteConfigHelper.init()

        val channel = NotificationChannel(
            "notes_channel",
            "Note Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminders for saved notes"
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

//        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}