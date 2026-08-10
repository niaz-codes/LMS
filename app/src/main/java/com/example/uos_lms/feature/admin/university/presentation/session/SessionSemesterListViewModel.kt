package com.example.uos_lms.feature.admin.university.presentation.session

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SessionSemesterListUiState(
    val departmentName: String = "",
    val sessionLabel: String = "",
    val semesters: List<Semester> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

/**
 * Middle of the Admin Department Management hierarchy: Session -> Semester, scoped to
 * the (departmentId, sessionId) selected on [com.example.uos_lms.feature.admin.university.presentation.department.DepartmentDetailScreen].
 * Semesters themselves aren't session-scoped in the data model (a department's semester
 * list is the same regardless of session) — this screen just presents that same list in
 * the navigational context of the chosen session, matching where "Add Subject" leads next.
 */
@HiltViewModel
class SessionSemesterListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: UniversityRepository,
) : ViewModel() {

    private val route: Routes.DepartmentSessionSemesters = savedStateHandle.toRoute()
    val departmentId: String get() = route.departmentId
    val sessionId: String get() = route.sessionId

    private val _uiState = MutableStateFlow(SessionSemesterListUiState())
    val uiState: StateFlow<SessionSemesterListUiState> = _uiState.asStateFlow()

    init {
        combine(
            repository.observeDepartments(),
            repository.observeSessionsForDepartment(departmentId),
            repository.observeSemesters(departmentId),
        ) { departments, sessions, semesters ->
            Triple(
                departments.find { it.id == departmentId }?.name.orEmpty(),
                sessions.find { it.id == sessionId }?.label.orEmpty(),
                semesters.sortedBy { it.number },
            )
        }
            .onEach { (departmentName, sessionLabel, semesters) ->
                _uiState.value = _uiState.value.copy(
                    departmentName = departmentName,
                    sessionLabel = sessionLabel,
                    semesters = semesters,
                    isLoading = false,
                )
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    suspend fun addSemester(number: Int): AppResult<Unit> =
        repository.createSemester(departmentId, number)

    fun deleteSemester(id: String) {
        viewModelScope.launch {
            when (val result = repository.deleteSemester(id)) {
                is AppResult.Success -> {}
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
