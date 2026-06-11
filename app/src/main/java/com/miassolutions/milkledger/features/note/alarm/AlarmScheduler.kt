package com.miassolutions.milkledger.features.note.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.miassolutions.milkledger.core.localdb.note.NoteEntity
import com.miassolutions.milkledger.features.activities.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private val alarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    
    fun schedule(note: NoteEntity): Boolean {
        val alarmAtMillis = note.alarmAtMillis ?: return false
        
        if (alarmAtMillis <= System.currentTimeMillis()) {
            Log.w("AlarmScheduler", "Alarm time already passed")
            return false
        }
        
        if (!canScheduleExactAlarm()) {
            Log.w("AlarmScheduler", "Exact alarm permission missing")
            return false
        }
        
        val alarmPendingIntent = createAlarmPendingIntent(note)
        val showPendingIntent = createShowPendingIntent(note)
        
        return try {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(
                    alarmAtMillis,
                    showPendingIntent
                ),
                alarmPendingIntent
            )
            
            Log.d("AlarmScheduler", "Real alarm scheduled: ${note.title}")
            true
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "Failed to schedule alarm", e)
            false
        }
    }
    
    fun cancel(note: NoteEntity) {
        val alarmPendingIntent = createAlarmPendingIntent(note)
        alarmManager.cancel(alarmPendingIntent)
        alarmPendingIntent.cancel()
    }
    
    private fun createAlarmPendingIntent(note: NoteEntity): PendingIntent {
        val intent = Intent(context, NoteAlarmReceiver::class.java).apply {
            putExtra(AlarmConstants.EXTRA_NOTE_ID, note.noteId)
            putExtra(AlarmConstants.EXTRA_TITLE, note.title)
            putExtra(AlarmConstants.EXTRA_MESSAGE, note.content)
        }
        
        return PendingIntent.getBroadcast(
            context,
            note.noteId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun createShowPendingIntent(note: NoteEntity): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmConstants.EXTRA_NOTE_ID, note.noteId)
        }
        
        return PendingIntent.getActivity(
            context,
            note.noteId.hashCode() + 1000,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    private fun canScheduleExactAlarm(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }
}