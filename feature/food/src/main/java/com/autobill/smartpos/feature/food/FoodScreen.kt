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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autobill.smartpos.domain.model.Food
import java.util.Locale

// Composable: FoodRoute
// Container composable for the Food feature
// Creates ViewModel and observes state changes
@Composable
fun FoodRoute(
    viewModelFactory: ViewModelProvider.Factory,
    modifier: Modifier = Modifier,
) {
    // Create ViewModel instance using the provided factory
    val viewModel: FoodViewModel = viewModel(factory = viewModelFactory)

    // Observe UI state and recompose on changes
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Render the actual screen with current state
    FoodScreen(
        state = state,
        onRetry = { viewModel.loadFoods(restaurantId = 1) },
        modifier = modifier,
    )
}

// Composable: FoodScreen
// Main UI for displaying list of foods
@Composable
fun FoodScreen(
    state: FoodUiState,
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

        // Show loading indicator only when loading and no items exist
        if (state.isLoading && state.foods.isEmpty()) {
            CircularProgressIndicator()
        }

        // Show error message with retry button if error occurs
        state.errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Button(onClick = onRetry) {
                Text(text = "Retry")
            }
        }

        // Scrollable list of food items
        // Uses LazyColumn for efficient rendering of large lists
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
        ) {
            // Use food.id as key for optimized recomposition
            items(items = state.foods, key = { food -> food.id }) { food ->
                FoodCard(food = food)
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

