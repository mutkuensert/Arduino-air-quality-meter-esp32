package com.mutkuensert.airqualitymonitor

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.mutkuensert.airqualitymonitor.application.ApplicationLifecycleObserver
import com.mutkuensert.airqualitymonitor.data.AirQualityService
import com.mutkuensert.airqualitymonitor.data.AirQualityStateManager
import com.mutkuensert.airqualitymonitor.data.AirQualityRepository
import com.mutkuensert.airqualitymonitor.presentation.MainViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val koinModule = module {
    single { ApplicationLifecycleObserver() }
    single { AirQualityStateManager() }
    single { AirQualityRepository(get(), get(), androidApplication()) }
    single { createRetrofit(androidApplication()) }
    single { createAirQualityService(androidApplication()) }
    single { createJson() }
    viewModel { MainViewModel(get(), get()) }
}

private fun createRetrofit(context: Context): Retrofit {
    return Retrofit.Builder()
        .client(createClient(context))
        .baseUrl("http://192.168.0.184")
        .addConverterFactory(createJson().asConverterFactory("application/json; charset=UTF8".toMediaType()))
        .build()
}

private fun createAirQualityService(context: Context): AirQualityService {
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