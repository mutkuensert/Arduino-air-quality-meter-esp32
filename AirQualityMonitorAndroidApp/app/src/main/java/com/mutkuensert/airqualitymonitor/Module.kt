package com.mutkuensert.airqualitymonitor

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.mutkuensert.airqualitymonitor.data.AirQualityService
import com.mutkuensert.airqualitymonitor.data.AirQualityStateManager
import com.mutkuensert.airqualitymonitor.data.Repository
import com.mutkuensert.airqualitymonitor.application.ApplicationLifecycleObserver
import com.mutkuensert.airqualitymonitor.application.AirQualityForegroundMonitor
import com.mutkuensert.airqualitymonitor.application.AirQualityNotification
import com.mutkuensert.airqualitymonitor.application.MonitorWorkerFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object Module {
    object Single {
        lateinit var applicationContext: Context
        lateinit var repository: Repository
        lateinit var applicationLifecycleObserver: ApplicationLifecycleObserver
        lateinit var airQualityStateManager: AirQualityStateManager
        lateinit var airQualityForegroundMonitor: AirQualityForegroundMonitor
    }

    fun createRequestWorkerFactory(): MonitorWorkerFactory {
        return MonitorWorkerFactory(
            Single.repository,
            Single.applicationLifecycleObserver,
            createParticleNotification(),
            Single.airQualityStateManager
        )
    }

    private fun createParticleNotification(): AirQualityNotification {
        return AirQualityNotification(Single.applicationContext)
    }

    private fun createRetrofit(context: Context): Retrofit {
        return Retrofit.Builder()
            .client(createClient(context))
            .baseUrl("http://192.168.0.184")
            .addConverterFactory(createJson().asConverterFactory("application/json; charset=UTF8".toMediaType()))
            .build()
    }

    fun createPmService(context: Context): AirQualityService {
        return createRetrofit(context).create(AirQualityService::class.java)
    }

    private fun createJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            explicitNulls = false
            prettyPrint = true
            isLenient = true
            encodeDefaults = true
        }
    }

    private fun createClient(context: Context): OkHttpClient {
        return OkHttpClient()
            .newBuilder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .addInterceptor(ChuckerInterceptor(context))
            .build()
    }
}
