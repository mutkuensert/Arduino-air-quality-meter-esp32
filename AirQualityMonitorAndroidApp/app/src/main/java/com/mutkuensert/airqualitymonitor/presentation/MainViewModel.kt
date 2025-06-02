package com.mutkuensert.airqualitymonitor.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

class MainViewModel(
    private val repository: Repository,
    private val airQualityStateManager: AirQualityStateManager
) : ViewModel() {

    private val _uiModel = MutableStateFlow(
        MainActivityUiModel.initial(
            repository.thresholdPm25.toString(),
            repository.thresholdPm10.toString(),
            repository.monitoringIntervalSeconds.toString(),
        )
    )
    val uiModel: StateFlow<MainActivityUiModel> = _uiModel.asStateFlow()

    init {
        viewModelScope.launch {
            updateHistory()
            collectAirQualityData()
        }
    }

    private suspend fun updateHistory() {
        _uiModel.update { uiModel ->
            uiModel.copy(
                airQualityHistory = repository.getAirQualityHistory()
                    .map { it.toUiModel() })
        }
    }

    private suspend fun collectAirQualityData() {
        airQualityStateManager.state.collectLatest { state ->
            when (state) {
                is AirQualityState.Empty -> {
                    _uiModel.update {
                        it.copy(
                            dateTime = CurrentTime.now(),
                            stateInfoText = R.string.air_quality_monitoring_is_started
                        )
                    }
                }

                is AirQualityState.Failure -> {
                    _uiModel.update {
                        it.copy(
                            dateTime = CurrentTime.now(),
                            stateInfoText = R.string.something_happened_during_control
                        )
                    }
                }

                is AirQualityState.AirQualityData -> {
                    _uiModel.update { uiModel ->
                        uiModel.copy(
                            dateTime = CurrentTime.now(),
                            pm25 = state.pm25.toString(),
                            pm10 = state.pm10.toString(),
                            stateInfoText = R.string.monitoring_is_successful,
                            airQualityHistory = repository.getAirQualityHistory()
                                .map { it.toUiModel() }
                        )
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
            repository.saveThresholdPm25(
                text.toIntOrNull() ?: THRESHOLD_PM_25_DEFAULT
            )
        }
    }

    fun handlePm10ThresholdTextChange(text: String) {
        _uiModel.update {
            it.copy(pm10Threshold = text, pm10CurrentThreshold = text)
        }
        viewModelScope.launch {
            repository.saveThresholdPm10(
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