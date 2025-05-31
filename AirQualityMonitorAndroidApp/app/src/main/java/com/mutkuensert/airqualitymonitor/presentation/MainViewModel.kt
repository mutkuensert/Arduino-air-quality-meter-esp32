package com.mutkuensert.airqualitymonitor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutkuensert.airqualitymonitor.Module
import com.mutkuensert.airqualitymonitor.R
import com.mutkuensert.airqualitymonitor.data.AirQualityState
import com.mutkuensert.airqualitymonitor.data.AirQualityStateManager
import com.mutkuensert.airqualitymonitor.data.MONITORING_INTERVAL_SECONDS_DEFAULT
import com.mutkuensert.airqualitymonitor.data.Repository
import com.mutkuensert.airqualitymonitor.data.THRESHOLD_PM_10_DEFAULT
import com.mutkuensert.airqualitymonitor.data.THRESHOLD_PM_25_DEFAULT
import com.mutkuensert.airqualitymonitor.util.CurrentTime
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val repository: Repository = Module.Single.repository
    private val airQualityStateManager: AirQualityStateManager =
        Module.Single.airQualityStateManager

    private val _uiModel = MutableStateFlow(
        MainActivityUiModel.initial(
            repository.thresholdPm25.toString(),
            repository.thresholdPm10.toString(),
            repository.foregroundMonitoringIntervalSeconds.toString(),
        )
    )
    val uiModel: StateFlow<MainActivityUiModel> = _uiModel.asStateFlow()

    init {
        viewModelScope.launch {
            airQualityStateManager.state.collectLatest { state ->
                when (state) {
                    is AirQualityState.Empty -> {
                        _uiModel.update {
                            it.copy(
                                dateTime = CurrentTime.now(),
                                stateInfo = R.string.particle_monitoring_is_about_to_start
                            )
                        }
                    }

                    is AirQualityState.Failure -> {
                        _uiModel.update {
                            it.copy(
                                dateTime = CurrentTime.now(),
                                stateInfo = R.string.something_happened_during_control
                            )
                        }
                    }

                    is AirQualityState.AirQualityData -> {
                        _uiModel.update {
                            it.copy(
                                dateTime = CurrentTime.now(),
                                pm25 = state.pm25.toString(),
                                pm10 = state.pm10.toString(),
                                stateInfo = R.string.control_is_successful
                            )
                        }
                    }
                }
            }
        }
    }

    fun handlePm25ThresholdTextChange(text: String) {
        _uiModel.update {
            it.copy(pm25Threshold = text, pm25CurrentThreshold = text)
        }
        viewModelScope.launch {
            repository.setThresholdPm25(
                text.toIntOrNull() ?: THRESHOLD_PM_25_DEFAULT
            )
        }
    }

    fun handlePm10ThresholdTextChange(text: String) {
        _uiModel.update {
            it.copy(pm10Threshold = text, pm10CurrentThreshold = text)
        }
        viewModelScope.launch {
            repository.setThresholdPm10(
                text.toIntOrNull() ?: THRESHOLD_PM_10_DEFAULT
            )
        }
    }

    fun handleForegroundMonitoringIntervalSecondsTextChange(text: String) {
        _uiModel.update {
            it.copy(trackIntervalSeconds = text, monitoringIntervalSeconds = text)
        }
        viewModelScope.launch {
            repository.setAirQualityMonitoringIntervalSeconds(
                text.toIntOrNull() ?: MONITORING_INTERVAL_SECONDS_DEFAULT
            )
        }
    }
}