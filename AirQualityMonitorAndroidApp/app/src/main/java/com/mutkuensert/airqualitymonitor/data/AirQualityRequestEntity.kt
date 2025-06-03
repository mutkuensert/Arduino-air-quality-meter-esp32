package com.mutkuensert.airqualitymonitor.data

import kotlinx.serialization.Serializable

@Serializable
data class AirQualityRequestEntity(
    val airQuality: AirQualityEntity? = null,
    val failure: FailureEntity? = null
)

@Serializable
data class AirQualityEntity(
    val date: String,
    val pm25: Float,
    val pm10: Float
)

@Serializable
data class FailureEntity(val date: String, val errorMessage: String)