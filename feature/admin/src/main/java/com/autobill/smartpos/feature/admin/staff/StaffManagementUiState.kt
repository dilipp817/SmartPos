package com.autobill.smartpos.feature.admin.staff

import com.autobill.smartpos.domain.model.User

data class StaffManagementUiState(
    val currentUser: User? = null,
    val isLoading: Boolean = false,
)

