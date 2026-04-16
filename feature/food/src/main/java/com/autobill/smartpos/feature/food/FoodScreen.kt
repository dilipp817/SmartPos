package com.autobill.smartpos.feature.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.common.Pagination
import com.autobill.smartpos.domain.common.UiState
import com.autobill.smartpos.feature.food.R
import com.autobill.smartpos.ui.components.InfiniteScrollHandler

/**
 * Composable: FoodRoute
 * Container composable for the Food feature.
 * Creates ViewModel and observes paginated state for infinite scroll.
 */
@Composable
fun FoodRoute(
    modifier: Modifier = Modifier,
    onFoodClick: (Long) -> Unit = {},
) {
    val viewModel: FoodViewModel = hiltViewModel()
    val paginatedState by viewModel.paginatedFoodsState.collectAsStateWithLifecycle()
    val isLoadingMore by viewModel.isLoadingMore.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()

    FoodScreen(
        state = paginatedState,
        isLoadingMore = isLoadingMore,
        lazyListState = lazyListState,
        onRetry = { viewModel.retryLoadPaginatedFoods() },
        onLoadMore = { viewModel.loadNextPage() },
        onFoodClick = onFoodClick,
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
    onFoodClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        // Header text
        Text(
            text = stringResource(R.string.foods),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        // Handle different UI states
        when (state) {
            UiState.Idle -> {
                Text(stringResource(R.string.loading_foods))
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
                        text = stringResource(R.string.loading_foods),
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
            }

            is UiState.Success -> {
                val pagination = state.data
                
                if (pagination.data.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_foods_available),
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
                                FoodCard(food = food, onClick = { onFoodClick(food.id) })
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
                                    text = stringResource(R.string.loading_more),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                            }
                        }

                        // Pagination info
                        Text(
                            text = stringResource(R.string.loaded_x_of_y, pagination.data.size, pagination.total),
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
                        Text(text = stringResource(R.string.retry))
                    }
                }
            }
        }
    }
}

// Composable: FoodCard
// Individual food item card component
@Composable
private fun FoodCard(food: Food, onClick: () -> Unit = {}) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = food.name, style = MaterialTheme.typography.titleMedium)
            Text(text = stringResource(R.string.food_id_label, food.id))
            Text(text = stringResource(R.string.food_restaurant_label, food.restaurantId))
            Text(text = stringResource(R.string.food_price_label, food.price))
        }
    }
}
