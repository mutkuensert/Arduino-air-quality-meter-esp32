package com.mutkuensert.airqualitymonitor.data

import retrofit2.Response
import retrofit2.http.GET

interface AirQualityService {
    @GET("/json")
    suspend fun get(): Response<AirQualityResponse>
}