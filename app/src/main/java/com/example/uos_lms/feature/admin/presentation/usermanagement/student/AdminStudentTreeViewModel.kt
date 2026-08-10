package com.example.uos_lms.feature.admin.presentation.usermanagement.student

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.domain.model.Session
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Top level of the Student Management hierarchy: Department -> Session. Clicking a
 * Session navigates away to [AdminStudentSemesterListViewModel] (Session -> Semester ->
 * Students) rather than expanding further inline, so a Session node here is a plain
 * leaf with no expand/student state of its own.
 */
data class SessionNode(
    val session: Session,
    val totalCount: Long? = null,
)

data class DepartmentStudentNode(
    val department: Department,
    val isExpanded: Boolean = false,
    val totalCount: Long? = null,
    val isLoadingSessions: Boolean = false,
    val sessionSearchQuery: String = "",
    val sessions: List<SessionNode> = emptyList(),
    val showAddSessionDialog: Boolean = false,
    val isSavingSession: Boolean = false,
    val addSessionError: String? = null,
) {
    val filteredSessions: List<SessionNode>
        get() = sessions.filter {
            sessionSearchQuery.isBlank() || it.session.label.contains(sessionSearchQuery, ignoreCase = true)
        }
}

data class AdminStudentTreeUiState(
    val departmentSearchQuery: String = "",
    val nodes: List<DepartmentStudentNode> = emptyList(),
    val isLoadingDepartments: Boolean = true,
    val totalStudents: Long? = null,
    val totalSemesters: Long? = null,
    val totalSessions: Long? = null,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
) {
    val filteredNodes: List<DepartmentStudentNode>
        get() = nodes.filter {
            departmentSearchQuery.isBlank() ||
                it.department.name.contains(departmentSearchQuery, ignoreCase = true) ||
                it.department.code.contains(departmentSearchQuery, ignoreCase = true)
        }
}

@HiltViewModel
class AdminStudentTreeViewModel @Inject constructor(
    private val universityRepository: UniversityRepository,
    private val adminRepository: AdminRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminStudentTreeUiState())
    val uiState: StateFlow<AdminStudentTreeUiState> = _uiState.asStateFlow()

    private val sessionsJobs = mutableMapOf<String, Job>()

    init {
        universityRepository.observeDepartments()
            .onEach { departments -> onDepartmentsLoaded(departments) }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoadingDepartments = false, errorMessage = e.message) }
            .launchIn(viewModelScope)

        loadHeaderStats()
    }

    private fun loadHeaderStats() {
        viewModelScope.launch {
            when (val result = adminRepository.countUsersByRole(UserRole.STUDENT)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(totalStudents = result.data)
                is AppResult.Error -> Unit
            }
        }
        viewModelScope.launch {
            when (val result = universityRepository.countAllSemesters()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(totalSemesters = result.data)
                is AppResult.Error -> Unit
            }
        }
        viewModelScope.launch {
            when (val result = universityRepository.countAllSessions()) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(totalSessions = result.data)
                is AppResult.Error -> Unit
            }
        }
    }

    private fun onDepartmentsLoaded(departments: List<Department>) {
        val existing = _uiState.value.nodes.associateBy { it.department.id }
        val nodes = departments.sortedBy { it.name }.map { dept ->
            existing[dept.id] ?: DepartmentStudentNode(department = dept).also { loadDepartmentCount(dept.id) }
        }
        _uiState.value = _uiState.value.copy(nodes = nodes, isLoadingDepartments = false)
    }

    private fun loadDepartmentCount(departmentId: String) {
        viewModelScope.launch {
            when (val result = adminRepository.countUsersInDepartmentByRole(departmentId, UserRole.STUDENT)) {
                is AppResult.Success -> updateDept(departmentId) { it.copy(totalCount = result.data) }
                is AppResult.Error -> Unit
            }
        }
    }

    fun onDepartmentSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(departmentSearchQuery = query)
    }

    fun toggleDepartment(departmentId: String) {
        val node = _uiState.value.nodes.find { it.department.id == departmentId } ?: return
        if (node.isExpanded) {
            sessionsJobs.remove(departmentId)?.cancel()
            updateDept(departmentId) { it.copy(isExpanded = false) }
            return
        }
        updateDept(departmentId) { it.copy(isExpanded = true, isLoadingSessions = true) }
        sessionsJobs[departmentId]?.cancel()
        sessionsJobs[departmentId] = universityRepository.observeSessionsForDepartment(departmentId)
            .onEach { sessions -> onSessionsLoaded(departmentId, sessions) }
            .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    private fun onSessionsLoaded(departmentId: String, sessions: List<Session>) {
        val newCounts = mutableListOf<String>()
        updateDept(departmentId) { dept ->
            val previousById = dept.sessions.associateBy { it.session.id }
            val merged = sessions.sortedByDescending { it.label }.map { sess ->
                val previous = previousById[sess.id]
                if (previous != null) {
                    previous.copy(session = sess)
                } else {
                    newCounts += sess.id
                    SessionNode(session = sess)
                }
            }
            dept.copy(sessions = merged, isLoadingSessions = false)
        }
        newCounts.forEach { sessionId -> loadSessionCount(departmentId, sessionId) }
    }

    private fun loadSessionCount(departmentId: String, sessionId: String) {
        viewModelScope.launch {
            when (val result = adminRepository.countStudentsInDepartmentSession(departmentId, sessionId)) {
                is AppResult.Success -> updateSession(departmentId, sessionId) { it.copy(totalCount = result.data) }
                is AppResult.Error -> Unit
            }
        }
    }

    fun onSessionSearchChange(departmentId: String, query: String) {
        updateDept(departmentId) { it.copy(sessionSearchQuery = query) }
    }

    fun showAddSessionDialog(departmentId: String) {
        updateDept(departmentId) { it.copy(showAddSessionDialog = true, addSessionError = null) }
    }

    fun dismissAddSessionDialog(departmentId: String) {
        updateDept(departmentId) { it.copy(showAddSessionDialog = false, addSessionError = null) }
    }

    fun createSession(departmentId: String, label: String) {
        viewModelScope.launch {
            updateDept(departmentId) { it.copy(isSavingSession = true, addSessionError = null) }
            when (val result = universityRepository.createSession(departmentId, label)) {
                is AppResult.Success -> {
                    updateDept(departmentId) { it.copy(isSavingSession = false, showAddSessionDialog = false) }
                    loadHeaderStats()
                }
                is AppResult.Error ->
                    updateDept(departmentId) { it.copy(isSavingSession = false, addSessionError = result.message) }
            }
        }
    }

    private inline fun updateDept(departmentId: String, transform: (DepartmentStudentNode) -> DepartmentStudentNode) {
        _uiState.value = _uiState.value.copy(
            nodes = _uiState.value.nodes.map { if (it.department.id == departmentId) transform(it) else it },
        )
    }

    private inline fun updateSession(
        departmentId: String,
        sessionId: String,
        transform: (SessionNode) -> SessionNode,
    ) {
        updateDept(departmentId) { dept ->
            dept.copy(
                sessions = dept.sessions.map { if (it.session.id == sessionId) transform(it) else it },
            )
        }
    }

    fun consumeMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, actionMessage = null)
    }
}
