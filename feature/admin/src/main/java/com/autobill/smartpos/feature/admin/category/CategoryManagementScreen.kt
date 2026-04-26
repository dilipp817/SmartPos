package com.autobill.smartpos.feature.admin.category

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import com.autobill.smartpos.ui.components.layout.LayoutTokens
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autobill.smartpos.domain.model.Category
import com.autobill.smartpos.feature.admin.R

@Composable
fun CategoryManagementRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CategoryManagementViewModel = hiltViewModel(),
) {
    val uiState  by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()

    CategoryManagementScreen(
        uiState          = uiState,
        formState        = formState,
        onBack           = onBack,
        onCreateClick    = viewModel::openCreateDialog,
        onEditClick      = viewModel::openEditDialog,
        onDeleteClick    = viewModel::openDeleteDialog,
        onNameChange     = viewModel::onNameChange,
        onDescChange     = viewModel::onDescriptionChange,
        onImageUrlChange = viewModel::onImageUrlChange,
        onOrderChange    = viewModel::onDisplayOrderChange,
        onSave           = viewModel::saveCategory,
        onDismissDialog  = viewModel::dismissDialog,
        onConfirmDelete  = viewModel::confirmDelete,
        onDismissDelete  = viewModel::dismissDeleteDialog,
        onDismissError   = viewModel::dismissError,
        onDismissSuccess = viewModel::dismissSuccess,
        modifier         = modifier,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryManagementScreen(
    uiState: CategoryManagementUiState,
    formState: CategoryFormState,
    onBack: () -> Unit,
    onCreateClick: () -> Unit,
    onEditClick: (Category) -> Unit,
    onDeleteClick: (Category) -> Unit,
    onNameChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onImageUrlChange: (String) -> Unit,
    onOrderChange: (String) -> Unit,
    onSave: () -> Unit,
    onDismissDialog: () -> Unit,
    onConfirmDelete: () -> Unit,
    onDismissDelete: () -> Unit,
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.category_mgmt_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateClick,
                containerColor = Color(0xFFE33E3E),
            ) {
                Icon(Icons.Default.Add, contentDescription = stringResource(R.string.category_mgmt_add))
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = modifier,
    ) { padding ->

        if (uiState.isLoading || uiState.isDeleting) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (uiState.categories.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Category,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        stringResource(R.string.category_mgmt_empty),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.category_mgmt_empty_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.TopCenter,
            ) {
                LazyColumn(
                    modifier = Modifier
                        .widthIn(max = LayoutTokens.MAX_WIDTH_CONTENT)
                        .fillMaxWidth()
                        .padding(padding)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.categories, key = { it.id }) { category ->
                        CategoryCard(
                            category    = category,
                            onEdit      = { onEditClick(category) },
                            onDelete    = { onDeleteClick(category) },
                        )
                    }
                }
            }
        }
    }

    // ── Create / Edit Dialog ──────────────────────────────────────────────────

    val isDialogOpen = uiState.showCreateDialog || uiState.editingCategory != null
    if (isDialogOpen) {
        CategoryFormDialog(
            isEditMode   = uiState.editingCategory != null,
            formState    = formState,
            isSaving     = uiState.isSaving,
            onNameChange = onNameChange,
            onDescChange = onDescChange,
            onImageUrl   = onImageUrlChange,
            onOrderChange= onOrderChange,
            onConfirm    = onSave,
            onDismiss    = onDismissDialog,
        )
    }

    // ── Delete Confirm Dialog ─────────────────────────────────────────────────

    uiState.deletingCategory?.let { category ->
        AlertDialog(
            onDismissRequest = onDismissDelete,
            containerColor   = Color.White,
            shape            = RoundedCornerShape(20.dp),
            title = { Text(stringResource(R.string.category_delete_title), fontWeight = FontWeight.Bold) },
            text  = { Text(stringResource(R.string.category_delete_message, category.name)) },
            confirmButton = {
                Button(
                    onClick = onConfirmDelete,
                    colors  = androidx.compose.material3.ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(R.string.category_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissDelete) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}

// ── Category Card ─────────────────────────────────────────────────────────────

@Composable
private fun CategoryCard(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier  = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors    = CardDefaults.cardColors(containerColor = Color.White),
        shape     = RoundedCornerShape(12.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text  = category.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                if (!category.description.isNullOrBlank()) {
                    Text(
                        text  = category.description.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text  = stringResource(R.string.category_food_count, category.foodCount),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            IconButton(onClick = onEdit) {
                Icon(
                    Icons.Default.Edit,
                    contentDescription = stringResource(R.string.category_edit_action),
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.width(4.dp))
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.category_delete_action),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

// ── Category Form Dialog ──────────────────────────────────────────────────────

@Composable
private fun CategoryFormDialog(
    isEditMode: Boolean,
    formState: CategoryFormState,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onDescChange: (String) -> Unit,
    onImageUrl: (String) -> Unit,
    onOrderChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    val brandRed = Color(0xFFE33E3E)
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = brandRed,
        focusedLabelColor  = brandRed,
    )

    AlertDialog(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor   = Color.White,
        shape            = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = if (isEditMode) stringResource(R.string.category_form_title_edit)
                       else stringResource(R.string.category_form_title_create),
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                HorizontalDivider(color = Color(0xFFEEEEEE))

                // Name — required
                OutlinedTextField(
                    value           = formState.name,
                    onValueChange   = onNameChange,
                    label           = { Text(stringResource(R.string.category_form_name)) },
                    singleLine      = true,
                    isError         = formState.hasNameError && formState.nameTouched,
                    supportingText  = if (formState.hasNameError && formState.nameTouched) {
                        { Text(stringResource(R.string.validation_name_required)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction      = ImeAction.Next,
                    ),
                    colors          = fieldColors,
                    modifier        = Modifier.fillMaxWidth(),
                )

                // Description — optional
                OutlinedTextField(
                    value         = formState.description,
                    onValueChange = onDescChange,
                    label         = { Text(stringResource(R.string.category_form_description)) },
                    maxLines      = 3,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    colors        = fieldColors,
                    modifier      = Modifier.fillMaxWidth(),
                )

                // Image URL — optional
                OutlinedTextField(
                    value         = formState.imageUrl,
                    onValueChange = onImageUrl,
                    label         = { Text(stringResource(R.string.category_form_image_url)) },
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Uri,
                        imeAction    = ImeAction.Next,
                    ),
                    colors   = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )

                // Display Order
                OutlinedTextField(
                    value           = formState.displayOrder,
                    onValueChange   = onOrderChange,
                    label           = { Text(stringResource(R.string.category_form_display_order)) },
                    singleLine      = true,
                    isError         = formState.hasDisplayOrderError,
                    supportingText  = if (formState.hasDisplayOrderError) {
                        { Text(stringResource(R.string.validation_must_be_number)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction    = ImeAction.Done,
                    ),
                    colors   = fieldColors,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            Button(
                onClick  = onConfirm,
                enabled  = formState.isValid && !isSaving,
                colors   = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = brandRed),
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                else Text(stringResource(R.string.category_form_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSaving) {
                Text(stringResource(android.R.string.cancel))
            }
        },
    )
}


