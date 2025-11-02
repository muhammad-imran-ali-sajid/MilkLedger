package com.miassolutions.milkledger.core.alarm



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.miassolutions.milkledger.R

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Note Reminder"
        val content = intent.getStringExtra("content") ?: ""

        val notification = NotificationCompat.Builder(context, "notes_channel")
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(title.hashCode(), notification)
        }
    }
}
