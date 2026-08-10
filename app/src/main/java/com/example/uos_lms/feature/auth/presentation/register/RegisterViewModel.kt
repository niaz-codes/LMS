package com.example.uos_lms.feature.auth.presentation.register

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.validation.CnicValidator
import com.example.uos_lms.core.validation.EmailValidator
import com.example.uos_lms.core.validation.NameValidator
import com.example.uos_lms.core.validation.PasswordValidator
import com.example.uos_lms.core.validation.PhoneValidator
import com.example.uos_lms.core.validation.errorMessageOrNull
import com.example.uos_lms.feature.auth.domain.model.RegisterRequest
import com.example.uos_lms.feature.auth.domain.usecase.RegisterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val registerUseCase: RegisterUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    private val _registrationSubmitted = Channel<Unit>()
    val registrationSubmitted = _registrationSubmitted.receiveAsFlow()

    fun onFullNameChange(v: String) { _uiState.value = _uiState.value.copy(fullName = v, fullNameError = null) }
    fun onFatherNameChange(v: String) { _uiState.value = _uiState.value.copy(fatherName = v, fatherNameError = null) }
    fun onCnicChange(v: String) { _uiState.value = _uiState.value.copy(cnic = v, cnicError = null) }
    fun onPhoneChange(v: String) { _uiState.value = _uiState.value.copy(phone = v, phoneError = null) }
    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v, emailError = null) }
    fun onPasswordChange(v: String) { _uiState.value = _uiState.value.copy(password = v, passwordError = null) }
    fun onConfirmPasswordChange(v: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = v, confirmPasswordError = null)
    }
    fun onRoleChange(role: UserRole) { _uiState.value = _uiState.value.copy(role = role) }
    fun onPhotoPicked(uri: Uri) { _uiState.value = _uiState.value.copy(photoUri = uri) }

    fun submit() {
        val state = _uiState.value

        val fullNameResult = NameValidator.validate(state.fullName, "Full name")
        val fatherNameResult = NameValidator.validate(state.fatherName, "Father name")
        val cnicResult = CnicValidator.validate(state.cnic)
        val phoneResult = PhoneValidator.validate(state.phone)
        val emailResult = EmailValidator.validate(state.email)
        val passwordResult = PasswordValidator.validate(state.password)
        val confirmResult = PasswordValidator.validateConfirmation(state.password, state.confirmPassword)

        _uiState.value = state.copy(
            fullNameError = fullNameResult.errorMessageOrNull,
            fatherNameError = fatherNameResult.errorMessageOrNull,
            cnicError = cnicResult.errorMessageOrNull,
            phoneError = phoneResult.errorMessageOrNull,
            emailError = emailResult.errorMessageOrNull,
            passwordError = passwordResult.errorMessageOrNull,
            confirmPasswordError = confirmResult.errorMessageOrNull,
        )

        val hasErrors = listOf(
            fullNameResult, fatherNameResult, cnicResult, phoneResult, emailResult, passwordResult, confirmResult,
        ).any { it.errorMessageOrNull != null }
        if (hasErrors) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, uploadProgress = null, submitError = null)
            val request = RegisterRequest(
                fullName = state.fullName.trim(),
                fatherName = state.fatherName.trim(),
                cnic = CnicValidator.normalize(state.cnic),
                phone = PhoneValidator.normalize(state.phone),
                email = state.email.trim(),
                password = state.password,
                role = state.role,
                photoUri = state.photoUri,
            )
            val result = registerUseCase(request) { progress ->
                _uiState.value = _uiState.value.copy(uploadProgress = progress)
            }
            when (result) {
                is AppResult.Success -> {
                    _uiState.value = RegisterUiState()
                    _registrationSubmitted.send(Unit)
                }
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, uploadProgress = null, submitError = result.message)
                }
            }
        }
    }
}
