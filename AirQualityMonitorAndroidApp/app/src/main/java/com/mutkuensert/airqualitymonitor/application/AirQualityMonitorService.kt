package com.mutkuensert.airqualitymonitor.application

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import com.mutkuensert.airqualitymonitor.Module
import com.mutkuensert.airqualitymonitor.R
import com.mutkuensert.airqualitymonitor.data.AirQualityState
import com.mutkuensert.airqualitymonitor.data.AirQualityStateManager
import com.mutkuensert.airqualitymonitor.data.Repository
import com.mutkuensert.airqualitymonitor.util.CurrentTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AirQualityMonitorService : Service() {
    private lateinit var repository: Repository
    private lateinit var airQualityNotification: AirQualityNotification
    private lateinit var airQualityStateManager: AirQualityStateManager
    private lateinit var context: Context
    private lateinit var scope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        inject()

        val notification = airQualityNotification.getNotificationBuilder(
            context.getString(R.string.air_quality_monitoring),
            context.getString(R.string.air_quality_monitoring_is_about_to_start)
        ).build()

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

    private fun inject() {
        repository = Module.Single.repository
        context = Module.Single.applicationContext
        airQualityNotification = AirQualityNotification(context)
        airQualityStateManager = Module.Single.airQualityStateManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        scope.launch {
            while (true) {
                startMonitoring()
                delay(repository.monitoringIntervalSeconds * 1000L)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private suspend fun startMonitoring() {
        val result = repository.fetchAirQualityData()

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
                val notifTitle = context.getString(R.string.warning)
                val notifContent = context.getString(
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