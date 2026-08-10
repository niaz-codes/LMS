package com.example.uos_lms.feature.admin.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.UserStatus
import com.example.uos_lms.feature.admin.domain.model.UserFilter
import com.example.uos_lms.feature.admin.domain.usecase.DeleteUserUseCase
import com.example.uos_lms.feature.admin.domain.usecase.ObserveUsersUseCase
import com.example.uos_lms.feature.admin.domain.usecase.ResetUserPasswordUseCase
import com.example.uos_lms.feature.admin.domain.usecase.UpdateUserStatusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminUserListViewModel @Inject constructor(
    private val observeUsersUseCase: ObserveUsersUseCase,
    private val updateUserStatusUseCase: UpdateUserStatusUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val resetUserPasswordUseCase: ResetUserPasswordUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminDashboardUiState())
    val uiState: StateFlow<AdminDashboardUiState> = _uiState.asStateFlow()

    init {
        observeUsersUseCase()
            .onEach { users -> _uiState.value = _uiState.value.copy(allUsers = users, isLoading = false) }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onFilterChange(filter: UserFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
    }

    fun approve(uid: String) = updateStatus(uid, UserStatus.APPROVED, "User approved.")
    fun reject(uid: String) = updateStatus(uid, UserStatus.REJECTED, "User rejected.")
    fun suspendUser(uid: String) = updateStatus(uid, UserStatus.SUSPENDED, "User suspended.")
    fun activate(uid: String) = updateStatus(uid, UserStatus.APPROVED, "User activated.")

    private fun updateStatus(uid: String, status: UserStatus, successMessage: String) {
        viewModelScope.launch {
            when (val result = updateUserStatusUseCase(uid, status)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(actionMessage = successMessage)
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun delete(uid: String) {
        viewModelScope.launch {
            when (val result = deleteUserUseCase(uid)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(actionMessage = "User deleted.")
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun resetPassword(email: String) {
        viewModelScope.launch {
            when (val result = resetUserPasswordUseCase(email)) {
                is AppResult.Success ->
                    _uiState.value = _uiState.value.copy(actionMessage = "Password reset email sent.")
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun consumeMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, actionMessage = null)
    }
}
