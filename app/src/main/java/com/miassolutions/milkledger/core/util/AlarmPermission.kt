package com.miassolutions.milkledger.core.util


import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.core.net.toUri



fun Context.requestExactAlarmPermissionIfNeeded() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val alarmManager = getSystemService(AlarmManager::class.java)
        if (!alarmManager.canScheduleExactAlarms()) {
            Toast.makeText(this, "Please allow exact alarms in settings", Toast.LENGTH_LONG).show()
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = "package:$packageName".toUri()
            }
            startActivity(intent)
        }
    }
}

