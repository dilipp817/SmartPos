package com.autobill.smartpos.feature.admin.staff

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun StaffManagementRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: StaffManagementViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    StaffManagementScreen(
        uiState  = uiState,
        onBack   = onBack,
        modifier = modifier,
    )
}

