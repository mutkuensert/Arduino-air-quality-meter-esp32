package com.mutkuensert.airqualitymeter.presentation

import android.Manifest
import android.os.Build
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
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutkuensert.airqualitymeter.R
import com.mutkuensert.airqualitymeter.ui.theme.AirQualityMeterTheme
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestPermissions()
        setContent {
            val uiModel by viewModel.uiModel.collectAsStateWithLifecycle()
            AirQualityMeterTheme {
                MainScreen(
                    uiModel = uiModel,
                    onPm25ThresholdTextChange = viewModel::handlePm25ThresholdTextChange,
                    onPm10ThresholdTextChange = viewModel::handlePm10ThresholdTextChange
                )
            }
        }
    }

    private fun requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val permission = Manifest.permission.POST_NOTIFICATIONS
            if (shouldShowRequestPermissionRationale(permission)) {
                ActivityCompat.requestPermissions(this, arrayOf(permission), 0)
            }
        }
    }
}

@Composable
private fun MainScreen(
    uiModel: MainActivityUiModel,
    onPm25ThresholdTextChange: (String) -> Unit,
    onPm10ThresholdTextChange: (String) -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(text = uiModel.dateTime)

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
                    Text(text = stringResource(R.string.warning_threshold_value))
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
                    Text(text = stringResource(R.string.warning_threshold_value))
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
            dateTime = LocalDateTime.now().toString(),
            pm25 = "10.1",
            pm25Threshold = "",
            pm10 = "19.2",
            pm10Threshold = ""
        )
        MainScreen(
            uiModel,
            {},
            {}
        )
    }
}