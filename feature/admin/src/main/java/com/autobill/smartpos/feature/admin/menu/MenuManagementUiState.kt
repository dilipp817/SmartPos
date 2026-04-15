package com.autobill.smartpos.feature.admin.menu

import com.autobill.smartpos.domain.model.Category
import com.autobill.smartpos.domain.model.Food

data class MenuManagementUiState(
    val isLoading: Boolean = false,
    val foods: List<Food> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    /** Non-null → FoodFormDialog is open in create mode. */
    val showCreateDialog: Boolean = false,
    /** Non-null → FoodFormDialog is open in edit mode with this food pre-filled. */
    val editingFood: Food? = null,
    /** Non-null → delete-confirm dialog is shown for this food. */
    val deletingFood: Food? = null,
    val error: String? = null,
    val successMessage: String? = null,
)

/** Immutable form state shared between create and edit. */
data class FoodFormState(
    val name: String = "",
    val price: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val categoryId: Long? = null,
    val isVegetarian: Boolean = false,
    val isSpicy: Boolean = false,
    val isAvailable: Boolean = true,
    val preparationTime: String = "",
    val allergens: String = "",
    val calories: String = "",
) {
    val nameError: String? get() = if (name.isBlank()) "Name is required" else null
    val priceError: String? get() = when {
        price.isBlank()            -> "Price is required"
        price.toDoubleOrNull() == null -> "Invalid price"
        price.toDouble() < 0       -> "Price must be ≥ 0"
        else                       -> null
    }
    val isValid: Boolean get() = nameError == null && priceError == null
}

