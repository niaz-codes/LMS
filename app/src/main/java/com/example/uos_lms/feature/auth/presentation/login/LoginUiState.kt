package com.example.uos_lms.feature.auth.presentation.login

import com.example.uos_lms.core.domain.model.UserRole

data class LoginUiState(
    val selectedRole: UserRole = UserRole.STUDENT,
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val showForgotPasswordDialog: Boolean = false,
    val forgotPasswordEmail: String = "",
    val isSendingPasswordReset: Boolean = false,
    val forgotPasswordError: String? = null,
    val forgotPasswordSuccessMessage: String? = null,
)
