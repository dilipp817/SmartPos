package com.autobill.smartpos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.autobill.smartpos.feature.food.FoodRoute
import com.autobill.smartpos.ui.theme.SmartPosTheme
import dagger.hilt.android.AndroidEntryPoint

// Main Activity - Entry point for UI
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SmartPosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Display the Food feature screen
                    FoodRoute(
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
