package com.miassolutions.milkledger.features.backup.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import com.miassolutions.milkledger.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupNotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val notificationManager: NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    fun createChannelIfNeeded() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Backup notifications",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Shows Milk Ledger backup progress and result"
        }
        
        notificationManager.createNotificationChannel(channel)
    }
    

    
    fun showSuccessNotification(fileName: String?) {
        createChannelIfNeeded()
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_backup)
            .setContentTitle("Backup completed")
            .setContentText(fileName ?: "Milk Ledger data was backed up successfully.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_SUCCESS_ID, notification)
    }
    
    fun showFailureNotification(message: String?) {
        createChannelIfNeeded()
        
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_backup)
            .setContentTitle("Backup failed")
            .setContentText(message ?: "Backup will retry when internet is available.")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(NOTIFICATION_FAILURE_ID, notification)
    }
    
    companion object {
        const val CHANNEL_ID = "milk_ledger_backup_channel"
        
        const val NOTIFICATION_SUCCESS_ID = 6002
        const val NOTIFICATION_FAILURE_ID = 6003
    }
}