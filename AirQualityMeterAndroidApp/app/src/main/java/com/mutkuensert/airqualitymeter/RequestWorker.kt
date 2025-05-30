package com.mutkuensert.airqualitymeter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.mutkuensert.airqualitymeter.data.PmResponse
import com.mutkuensert.airqualitymeter.data.Repository
import timber.log.Timber
import java.util.concurrent.TimeUnit

class RequestWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val repository: Repository
) : CoroutineWorker(appContext, workerParams) {
    private val context = appContext

    companion object {
        private const val NOTIFICATION_CHANNEL_ID = "THRESHOLD_NOTIFICATION_CHANNEL_ID"

        fun enqueue(context: Context) {
            val periodicRequestWorker =
                PeriodicWorkRequestBuilder<RequestWorker>(15, TimeUnit.MINUTES) //Minimum: https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work#:~:text=The%20minimum%20repeat%20interval%20that%20can%20be%20defined%20is%2015%20minutes
                    .setConstraints(
                        Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    )
                    .build()
            WorkManager.getInstance(context).enqueue(periodicRequestWorker)
        }
    }

    private val NOTIFICATION_CHANNEL_NAME = context.getString(R.string.threshold_exceeded_notify)

    override suspend fun doWork(): Result {
        val result = repository.updatePmData()
        return if (result.isSuccess) {
            val pmResponse = result.getOrNull()!!
            if (pmResponse.pm25 > repository.thresholdPm25) {
                try {
                    setForeground(createForegroundInfo(pmResponse))
                } catch (exception: Exception) {
                    Timber.e(exception)
                }
            }
            Result.success()
        } else {
            Result.failure()
        }
    }

    private fun createForegroundInfo(pmResponse: PmResponse): ForegroundInfo {
        val intent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.pm_2_5_pm_10, pmResponse.pm25, pmResponse.pm10)
        }
        val notificationManager: NotificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.warning))
            .setContentText(context.getString(R.string.air_quality_threshold_exceeded))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .addAction(
                android.R.drawable.ic_delete,
                context.getString(R.string.cancel_periodic_control),
                intent
            )
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(1, notification)
        }
    }
}