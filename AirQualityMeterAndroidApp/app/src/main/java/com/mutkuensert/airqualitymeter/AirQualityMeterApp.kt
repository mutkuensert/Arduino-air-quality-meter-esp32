package com.mutkuensert.airqualitymeter

import android.app.Application
import androidx.work.Configuration
import androidx.work.WorkManager
import com.mutkuensert.airqualitymeter.data.Repository
import timber.log.Timber

class AirQualityMeterApp : Application() {

    override fun onCreate() {
        super.onCreate()
        plantTimber()
        Module.repository = Repository(getPmService(this))
        WorkManager.initialize(
            this,
            Configuration.Builder()
                .setWorkerFactory(RequestWorkerFactory(Module.repository))
                .build()
        )
        RequestWorker.enqueue(this)
    }

    private fun plantTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}