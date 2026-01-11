package com.miassolutions.milkledger.core.localdb.backup

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.miassolutions.milkledger.core.activities.LauncherActivity

class BackupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val launchIntent = Intent(context, LauncherActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        }
        context.startActivity(launchIntent)
    }
}
