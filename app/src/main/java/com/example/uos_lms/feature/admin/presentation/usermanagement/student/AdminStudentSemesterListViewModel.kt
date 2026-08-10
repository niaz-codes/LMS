package com.example.uos_lms.feature.admin.presentation.usermanagement.student

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Semester
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

private const val PAGE_SIZE = 20

data class SemesterStudentsNode(
    val semester: Semester,
    val isExpanded: Boolean = false,
    val totalCount: Long? = null,
    val students: List<User> = emptyList(),
    val isLoadingStudents: Boolean = false,
    val searchQuery: String = "",
    val filter: UserFilter = UserFilter.ALL,
    val sortOption: UserSortOption = UserSortOption.NAME_ASC,
    val visibleCount: Int = PAGE_SIZE,
) {
    val filteredStudents: List<User>
        get() = students
            .filter { filter.status == null || it.status == filter.status }
            .filter {
                searchQuery.isBlank() ||
                    it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true) ||
                    it.registrationNumber?.contains(searchQuery, ignoreCase = true) == true ||
                    it.rollNumber?.contains(searchQuery, ignoreCase = true) == true
            }
            .sortedByOption(sortOption)

    val visibleStudents: List<User> get() = filteredStudents.take(visibleCount)
    val hasMore: Boolean get() = filteredStudents.size > visibleCount
}

data class AdminStudentSemesterListUiState(
    val departmentName: String = "",
    val sessionLabel: String = "",
    val semesterSearchQuery: String = "",
    val semesters: List<SemesterStudentsNode> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
) {
    val filteredSemesters: List<SemesterStudentsNode>
        get() = semesters.filter {
            semesterSearchQuery.isBlank() || it.semester.displayName.contains(semesterSearchQuery, ignoreCase = true)
        }
}

/**
 * Bottom of the Student Management hierarchy: Session -> Semester -> Students, scoped
 * to the (departmentId, sessionId) the admin drilled into from [AdminStudentTreeScreen].
 */
@HiltViewModel
class AdminStudentSemesterListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val universityRepository: UniversityRepository,
    private val adminRepository: AdminRepository,
    private val updateUserStatusUseCase: UpdateUserStatusUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val resetUserPasswordUseCase: ResetUserPasswordUseCase,
) : ViewModel() {

    private val route: Routes.AdminStudentSemesterList = savedStateHandle.toRoute()
    private val departmentId get() = route.departmentId
    private val sessionId get() = route.sessionId

    private val _uiState = MutableStateFlow(AdminStudentSemesterListUiState())
    val uiState: StateFlow<AdminStudentSemesterListUiState> = _uiState.asStateFlow()

    private val studentJobs = mutableMapOf<String, Job>()

    init {
        universityRepository.observeDepartments()
            .onEach { departments ->
                val name = departments.find { it.id == departmentId }?.name.orEmpty()
                _uiState.value = _uiState.value.copy(departmentName = name)
            }
            .launchIn(viewModelScope)

        universityRepository.observeSessionsForDepartment(departmentId)
            .onEach { sessions ->
                val label = sessions.find { it.id == sessionId }?.label.orEmpty()
                _uiState.value = _uiState.value.copy(sessionLabel = label)
            }
            .launchIn(viewModelScope)

        universityRepository.observeSemesters(departmentId)
            .onEach { semesters -> onSemestersLoaded(semesters) }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    private fun onSemestersLoaded(semesters: List<Semester>) {
        val newCounts = mutableListOf<String>()
        val previousById = _uiState.value.semesters.associateBy { it.semester.id }
        val merged = semesters.sortedBy { it.number }.map { sem ->
            val previous = previousById[sem.id]
            if (previous != null) {
                previous.copy(semester = sem)
            } else {
                newCounts += sem.id
                SemesterStudentsNode(semester = sem)
            }
        }
        _uiState.value = _uiState.value.copy(semesters = merged, isLoading = false)
        newCounts.forEach { semesterId -> loadSemesterCount(semesterId) }
    }

    private fun loadSemesterCount(semesterId: String) {
        viewModelScope.launch {
            when (val result = adminRepository.countStudentsInSession(departmentId, sessionId, semesterId)) {
                is AppResult.Success -> updateSemester(semesterId) { it.copy(totalCount = result.data) }
                is AppResult.Error -> Unit
            }
        }
    }

    fun onSemesterSearchChange(query: String) {
        _uiState.value = _uiState.value.copy(semesterSearchQuery = query)
    }

    fun toggleSemester(semesterId: String) {
        val node = _uiState.value.semesters.find { it.semester.id == semesterId } ?: return
        if (node.isExpanded) {
            studentJobs.remove(semesterId)?.cancel()
            updateSemester(semesterId) { it.copy(isExpanded = false) }
            return
        }
        updateSemester(semesterId) { it.copy(isExpanded = true, isLoadingStudents = true) }
        studentJobs[semesterId]?.cancel()
        studentJobs[semesterId] = adminRepository.observeStudentsInSession(departmentId, sessionId, semesterId)
            .onEach { students -> updateSemester(semesterId) { it.copy(students = students, isLoadingStudents = false) } }
            .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    fun onStudentSearchChange(semesterId: String, query: String) {
        updateSemester(semesterId) { it.copy(searchQuery = query, visibleCount = PAGE_SIZE) }
    }

    fun onStudentFilterChange(semesterId: String, filter: UserFilter) {
        updateSemester(semesterId) { it.copy(filter = filter, visibleCount = PAGE_SIZE) }
    }

    fun onStudentSortChange(semesterId: String, sort: UserSortOption) {
        updateSemester(semesterId) { it.copy(sortOption = sort) }
    }

    fun onStudentLoadMore(semesterId: String) {
        updateSemester(semesterId) { it.copy(visibleCount = it.visibleCount + PAGE_SIZE) }
    }

    private inline fun updateSemester(semesterId: String, transform: (SemesterStudentsNode) -> SemesterStudentsNode) {
        _uiState.value = _uiState.value.copy(
            semesters = _uiState.value.semesters.map { if (it.semester.id == semesterId) transform(it) else it },
        )
    }

    fun approve(uid: String) = updateStatus(uid, UserStatus.APPROVED, "Student approved.")
    fun reject(uid: String) = updateStatus(uid, UserStatus.REJECTED, "Student rejected.")
    fun suspend(uid: String) = updateStatus(uid, UserStatus.SUSPENDED, "Student suspended.")
    fun activate(uid: String) = updateStatus(uid, UserStatus.APPROVED, "Student activated.")

    private fun updateStatus(uid: String, status: UserStatus, successMessage: String) {
        viewModelScope.launch {
            when (val result = updateUserStatusUseCase(uid, status)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(actionMessage = successMessage)
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun delete(uid: String, semesterId: String) {
        viewModelScope.launch {
            when (val result = deleteUserUseCase(uid)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionMessage = "Student deleted.")
                    loadSemesterCount(semesterId)
                }
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
