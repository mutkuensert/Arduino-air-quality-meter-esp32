package com.mutkuensert.airqualitymonitor.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutkuensert.airqualitymonitor.R
import com.mutkuensert.airqualitymonitor.application.AirQualityMonitorService
import com.mutkuensert.airqualitymonitor.application.setStatusBarAppearance
import com.mutkuensert.airqualitymonitor.ui.theme.AirQualityMeterTheme
import com.mutkuensert.airqualitymonitor.util.CurrentTime

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startAirQualityMonitoring()
        enableEdgeToEdge()
        setStatusBarAppearance(true)
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
        val intent = Intent(this, AirQualityMonitorService::class.java)
        ContextCompat.startForegroundService(this, intent)
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
            .windowInsetsPadding(WindowInsets.systemBars)
            .padding(20.dp)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(text = uiModel.dateTime)
            Text(text = stringResource(uiModel.stateInfoText))

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

            Spacer(Modifier.height(30.dp))

            Text(
                text = stringResource(R.string.previous_data),
                style = TextStyle(fontWeight = FontWeight.Bold)
            )

            Spacer(Modifier.height(4.dp))

            DataHistory(uiModel)
        }
    }
}

@Composable
private fun DataHistory(uiModel: MainActivityUiModel) {
    Box(Modifier.border(1.dp, Color.Black, MaterialTheme.shapes.medium)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(10.dp)
        ) {
            uiModel.airQualityHistory.forEach {
                Spacer(Modifier.height(10.dp))
                Text(text = it.date)
                Text(text = "Pm2.5: ${it.pm25} Pm10: ${it.pm10}")
            }
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
            stateInfoText = -1,
            emptyList(),
        )
        MainScreen(
            uiModel,
            {},
            {},
            {},
        )
    }
}