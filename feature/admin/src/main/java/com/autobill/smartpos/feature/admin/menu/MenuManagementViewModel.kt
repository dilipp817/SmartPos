package com.autobill.smartpos.feature.admin.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.usecase.CreateFoodUseCase
import com.autobill.smartpos.domain.usecase.DeleteFoodUseCase
import com.autobill.smartpos.domain.usecase.GetCategoriesUseCase
import com.autobill.smartpos.domain.usecase.GetFoodsUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.UpdateFoodUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuManagementViewModel @Inject constructor(
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val getFoodsUseCase: GetFoodsUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createFoodUseCase: CreateFoodUseCase,
    private val updateFoodUseCase: UpdateFoodUseCase,
    private val deleteFoodUseCase: DeleteFoodUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MenuManagementUiState())
    val uiState: StateFlow<MenuManagementUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(FoodFormState())
    val formState: StateFlow<FoodFormState> = _formState.asStateFlow()

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

    // ── Search ────────────────────────────────────────────────────────────────

    fun updateSearchQuery(query: String) = _uiState.update { it.copy(searchQuery = query) }

    val filteredFoods get() = uiState.value.let { state ->
        if (state.searchQuery.isBlank()) state.foods
        else state.foods.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
    }

    // ── Dialog management ─────────────────────────────────────────────────────

    fun openCreateDialog() {
        _formState.value = FoodFormState()
        _uiState.update { it.copy(showCreateDialog = true, editingFood = null) }
    }

    fun openEditDialog(food: com.autobill.smartpos.domain.model.Food) {
        _formState.value = FoodFormState(
            name            = food.name,
            price           = food.price.toString(),
            description     = food.description.orEmpty(),
            imageUrl        = food.imageUrl.orEmpty(),
            categoryId      = food.categoryId,
            isVegetarian    = food.isVegetarian,
            isSpicy         = food.isSpicy,
            isAvailable     = food.isAvailable,
            preparationTime = food.preparationTime?.toString().orEmpty(),
            allergens       = food.allergens.orEmpty(),
            calories        = food.calories?.toString().orEmpty(),
        )
        _uiState.update { it.copy(editingFood = food, showCreateDialog = false) }
    }

    fun closeDialog() = _uiState.update { it.copy(showCreateDialog = false, editingFood = null) }

    fun requestDelete(food: com.autobill.smartpos.domain.model.Food) =
        _uiState.update { it.copy(deletingFood = food) }

    fun cancelDelete() = _uiState.update { it.copy(deletingFood = null) }

    // ── Form field updates ────────────────────────────────────────────────────

    fun onNameChange(v: String)            = _formState.update { it.copy(name = v) }
    fun onPriceChange(v: String)           = _formState.update { it.copy(price = v) }
    fun onDescriptionChange(v: String)     = _formState.update { it.copy(description = v) }
    fun onImageUrlChange(v: String)        = _formState.update { it.copy(imageUrl = v) }
    fun onCategoryChange(id: Long?)        = _formState.update { it.copy(categoryId = id) }
    fun onVegetarianChange(v: Boolean)     = _formState.update { it.copy(isVegetarian = v) }
    fun onSpicyChange(v: Boolean)          = _formState.update { it.copy(isSpicy = v) }
    fun onAvailableChange(v: Boolean)      = _formState.update { it.copy(isAvailable = v) }
    fun onPrepTimeChange(v: String)        = _formState.update { it.copy(preparationTime = v) }
    fun onAllergensChange(v: String)       = _formState.update { it.copy(allergens = v) }
    fun onCaloriesChange(v: String)        = _formState.update { it.copy(calories = v) }

    // ── Save (create / update) ────────────────────────────────────────────────

    fun saveFood() {
        val form = _formState.value
        if (!form.isValid) return
        val rid = restaurantId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val editing = _uiState.value.editingFood
            val result = if (editing == null) {
                createFoodUseCase(
                    restaurantId    = rid,
                    name            = form.name.trim(),
                    price           = form.price.toDouble(),
                    description     = form.description.trim().ifBlank { null },
                    imageUrl        = form.imageUrl.trim().ifBlank { null },
                    categoryId      = form.categoryId,
                    isVegetarian    = form.isVegetarian,
                    isSpicy         = form.isSpicy,
                    preparationTime = form.preparationTime.toIntOrNull(),
                    allergens       = form.allergens.trim().ifBlank { null },
                    calories        = form.calories.toIntOrNull(),
                )
            } else {
                updateFoodUseCase(
                    foodId          = editing.id,
                    restaurantId    = rid,
                    name            = form.name.trim(),
                    price           = form.price.toDouble(),
                    description     = form.description.trim().ifBlank { null },
                    imageUrl        = form.imageUrl.trim().ifBlank { null },
                    categoryId      = form.categoryId,
                    isVegetarian    = form.isVegetarian,
                    isSpicy         = form.isSpicy,
                    isAvailable     = form.isAvailable,
                    preparationTime = form.preparationTime.toIntOrNull(),
                    allergens       = form.allergens.trim().ifBlank { null },
                    calories        = form.calories.toIntOrNull(),
                )
            }
            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSaving = false, showCreateDialog = false,
                        editingFood = null, successMessage = if (editing == null) "Item created" else "Item updated") }
                    loadFoods()
                }
                is Result.Failure -> _uiState.update { it.copy(isSaving = false,
                    error = result.exception.message ?: "Save failed") }
                else -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun confirmDelete() {
        val food = _uiState.value.deletingFood ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, deletingFood = null, error = null) }
            when (val r = deleteFoodUseCase(food.id)) {
                is Result.Success -> {
                    _uiState.update { it.copy(isDeleting = false,
                        successMessage = "${food.name} deleted") }
                    loadFoods()
                }
                is Result.Failure -> _uiState.update { it.copy(isDeleting = false,
                    error = r.exception.message ?: "Delete failed") }
                else -> _uiState.update { it.copy(isDeleting = false) }
            }
        }
    }

    fun dismissSuccess() = _uiState.update { it.copy(successMessage = null) }
    fun dismissError()   = _uiState.update { it.copy(error = null) }
}
