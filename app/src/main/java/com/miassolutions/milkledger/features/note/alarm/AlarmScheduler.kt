package com.miassolutions.milkledger.features.note.alarm


import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.miassolutions.milkledger.features.note.data.local.NoteEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(note: NoteEntity) {
        if (note.alarmAtMillis == null) return

        // Agar time guzar chuka hai to alarm na lagayen
        if (note.alarmAtMillis <= System.currentTimeMillis()) return

        val intent = Intent(context, NoteAlarmReceiver::class.java).apply {
            putExtra(NoteAlarmReceiver.EXTRA_NOTE_ID, note.noteId)
            putExtra(NoteAlarmReceiver.EXTRA_TITLE, note.title)
            putExtra(NoteAlarmReceiver.EXTRA_MESSAGE, note.content)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.noteId.hashCode(), // Unique ID per note
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            // 🔥 Exact Alarm: Doze mode mein bhi bajega
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                note.alarmAtMillis,
                pendingIntent
            )
            Log.d("AlarmScheduler", "Alarm set for: ${note.title} at ${note.alarmAtMillis}")
        } catch (e: SecurityException) {
            // Android 12+ pe agar permission nahi mili to crash se bachen
            e.printStackTrace()
        }
    }

    fun cancel(note: NoteEntity) {
        val intent = Intent(context, NoteAlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            note.noteId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }
}