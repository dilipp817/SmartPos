package com.autobill.smartpos.feature.admin.settings

import com.autobill.smartpos.domain.model.Restaurant

data class AdminSettingsUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val restaurant: Restaurant? = null,
    // Editable fields (mirror Restaurant + RestaurantSettings)
    val taxRate: String = "",
    val enableTips: Boolean = false,
    val defaultTipPercentage: String = "",
    val autoPrintBill: Boolean = false,
    val taxInclusive: Boolean = false,
    // Dirty flag — true once user has changed any value
    val isDirty: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null,
)

