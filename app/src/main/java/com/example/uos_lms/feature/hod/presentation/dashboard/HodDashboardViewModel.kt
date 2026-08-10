package com.example.uos_lms.feature.hod.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.uos_lms.feature.auth.domain.usecase.LogoutUseCase
import com.example.uos_lms.feature.hod.domain.repository.HodRepository
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HodDashboardUiState(
    val fullName: String = "",
    val departmentId: String? = null,
    val departmentName: String = "",
    val semesters: List<Semester> = emptyList(),
    val teacherCount: Int = 0,
    val studentCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class HodDashboardViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManager,
    private val universityRepository: UniversityRepository,
    private val hodRepository: HodRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HodDashboardUiState())
    val uiState: StateFlow<HodDashboardUiState> = _uiState.asStateFlow()

    init {
        loadHod()
    }

    private fun loadHod() {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val user = result.data
                    if (user == null) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Session expired. Please log in again.",
                        )
                        return@launch
                    }
                    _uiState.value = _uiState.value.copy(fullName = user.fullName, departmentId = user.department)
                    val departmentId = user.department
                    if (departmentId == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@launch
                    }
                    observeDepartmentData(departmentId)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    private fun observeDepartmentData(departmentId: String) {
        universityRepository.observeDepartments()
            .onEach { departments ->
                val name = departments.find { it.id == departmentId }?.name.orEmpty()
                _uiState.value = _uiState.value.copy(departmentName = name)
            }
            .launchIn(viewModelScope)

        universityRepository.observeSemesters(departmentId)
            .onEach { semesters ->
                _uiState.value = _uiState.value.copy(
                    semesters = semesters.sortedBy { it.number },
                    isLoading = false,
                )
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)

        hodRepository.observeTeachersInDepartment(departmentId)
            .onEach { teachers -> _uiState.value = _uiState.value.copy(teacherCount = teachers.size) }
            .launchIn(viewModelScope)

        hodRepository.observeUsersInDepartment(departmentId)
            .onEach { users ->
                _uiState.value = _uiState.value.copy(studentCount = users.count { it.role == UserRole.STUDENT })
            }
            .launchIn(viewModelScope)
    }

    fun logout(onComplete: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            sessionManager.clear()
            onComplete()
        }
    }
}
