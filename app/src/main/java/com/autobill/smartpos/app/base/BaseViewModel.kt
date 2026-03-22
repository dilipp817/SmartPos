package com.autobill.smartpos.app.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Base ViewModel providing common functionality for all ViewModels.
 * Handles error state management and other common operations.
 */
abstract class BaseViewModel : ViewModel() {
    
    private val _errorState = MutableStateFlow<String?>(null)
    val errorState: StateFlow<String?> = _errorState.asStateFlow()

    /**
     * Sets error message to display in UI
     */
    protected fun setError(message: String?) {
        _errorState.value = message
    }

    /**
     * Clears error message
     */
    fun clearError() {
        _errorState.value = null
    }
}

