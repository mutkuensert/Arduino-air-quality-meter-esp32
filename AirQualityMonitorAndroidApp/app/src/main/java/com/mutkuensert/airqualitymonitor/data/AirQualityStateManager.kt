package com.mutkuensert.airqualitymonitor.data

import com.mutkuensert.airqualitymonitor.data.AirQualityState.Empty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AirQualityStateManager() {
    private val _state = MutableStateFlow<AirQualityState>(Empty)
    val state = _state.asStateFlow()

    fun update(state: AirQualityState) {
        _state.update { state }
    }
}


sealed interface AirQualityState {
    data object Empty : AirQualityState
    data class AirQualityData(val pm25: Float, val pm10: Float) : AirQualityState
    data class Failure(val message: String) : AirQualityState
}
