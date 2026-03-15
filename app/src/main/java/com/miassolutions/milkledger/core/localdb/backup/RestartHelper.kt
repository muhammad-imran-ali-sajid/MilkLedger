package com.miassolutions.milkledger.core.localdb.backup

import android.content.Context
import android.content.Intent
import com.miassolutions.milkledger.features.activities.LauncherActivity

object RestartHelper {

    fun restart(context: Context) {
        val packageManager = context.packageManager

        // App ka main launch intent get karein
        val intent = packageManager.getLaunchIntentForPackage(context.packageName)

        if (intent != null) {
            val componentName = intent.component

            // Yeh intent specifically restart ke liye best hai
            val mainIntent = Intent.makeRestartActivityTask(componentName)

            // New Task ke taur par start karein
            context.startActivity(mainIntent)

            // Current process kill kar dein
            Runtime.getRuntime().exit(0)
        } else {
            // Agar intent na miley to fallback (Normal restart)
            val fallbackIntent = Intent(context, LauncherActivity::class.java)
            fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            context.startActivity(fallbackIntent)
            Runtime.getRuntime().exit(0)
        }
    }
}