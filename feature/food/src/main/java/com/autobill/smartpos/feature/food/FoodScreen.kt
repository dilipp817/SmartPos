package com.autobill.smartpos.feature.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.common.Pagination
import com.autobill.smartpos.domain.common.UiState
import com.autobill.smartpos.ui.components.InfiniteScrollHandler
import java.util.Locale

/**
 * Composable: FoodRoute
 * Container composable for the Food feature.
 * Creates ViewModel and observes paginated state for infinite scroll.
 */
@Composable
fun FoodRoute(
    modifier: Modifier = Modifier,
) {
    // Create ViewModel instance using Hilt
    val viewModel: FoodViewModel = hiltViewModel()

    // Observe paginated UI state with infinite scroll support
    val paginatedState by viewModel.paginatedFoodsState.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()

    // Remember LazyListState for infinite scroll detection
    val lazyListState = rememberLazyListState()

    // Render the actual screen with current state
    FoodScreen(
        state = paginatedState,
        isLoadingMore = isLoadingMore,
        lazyListState = lazyListState,
        onRetry = { viewModel.retryLoadPaginatedFoods() },
        onLoadMore = { viewModel.loadNextPage() },
        modifier = modifier,
    )
}

/**
 * Composable: FoodScreen
 * Main UI for displaying paginated list of foods with infinite scroll.
 * Handles Loading, Success, Error, and Idle states.
 * Auto-loads next page when user scrolls near bottom.
 */
@Composable
fun FoodScreen(
    state: UiState<Pagination<Food>>,
    isLoadingMore: Boolean,
    lazyListState: androidx.compose.foundation.lazy.LazyListState,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
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
                Text("Loading foods...")
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
                val pagination = state.data
                
                if (pagination.data.isEmpty()) {
                    Text(
                        text = "No foods available",
                        modifier = Modifier.padding(top = 16.dp),
                    )
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Paginated food list with infinite scroll
                        LazyColumn(
                            modifier = Modifier.weight(1f),
                            state = lazyListState,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(vertical = 8.dp),
                        ) {
                            items(
                                items = pagination.data,
                                key = { food -> food.id },
                            ) { food ->
                                FoodCard(food = food)
                            }
                        }

                        // Loading indicator for "load more"
                        if (isLoadingMore) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .padding(8.dp),
                                )
                                Text(
                                    text = "Loading more...",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }

                        // Pagination info
                        Text(
                            text = "Loaded ${pagination.data.size} of ${pagination.total}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                        )
                    }
                }

                // Detect scroll to bottom and load more
                InfiniteScrollHandler(
                    listState = lazyListState,
                    threshold = 3,
                    onLoadMore = onLoadMore,
                )
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

