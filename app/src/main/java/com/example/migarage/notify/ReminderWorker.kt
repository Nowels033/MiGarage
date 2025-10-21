package com.example.migarage.notify

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val title = inputData.getString("title") ?: "Mantenimiento"
        val body  = inputData.getString("body")  ?: "Tienes un mantenimiento hoy"

        // Un id de noti estable: hash de workName si quieres
        val id = inputData.getInt("notifId", System.currentTimeMillis().toInt())
        NotificationHelper.show(applicationContext, id, title, body)
        return Result.success()
    }
}
