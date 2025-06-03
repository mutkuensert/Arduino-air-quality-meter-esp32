package com.mutkuensert.airqualitymonitor.data

import android.content.Context
import androidx.core.content.edit
import com.mutkuensert.airqualitymonitor.R
import com.mutkuensert.airqualitymonitor.util.CurrentTime
import kotlinx.serialization.json.Json
import timber.log.Timber

const val THRESHOLD_PM_25_DEFAULT = 10
const val THRESHOLD_PM_10_DEFAULT = 20
private const val PREFERENCES_NAME = "repository"
private const val MONITORING_INTERVAL_SECONDS_KEY = "monitoring_interval_seconds-key"
const val MONITORING_INTERVAL_SECONDS_DEFAULT = 30
private const val AIR_QUALITY_HISTORY_KEY = "air_quality_history_key"
private const val THRESHOLD_PM_25_KEY = "threshold_pm_25_key"
private const val THRESHOLD_PM_10_KEY = "threshold_pm_10_key"

class AirQualityRepository(
    private val service: AirQualityService,
    private val json: Json,
    private val applicationContext: Context
) {
    private val preferences = applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )

    val thresholdPm25: Int get() = preferences.getInt(THRESHOLD_PM_25_KEY, THRESHOLD_PM_25_DEFAULT)
    val thresholdPm10: Int get() = preferences.getInt(THRESHOLD_PM_10_KEY, THRESHOLD_PM_10_DEFAULT)

    val monitoringIntervalSeconds: Int
        get() = preferences.getInt(
            MONITORING_INTERVAL_SECONDS_KEY,
            MONITORING_INTERVAL_SECONDS_DEFAULT
        )

    suspend fun fetch(): Result<AirQualityResponse> {
        val response = try {
            service.get()
        } catch (exception: Exception) {
            Timber.e(exception)
            val errorMessage = applicationContext.getString(R.string.error_occurred_during_request)
            saveFailure(errorMessage)
            return Result.failure(RuntimeException(errorMessage))
        }

        val airQuality = response.body()
        if (airQuality == null) {
            val errorMessage = applicationContext.getString(R.string.response_body_is_null)
            saveFailure(errorMessage)
            return Result.failure(RuntimeException(errorMessage))
        }

        return if (response.isSuccessful) {
            saveSuccess(airQuality)
            Result.success(airQuality)
        } else {
            val errorMessage = applicationContext.getString(R.string.request_is_not_successful)
            saveFailure(errorMessage)
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun getHistory(): List<AirQualityRequestEntity> {
        return preferences.getString(AIR_QUALITY_HISTORY_KEY, "[]").run {
            json.decodeFromString(this!!)
        }
    }

    private suspend fun saveSuccess(airQuality: AirQualityResponse) {
        var history: List<AirQualityRequestEntity> = getHistory()
        history = listOf(
            AirQualityRequestEntity(
                airQuality = AirQualityEntity(
                    CurrentTime.now(),
                    airQuality.pm25,
                    airQuality.pm10
                )
            )
        ) + history
        preferences.edit(commit = true) {
            putString(
                AIR_QUALITY_HISTORY_KEY,
                json.encodeToString(history)
            )
        }
    }

    private suspend fun saveFailure(errorMessage: String) {
        var history: List<AirQualityRequestEntity> = getHistory()
        history = listOf(
            AirQualityRequestEntity(
                failure = FailureEntity(
                    CurrentTime.now(),
                    errorMessage
                )
            )
        ) + history

        preferences.edit(commit = true) {
            putString(
                AIR_QUALITY_HISTORY_KEY,
                json.encodeToString(history)
            )
        }
    }

    suspend fun saveThresholdPm25(value: Int) {
        preferences.edit(commit = true) {
            putInt(THRESHOLD_PM_25_KEY, value)
        }
    }

    suspend fun saveThresholdPm10(value: Int) {
        preferences.edit(commit = true) {
            putInt(THRESHOLD_PM_10_KEY, value)
        }
    }

    suspend fun setMonitoringIntervalSeconds(value: Int) {
        preferences.edit(commit = true) {
            putInt(MONITORING_INTERVAL_SECONDS_KEY, value)
        }
    }
}