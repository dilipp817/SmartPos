package com.autobill.smartpos.feature.food

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autobill.smartpos.domain.common.UiState

/**
 * FoodDetailRoute — Hilt-injected entry point for the Food Detail screen.
 *
 * Collects ViewModel state and delegates rendering to stateless composables.
 * foodId is recovered from SavedStateHandle inside [FoodDetailViewModel] —
 * no need to pass it explicitly here.
 *
 * Called from NavHost:
 *   composable(Screen.FoodDetail.route) {
 *       FoodDetailRoute(onBack = { navController.popBackStack() })
 *   }
 */
@Composable
fun FoodDetailRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FoodDetailViewModel = hiltViewModel(),
) {
    val foodState by viewModel.foodState.collectAsStateWithLifecycle()
    val cartQuantity by viewModel.cartQuantity.collectAsStateWithLifecycle()

    when (val state = foodState) {
        UiState.Loading, UiState.Idle -> {
            FoodDetailLoadingScreen(modifier = modifier)
        }

        is UiState.Success -> {
            FoodDetailScreen(
                food = state.data,
                cartQuantity = cartQuantity,
                onAddToCart = viewModel::addToCart,
                onBack = onBack,
                modifier = modifier,
            )
        }

        is UiState.Error -> {
            FoodDetailErrorScreen(
                message = state.message,
                onRetry = viewModel::retry,
                onBack = onBack,
                modifier = modifier,
            )
        }
    }
}


