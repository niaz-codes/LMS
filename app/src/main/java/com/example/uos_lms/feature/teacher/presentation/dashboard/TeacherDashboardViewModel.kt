package com.example.uos_lms.feature.teacher.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.uos_lms.feature.auth.domain.usecase.LogoutUseCase
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

data class AssignedSubject(
    val subject: Subject,
    val semesterNumber: Int,
    val semesterLabel: String,
)

data class TeacherDashboardUiState(
    val fullName: String = "",
    val departmentId: String? = null,
    val departmentName: String = "",
    val subjects: List<AssignedSubject> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class TeacherDashboardViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManager,
    private val universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherDashboardUiState())
    val uiState: StateFlow<TeacherDashboardUiState> = _uiState.asStateFlow()

    init {
        loadTeacher()
    }

    private fun loadTeacher() {
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
                    _uiState.value = _uiState.value.copy(fullName = user.fullName, departmentId = user.departmentIds.firstOrNull())
                    val departmentIds = user.departmentIds
                    if (departmentIds.isEmpty()) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@launch
                    }
                    observeAssignedSubjects(user.uid, departmentIds)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    // A teacher can be assigned to several departments; each assigned subject
    // carries its own departmentId, so semesters are resolved per-subject's
    // department rather than assuming a single teacher-wide department.
    private fun observeAssignedSubjects(uid: String, departmentIds: List<String>) {
        universityRepository.observeDepartments()
            .onEach { departments ->
                val names = departmentIds.mapNotNull { id -> departments.find { it.id == id }?.name }
                _uiState.value = _uiState.value.copy(departmentName = names.joinToString(", "))
            }
            .launchIn(viewModelScope)

        combine(
            universityRepository.observeSubjectsForTeacher(uid),
            combine(departmentIds.map { universityRepository.observeSemesters(it) }) { it.toList().flatten() },
        ) { subjects, semesters ->
            subjects
                .map { subject ->
                    val semester = semesters.find { it.id == subject.semesterId }
                    AssignedSubject(
                        subject = subject,
                        semesterNumber = semester?.number ?: 0,
                        semesterLabel = semester?.displayName ?: "Semester —",
                    )
                }
                .sortedWith(compareBy({ it.semesterNumber }, { it.subject.code }))
        }
            .onEach { assigned ->
                _uiState.value = _uiState.value.copy(subjects = assigned, isLoading = false)
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
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
