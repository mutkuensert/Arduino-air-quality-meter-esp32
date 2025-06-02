package com.mutkuensert.airqualitymonitor.data

import android.content.Context
import androidx.core.content.edit
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
private const val AIR_QUALITY_MONITORING_INTERVAL_SECONDS_KEY =
    "air_quality_monitoring_interval_seconds_key"

class Repository(
    private val service: AirQualityService,
    private val json: Json,
    applicationContext: Context
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

    suspend fun fetchAirQualityData(): Result<AirQualityResponse> {
        val response = try {
            service.get()
        } catch (exception: Exception) {
            Timber.e(exception)
            return Result.failure(RuntimeException("An error occurred during request."))
        }

        Timber.d("Service request has been executed.")

        val pm =
            response.body() ?: return Result.failure(RuntimeException("Response body is null."))

        return if (response.isSuccessful) {
            saveAirQuality(pm)
            Result.success(pm)
        } else {
            Result.failure(Exception("Request is not successful."))
        }
    }

    suspend fun getAirQualityHistory(): List<AirQualityEntity> {
        return preferences.getString(AIR_QUALITY_HISTORY_KEY, "[]").run {
            json.decodeFromString(this!!)
        }
    }

    private suspend fun saveAirQuality(airQuality: AirQualityResponse) {
        var history: List<AirQualityEntity> = getAirQualityHistory().toMutableList()
        history =
            listOf(AirQualityEntity(CurrentTime.now(), airQuality.pm25, airQuality.pm10)) + history
        preferences.edit {
            putString(
                AIR_QUALITY_HISTORY_KEY,
                json.encodeToString(history)
            )
        }
    }

    suspend fun saveThresholdPm25(value: Int) {
        preferences.edit {
            putInt(THRESHOLD_PM_25_KEY, value)
        }
    }

    suspend fun saveThresholdPm10(value: Int) {
        preferences.edit {
            putInt(THRESHOLD_PM_10_KEY, value)
        }
    }

    suspend fun setAirQualityMonitoringIntervalSeconds(value: Int) {
        preferences.edit {
            putInt(AIR_QUALITY_MONITORING_INTERVAL_SECONDS_KEY, value)
        }
    }
}