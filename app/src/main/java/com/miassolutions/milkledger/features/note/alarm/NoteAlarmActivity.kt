package com.miassolutions.milkledger.features.note.alarm

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.miassolutions.milkledger.databinding.ActivityNoteAlarmBinding
import com.miassolutions.milkledger.features.activities.MainActivity

class NoteAlarmActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityNoteAlarmBinding
    
    private var noteId: String? = null
    private var title: String = "Reminder"
    private var message: String = "Check your note"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        showOverLockScreen()
        
        binding = ActivityNoteAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        readIntentData()
        bindViews()
        setupListeners()
    }
    
    private fun readIntentData() {
        noteId = intent.getStringExtra(AlarmConstants.EXTRA_NOTE_ID)
        title = intent.getStringExtra(AlarmConstants.EXTRA_TITLE) ?: "Reminder"
        message = intent.getStringExtra(AlarmConstants.EXTRA_MESSAGE) ?: "Check your note"
    }
    
    private fun bindViews() {
        binding.tvAlarmTitle.text = title
        binding.tvAlarmMessage.text = message
    }
    
    private fun setupListeners() {
        binding.btnDismiss.setOnClickListener {
            dismissAlarm()
            finish()
        }
        
        binding.btnOpenNote.setOnClickListener {
            dismissAlarm()
            openMainActivity()
            finish()
        }
    }
    
    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
    
    private fun dismissAlarm() {
        val dismissIntent = Intent(this, NoteAlarmService::class.java).apply {
            action = AlarmConstants.ACTION_DISMISS_ALARM
        }
        
        startService(dismissIntent)
    }
    
    private fun openMainActivity() {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmConstants.EXTRA_NOTE_ID, noteId)
        }
        
        startActivity(mainIntent)
    }
}