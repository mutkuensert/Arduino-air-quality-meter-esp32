package com.mutkuensert.airqualitymonitor

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.mutkuensert.airqualitymonitor.data.AirQualityStateManager
import com.mutkuensert.airqualitymonitor.data.Repository
import com.mutkuensert.airqualitymonitor.application.ApplicationLifecycleObserver
import com.mutkuensert.airqualitymonitor.application.AirQualityForegroundMonitor
import com.mutkuensert.airqualitymonitor.application.AirQualityNotification
import com.mutkuensert.airqualitymonitor.application.MonitorWorker
import timber.log.Timber

class AirQualityMeterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        plantTimber()
        injectDependencies()
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(Module.createRequestWorkerFactory())
                .build()
        )
        MonitorWorker.enqueue(this)
    }

    private fun injectDependencies() {
        Module.Single.applicationContext = this
        Module.Single.applicationLifecycleObserver = ApplicationLifecycleObserver()
        Module.Single.airQualityStateManager = AirQualityStateManager()
        Module.Single.repository = Repository(Module.createPmService(this), this)
        Module.Single.airQualityForegroundMonitor = AirQualityForegroundMonitor(
            Module.Single.applicationLifecycleObserver,
            Module.Single.airQualityStateManager,
            Module.Single.repository,
            AirQualityNotification(this),
            Module.Single.applicationContext,
        )
    }

    private fun plantTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}