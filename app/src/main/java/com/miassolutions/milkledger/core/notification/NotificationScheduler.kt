package com.miassolutions.milkledger.core.notification

import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    /**
     * Schedule a one-time notification at a specific date/time.
     */
    fun scheduleNotification(
        context: android.content.Context,
        title: String,
        message: String,
        triggerTime: LocalDateTime
    ) {
        val delayMillis = Duration.between(
            LocalDateTime.now(),
            triggerTime
        ).toMillis().coerceAtLeast(0)

        val data = Data.Builder()
            .putString("title", title)
            .putString("message", message)
            .build()

        val workRequest: WorkRequest = OneTimeWorkRequestBuilder<ScheduledNotificationWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(workRequest)
    }
}