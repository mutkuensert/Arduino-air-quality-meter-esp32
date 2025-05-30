package com.mutkuensert.airqualitymeter.data

import retrofit2.Response
import retrofit2.http.GET

interface PmService {
    @GET("/json")
    suspend fun getPmData(): Response<PmResponse>
}