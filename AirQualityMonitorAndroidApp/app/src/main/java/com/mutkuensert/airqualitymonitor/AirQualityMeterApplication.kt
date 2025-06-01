package com.mutkuensert.airqualitymonitor

import android.app.Application
import com.mutkuensert.airqualitymonitor.application.ApplicationLifecycleObserver
import com.mutkuensert.airqualitymonitor.data.AirQualityStateManager
import com.mutkuensert.airqualitymonitor.data.Repository
import timber.log.Timber

class AirQualityMeterApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        plantTimber()
        injectDependencies()
    }

    private fun injectDependencies() {
        Module.Single.applicationContext = this
        Module.Single.applicationLifecycleObserver = ApplicationLifecycleObserver()
        Module.Single.airQualityStateManager = AirQualityStateManager()
        Module.Single.repository = Repository(Module.createPmService(this), this)
    }

    private fun plantTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}