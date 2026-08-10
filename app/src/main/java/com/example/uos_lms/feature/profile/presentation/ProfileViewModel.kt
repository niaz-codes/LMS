package com.example.uos_lms.feature.profile.presentation

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.auth.domain.usecase.ChangePasswordUseCase
import com.example.uos_lms.feature.auth.domain.usecase.LogoutUseCase
import com.example.uos_lms.feature.auth.domain.usecase.ObserveCurrentUserUseCase
import com.example.uos_lms.feature.auth.domain.usecase.RemoveProfilePhotoUseCase
import com.example.uos_lms.feature.auth.domain.usecase.UpdateOwnProfileUseCase
import com.example.uos_lms.feature.auth.domain.usecase.UpdateProfilePhotoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = true,
    val isUploadingPhoto: Boolean = false,
    val uploadProgress: Int? = null,
    val isSavingProfile: Boolean = false,
    val isChangingPassword: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val observeCurrentUserUseCase: ObserveCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val updateProfilePhotoUseCase: UpdateProfilePhotoUseCase,
    private val removeProfilePhotoUseCase: RemoveProfilePhotoUseCase,
    private val updateOwnProfileUseCase: UpdateOwnProfileUseCase,
    private val changePasswordUseCase: ChangePasswordUseCase,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        observeCurrentUserUseCase()
            .onEach { user -> _uiState.value = _uiState.value.copy(user = user, isLoading = false) }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    fun updatePhoto(uri: Uri) {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingPhoto = true, uploadProgress = null, errorMessage = null)
            val result = updateProfilePhotoUseCase(uid, uri) { progress ->
                _uiState.value = _uiState.value.copy(uploadProgress = progress)
            }
            when (result) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(isUploadingPhoto = false, uploadProgress = null)
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isUploadingPhoto = false, uploadProgress = null, errorMessage = result.message)
            }
        }
    }

    fun removePhoto() {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isUploadingPhoto = true, errorMessage = null)
            when (val result = removeProfilePhotoUseCase(uid)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(isUploadingPhoto = false)
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isUploadingPhoto = false, errorMessage = result.message)
            }
        }
    }

    fun updateProfile(fullName: String, fatherName: String, phone: String, cnic: String) {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSavingProfile = true, errorMessage = null)
            when (val result = updateOwnProfileUseCase(uid, fullName, fatherName, phone, cnic)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(isSavingProfile = false)
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isSavingProfile = false, errorMessage = result.message)
            }
        }
    }

    fun changePassword(oldPassword: String, newPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isChangingPassword = true, errorMessage = null)
            when (val result = changePasswordUseCase(oldPassword, newPassword)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(isChangingPassword = false)
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isChangingPassword = false, errorMessage = result.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            sessionManager.clear()
            onComplete()
        }
    }
}
