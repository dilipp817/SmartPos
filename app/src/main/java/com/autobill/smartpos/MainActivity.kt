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
import com.autobill.smartpos.feature.food.FoodViewModel
import com.autobill.smartpos.ui.theme.SmartPosTheme

// Main Activity - Entry point for UI
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get DI container from Application instance
        val appContainer = (application as SmartPosApp).appContainer

        // Create ViewModel factory using the injected use case
        val viewModelFactory = FoodViewModel.provideFactory(appContainer.getFoodsUseCase)

        // Set the Compose content
        setContent {
            SmartPosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Display the Food feature screen
                    FoodRoute(
                        viewModelFactory = viewModelFactory,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
