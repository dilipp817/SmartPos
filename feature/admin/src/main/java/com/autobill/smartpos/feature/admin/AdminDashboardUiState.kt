package com.autobill.smartpos.feature.admin

import com.autobill.smartpos.domain.model.AdminStats
import com.autobill.smartpos.domain.model.Restaurant

data class AdminDashboardUiState(
    val isLoading: Boolean = false,
    val stats: AdminStats = AdminStats(),
    val restaurant: Restaurant? = null,
    val isSuperAdmin: Boolean = false,
    val username: String = "",
    val role: String = "",
    val error: String? = null,
)

