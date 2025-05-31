package com.mutkuensert.airqualitymonitor.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.mutkuensert.airqualitymonitor.Module
import com.mutkuensert.airqualitymonitor.R
import com.mutkuensert.airqualitymonitor.application.AirQualityForegroundMonitor
import com.mutkuensert.airqualitymonitor.application.AirQualityNotification
import com.mutkuensert.airqualitymonitor.ui.theme.AirQualityMeterTheme
import com.mutkuensert.airqualitymonitor.util.CurrentTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var airQualityNotification: AirQualityNotification
    private lateinit var airQualityForegroundMonitor: AirQualityForegroundMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        airQualityForegroundMonitor = Module.Single.airQualityForegroundMonitor
        airQualityNotification = AirQualityNotification(this)
        startAirQualityMonitoring()
        enableEdgeToEdge()
        PermissionHandler.requestNotificationPermission(this)
        setContent {
            val uiModel by viewModel.uiModel.collectAsStateWithLifecycle()
            AirQualityMeterTheme {
                MainScreen(
                    uiModel = uiModel,
                    onPm25ThresholdTextChange = viewModel::handlePm25ThresholdTextChange,
                    onPm10ThresholdTextChange = viewModel::handlePm10ThresholdTextChange,
                    onForegroundMonitoringIntervalSecondsTextChange = viewModel::handleForegroundMonitoringIntervalSecondsTextChange
                )
            }
        }
    }

    private fun startAirQualityMonitoring() {
        lifecycleScope.launch(Dispatchers.IO) {
            airQualityForegroundMonitor.start()
        }
    }

    override fun onStart() {
        super.onStart()
        airQualityNotification.push(
            getString(R.string.particle_monitoring),
            getString(R.string.particle_monitoring_is_about_to_start)
        )
    }
}

@Composable
private fun MainScreen(
    uiModel: MainActivityUiModel,
    onPm25ThresholdTextChange: (String) -> Unit,
    onPm10ThresholdTextChange: (String) -> Unit,
    onForegroundMonitoringIntervalSecondsTextChange: (String) -> Unit
) {
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(text = uiModel.dateTime)
            Text(text = stringResource(uiModel.stateInfo))

            Spacer(Modifier.height(20.dp))

            Text(text = stringResource(R.string.pm_2_5_value, uiModel.pm25))

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = uiModel.pm25Threshold,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        onPm25ThresholdTextChange.invoke(input)
                    }
                },
                label = {
                    Text(
                        text = stringResource(
                            R.string.warning_threshold_value,
                            uiModel.pm25CurrentThreshold
                        )
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                )
            )

            Spacer(Modifier.height(20.dp))

            Text(text = stringResource(R.string.pm_10_value, uiModel.pm10))

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = uiModel.pm10Threshold,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        onPm10ThresholdTextChange.invoke(input)
                    }
                },
                label = {
                    Text(
                        text = stringResource(
                            R.string.warning_threshold_value,
                            uiModel.pm10CurrentThreshold
                        )
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                )
            )

            Spacer(Modifier.height(20.dp))

            OutlinedTextField(
                value = uiModel.trackIntervalSeconds,
                onValueChange = { input ->
                    if (input.all { it.isDigit() }) {
                        onForegroundMonitoringIntervalSecondsTextChange.invoke(input)
                    }
                },
                label = {
                    Text(
                        text = stringResource(
                            R.string.monitoring_interval_seconds,
                            uiModel.monitoringIntervalSeconds
                        )
                    )
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AirQualityMeterTheme {
        val uiModel = MainActivityUiModel(
            dateTime = CurrentTime.now(),
            pm25 = "10.1",
            pm25Threshold = "1",
            pm25CurrentThreshold = "2",
            pm10 = "19.2",
            pm10Threshold = "2",
            pm10CurrentThreshold = "10",
            trackIntervalSeconds = "",
            monitoringIntervalSeconds = "",
            stateInfo = -1
        )
        MainScreen(
            uiModel,
            {},
            {},
            {},
        )
    }
}