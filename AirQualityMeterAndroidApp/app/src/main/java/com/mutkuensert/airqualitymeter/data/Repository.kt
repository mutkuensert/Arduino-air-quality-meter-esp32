package com.mutkuensert.airqualitymeter.data

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.mutkuensert.airqualitymeter.RequestWorker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import timber.log.Timber
import java.util.concurrent.TimeUnit

class Repository(private val service: PmService) {
    private val _pmResponse = MutableStateFlow(PmResponse(0.0f, 0.0f))
    val pmResponse = _pmResponse.asStateFlow()

    var thresholdPm25: Float = 1.0f
    var thresholdPm10: Float = 20f

    suspend fun updatePmData(): Result<PmResponse> {
        val response = service.getPmData()
        Timber.d("Service request has been executed.")
        val pm = response.body()
        if (pm != null) {
            _pmResponse.update {
                it.copy(pm25 = pm.pm25, pm10 = pm.pm10)
            }
        }

        return if (response.isSuccessful && pm != null) {
            Result.success(pm)
        } else {
            Result.failure(Exception("Something is wrong."))
        }
    }
}