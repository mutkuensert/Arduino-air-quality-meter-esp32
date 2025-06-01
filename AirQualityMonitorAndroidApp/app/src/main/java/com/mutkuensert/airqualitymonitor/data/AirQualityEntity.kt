package com.mutkuensert.airqualitymonitor.data

import kotlinx.serialization.Serializable

@Serializable
data class AirQualityEntity(val date: String, val pm25: Float, val pm10: Float)
