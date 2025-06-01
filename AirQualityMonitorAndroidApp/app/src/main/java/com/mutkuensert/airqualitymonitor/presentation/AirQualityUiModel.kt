package com.mutkuensert.airqualitymonitor.presentation

import com.mutkuensert.airqualitymonitor.data.AirQualityEntity

data class AirQualityUiModel(val date: String, val pm25: String, val pm10: String)

fun AirQualityEntity.toUiModel(): AirQualityUiModel {
    return AirQualityUiModel(date, pm25.toString(), pm10.toString())
}
