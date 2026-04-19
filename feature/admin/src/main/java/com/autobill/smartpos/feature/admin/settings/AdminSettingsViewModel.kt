package com.autobill.smartpos.feature.admin.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.UpdateRestaurantSettingsRequest
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantUseCase
import com.autobill.smartpos.domain.usecase.ObserveRestaurantUseCase
import com.autobill.smartpos.domain.usecase.UpdateRestaurantSettingsUseCase
import com.autobill.smartpos.feature.admin.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Edit Outlet Info screen.
 *
 * Wires to PATCH /api/v1/restaurants/{id} — contract §8.4.
 * Removed settings (tax, tips, auto-print) are post-production backlog.
 */
@HiltViewModel
class AdminSettingsViewModel @Inject constructor(
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val getRestaurantUseCase: GetRestaurantUseCase,
    private val observeRestaurantUseCase: ObserveRestaurantUseCase,
    private val updateRestaurantSettingsUseCase: UpdateRestaurantSettingsUseCase,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSettingsUiState())
    val uiState: StateFlow<AdminSettingsUiState> = _uiState.asStateFlow()

    init {
        observeRestaurantUseCase()
            .onEach { r ->
                if (r != null && !_uiState.value.isDirty) {
                    _uiState.update {
                        it.copy(
                            restaurant   = r,
                            outletName   = r.outletName,
                            displayName  = r.displayName,
                            outletManager = r.outletManager,
                            building     = r.address.building,
                            street       = r.address.street,
                            location     = r.address.location,
                            zipCode      = r.address.zipCode,
                        )
                    }
                }
            }
            .launchIn(viewModelScope)
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val rid = getRestaurantIdUseCase()
            if (rid == null) { _uiState.update { it.copy(isLoading = false) }; return@launch }
            getRestaurantUseCase(rid)
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    // ── Field updates ────────────────────────────────────────────────────────

    fun onOutletNameChange(v: String)    = _uiState.update { it.copy(outletName = v, isDirty = true) }
    fun onDisplayNameChange(v: String)   = _uiState.update { it.copy(displayName = v, isDirty = true) }
    fun onOutletManagerChange(v: String) = _uiState.update { it.copy(outletManager = v, isDirty = true) }
    fun onBuildingChange(v: String)      = _uiState.update { it.copy(building = v, isDirty = true) }
    fun onStreetChange(v: String)        = _uiState.update { it.copy(street = v, isDirty = true) }
    fun onLocationChange(v: String)      = _uiState.update { it.copy(location = v, isDirty = true) }
    fun onZipCodeChange(v: String)       = _uiState.update { it.copy(zipCode = v, isDirty = true) }

    // ── Save ─────────────────────────────────────────────────────────────────

    fun saveSettings() {
        val state = _uiState.value
        val rid   = state.restaurant?.id ?: return
        if (state.outletName.isBlank()) {
            _uiState.update { it.copy(error = context.getString(R.string.validation_name_required)) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = updateRestaurantSettingsUseCase(
                restaurantId = rid,
                request = UpdateRestaurantSettingsRequest(
                    outletName    = state.outletName.trim(),
                    displayName   = state.displayName.trim().ifBlank { null },
                    outletManager = state.outletManager.trim().ifBlank { null },
                    building      = state.building.trim().ifBlank { null },
                    street        = state.street.trim().ifBlank { null },
                    location      = state.location.trim().ifBlank { null },
                    zipCode       = state.zipCode.trim().ifBlank { null },
                ),
            )
            when (result) {
                is Result.Success -> _uiState.update {
                    it.copy(isSaving = false, isDirty = false,
                            successMessage = context.getString(R.string.settings_saved_success),
                            restaurant = result.data)
                }
                is Result.Failure -> _uiState.update {
                    it.copy(isSaving = false,
                            error = result.exception.message ?: context.getString(R.string.settings_save_failed))
                }
                else -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun dismissError()   = _uiState.update { it.copy(error = null) }
    fun dismissSuccess() = _uiState.update { it.copy(successMessage = null) }
}
