package com.mutkuensert.airqualitymonitor.application

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.mutkuensert.airqualitymonitor.R

class AirQualityNotification(private val context: Context) {
    val NOTIFICATION_CHANNEL_NAME = context.getString(R.string.threshold_exceeded_notify)

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "THRESHOLD_NOTIFICATION_CHANNEL_ID"
        const val NOTIFICATION_ID = 1
    }

    private val notificationCompat = NotificationManagerCompat.from(context)

    fun getNotificationBuilder(title: String, content: String): NotificationCompat.Builder {
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
    }

    fun push(title: String, content: String, priority: Int = NotificationCompat.PRIORITY_LOW) {
        createNotificationChannel()
        val notification = getNotificationBuilder(title, content).setPriority(priority).build()

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        notificationCompat.notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        )
        channel.description =
            context.getString(R.string.notification_channel_for_threshold_exceeded_info)

        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }
}