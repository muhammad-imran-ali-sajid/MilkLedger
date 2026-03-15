package com.miassolutions.milkledger.features.note.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.features.activities.MainActivity

class NoteAlarmReceiver : BroadcastReceiver() {

    // ✅ In constants ko AlarmScheduler me use krna hy
    companion object {
        const val EXTRA_NOTE_ID = "extra_note_id"
        const val EXTRA_TITLE = "extra_note_title"
        const val EXTRA_MESSAGE = "extra_note_message"
        const val CHANNEL_ID = "note_alarm_channel"
    }

    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("ALARM_TEST", "Receiver Triggered! ⏰") // 🔥 Log check krna

        val noteId = intent?.getStringExtra(EXTRA_NOTE_ID)
        val title = intent?.getStringExtra(EXTRA_TITLE) ?: "Reminder"
        val message = intent?.getStringExtra(EXTRA_MESSAGE) ?: "Check your note"

        if (noteId == null) {
            Log.e("ALARM_TEST", "Note ID missing!")
            return
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Channel Creation (Android 8+)
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Note Reminders",
            NotificationManager.IMPORTANCE_HIGH // 🔥 HIGH zaroori hai
        ).apply {
            description = "Alarm Notifications"
            enableLights(true)
            enableVibration(true)
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
            )
        }
        notificationManager.createNotificationChannel(channel)

        // Notification Build
        val appIntent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            noteId.hashCode(),
            appIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // ⚠️ Make sure ye icon ho
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(noteId.hashCode(), notification)
        Log.d("ALARM_TEST", "Notification Sent! 🚀")
    }
}