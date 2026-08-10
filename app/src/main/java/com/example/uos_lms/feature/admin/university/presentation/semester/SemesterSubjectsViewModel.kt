package com.example.uos_lms.feature.admin.university.presentation.semester

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserStatus
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.feature.admin.domain.model.UserFilter
import com.example.uos_lms.feature.admin.domain.model.UserSortOption
import com.example.uos_lms.feature.admin.domain.model.sortedByOption
import com.example.uos_lms.feature.admin.domain.repository.AdminRepository
import com.example.uos_lms.feature.admin.domain.usecase.DeleteUserUseCase
import com.example.uos_lms.feature.admin.domain.usecase.ResetUserPasswordUseCase
import com.example.uos_lms.feature.admin.domain.usecase.UpdateUserStatusUseCase
import com.example.uos_lms.feature.hod.domain.repository.HodRepository
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

private const val PAGE_SIZE = 20

data class SemesterSubjectsUiState(
    val semester: Semester? = null,
    val subjects: List<Subject> = emptyList(),
    val teachers: List<User> = emptyList(),
    val departmentName: String = "",
    val sessionLabel: String = "",
    val students: List<User> = emptyList(),
    val isLoadingStudents: Boolean = true,
    val studentSearchQuery: String = "",
    val studentFilter: UserFilter = UserFilter.ALL,
    val studentSortOption: UserSortOption = UserSortOption.NAME_ASC,
    val visibleStudentCount: Int = PAGE_SIZE,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
) {
    val filteredStudents: List<User>
        get() = students
            .filter { studentFilter.status == null || it.status == studentFilter.status }
            .filter {
                studentSearchQuery.isBlank() ||
                    it.fullName.contains(studentSearchQuery, ignoreCase = true) ||
                    it.email.contains(studentSearchQuery, ignoreCase = true) ||
                    it.registrationNumber?.contains(studentSearchQuery, ignoreCase = true) == true ||
                    it.rollNumber?.contains(studentSearchQuery, ignoreCase = true) == true
            }
            .sortedByOption(studentSortOption)

    val visibleStudents: List<User> get() = filteredStudents.take(visibleStudentCount)
    val hasMoreStudents: Boolean get() = filteredStudents.size > visibleStudentCount
}

/**
 * Leaf of the Admin Department Management hierarchy: Semester -> (Students + Subjects),
 * scoped to the (departmentId, sessionId, semesterId) selected via DepartmentDetail ->
 * SessionSemesterList -> here. Subjects stay Department+Semester scoped as before (no
 * session field) — sessionId is only used to scope the new Students section.
 */
@HiltViewModel
class SemesterSubjectsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: UniversityRepository,
    private val hodRepository: HodRepository,
    private val adminRepository: AdminRepository,
    private val updateUserStatusUseCase: UpdateUserStatusUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val resetUserPasswordUseCase: ResetUserPasswordUseCase,
) : ViewModel() {

    private val route: Routes.SemesterSubjects = savedStateHandle.toRoute()
    val departmentId: String get() = route.departmentId
    val semesterId: String get() = route.semesterId
    val sessionId: String get() = route.sessionId

    private val _uiState = MutableStateFlow(SemesterSubjectsUiState())
    val uiState: StateFlow<SemesterSubjectsUiState> = _uiState.asStateFlow()

    init {
        combine(
            repository.observeSemesters(departmentId),
            repository.observeSubjects(departmentId, semesterId),
            hodRepository.observeTeachersInDepartment(departmentId),
        ) { semesters, subjects, teachers ->
            Triple(semesters.find { it.id == semesterId }, subjects.sortedBy { it.code }, teachers)
        }
            .onEach { (semester, subjects, teachers) ->
                _uiState.value = _uiState.value.copy(
                    semester = semester,
                    subjects = subjects,
                    teachers = teachers,
                    isLoading = false,
                )
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)

        repository.observeDepartments()
            .onEach { departments ->
                _uiState.value = _uiState.value.copy(departmentName = departments.find { it.id == departmentId }?.name.orEmpty())
            }
            .launchIn(viewModelScope)

        repository.observeSessionsForDepartment(departmentId)
            .onEach { sessions ->
                _uiState.value = _uiState.value.copy(sessionLabel = sessions.find { it.id == sessionId }?.label.orEmpty())
            }
            .launchIn(viewModelScope)

        adminRepository.observeStudentsInSession(departmentId, sessionId, semesterId)
            .onEach { students -> _uiState.value = _uiState.value.copy(students = students, isLoadingStudents = false) }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoadingStudents = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    suspend fun createSubject(code: String, title: String, creditHours: Int): AppResult<Unit> =
        repository.createSubject(departmentId, semesterId, code, title, creditHours)

    suspend fun updateSubject(id: String, code: String, title: String, creditHours: Int): AppResult<Unit> =
        repository.updateSubject(id, departmentId, semesterId, code, title, creditHours)

    fun deleteSubject(id: String) {
        viewModelScope.launch {
            when (val result = repository.deleteSubject(id)) {
                is AppResult.Success -> {}
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun assignTeacher(subjectId: String, teacher: User) {
        viewModelScope.launch {
            when (val result = repository.assignTeacherToSubject(subjectId, teacher.uid, teacher.fullName)) {
                is AppResult.Success -> {}
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun unassignTeacher(subjectId: String) {
        viewModelScope.launch {
            when (val result = repository.assignTeacherToSubject(subjectId, null, null)) {
                is AppResult.Success -> {}
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun onStudentSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(studentSearchQuery = query, visibleStudentCount = PAGE_SIZE)
    }

    fun onStudentFilterChange(filter: UserFilter) {
        _uiState.value = _uiState.value.copy(studentFilter = filter, visibleStudentCount = PAGE_SIZE)
    }

    fun onStudentSortChange(sort: UserSortOption) {
        _uiState.value = _uiState.value.copy(studentSortOption = sort)
    }

    fun onStudentLoadMore() {
        _uiState.value = _uiState.value.copy(visibleStudentCount = _uiState.value.visibleStudentCount + PAGE_SIZE)
    }

    fun approveStudent(uid: String) = updateStudentStatus(uid, UserStatus.APPROVED, "Student approved.")
    fun rejectStudent(uid: String) = updateStudentStatus(uid, UserStatus.REJECTED, "Student rejected.")
    fun suspendStudent(uid: String) = updateStudentStatus(uid, UserStatus.SUSPENDED, "Student suspended.")
    fun activateStudent(uid: String) = updateStudentStatus(uid, UserStatus.APPROVED, "Student activated.")

    private fun updateStudentStatus(uid: String, status: UserStatus, successMessage: String) {
        viewModelScope.launch {
            when (val result = updateUserStatusUseCase(uid, status)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(actionMessage = successMessage)
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun deleteStudent(uid: String) {
        viewModelScope.launch {
            when (val result = deleteUserUseCase(uid)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(actionMessage = "Student deleted.")
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun resetStudentPassword(email: String) {
        viewModelScope.launch {
            when (val result = resetUserPasswordUseCase(email)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(actionMessage = "Password reset email sent.")
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun consumeActionMessage() {
        _uiState.value = _uiState.value.copy(actionMessage = null)
    }
}
