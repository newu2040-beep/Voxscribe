package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.TranscriptionDashboard
import com.example.ui.TranscriptionViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Compose-safe ViewModel retrieval
                val viewModel: TranscriptionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    TranscriptionDashboard(viewModel = viewModel)
                }
            }
        }
    }
}
