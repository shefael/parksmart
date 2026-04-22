package com.example.parksmart

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.parksmart.navigation.ParkSmartNavGraph
import com.example.parksmart.ui.theme.ParkSmartTheme
import com.example.parksmart.worker.WorkManagerSetup
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Planification des alertes WorkManager
        WorkManagerSetup.scheduleParkingAlerts(this)

        setContent {
            ParkSmartTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    ParkSmartNavGraph(navController = navController)
                }
            }
        }
    }
}