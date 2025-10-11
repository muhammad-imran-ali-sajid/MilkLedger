package com.miassolutions.milkledger.core.backup

import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.room.RoomDatabase

/**
 * Handles post-restore actions like safely restarting Room or the whole app.
 */
object BackupHelper {

    /**
     * Closes Room database and restarts the app to ensure restored data loads.
     */
    fun restartApp(context: Context, activity: Activity? = null) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)

        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_CANCEL_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + 500, pendingIntent)
        Runtime.getRuntime().exit(0)
    }

    /**
     * Optional helper if you manage Room instance manually.
     */
    fun closeRoomDatabase(db: RoomDatabase?) {
        db?.close()
    }
}