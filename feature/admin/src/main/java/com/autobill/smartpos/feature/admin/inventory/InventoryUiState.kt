package com.autobill.smartpos.feature.admin.inventory

import com.autobill.smartpos.domain.model.Category
import com.autobill.smartpos.domain.model.Food

data class InventoryUiState(
    val isLoading: Boolean = false,
    val foods: List<Food> = emptyList(),
    val categories: List<Category> = emptyList(),
    /** Currently selected category filter — null means "All". */
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    /** ID of the food item whose availability is currently being toggled. */
    val togglingFoodId: Long? = null,
    val error: String? = null,
    val successMessage: String? = null,
) {
    val filteredFoods: List<Food> get() {
        var result = foods
        if (selectedCategoryId != null) {
            result = result.filter { it.categoryId == selectedCategoryId }
        }
        if (searchQuery.isNotBlank()) {
            result = result.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
        return result
    }

    val availableCount: Int   get() = foods.count { it.isAvailable }
    val unavailableCount: Int get() = foods.count { !it.isAvailable }
}

