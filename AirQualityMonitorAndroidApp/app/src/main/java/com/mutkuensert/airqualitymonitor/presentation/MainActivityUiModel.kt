package com.mutkuensert.airqualitymonitor.presentation

import androidx.annotation.StringRes

data class MainActivityUiModel(
    val dateTime: String,
    val pm25: String,
    val pm25Threshold: String,
    val pm25CurrentThreshold: String,
    val pm10: String,
    val pm10Threshold: String,
    val pm10CurrentThreshold: String,
    val trackIntervalSeconds: String,
    val monitoringIntervalSeconds: String,
    @StringRes val stateInfo: Int,
) {
    companion object {
        fun initial(
            pm25CurrentThreshold: String,
            pm10CurrentThreshold: String,
            monitoringIntervalSeconds: String,
        ) = MainActivityUiModel(
            "",
            "",
            "",
            pm25CurrentThreshold,
            "",
            "",
            pm10CurrentThreshold,
            "",
            monitoringIntervalSeconds,
            -1
        )
    }
}
