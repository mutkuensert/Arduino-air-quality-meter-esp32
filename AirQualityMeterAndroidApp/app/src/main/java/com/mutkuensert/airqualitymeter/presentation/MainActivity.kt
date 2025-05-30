package com.mutkuensert.airqualitymeter.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mutkuensert.airqualitymeter.ui.theme.AirQualityMeterTheme
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDateTime

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.getPmData()
        setContent {
            val uiModel by viewModel.uiModel.collectAsStateWithLifecycle()
            AirQualityMeterTheme {
                MainScreen(uiModel)
            }
        }
    }
}

@Composable
private fun MainScreen(uiModel: MainActivityUiModel) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Text(text = uiModel.dateTime)

            Spacer(Modifier.height(20.dp))

            Text(text = "Pm 2.5: " + uiModel.pm25)

            Spacer(Modifier.height(20.dp))

            Text(text = "Pm 10: " + uiModel.pm10)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AirQualityMeterTheme {
        MainScreen(
            MainActivityUiModel(
                dateTime = LocalDateTime.now().toString(),
                pm25 = "10.1",
                pm10 = "19.2"
            )
        )
    }
}