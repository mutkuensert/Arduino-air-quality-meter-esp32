package com.mutkuensert.airqualitymeter.presentation

data class MainActivityUiModel(
    val dateTime: String,
    val pm25: String,
    val pm25Threshold: String,
    val pm10: String,
    val pm10Threshold: String
) {
    companion object {
        fun initial() = MainActivityUiModel(
            "",
            "",
            "",
            "",
            ""
        )
    }
}
