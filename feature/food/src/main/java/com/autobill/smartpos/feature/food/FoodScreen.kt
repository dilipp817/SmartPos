package com.autobill.smartpos.feature.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.common.UiState
import java.util.Locale

/**
 * Composable: FoodRoute
 * Container composable for the Food feature.
 * Creates ViewModel and observes state changes.
 */
@Composable
fun FoodRoute(
    modifier: Modifier = Modifier,
) {
    // Create ViewModel instance using Hilt
    val viewModel: FoodViewModel = viewModel()

    // Observe UI state and recompose on changes
    val state by viewModel.foodsState.collectAsStateWithLifecycle()

    // Render the actual screen with current state
    FoodScreen(
        state = state,
        onRetry = { viewModel.retryLoadFoods() },
        modifier = modifier,
    )
}

/**
 * Composable: FoodScreen
 * Main UI for displaying list of foods.
 * Handles Loading, Success, Error, and Idle states.
 */
@Composable
fun FoodScreen(
    state: UiState<List<Food>>,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Header text
        Text(
            text = "Foods",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Handle different UI states
        when (state) {
            UiState.Idle -> {
                Text("Ready to load foods")
            }

            UiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading foods...",
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }

            is UiState.Success -> {
                if (state.data.isEmpty()) {
                    Text(
                        text = "No foods available",
                        modifier = Modifier.padding(top = 16.dp),
                    )
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 8.dp),
                    ) {
                        items(items = state.data, key = { food -> food.id }) { food ->
                            FoodCard(food = food)
                        }
                    }
                }
            }

            is UiState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp),
                    )
                    Button(onClick = onRetry) {
                        Text(text = "Retry")
                    }
                }
            }
        }
    }
}

// Composable: FoodCard
// Individual food item card component
@Composable
private fun FoodCard(food: Food) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = food.name, style = MaterialTheme.typography.titleMedium)
            Text(text = "ID: ${food.id}")
            Text(text = "Restaurant: ${food.restaurantId}")
            Text(text = "Price: Rs ${String.format(Locale.US, "%.2f", food.price)}")
        }
    }
}

