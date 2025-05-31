package com.mutkuensert.airqualitymonitor.data

import android.content.Context
import androidx.core.content.edit
import timber.log.Timber

const val THRESHOLD_PM_25_DEFAULT = 10
const val THRESHOLD_PM_10_DEFAULT = 20
private const val PREFERENCES_NAME = "repository"
private const val MONITORING_INTERVAL_SECONDS_KEY = "monitoring_interval_seconds-key"
const val MONITORING_INTERVAL_SECONDS_DEFAULT = 30

class Repository(
    private val service: AirQualityService,
    private val applicationContext: Context
) {
    private val preferences = applicationContext.getSharedPreferences(
        PREFERENCES_NAME,
        Context.MODE_PRIVATE
    )
    private val THRESHOLD_PM_25_KEY = "threshold_pm_25_key"
    private val THRESHOLD_PM_10_KEY = "threshold_pm_10_key"
    private val AIR_QUALITY_MONITORING_INTERVAL_SECONDS_KEY =
        "air_quality_monitoring_seconds_interval_key"

    val thresholdPm25: Int get() = preferences.getInt(THRESHOLD_PM_25_KEY, THRESHOLD_PM_25_DEFAULT)
    val thresholdPm10: Int get() = preferences.getInt(THRESHOLD_PM_10_KEY, THRESHOLD_PM_10_DEFAULT)
    val foregroundMonitoringIntervalSeconds: Int
        get() = preferences.getInt(
            MONITORING_INTERVAL_SECONDS_KEY,
            MONITORING_INTERVAL_SECONDS_DEFAULT
        )

    suspend fun fetchPmData(): Result<AirQualityResponse> {
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
            Result.success(pm)
        } else {
            Result.failure(Exception("Request is not successful."))
        }
    }

    suspend fun setThresholdPm25(value: Int) {
        applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit {
            putInt(THRESHOLD_PM_25_KEY, value)
        }
    }

    suspend fun setThresholdPm10(value: Int) {
        applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit {
            putInt(THRESHOLD_PM_10_KEY, value)
        }
    }

    suspend fun setAirQualityMonitoringIntervalSeconds(value: Int) {
        applicationContext.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit {
            putInt(AIR_QUALITY_MONITORING_INTERVAL_SECONDS_KEY, value)
        }
    }
}