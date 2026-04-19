package com.autobill.smartpos.feature.admin.category

import com.autobill.smartpos.domain.model.Category

data class CategoryManagementUiState(
    val isLoading: Boolean = false,
    val categories: List<Category> = emptyList(),
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val showCreateDialog: Boolean = false,
    val editingCategory: Category? = null,
    val deletingCategory: Category? = null,
    val error: String? = null,
    val successMessage: String? = null,
)

/** Immutable form state shared between create and edit dialogs. */
data class CategoryFormState(
    val name: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val displayOrder: String = "0",
    /** Set to true the first time the user edits the name field; gates error display. */
    val nameTouched: Boolean = false,
) {
    /** True when name is blank — UI layer resolves the display string via stringResource(R.string.validation_name_required). */
    val hasNameError: Boolean get() = name.isBlank()
    /** True when display order is non-empty but not a valid integer — UI resolves via stringResource(R.string.validation_must_be_number). */
    val hasDisplayOrderError: Boolean get() =
        displayOrder.isNotBlank() && displayOrder.toIntOrNull() == null
    val isValid: Boolean get() = !hasNameError && !hasDisplayOrderError
}

