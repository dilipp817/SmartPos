package com.autobill.smartpos.feature.admin.category

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.usecase.CreateCategoryUseCase
import com.autobill.smartpos.domain.usecase.DeleteCategoryUseCase
import com.autobill.smartpos.domain.usecase.GetCategoriesUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.UpdateCategoryUseCase
import com.autobill.smartpos.feature.admin.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoryManagementViewModel @Inject constructor(
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val getCategoriesUseCase: GetCategoriesUseCase,
    private val createCategoryUseCase: CreateCategoryUseCase,
    private val updateCategoryUseCase: UpdateCategoryUseCase,
    private val deleteCategoryUseCase: DeleteCategoryUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoryManagementUiState())
    val uiState: StateFlow<CategoryManagementUiState> = _uiState.asStateFlow()

    private val _formState = MutableStateFlow(CategoryFormState())
    val formState: StateFlow<CategoryFormState> = _formState.asStateFlow()

    private var restaurantId: Long? = null

    init {
        viewModelScope.launch {
            restaurantId = getRestaurantIdUseCase()
            loadCategories()
        }
    }

    // ── Load ──────────────────────────────────────────────────────────────────

    fun loadCategories() {
        val rid = restaurantId ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            when (val result = getCategoriesUseCase(rid)) {
                is Result.Success -> _uiState.update { it.copy(isLoading = false, categories = result.data) }
                is Result.Failure -> _uiState.update { it.copy(isLoading = false, error = result.exception.message) }
                else -> _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // ── Dialog state ──────────────────────────────────────────────────────────

    fun openCreateDialog() {
        _formState.value = CategoryFormState()
        _uiState.update { it.copy(showCreateDialog = true, editingCategory = null) }
    }

    fun openEditDialog(category: com.autobill.smartpos.domain.model.Category) {
        _formState.value = CategoryFormState(
            name         = category.name,
            description  = category.description ?: "",
            imageUrl     = category.imageUrl ?: "",
            displayOrder = category.displayOrder.toString(),
        )
        _uiState.update { it.copy(editingCategory = category, showCreateDialog = false) }
    }

    fun dismissDialog() {
        _uiState.update { it.copy(showCreateDialog = false, editingCategory = null) }
        _formState.value = CategoryFormState()
    }

    fun openDeleteDialog(category: com.autobill.smartpos.domain.model.Category) {
        _uiState.update { it.copy(deletingCategory = category) }
    }

    fun dismissDeleteDialog() {
        _uiState.update { it.copy(deletingCategory = null) }
    }

    // ── Form field updates ────────────────────────────────────────────────────

    fun onNameChange(v: String)         = _formState.update { it.copy(name = v) }
    fun onDescriptionChange(v: String)  = _formState.update { it.copy(description = v) }
    fun onImageUrlChange(v: String)     = _formState.update { it.copy(imageUrl = v) }
    fun onDisplayOrderChange(v: String) = _formState.update { it.copy(displayOrder = v) }

    // ── Save (create or update) ───────────────────────────────────────────────

    fun saveCategory() {
        val rid    = restaurantId ?: return
        val form   = _formState.value
        if (!form.isValid) return

        val editing = _uiState.value.editingCategory

        _uiState.update { it.copy(isSaving = true, error = null) }
        viewModelScope.launch {
            val result = if (editing == null) {
                createCategoryUseCase(
                    restaurantId = rid,
                    name         = form.name.trim(),
                    description  = form.description.takeIf { it.isNotBlank() },
                    imageUrl     = form.imageUrl.takeIf { it.isNotBlank() },
                    displayOrder = form.displayOrder.toIntOrNull() ?: 0,
                )
            } else {
                updateCategoryUseCase(
                    id           = editing.id,
                    name         = form.name.trim(),
                    description  = form.description.takeIf { it.isNotBlank() },
                    imageUrl     = form.imageUrl.takeIf { it.isNotBlank() },
                    displayOrder = form.displayOrder.toIntOrNull() ?: 0,
                )
            }
            when (result) {
                is Result.Success -> {
                    _uiState.update { it.copy(isSaving = false, showCreateDialog = false, editingCategory = null) }
                    _formState.value = CategoryFormState()
                    loadCategories()
                    _uiState.update { it.copy(successMessage = if (editing == null)
                        context.getString(R.string.category_created_success)
                    else
                        context.getString(R.string.category_updated_success))
                    }
                }
                is Result.Failure -> _uiState.update { it.copy(isSaving = false, error = result.exception.message) }
                else -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    fun confirmDelete() {
        val category = _uiState.value.deletingCategory ?: return
        _uiState.update { it.copy(isDeleting = true, deletingCategory = null) }
        viewModelScope.launch {
            when (val result = deleteCategoryUseCase(category.id)) {
                is Result.Success -> {
                    _uiState.update { it.copy(
                        isDeleting     = false,
                        successMessage = context.getString(R.string.category_deleted_success),
                    ) }
                    loadCategories()
                }
                is Result.Failure -> _uiState.update { it.copy(
                    isDeleting = false,
                    error      = result.exception.message,
                ) }
                else -> _uiState.update { it.copy(isDeleting = false) }
            }
        }
    }

    // ── One-shot consumers ────────────────────────────────────────────────────

    fun dismissError()   = _uiState.update { it.copy(error = null) }
    fun dismissSuccess() = _uiState.update { it.copy(successMessage = null) }
}

