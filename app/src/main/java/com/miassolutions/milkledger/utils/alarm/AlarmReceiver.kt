package com.miassolutions.milkledger.utils.alarm



import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.miassolutions.milkledger.core.notification.AppNotifier


class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val title = intent?.getStringExtra("title") ?: "Reminder"
        val message = intent?.getStringExtra("message") ?: "Your scheduled task is due"

        AppNotifier.show(
            context = context,
            title = title,
            message = message
        )

        Toast.makeText(context, "Alarm Triggered: $title", Toast.LENGTH_SHORT).show()
    }
}
