package com.miassolutions.milkledger.core.notification


import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.miassolutions.milkledger.R

object AppNotifier {

    private const val DEFAULT_CHANNEL_ID = "default_channel"
    private const val DEFAULT_CHANNEL_NAME = "General Notifications"

    /**
     * Call this once (e.g., in Application.onCreate())
     */
    @SuppressLint("ObsoleteSdkInt")
    fun init(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                DEFAULT_CHANNEL_ID,
                DEFAULT_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Default app notifications"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Show notification with automatic permission and channel handling.
     */
    fun show(
        context: Context,
        title: String,
        message: String,
        channelId: String = DEFAULT_CHANNEL_ID,
        @DrawableRes icon: Int = R.drawable.ic_notification,
        intent: Intent? = null,
        notificationId: Int = System.currentTimeMillis().toInt()
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Log or ignore — app should request this permission before showing
            return
        }

        val pendingIntent = intent?.let {
            PendingIntent.getActivity(
                context, 0, it,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(message)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .apply {
                if (pendingIntent != null) setContentIntent(pendingIntent)
            }

        with(NotificationManagerCompat.from(context)) {
            notify(notificationId, builder.build())
        }
    }

    /**
     * Create additional channels if needed (for categorized notifications)
     */
    @SuppressLint("ObsoleteSdkInt")
    fun createChannel(
        context: Context,
        channelId: String,
        channelName: String,
        description: String = "",
        importance: Int = NotificationManager.IMPORTANCE_DEFAULT
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance)
            channel.description = description
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}
