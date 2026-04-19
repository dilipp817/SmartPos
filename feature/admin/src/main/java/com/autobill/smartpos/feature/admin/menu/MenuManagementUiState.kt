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

/** Typed price validation result — UI maps each variant to a stringResource call. */
enum class PriceValidationError { REQUIRED, INVALID, NEGATIVE }

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
    /** True when name is blank — UI resolves via stringResource(R.string.validation_name_required). */
    val hasNameError: Boolean get() = name.isBlank()
    /** Non-null when price is invalid — UI maps each variant to the appropriate stringResource. */
    val priceError: PriceValidationError? get() = when {
        price.isBlank()                -> PriceValidationError.REQUIRED
        price.toDoubleOrNull() == null -> PriceValidationError.INVALID
        price.toDouble() < 0           -> PriceValidationError.NEGATIVE
        else                           -> null
    }
    val isValid: Boolean get() = !hasNameError && priceError == null
}
