package com.mutkuensert.airqualitymeter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutkuensert.airqualitymeter.Module
import com.mutkuensert.airqualitymeter.data.Repository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainViewModel(private val repository: Repository = Module.repository) : ViewModel() {
    private val _uiModel = MutableStateFlow(MainActivityUiModel.initial())
    val uiModel: StateFlow<MainActivityUiModel> = _uiModel.asStateFlow()

    init {
        viewModelScope.launch {
            repository.pmResponse.collectLatest { response ->
                _uiModel.update {
                    it.copy(
                        dateTime = LocalDateTime.now().toString(),
                        pm25 = response.pm25.toString(),
                        pm10 = response.pm10.toString()
                    )
                }
            }
        }
    }

    fun handlePm25ThresholdTextChange(text: String) {
        _uiModel.update {
            it.copy(pm25Threshold = text)
        }
        repository.thresholdPm25 = text.toFloatOrNull() ?: 10.0f
    }

    fun handlePm10ThresholdTextChange(text: String) {
        _uiModel.update {
            it.copy(pm10Threshold = text)
        }
        repository.thresholdPm10 = text.toFloatOrNull() ?: 20.0f
    }
}