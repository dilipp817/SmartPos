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
) {
    val nameError: String? get() = if (name.isBlank()) "Name is required" else null
    val displayOrderError: String? get() =
        if (displayOrder.isNotBlank() && displayOrder.toIntOrNull() == null) "Must be a number" else null
    val isValid: Boolean get() = nameError == null && displayOrderError == null
}

