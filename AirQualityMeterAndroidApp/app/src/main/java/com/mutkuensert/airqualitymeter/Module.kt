package com.mutkuensert.airqualitymeter

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.mutkuensert.airqualitymeter.data.PmService
import com.mutkuensert.airqualitymeter.presentation.MainViewModel
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

val module = module {
    single { getJson() }
    single {
        Retrofit.Builder()
            .client(getClient(get()))
            .baseUrl("http://192.168.0.184")
            .addConverterFactory(get<Json>().asConverterFactory("application/json; charset=UTF8".toMediaType()))
            .build()
    }
    single { get<Retrofit>().create(PmService::class.java) }
    viewModelOf(::MainViewModel)
}

private fun getJson(): Json {
    return Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        prettyPrint = true
        isLenient = true
        encodeDefaults = true
    }
}

private fun getClient(context: Context): OkHttpClient {
    return OkHttpClient()
        .newBuilder()
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        })
        .addInterceptor(ChuckerInterceptor(context))
        .build()
}