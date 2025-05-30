package com.mutkuensert.airqualitymeter

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.mutkuensert.airqualitymeter.data.PmService
import com.mutkuensert.airqualitymeter.data.Repository
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

object Module {
    lateinit var repository: Repository
}

fun getRetrofit(context: Context): Retrofit {
    return Retrofit.Builder()
        .client(getClient(context))
        .baseUrl("http://192.168.0.184")
        .addConverterFactory(getJson().asConverterFactory("application/json; charset=UTF8".toMediaType()))
        .build()
}

fun getPmService(context: Context): PmService {
    return getRetrofit(context).create(PmService::class.java)
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