package com.autobill.smartpos.feature.admin.menu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.autobill.smartpos.domain.model.Food
import com.autobill.smartpos.feature.admin.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuManagementScreen(
    uiState: MenuManagementUiState,
    formState: FoodFormState,
    filteredFoods: List<Food>,
    onBack: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onCreateClick: () -> Unit,
    onEditClick: (Food) -> Unit,
    onDeleteClick: (Food) -> Unit,
    onConfirmDelete: () -> Unit,
    onCancelDelete: () -> Unit,
    onSaveFood: () -> Unit,
    onCloseDialog: () -> Unit,
    onNameChange: (String) -> Unit,
    onPriceChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onImageUrlChange: (String) -> Unit,
    onCategoryChange: (Long?) -> Unit,
    onVegetarianChange: (Boolean) -> Unit,
    onSpicyChange: (Boolean) -> Unit,
    onAvailableChange: (Boolean) -> Unit,
    onPrepTimeChange: (String) -> Unit,
    onAllergensChange: (String) -> Unit,
    onCaloriesChange: (String) -> Unit,
    onDismissError: () -> Unit,
    onDismissSuccess: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { snackbarHostState.showSnackbar(it); onDismissError() }
    }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); onDismissSuccess() }
    }

    // ── Dialogs ───────────────────────────────────────────────────────────────

    if (uiState.showCreateDialog || uiState.editingFood != null) {
        FoodFormDialog(
            formState           = formState,
            categories          = uiState.categories,
            isSaving            = uiState.isSaving,
            isEditMode          = uiState.editingFood != null,
            onNameChange        = onNameChange,
            onPriceChange       = onPriceChange,
            onDescriptionChange = onDescriptionChange,
            onImageUrlChange    = onImageUrlChange,
            onCategoryChange    = onCategoryChange,
            onVegetarianChange  = onVegetarianChange,
            onSpicyChange       = onSpicyChange,
            onAvailableChange   = onAvailableChange,
            onPrepTimeChange    = onPrepTimeChange,
            onAllergensChange   = onAllergensChange,
            onCaloriesChange    = onCaloriesChange,
            onSave              = onSaveFood,
            onDismiss           = onCloseDialog,
        )
    }

    uiState.deletingFood?.let { food ->
        AlertDialog(
            onDismissRequest = onCancelDelete,
            title     = { Text(stringResource(R.string.menu_delete_dialog_title, food.name)) },
            text      = { Text(stringResource(R.string.menu_delete_dialog_message)) },
            confirmButton = {
                TextButton(onClick = onConfirmDelete) {
                    Text(stringResource(R.string.menu_delete_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelDelete) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.menu_management_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateClick) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.cd_add))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            OutlinedTextField(
                value         = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder   = { Text(stringResource(R.string.menu_search_placeholder)) },
                singleLine    = true,
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (uiState.isLoading || uiState.isDeleting) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredFoods.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = if (uiState.searchQuery.isBlank()) stringResource(R.string.menu_empty_no_items)
                               else stringResource(R.string.menu_empty_no_results, uiState.searchQuery),
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(filteredFoods, key = { it.id }) { food ->
                        FoodItemRow(
                            food     = food,
                            onEdit   = { onEditClick(food) },
                            onDelete = { onDeleteClick(food) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FoodItemRow(
    food: Food,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Text(
                        food.name,
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    if (!food.isAvailable)
                        Badge(containerColor = MaterialTheme.colorScheme.error) {
                            Text(stringResource(R.string.menu_badge_unavailable))
                        }
                    if (food.isVegetarian)
                        Badge(containerColor = MaterialTheme.colorScheme.tertiary) {
                            Text(stringResource(R.string.menu_badge_veg))
                        }
                }
                Text(
                    "₹%.2f".format(food.price),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                food.categoryName?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.cd_edit),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.cd_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
