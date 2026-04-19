package com.autobill.smartpos.feature.admin.settings

import com.autobill.smartpos.domain.model.Restaurant

/**
 * UI state for the Edit Outlet Info screen.
 * All editable fields map directly to PATCH /api/v1/restaurants/{id} — contract §8.4.
 * Removed: taxRate, enableTips, defaultTipPercentage, autoPrintBill, taxInclusive (post-production backlog).
 */
data class AdminSettingsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val restaurant: Restaurant? = null,
    // Editable fields — contract §8.4
    val outletName: String = "",
    val displayName: String = "",
    val outletManager: String = "",
    val building: String = "",
    val street: String = "",
    val location: String = "",
    val zipCode: String = "",
    // Dirty flag — true once user has changed any value
    val isDirty: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)
