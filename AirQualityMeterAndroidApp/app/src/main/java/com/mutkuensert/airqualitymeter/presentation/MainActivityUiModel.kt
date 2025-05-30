package com.mutkuensert.airqualitymeter.presentation

data class MainActivityUiModel(
    val dateTime: String,
    val pm25: String,
    val pm10: String,
) {
    companion object {
        fun initial() = MainActivityUiModel("", "", "")
    }
}
