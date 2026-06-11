package com.miassolutions.milkledger.features.note.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat

class NoteAlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent?) {
        Log.d("ALARM_TEST", "Alarm receiver triggered")
        
        val noteId = intent?.getStringExtra(AlarmConstants.EXTRA_NOTE_ID)
        val title = intent?.getStringExtra(AlarmConstants.EXTRA_TITLE) ?: "Reminder"
        val message = intent?.getStringExtra(AlarmConstants.EXTRA_MESSAGE) ?: "Check your note"
        
        if (noteId.isNullOrBlank()) {
            Log.e("ALARM_TEST", "Note ID missing")
            return
        }
        
        val serviceIntent = Intent(context, NoteAlarmService::class.java).apply {
            action = AlarmConstants.ACTION_START_ALARM
            putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
            putExtra(AlarmConstants.EXTRA_TITLE, title)
            putExtra(AlarmConstants.EXTRA_MESSAGE, message)
        }
        
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}