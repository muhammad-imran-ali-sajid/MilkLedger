package com.miassolutions.milkledger.features.notes.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.miassolutions.milkledger.features.note.alarm.NoteAlarmReceiver
import com.miassolutions.milkledger.features.note.data.local.NoteEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    // ✅ Function 1: Schedule Alarm using NoteEntity
    fun schedule(note: NoteEntity) {
        // 1. Agar time null hai ya guzar chuka hai, to return
        val triggerTime = note.alarmAtMillis ?: return
        if (triggerTime <= System.currentTimeMillis()) return

        // 2. Intent banayen jo Receiver par jayega
        val intent = Intent(context, NoteAlarmReceiver::class.java).apply {
            putExtra(NoteAlarmReceiver.EXTRA_NOTE_ID, note.noteId)
            putExtra(NoteAlarmReceiver.EXTRA_TITLE, note.title)
            putExtra(NoteAlarmReceiver.EXTRA_MESSAGE, note.content)
        }

        // 3. Unique ID generate karein (Note ID ka hash)
        // Taake har note ka apna alag alarm ho, overwrite na ho
        val uniqueRequestCode = note.noteId.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Permission Check & Set Alarm
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    setExactAlarm(triggerTime, pendingIntent)
                } else {
                    Log.e("AlarmScheduler", "Permission denied for exact alarm")
                    // Yahan aap chahen to standard alarm set kr den ya user ko ignore karen
                    // Kyunki permission fragment me handle ho chuki hoti hai usually
                }
            } else {
                setExactAlarm(triggerTime, pendingIntent)
            }
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    // ✅ Function 2: Cancel Alarm using NoteEntity
    fun cancel(note: NoteEntity) {
        val intent = Intent(context, NoteAlarmReceiver::class.java)

        // ID same honi chahiye jo schedule krte waqt thi
        val uniqueRequestCode = note.noteId.hashCode()

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            uniqueRequestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    // Helper Function
    private fun setExactAlarm(triggerTime: Long, pendingIntent: PendingIntent) {
        // Doze mode me bhi bajega
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }
}