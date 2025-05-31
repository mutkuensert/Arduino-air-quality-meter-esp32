package com.mutkuensert.airqualitymonitor.application

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
import androidx.work.WorkRequest
import androidx.work.WorkerParameters
import com.mutkuensert.airqualitymonitor.R
import com.mutkuensert.airqualitymonitor.data.AirQualityState
import com.mutkuensert.airqualitymonitor.data.AirQualityStateManager
import com.mutkuensert.airqualitymonitor.data.Repository
import com.mutkuensert.airqualitymonitor.util.CurrentTime
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.TimeUnit

class MonitorWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val repository: Repository,
    private val applicationLifecycleObserver: ApplicationLifecycleObserver,
    private val airQualityNotification: AirQualityNotification,
    private val airQualityStateManager: AirQualityStateManager,
) : CoroutineWorker(appContext, workerParams) {
    private val context = appContext

    /**
     * setForeground must be called only once https://stackoverflow.com/a/45047542
     */
    private var isFirstWorkExecution = true

    companion object {

        /**
         * @return Worker [UUID]
         */
        fun enqueue(context: Context): UUID {
            val periodicRequestWorker = buildMostFrequentWorkRequest()
            WorkManager.getInstance(context).enqueue(periodicRequestWorker)
            return periodicRequestWorker.id
        }

        private fun buildMostFrequentWorkRequest(): WorkRequest {
            return PeriodicWorkRequestBuilder<MonitorWorker>(
                15,
                TimeUnit.MINUTES
            ) //Minimum: https://developer.android.com/develop/background-work/background-tasks/persistent/getting-started/define-work#:~:text=The%20minimum%20repeat%20interval%20that%20can%20be%20defined%20is%2015%20minutes
                .setConstraints(getConstraints())
                .build()
        }

        private fun getConstraints() = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }

    override suspend fun doWork(): Result {
        if (shouldSetForeground()) {
            try {
                setForeground(createForegroundInfoToUpdateNotification())
                isFirstWorkExecution = false
            } catch (e: Exception) {
                Timber.e(e)
            }
        }

        if (applicationLifecycleObserver.isAppForegrounded()) {
            return Result.success()
        }

        val result = repository.fetchPmData()

        return if (result.isSuccess) {
            val pmResponse = result.getOrNull()!!
            airQualityStateManager.update(
                AirQualityState.AirQualityData(
                    pmResponse.pm25,
                    pmResponse.pm10
                )
            )

            if (pmResponse.pm25 > repository.thresholdPm25
                || pmResponse.pm10 > repository.thresholdPm10
            ) {
                val notifTitle = context.getString(R.string.warning)
                val notifContent = context.getString(
                    R.string.air_quality_threshold_exceeded,
                    CurrentTime.now(),
                    pmResponse.pm25,
                    pmResponse.pm10,
                )
                airQualityNotification.push(
                    notifTitle,
                    notifContent,
                    NotificationCompat.PRIORITY_MAX
                )
            } else {
                val notifTitle = context.getString(R.string.particle_monitoring)
                val notifContent = context.getString(
                    R.string.air_quality_is_monitored,
                    CurrentTime.now(),
                    pmResponse.pm25,
                    pmResponse.pm10,
                )
                airQualityNotification.push(notifTitle, notifContent)
            }
            Result.success()
        } else {
            airQualityNotification.push(
                context.getString(R.string.warning),
                context.getString(R.string.data_could_not_be_retrieved, CurrentTime.now())
            )
            Result.retry()
        }
    }

    private fun shouldSetForeground(): Boolean {
        return applicationLifecycleObserver.isAppForegrounded() && isFirstWorkExecution
    }

    private fun createForegroundInfoToUpdateNotification(): ForegroundInfo {
        val intent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)
        val title = context.getString(R.string.particle_monitoring)
        val notification = airQualityNotification.getNotificationBuilder(title, "")
            .addAction(
                android.R.drawable.ic_delete,
                context.getString(R.string.cancel_periodic_control),
                intent
            )
            .build()

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                AirQualityNotification.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(AirQualityNotification.NOTIFICATION_ID, notification)
        }
    }
}