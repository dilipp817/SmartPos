package com.autobill.smartpos.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.domain.common.UiState
import com.autobill.smartpos.domain.model.User
import com.autobill.smartpos.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Login screen.
 *
 * After a successful login the [loginState] transitions to [UiState.Success] and the
 * screen should navigate to the main app — the restaurantId is already persisted in
 * SessionDataStore and is available to all subsequent API calls.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
) : ViewModel() {

    private val _loginState = MutableStateFlow<UiState<User>>(UiState.Idle)
    val loginState: StateFlow<UiState<User>> = _loginState.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun onUsernameChange(value: String) { _username.value = value }
    fun onPasswordChange(value: String) { _password.value = value }

    fun login() {
        viewModelScope.launch {
            _loginState.value = UiState.Loading
            val result = loginUseCase(
                username = _username.value.trim(),
                password = _password.value,
            )
            _loginState.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(
                    message = result.exceptionOrNull()?.message ?: "Login failed. Please try again.",
                    exception = result.exceptionOrNull() as? Exception,
                )
            }
        }
    }

    fun resetState() {
        _loginState.value = UiState.Idle
    }
}

