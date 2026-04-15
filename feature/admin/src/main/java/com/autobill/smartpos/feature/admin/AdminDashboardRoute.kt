package com.autobill.smartpos.feature.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun AdminDashboardRoute(
    onNavigateToMenuManagement: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToStaffManagement: () -> Unit,
    onNavigateToInventory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: AdminDashboardViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    AdminDashboardScreen(
        uiState                      = uiState,
        onRefresh                    = viewModel::loadStats,
        onNavigateToMenuManagement   = onNavigateToMenuManagement,
        onNavigateToSettings         = onNavigateToSettings,
        onNavigateToReports          = onNavigateToReports,
        onNavigateToStaffManagement  = onNavigateToStaffManagement,
        onNavigateToInventory        = onNavigateToInventory,
        onDismissError               = viewModel::dismissError,
        modifier                     = modifier,
    )
}

