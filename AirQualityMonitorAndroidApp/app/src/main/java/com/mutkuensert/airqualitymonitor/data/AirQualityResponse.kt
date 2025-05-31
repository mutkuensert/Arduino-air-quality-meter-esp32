package com.mutkuensert.airqualitymonitor.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AirQualityResponse(
    @SerialName("pm2.5")
    val pm25: Float,
    @SerialName("pm10")
    val pm10: Float
)
