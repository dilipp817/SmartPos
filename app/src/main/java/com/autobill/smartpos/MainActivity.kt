package com.autobill.smartpos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.autobill.smartpos.presentation.food.FoodRoute
import com.autobill.smartpos.presentation.food.FoodViewModel
import com.autobill.smartpos.ui.theme.SmartPosTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as SmartPosApp).appContainer
        val viewModelFactory = FoodViewModel.provideFactory(appContainer.getFoodsUseCase)

        setContent {
            SmartPosTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    FoodRoute(
                        viewModelFactory = viewModelFactory,
                        modifier = Modifier.padding(innerPadding),
                    )
                }
            }
        }
    }
}
