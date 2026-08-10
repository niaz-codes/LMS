package com.example.uos_lms.feature.auth.presentation.register

import android.net.Uri
import com.example.uos_lms.core.domain.model.UserRole

data class RegisterUiState(
    val fullName: String = "",
    val fatherName: String = "",
    val cnic: String = "",
    val phone: String = "",
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val role: UserRole = UserRole.STUDENT,
    val photoUri: Uri? = null,
    val fullNameError: String? = null,
    val fatherNameError: String? = null,
    val cnicError: String? = null,
    val phoneError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val confirmPasswordError: String? = null,
    val isLoading: Boolean = false,
    val uploadProgress: Int? = null,
    val submitError: String? = null,
)
