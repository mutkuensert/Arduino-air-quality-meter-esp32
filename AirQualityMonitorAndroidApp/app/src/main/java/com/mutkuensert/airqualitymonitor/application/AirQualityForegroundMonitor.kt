package com.mutkuensert.airqualitymonitor.application

import android.content.Context
import androidx.core.app.NotificationCompat
import com.mutkuensert.airqualitymonitor.R
import com.mutkuensert.airqualitymonitor.data.AirQualityState
import com.mutkuensert.airqualitymonitor.data.AirQualityStateManager
import com.mutkuensert.airqualitymonitor.data.Repository
import com.mutkuensert.airqualitymonitor.util.CurrentTime
import kotlinx.coroutines.delay

class AirQualityForegroundMonitor(
    private val applicationLifecycleObserver: ApplicationLifecycleObserver,
    private val airQualityStateManager: AirQualityStateManager,
    private val repository: Repository,
    private val airQualityNotification: AirQualityNotification,
    private val context: Context,
) {

    suspend fun start() {
        while (applicationLifecycleObserver.isAppForegrounded()) {
            val pmResult = repository.fetchPmData()
            pmResult.onSuccess {
                airQualityStateManager.update(
                    AirQualityState.AirQualityData(
                        pm25 = it.pm25,
                        pm10 = it.pm10
                    )
                )
                pushNotification(it.pm25, it.pm10)
            }.onFailure {
                airQualityStateManager.update(AirQualityState.Failure(it.message ?: ""))
                pushCouldNotRetrievedNotification()
            }
            delay(repository.foregroundMonitoringIntervalSeconds * 1000L)
        }
    }

    private fun pushCouldNotRetrievedNotification() {
        airQualityNotification.push(
            context.getString(R.string.warning),
            context.getString(R.string.data_could_not_be_retrieved, CurrentTime.now())
        )
    }

    private fun pushNotification(pm25: Float, pm10: Float) {
        if (pm25 > repository.thresholdPm25
            || pm10 > repository.thresholdPm10
        ) {
            val notifTitle = context.getString(R.string.warning)
            val notifContent = context.getString(
                R.string.air_quality_threshold_exceeded,
                CurrentTime.now(),
                pm25,
                pm10,
            )
            airQualityNotification.push(notifTitle, notifContent, NotificationCompat.PRIORITY_MAX)
        } else {
            val notifTitle = context.getString(R.string.particle_monitoring)
            val notifContent = context.getString(
                R.string.air_quality_is_monitored,
                CurrentTime.now(),
                pm25,
                pm10,
            )
            airQualityNotification.push(notifTitle, notifContent)
        }
    }
}