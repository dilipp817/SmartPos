package com.autobill.smartpos.feature.food

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import com.autobill.smartpos.ui.components.layout.LayoutTokens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
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
import com.autobill.smartpos.ui.components.FullScreenLoading
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

    FoodScreen(
        state = paginatedState,
        isLoadingMore = isLoadingMore,
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
/**
 * [FoodScreen] is a simple list screen used in non-HomeScreen flows.
 * Uses an adaptive [LazyVerticalGrid] so it looks good on all tablet sizes
 * (8" → 2 columns, 10"+ → 3 columns, 14" → 4+ columns).
 * Content is capped at 1200 dp and centred to prevent over-stretching.
 */
@Composable
fun FoodScreen(
    state: UiState<Pagination<Food>>,
    isLoadingMore: Boolean,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onFoodClick: (Long) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = LayoutTokens.MAX_WIDTH_CATALOGUE)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // Header
            Text(
                text = stringResource(R.string.foods),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            when (state) {
                UiState.Idle -> {
                    Text(stringResource(R.string.loading_foods))
                }

                UiState.Loading -> {
                    FullScreenLoading(message = stringResource(R.string.loading_foods))
                }

                is UiState.Success -> {
                    val pagination = state.data

                    if (pagination.data.isEmpty()) {
                        Text(
                            text = stringResource(R.string.no_foods_available),
                            modifier = Modifier.padding(top = 16.dp),
                        )
                    } else {
                        val gridState = rememberLazyGridState()

                        Column(modifier = Modifier.fillMaxSize()) {
                            // Adaptive grid — on 8" tablet: ~2 cols, 10": ~3 cols, 14": ~4 cols
                            LazyVerticalGrid(
                                columns = GridCells.Adaptive(minSize = LayoutTokens.GRID_CELL_FOOD),
                                state = gridState,
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                contentPadding = PaddingValues(vertical = 8.dp),
                            ) {
                                items(
                                    items = pagination.data,
                                    key = { food -> food.id },
                                ) { food ->
                                    FoodCard(food = food, onClick = { onFoodClick(food.id) })
                                }
                            }

                            // Load-more indicator
                            if (isLoadingMore) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    CircularProgressIndicator(modifier = Modifier.padding(8.dp))
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

                        // Infinite scroll — observes gridState so grid scrolling triggers onLoadMore
                        InfiniteScrollHandler(
                            gridState = gridState,
                            threshold = 3,
                            onLoadMore = onLoadMore,
                        )
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
                            Text(text = stringResource(R.string.retry))
                        }
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
