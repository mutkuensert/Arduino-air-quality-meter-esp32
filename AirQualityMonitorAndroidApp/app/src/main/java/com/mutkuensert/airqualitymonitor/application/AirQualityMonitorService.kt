package com.mutkuensert.airqualitymonitor.application

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.mutkuensert.airqualitymonitor.R
import com.mutkuensert.airqualitymonitor.data.AirQualityRepository
import com.mutkuensert.airqualitymonitor.data.AirQualityState
import com.mutkuensert.airqualitymonitor.data.AirQualityStateManager
import com.mutkuensert.airqualitymonitor.util.CurrentTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.android.ext.android.getKoin

private const val ACTION_STOP = "action_stop"

class AirQualityMonitorService : Service() {
    private val repository by lazy { getKoin().get<AirQualityRepository>() }
    private lateinit var airQualityNotification: AirQualityNotification
    private val airQualityStateManager by lazy { getKoin().get<AirQualityStateManager>() }
    private lateinit var scope: CoroutineScope
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        //dispatcher = newSingleThreadContext("BlockingThread")
        scope = CoroutineScope(Dispatchers.IO + Job())
        airQualityNotification = AirQualityNotification(this)

        val stopIntent = Intent(this, AirQualityMonitorService::class.java)
        stopIntent.action = ACTION_STOP
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = airQualityNotification.getNotificationBuilder(
            getString(R.string.air_quality_monitoring),
            getString(R.string.air_quality_monitoring_is_started)
        ).setPriority(NotificationCompat.PRIORITY_MAX)
            .addAction(
                android.R.drawable.ic_delete,
                getString(R.string.cancel_periodic_monitoring),
                stopPendingIntent
            )
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                AirQualityNotification.NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(
                AirQualityNotification.NOTIFICATION_ID,
                notification
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            scope.cancel()
            stopSelf()
        } else {
            if (job?.isActive != true) {
                job = startMonitoring()
            }
        }
        return START_STICKY
    }

    private fun startMonitoring(): Job {
        return scope.launch {
            while (true) {
                getAirQualityData()
                delay(1000L * repository.monitoringIntervalSeconds)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private suspend fun getAirQualityData() {
        val result = repository.fetch()

        if (result.isSuccess) {
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
                val notifTitle = getString(R.string.warning)
                val notifContent = getString(
                    R.string.air_quality_threshold_exceeded,
                    CurrentTime.now(),
                    pmResponse.pm25,
                    pmResponse.pm10,
                )
                airQualityNotification.push(notifTitle, notifContent)
            }
        }
    }
}