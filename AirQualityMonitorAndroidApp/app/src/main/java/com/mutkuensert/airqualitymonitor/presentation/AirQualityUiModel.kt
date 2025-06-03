package com.mutkuensert.airqualitymonitor.presentation

import com.mutkuensert.airqualitymonitor.data.AirQualityRequestEntity

sealed interface AirQualityUiModel {
    val date: String

    data class Success(override val date: String, val pm25: String, val pm10: String) :
        AirQualityUiModel

    data class Failure(override val date: String, val errorMessage: String) : AirQualityUiModel
}

fun AirQualityRequestEntity.toUiModel(): AirQualityUiModel {
    return if (airQuality != null) {
        AirQualityUiModel.Success(
            airQuality.date,
            airQuality.pm25.toString(),
            airQuality.pm10.toString()
        )
    } else {
        AirQualityUiModel.Failure(failure!!.date, failure.errorMessage)
    }
}
