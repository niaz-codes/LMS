package com.example.uos_lms.feature.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.domain.model.UserStatus
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.core.navigation.toDashboardRoute
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.core.validation.EmailValidator
import com.example.uos_lms.core.validation.errorMessageOrNull
import com.example.uos_lms.feature.auth.domain.usecase.LoginUseCase
import com.example.uos_lms.feature.auth.domain.usecase.SendPasswordResetEmailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val sendPasswordResetEmailUseCase: SendPasswordResetEmailUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _navigation = Channel<Routes>()
    val navigation = _navigation.receiveAsFlow()

    fun onRoleChange(role: UserRole) {
        _uiState.value = _uiState.value.copy(selectedRole = role, errorMessage = null)
    }

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, errorMessage = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    fun login() {
        val state = _uiState.value
        val emailError = EmailValidator.validate(state.email).errorMessageOrNull
        if (emailError != null) {
            _uiState.value = state.copy(errorMessage = emailError)
            return
        }
        if (state.password.isEmpty()) {
            _uiState.value = state.copy(errorMessage = "Password is required")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = loginUseCase(state.email, state.password, state.selectedRole)) {
                is AppResult.Success -> {
                    val user = result.data
                    _uiState.value = _uiState.value.copy(isLoading = false)
                    when (user.status) {
                        UserStatus.APPROVED -> {
                            sessionManager.cache(user.uid, user.role, user.fullName)
                            _navigation.send(user.role.toDashboardRoute())
                        }
                        UserStatus.PENDING -> _navigation.send(Routes.PendingApproval)
                        UserStatus.REJECTED -> _navigation.send(Routes.Rejected)
                        UserStatus.SUSPENDED -> _navigation.send(Routes.Suspended)
                    }
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }

    fun onForgotPasswordClick() {
        _uiState.value = _uiState.value.copy(
            showForgotPasswordDialog = true,
            forgotPasswordEmail = _uiState.value.email,
            forgotPasswordError = null,
            forgotPasswordSuccessMessage = null,
        )
    }

    fun onForgotPasswordDismiss() {
        _uiState.value = _uiState.value.copy(showForgotPasswordDialog = false)
    }

    fun onForgotPasswordEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(forgotPasswordEmail = value, forgotPasswordError = null)
    }

    fun sendPasswordReset() {
        val state = _uiState.value
        val emailError = EmailValidator.validate(state.forgotPasswordEmail).errorMessageOrNull
        if (emailError != null) {
            _uiState.value = state.copy(forgotPasswordError = emailError)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSendingPasswordReset = true,
                forgotPasswordError = null,
                forgotPasswordSuccessMessage = null,
            )
            when (val result = sendPasswordResetEmailUseCase(state.forgotPasswordEmail)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    isSendingPasswordReset = false,
                    forgotPasswordSuccessMessage = "A password reset link has been sent to your email. " +
                        "Please check your inbox (and spam folder).",
                )
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    isSendingPasswordReset = false,
                    forgotPasswordError = result.message,
                )
            }
        }
    }
}
