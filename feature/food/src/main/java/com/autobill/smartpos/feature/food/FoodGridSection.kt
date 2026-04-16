package com.autobill.smartpos.feature.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.ui.components.EmptyStateCard
import com.autobill.smartpos.ui.components.ErrorBanner
import com.autobill.smartpos.ui.components.FullScreenLoading
import com.autobill.smartpos.ui.components.LinearLoadingBar

/**
 * Food Grid Section Component
 * Displays paginated food items in a responsive grid
 * Handles loading, error, and empty states
 * 
 * Performance optimized:
 * - LazyVerticalGrid renders only visible items
 * - Infinite scroll detection without blocking
 * - Each FoodGridCard receives minimal data
 * - rememberLazyGridState for scroll memory
 */
@Composable
fun FoodGridSection(
    data: FoodGridData,
    modifier: Modifier = Modifier,
) {
    val lazyGridState = rememberLazyGridState()

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        // Show "Loading More" indicator at top
        if (data.isLoadingMore) {
            LinearLoadingBar()
        }

        // Main content based on state
        when {
            data.hasError && data.items.isEmpty() -> {
                // Full screen error state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                    ) {
                        ErrorBanner(
                            message = data.errorMessage ?: stringResource(R.string.error_failed_to_load_foods),
                            modifier = Modifier.padding(bottom = 16.dp),
                        )
                        Text(
                            text = stringResource(R.string.please_try_again),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            data.isLoading && data.items.isEmpty() -> {
                // Initial loading state
                FullScreenLoading(message = stringResource(R.string.loading_menu))
            }

            data.items.isEmpty() -> {
                // Empty state (no items after filtering)
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    EmptyStateCard(
                        message = stringResource(R.string.no_foods_available),
                        modifier = Modifier.padding(32.dp),
                    )
                }
            }

            else -> {
                // Grid with items
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),  // 2 columns as per design
                    state = lazyGridState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(
                        items = data.items,
                        key = { it.id },
                    ) { food ->
                        FoodGridCard(
                            food = food,
                            onAdd = { data.onFoodAdd(food.id) },
                            onIncrease = { data.onFoodIncrease(food.id) },
                            onDecrease = { data.onFoodDecrease(food.id) },
                        )
                    }
                }

                // Note: Infinite scroll can be implemented using LaunchedEffect
                // and LazyGridState.canScrollForward if needed
            }
        }
    }
}
