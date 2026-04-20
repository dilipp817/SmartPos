package com.autobill.smartpos.feature.admin.inventory

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.usecase.GetCategoriesUseCase
import com.autobill.smartpos.domain.usecase.GetFoodsUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.UpdateFoodUseCase
import com.autobill.smartpos.feature.admin.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val getFoodsUseCase: GetFoodsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val updateFoodUseCase: UpdateFoodUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(InventoryUiState())
    val uiState: StateFlow<InventoryUiState> = _uiState.asStateFlow()

    private var restaurantId: Long? = null

    init {
        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            loadFoods()
            loadCategories()
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    fun loadFoods() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getFoodsUseCase()) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, foods = result.data) }
                is Result.Failure -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            val rid = restaurantId ?: return@launch
            when (val result = getCategoriesUseCase(rid)) {
                is Result.Success -> _uiState.update { it.copy(categories = result.data) }
                else              -> Unit
            }
        }
    }

    // ── Filters ───────────────────────────────────────────────────────────────

    fun onSearchQueryChange(query: String) =
        _uiState.update { it.copy(searchQuery = query) }

    fun onCategorySelected(categoryId: Long?) =
        _uiState.update { it.copy(selectedCategoryId = categoryId) }

    // ── Availability toggle ───────────────────────────────────────────────────

    fun toggleAvailability(foodId: Long) {
        val food = _uiState.value.foods.firstOrNull { it.id == foodId } ?: return
        val newAvailable = !food.isAvailable
        val rid = restaurantId ?: return

        // Optimistic update
        _uiState.update { state ->
            state.copy(
                togglingFoodId = foodId,
                foods = state.foods.map {
                    if (it.id == foodId) it.copy(isAvailable = newAvailable) else it
                },
            )
        }

        viewModelScope.launch {
            val result = updateFoodUseCase(
                foodId          = food.id,
                restaurantId    = rid,
                name            = food.name,
                price           = food.price,
                description     = food.description,
                imageUrl        = food.imageUrl,
                categoryId      = food.categoryId,
                isVegetarian    = food.isVegetarian,
                isSpicy         = food.isSpicy,
                isAvailable     = newAvailable,
                preparationTime = food.preparationTime,
                allergens       = food.allergens,
                calories        = food.calories,
            )
            when (result) {
                is Result.Success -> _uiState.update { state ->
                    state.copy(
                        togglingFoodId = null,
                        foods = state.foods.map {
                            if (it.id == foodId) result.data else it
                        },
                        successMessage = if (newAvailable)
                            context.getString(R.string.inventory_marked_available, food.name)
                        else
                            context.getString(R.string.inventory_marked_unavailable, food.name),
                    )
                }
                is Result.Failure -> {
                    // Roll back optimistic update
                    _uiState.update { state ->
                        state.copy(
                            togglingFoodId = null,
                            foods = state.foods.map {
                                if (it.id == foodId) it.copy(isAvailable = !newAvailable) else it
                            },
                            error = result.exception.message
                                ?: context.getString(R.string.inventory_update_availability_failed),
                        )
                    }
                }
                else -> _uiState.update { it.copy(togglingFoodId = null) }
            }
        }
    }

    // ── One-shot consumers ────────────────────────────────────────────────────

    fun dismissError()   = _uiState.update { it.copy(error = null) }
    fun dismissSuccess() = _uiState.update { it.copy(successMessage = null) }
}
