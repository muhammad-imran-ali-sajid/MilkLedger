package com.miassolutions.milkledger.core.notification



import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class ScheduledNotificationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    override fun doWork(): Result {
        val title = inputData.getString("title") ?: "Reminder"
        val message = inputData.getString("message") ?: "Check your task"

        AppNotifier.show(
            context = context,
            title = title,
            message = message
        )
        return Result.success()
    }
}
