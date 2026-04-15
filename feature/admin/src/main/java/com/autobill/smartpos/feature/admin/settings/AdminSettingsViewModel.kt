package com.autobill.smartpos.feature.admin.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.Result
import com.autobill.smartpos.domain.model.UpdateRestaurantSettingsRequest
import com.autobill.smartpos.domain.usecase.GetRestaurantIdUseCase
import com.autobill.smartpos.domain.usecase.GetRestaurantUseCase
import com.autobill.smartpos.domain.usecase.ObserveRestaurantUseCase
import com.autobill.smartpos.domain.usecase.UpdateRestaurantSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminSettingsViewModel @Inject constructor(
    private val getRestaurantIdUseCase: GetRestaurantIdUseCase,
    private val getRestaurantUseCase: GetRestaurantUseCase,
    private val observeRestaurantUseCase: ObserveRestaurantUseCase,
    private val updateRestaurantSettingsUseCase: UpdateRestaurantSettingsUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminSettingsUiState())
    val uiState: StateFlow<AdminSettingsUiState> = _uiState.asStateFlow()

    init {
        // Populate fields from cached restaurant whenever it updates
        observeRestaurantUseCase()
            .onEach { r ->
                if (r != null && !_uiState.value.isDirty) {
                    _uiState.update { it.copy(
                        restaurant           = r,
                        taxRate              = r.taxRate.toString(),
                        enableTips           = r.settings.enableTips,
                        defaultTipPercentage = r.settings.defaultTipPercentage.toString(),
                        autoPrintBill        = r.settings.autoPrintBill,
                        taxInclusive         = r.settings.taxInclusive,
                    ) }
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

    // ── Field updates — each call marks form as dirty ────────────────────────

    fun onTaxRateChange(v: String)            = _uiState.update { it.copy(taxRate = v, isDirty = true) }
    fun onEnableTipsChange(v: Boolean)        = _uiState.update { it.copy(enableTips = v, isDirty = true) }
    fun onDefaultTipPercentageChange(v: String) = _uiState.update { it.copy(defaultTipPercentage = v, isDirty = true) }
    fun onAutoPrintBillChange(v: Boolean)     = _uiState.update { it.copy(autoPrintBill = v, isDirty = true) }
    fun onTaxInclusiveChange(v: Boolean)      = _uiState.update { it.copy(taxInclusive = v, isDirty = true) }

    // ── Save ─────────────────────────────────────────────────────────────────

    fun saveSettings() {
        val state = _uiState.value
        val rid   = state.restaurant?.id ?: return
        val taxRateValue = state.taxRate.toDoubleOrNull() ?: return
        val tipPct = state.defaultTipPercentage.toDoubleOrNull()

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            val result = updateRestaurantSettingsUseCase(
                restaurantId = rid,
                request = UpdateRestaurantSettingsRequest(
                    taxRate              = taxRateValue,
                    enableTips           = state.enableTips,
                    defaultTipPercentage = tipPct,
                    autoPrintBill        = state.autoPrintBill,
                    taxInclusive         = state.taxInclusive,
                ),
            )
            when (result) {
                is Result.Success -> _uiState.update { it.copy(
                    isSaving        = false,
                    isDirty         = false,
                    successMessage  = "Settings saved",
                    restaurant      = result.data,
                ) }
                is Result.Failure -> _uiState.update { it.copy(
                    isSaving = false,
                    error    = result.exception.message ?: "Save failed",
                ) }
                else -> _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    fun dismissError()   = _uiState.update { it.copy(error = null) }
    fun dismissSuccess() = _uiState.update { it.copy(successMessage = null) }
}

