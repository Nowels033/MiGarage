package com.example.migarage.notify

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.migarage.R

object NotificationHelper {
    const val CHANNEL_MAINT = "maint_reminders"

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val ch = NotificationChannel(
                CHANNEL_MAINT,
                "Recordatorios de mantenimiento",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            ch.description = "Avisos de mantenimientos programados"
            mgr.createNotificationChannel(ch)
        }
    }

    fun show(context: Context, id: Int, title: String, text: String) {
        val notif = NotificationCompat.Builder(context, CHANNEL_MAINT)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // usa tu icono
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(id, notif)
    }
}
