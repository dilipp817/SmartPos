package com.autobill.smartpos.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.autobill.smartpos.core.device.DeviceInfoProvider
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
 * Responsibilities (SRP):
 *  - Hold username / password input state
 *  - Invoke [LoginUseCase] and expose [loginState]
 *  - Nothing else — no platform APIs, no I/O, no session storage
 *
 * After a successful login [loginState] transitions to [UiState.Success].
 * The screen calls [onLoginSuccess]; restaurantId is already persisted in
 * SessionDataStore by [LoginUseCase] → [AuthRepositoryImpl] → [SessionDataStore].
 *
 * Testability:
 *  Inject fakes for both constructor params — no Robolectric, no Android runner:
 *      LoginViewModel(
 *          loginUseCase      = FakeLoginUseCase(),
 *          deviceInfoProvider = FakeDeviceInfoProvider("test-id", "tablet"),
 *      )
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val deviceInfoProvider: DeviceInfoProvider,   // interface — no Context here
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
                deviceId   = deviceInfoProvider.getDeviceId(),
                deviceType = deviceInfoProvider.getDeviceType(),
            )
            _loginState.value = if (result.isSuccess) {
                UiState.Success(result.getOrThrow())
            } else {
                UiState.Error(
                    message   = result.exceptionOrNull()?.message ?: "Login failed. Please try again.",
                    exception = result.exceptionOrNull() as? Exception,
                )
            }
        }
    }

    fun resetState() {
        _loginState.value = UiState.Idle
    }
}
