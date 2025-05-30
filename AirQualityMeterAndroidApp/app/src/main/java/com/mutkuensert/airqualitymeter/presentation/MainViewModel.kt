package com.mutkuensert.airqualitymeter.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mutkuensert.airqualitymeter.data.PmService
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class MainViewModel(private val service: PmService) : ViewModel() {
    private val _uiModel = MutableStateFlow(MainActivityUiModel.initial())
    val uiModel = _uiModel.asStateFlow()

    fun getPmData() {
        viewModelScope.launch {
            while (true) {
                val response = service.getPmData()
                if (response.isSuccessful) {
                    val pmResponse = response.body()
                    _uiModel.update {
                        it.copy(
                            dateTime = LocalDateTime.now().toString(),
                            pm25 = pmResponse?.pm25?.toString() ?: "",
                            pm10 = pmResponse?.pm10?.toString() ?: ""
                        )
                    }
                }

                delay(20000)
            }
        }
    }
}