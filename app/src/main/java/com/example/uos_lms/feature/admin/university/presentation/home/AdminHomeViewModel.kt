package com.example.uos_lms.feature.admin.university.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.domain.model.UserStatus
import com.example.uos_lms.feature.admin.domain.usecase.ObserveUsersUseCase
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminHomeUiState(
    val totalUsers: Int = 0,
    val pendingApprovals: Int = 0,
    val totalDepartments: Int = 0,
    val totalStudents: Int = 0,
    val totalTeachers: Int = 0,
    val isLoading: Boolean = true,
)

@HiltViewModel
class AdminHomeViewModel @Inject constructor(
    observeUsersUseCase: ObserveUsersUseCase,
    universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminHomeUiState())
    val uiState: StateFlow<AdminHomeUiState> = _uiState.asStateFlow()

    init {
        combine(observeUsersUseCase(), universityRepository.observeDepartments()) { users, departments ->
            AdminHomeUiState(
                totalUsers = users.size,
                pendingApprovals = users.count { it.status == UserStatus.PENDING },
                totalDepartments = departments.size,
                totalStudents = users.count { it.role == UserRole.STUDENT },
                totalTeachers = users.count { it.role == UserRole.TEACHER },
                isLoading = false,
            )
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }
}
