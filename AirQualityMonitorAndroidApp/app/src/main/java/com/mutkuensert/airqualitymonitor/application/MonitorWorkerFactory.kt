package com.mutkuensert.airqualitymonitor.application

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.mutkuensert.airqualitymonitor.data.AirQualityStateManager
import com.mutkuensert.airqualitymonitor.data.Repository

class MonitorWorkerFactory(
    private val repository: Repository,
    private val applicationLifecycleObserver: ApplicationLifecycleObserver,
    private val airQualityNotification: AirQualityNotification,
    private val airQualityStateManager: AirQualityStateManager,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker {
        return MonitorWorker(
            appContext,
            workerParameters,
            repository,
            applicationLifecycleObserver,
            airQualityNotification,
            airQualityStateManager
        )
    }
}