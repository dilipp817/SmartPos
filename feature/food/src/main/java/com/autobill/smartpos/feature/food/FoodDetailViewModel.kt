package com.autobill.smartpos.feature.food

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.common.UiState
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.domain.usecase.AddToCartUseCase
import com.autobill.smartpos.domain.usecase.GetCartUseCase
import com.autobill.smartpos.domain.usecase.GetFoodByIdUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Food Detail screen.
 *
 * Responsibilities:
 *  - Load a single food item by ID via [GetFoodByIdUseCase]
 *  - Expose current cart quantity for this item (so the Add button reflects cart state)
 *  - Handle Add To Cart action via [AddToCartUseCase]
 */
@HiltViewModel
class FoodDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle,
    private val getFoodByIdUseCase: GetFoodByIdUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val addToCartUseCase: AddToCartUseCase,
) : ViewModel() {

    // foodId is passed as a nav argument — see Screen.FoodDetail route
    private val foodId: Long = checkNotNull(savedStateHandle["foodId"]) {
        "FoodDetailViewModel requires a foodId nav argument"
    }

    private val _foodState = MutableStateFlow<UiState<Food>>(UiState.Loading)
    val foodState: StateFlow<UiState<Food>> = _foodState.asStateFlow()

    /** Current quantity of this item in the cart (0 = not in cart) */
    private val _cartQuantity = MutableStateFlow(0)
    val cartQuantity: StateFlow<Int> = _cartQuantity.asStateFlow()

    init {
        loadFood()
        observeCart()
    }

    private fun loadFood() {
        viewModelScope.launch {
            _foodState.value = UiState.Loading
            _foodState.value = when (val result = getFoodByIdUseCase(foodId)) {
                is Result.Success -> UiState.Success(result.data)
                is Result.Failure -> UiState.Error(
                    result.exception.message ?: context.getString(R.string.error_failed_to_load_food_details)
                )
                Result.Loading -> UiState.Loading
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            getCartUseCase().collect { cartItems ->
                _cartQuantity.value = cartItems
                    .find { it.foodId == foodId }
                    ?.quantity ?: 0
            }
        }
    }

    fun addToCart() {
        val food = (_foodState.value as? UiState.Success)?.data ?: return
        viewModelScope.launch {
            addToCartUseCase(food)
        }
    }

    fun retry() = loadFood()
}
