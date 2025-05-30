package com.mutkuensert.airqualitymeter.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PmResponse(
    @SerialName("pm2.5")
    val pm25: Float,
    @SerialName("pm10")
    val pm10: Float
)
