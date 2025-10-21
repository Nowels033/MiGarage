package com.example.migarage.notify

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import kotlin.math.max

object ReminderScheduler {

    private fun workName(carId: String, maintId: String) =
        "maint_reminder_${carId}_$maintId"

    fun schedule(
        context: Context,
        carId: String,
        maintId: String,
        triggerAtMillis: Long,
        title: String,
        body: String
    ) {
        val delay = max(0L, triggerAtMillis - System.currentTimeMillis())

        val data = Data.Builder()
            .putString("title", title)
            .putString("body", body)
            .putInt("notifId", (carId + maintId).hashCode())
            .build()

        val req = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(data)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(carId, maintId),
            ExistingWorkPolicy.REPLACE,
            req
        )
    }

    fun cancel(context: Context, carId: String, maintId: String) {
        WorkManager.getInstance(context)
            .cancelUniqueWork(workName(carId, maintId))
    }
}
