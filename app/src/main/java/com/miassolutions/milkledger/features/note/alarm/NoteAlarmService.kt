package com.miassolutions.milkledger.features.note.alarm

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.miassolutions.milkledger.R
import com.miassolutions.milkledger.features.activities.MainActivity

class NoteAlarmService : Service() {
    
    private var ringtone: Ringtone? = null
    private var vibrator: Vibrator? = null
    private var wakeLock: PowerManager.WakeLock? = null
    
    override fun onCreate() {
        super.onCreate()
        createAlarmChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            AlarmConstants.ACTION_DISMISS_ALARM -> {
                stopAlarm()
                stopSelf()
                return START_NOT_STICKY
            }
            
            AlarmConstants.ACTION_START_ALARM -> {
                val noteId = intent.getStringExtra(AlarmConstants.EXTRA_NOTE_ID) ?: "unknown"
                val title = intent.getStringExtra(AlarmConstants.EXTRA_TITLE) ?: "Reminder"
                val message = intent.getStringExtra(AlarmConstants.EXTRA_MESSAGE) ?: "Check your note"
                
                startAsForeground(noteId, title, message)
                startAlarmEffects()
            }
        }
        
        return START_NOT_STICKY
    }
    
    private fun startAsForeground(
        noteId: String,
        title: String,
        message: String
    ) {
        val notification = buildAlarmNotification(
            noteId = noteId,
            title = title,
            message = message
        )
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceCompat.startForeground(
                this,
                noteId.hashCode(),
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SYSTEM_EXEMPTED
            )
        } else {
            startForeground(noteId.hashCode(), notification)
        }
    }
    
    @SuppressLint("FullScreenIntentPolicy")
    private fun buildAlarmNotification(
        noteId: String,
        title: String,
        message: String
    ): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
        }
        
        val openAppPendingIntent = PendingIntent.getActivity(
            this,
            noteId.hashCode(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val dismissIntent = Intent(this, NoteAlarmService::class.java).apply {
            action = AlarmConstants.ACTION_DISMISS_ALARM
        }
        
        val dismissPendingIntent = PendingIntent.getService(
            this,
            noteId.hashCode() + 1,
            dismissIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val fullScreenIntent = Intent(this, NoteAlarmActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
            putExtra(AlarmConstants.EXTRA_TITLE, title)
            putExtra(AlarmConstants.EXTRA_MESSAGE, message)
        }
        
        val fullScreenPendingIntent = PendingIntent.getActivity(
            this,
            noteId.hashCode() + 2000,
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, AlarmConstants.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(true)
            .setAutoCancel(false)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(fullScreenPendingIntent)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .addAction(
                R.drawable.ic_launcher_foreground,
                "Dismiss",
                dismissPendingIntent
            )
            .build()
    }
    
    private fun startAlarmEffects() {
        acquireWakeLock()
        
        if (ringtone?.isPlaying == true) return
        
        val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        ringtone = RingtoneManager.getRingtone(this, alarmUri).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                isLooping = true
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            }
            play()
        }
        
        startVibration()
    }
    
    private fun startVibration() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(VibratorManager::class.java)
            manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        
        val pattern = longArrayOf(0, 800, 400, 800, 400, 800)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(
                VibrationEffect.createWaveform(pattern, 0)
            )
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }
    
    private fun acquireWakeLock() {
        if (wakeLock?.isHeld == true) return
        
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MilkLedger:NoteAlarmWakeLock"
        ).apply {
            acquire(5 * 60 * 1000L)
        }
    }
    
    private fun stopAlarm() {
        ringtone?.stop()
        ringtone = null
        
        vibrator?.cancel()
        vibrator = null
        
        if (wakeLock?.isHeld == true) {
            wakeLock?.release()
        }
        
        wakeLock = null
    }
    
    private fun createAlarmChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channel = NotificationChannel(
            AlarmConstants.CHANNEL_ID,
            "Note Alarms",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Note reminder alarms that ring until dismissed"
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            enableVibration(false)
            setSound(null, null)
        }
        
        notificationManager.createNotificationChannel(channel)
    }
    
    override fun onDestroy() {
        stopAlarm()
        super.onDestroy()
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}