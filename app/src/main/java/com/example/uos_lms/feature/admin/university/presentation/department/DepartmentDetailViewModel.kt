package com.example.uos_lms.feature.admin.university.presentation.department

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.domain.model.Session
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
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

/** Set once a delete is requested — carries how many students are currently linked so the
 * screen knows whether to show a plain confirm or the reassign-before-delete flow. */
data class SessionDeleteCheck(
    val session: Session,
    val linkedStudentCount: Long,
)

data class DepartmentDetailUiState(
    val department: Department? = null,
    val sessions: List<Session> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val updatingSessionIds: Set<String> = emptySet(),
    val sessionDeleteCheck: SessionDeleteCheck? = null,
    val isCheckingSessionDelete: Boolean = false,
)

@HiltViewModel
class DepartmentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: UniversityRepository,
    private val adminRepository: AdminRepository,
) : ViewModel() {

    private val route: Routes.DepartmentDetail = savedStateHandle.toRoute()
    val departmentId: String get() = route.departmentId

    private val _uiState = MutableStateFlow(DepartmentDetailUiState())
    val uiState: StateFlow<DepartmentDetailUiState> = _uiState.asStateFlow()

    init {
        combine(
            repository.observeDepartments(),
            repository.observeSessionsForDepartment(departmentId),
        ) { departments, sessions ->
            departments.find { it.id == departmentId } to sessions.sortedByDescending { it.label }
        }
            .onEach { (department, sessions) ->
                val currentState = _uiState.value
                val displayedSessions = sessions.map { persistedSession ->
                    currentState.sessions.find { it.id == persistedSession.id }
                        ?.takeIf { it.id in currentState.updatingSessionIds }
                        ?: persistedSession
                }
                _uiState.value = currentState.copy(
                    department = department,
                    sessions = displayedSessions,
                    isLoading = false,
                )
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    suspend fun updateDepartment(name: String, code: String, description: String): AppResult<Unit> =
        repository.updateDepartment(departmentId, name, code, description)

    fun deleteDepartment(onDeleted: () -> Unit) {
        viewModelScope.launch {
            when (val result = repository.deleteDepartment(departmentId)) {
                is AppResult.Success -> onDeleted()
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    suspend fun addSession(label: String): AppResult<Unit> =
        repository.createSession(departmentId, label)

    suspend fun editSessionLabel(id: String, label: String): AppResult<Unit> =
        repository.updateSessionLabel(id, departmentId, label)

    fun setSessionActive(session: Session, isActive: Boolean) {
        if (session.id in _uiState.value.updatingSessionIds || session.isActive == isActive) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.let { state ->
                state.copy(
                    sessions = state.sessions.map { if (it.id == session.id) it.copy(isActive = isActive) else it },
                    updatingSessionIds = state.updatingSessionIds + session.id,
                    errorMessage = null,
                )
            }
            when (val result = repository.setSessionActive(session.id, isActive)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    updatingSessionIds = _uiState.value.updatingSessionIds - session.id,
                )
                is AppResult.Error -> _uiState.value = _uiState.value.let { state ->
                    state.copy(
                        sessions = state.sessions.map { if (it.id == session.id) session else it },
                        updatingSessionIds = state.updatingSessionIds - session.id,
                        errorMessage = result.message,
                    )
                }
            }
        }
    }

    /** Step 1 of delete: find out how many students are linked before deciding which dialog to show. */
    fun requestDeleteSession(session: Session) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCheckingSessionDelete = true)
            when (val result = adminRepository.countStudentsBySessionId(session.id)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(
                    isCheckingSessionDelete = false,
                    sessionDeleteCheck = SessionDeleteCheck(session, result.data),
                )
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    isCheckingSessionDelete = false,
                    errorMessage = result.message,
                )
            }
        }
    }

    fun cancelSessionDelete() {
        _uiState.value = _uiState.value.copy(sessionDeleteCheck = null)
    }

    /** No students linked — safe to delete outright. */
    fun deleteSession(id: String) {
        viewModelScope.launch {
            when (val result = repository.deleteSession(id)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(sessionDeleteCheck = null)
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    sessionDeleteCheck = null,
                    errorMessage = result.message,
                )
            }
        }
    }

    /** Students were linked — move them all to [toSessionId] first, then delete [fromSessionId]. */
    fun reassignAndDeleteSession(fromSessionId: String, toSessionId: String) {
        viewModelScope.launch {
            when (val reassignResult = adminRepository.reassignSessionStudents(fromSessionId, toSessionId)) {
                is AppResult.Success -> {
                    when (val deleteResult = repository.deleteSession(fromSessionId)) {
                        is AppResult.Success -> _uiState.value = _uiState.value.copy(sessionDeleteCheck = null)
                        is AppResult.Error -> _uiState.value = _uiState.value.copy(
                            sessionDeleteCheck = null,
                            errorMessage = deleteResult.message,
                        )
                    }
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(
                    sessionDeleteCheck = null,
                    errorMessage = reassignResult.message,
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
