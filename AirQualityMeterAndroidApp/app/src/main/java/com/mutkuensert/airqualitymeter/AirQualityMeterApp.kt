package com.mutkuensert.airqualitymeter

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.GlobalContext.startKoin
import timber.log.Timber

class AirQualityMeterApp : Application() {
    override fun onCreate() {
        super.onCreate()
        plantTimber()
        startKoin {
            androidLogger()
            androidContext(this@AirQualityMeterApp)
            modules(module)
        }
    }

    private fun plantTimber() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}