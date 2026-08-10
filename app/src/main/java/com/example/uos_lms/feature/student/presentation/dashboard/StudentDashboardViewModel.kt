package com.example.uos_lms.feature.student.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.AttendanceStatus
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.assignment.domain.repository.AssignmentRepository
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.uos_lms.feature.auth.domain.usecase.LogoutUseCase
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentDashboardUiState(
    val fullName: String = "",
    val departmentId: String? = null,
    val departmentName: String = "",
    val semesterId: String? = null,
    val semesterLabel: String = "",
    val subjects: List<Subject> = emptyList(),
    val attendancePercentage: Int = 0,
    val pendingAssignments: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudentDashboardViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManager,
    private val universityRepository: UniversityRepository,
    private val attendanceRepository: AttendanceRepository,
    private val assignmentRepository: AssignmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentDashboardUiState())
    val uiState: StateFlow<StudentDashboardUiState> = _uiState.asStateFlow()

    init {
        loadStudent()
    }

    private fun loadStudent() {
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
                    _uiState.value = _uiState.value.copy(
                        fullName = user.fullName,
                        departmentId = user.department,
                        semesterId = user.semester,
                    )
                    val departmentId = user.department
                    val semesterId = user.semester
                    if (departmentId == null || semesterId == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@launch
                    }
                    observeSubjects(departmentId, semesterId)
                    observeOverview(departmentId, semesterId, user.uid)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    private fun observeSubjects(departmentId: String, semesterId: String) {
        universityRepository.observeDepartments()
            .onEach { departments ->
                val name = departments.find { it.id == departmentId }?.name.orEmpty()
                _uiState.value = _uiState.value.copy(departmentName = name)
            }
            .launchIn(viewModelScope)

        universityRepository.observeSemesters(departmentId)
            .onEach { semesters ->
                val label = semesters.find { it.id == semesterId }?.displayName.orEmpty()
                _uiState.value = _uiState.value.copy(semesterLabel = label)
            }
            .launchIn(viewModelScope)

        universityRepository.observeSubjects(departmentId, semesterId)
            .onEach { subjects ->
                _uiState.value = _uiState.value.copy(
                    subjects = subjects.sortedBy { it.code },
                    isLoading = false,
                )
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    private fun observeOverview(departmentId: String, semesterId: String, uid: String) {
        universityRepository.observeSubjects(departmentId, semesterId)
            .flatMapLatest { subjects ->
                if (subjects.isEmpty()) {
                    flowOf(0 to 0)
                } else {
                    combine(
                        combine(
                            subjects.map { attendanceRepository.observeStudentAttendanceForSubject(it.id, uid) },
                        ) { lists -> lists.toList().flatten() },
                        combine(
                            subjects.map { assignmentRepository.observeAssignmentsForSubject(it.id) },
                        ) { lists -> lists.toList().flatten() },
                        assignmentRepository.observeSubmissionsForStudent(uid),
                    ) { records, assignments, submissions ->
                        val present = records.count { it.status == AttendanceStatus.PRESENT }
                        val percentage = if (records.isEmpty()) 0 else (present * 100) / records.size
                        val submittedIds = submissions.map { it.assignmentId }.toSet()
                        val pending = assignments.count { it.id !in submittedIds }
                        percentage to pending
                    }
                }
            }
            .onEach { (percentage, pending) ->
                _uiState.value = _uiState.value.copy(attendancePercentage = percentage, pendingAssignments = pending)
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
