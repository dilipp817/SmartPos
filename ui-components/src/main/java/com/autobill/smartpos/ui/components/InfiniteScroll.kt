package com.autobill.smartpos.ui.components

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Composable that detects when user scrolls near the end of a list.
 * Triggers load more callback when within threshold items from the end.
 *
 * @param listState The LazyListState of your LazyColumn
 * @param threshold Number of items from end to trigger load more (default: 3)
 * @param onLoadMore Callback when user scrolls near the end
 *
 * Usage:
 * ```kotlin
 * val lazyListState = rememberLazyListState()
 * LazyColumn(state = lazyListState) { ... }
 * InfiniteScrollHandler(
 *     listState = lazyListState,
 *     threshold = 3,
 *     onLoadMore = { viewModel.loadNextPage() }
 * )
 * ```
 */
@Composable
fun InfiniteScrollHandler(
    listState: LazyListState,
    threshold: Int = 3,
    onLoadMore: () -> Unit,
) {
    LaunchedEffect(Unit) {
        snapshotFlow { listState.layoutInfo }
            .distinctUntilChanged()
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                // Trigger load more when user is within threshold items from end
                if (lastVisibleIndex >= totalItems - threshold && totalItems > 0) {
                    onLoadMore()
                }
            }
    }
}

/**
 * Grid variant of [InfiniteScrollHandler] for use with [LazyVerticalGrid].
 * Observes [LazyGridState] so scrolling the grid correctly triggers [onLoadMore].
 *
 * @param gridState The [LazyGridState] of your LazyVerticalGrid
 * @param threshold Number of items from end to trigger load more (default: 3)
 * @param onLoadMore Callback when user scrolls near the end
 *
 * Usage:
 * ```kotlin
 * val gridState = rememberLazyGridState()
 * LazyVerticalGrid(state = gridState) { ... }
 * InfiniteScrollHandler(
 *     gridState = gridState,
 *     threshold = 3,
 *     onLoadMore = { viewModel.loadNextPage() }
 * )
 * ```
 */
@Composable
fun InfiniteScrollHandler(
    gridState: LazyGridState,
    threshold: Int = 3,
    onLoadMore: () -> Unit,
) {
    LaunchedEffect(Unit) {
        snapshotFlow { gridState.layoutInfo }
            .distinctUntilChanged()
            .collect { layoutInfo ->
                val totalItems = layoutInfo.totalItemsCount
                val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0

                if (lastVisibleIndex >= totalItems - threshold && totalItems > 0) {
                    onLoadMore()
                }
            }
    }
}

/**
 * Utility function to calculate if list is scrollable (has vertical scroll).
 * Useful for deciding whether to show infinite scroll handler.
 *
 * @return True if content height > viewport height (i.e., list can scroll)
 */
fun LazyListState.isScrollable(): Boolean {
    return layoutInfo.totalItemsCount > 0 &&
            layoutInfo.viewportEndOffset > layoutInfo.viewportStartOffset
}

/**
 * Utility function to check if list is at the bottom.
 *
 * @return True if last item is visible
 */
fun LazyListState.isAtBottom(): Boolean {
    val lastIndex = layoutInfo.totalItemsCount - 1
    return layoutInfo.visibleItemsInfo.lastOrNull()?.index == lastIndex
}

/**
 * Utility function to check if list is at the top.
 *
 * @return True if first item is visible
 */
fun LazyListState.isAtTop(): Boolean {
    return layoutInfo.visibleItemsInfo.firstOrNull()?.index == 0
}

