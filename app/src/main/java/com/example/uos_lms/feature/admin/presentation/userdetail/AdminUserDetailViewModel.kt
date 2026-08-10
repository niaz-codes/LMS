package com.example.uos_lms.feature.admin.presentation.userdetail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.core.domain.model.Session
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.domain.model.UserStatus
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.feature.admin.domain.usecase.AssignDepartmentUseCase
import com.example.uos_lms.feature.admin.domain.usecase.AssignDepartmentsUseCase
import com.example.uos_lms.feature.admin.domain.usecase.AssignSemesterUseCase
import com.example.uos_lms.feature.admin.domain.usecase.AssignSessionUseCase
import com.example.uos_lms.feature.admin.domain.usecase.DeleteUserUseCase
import com.example.uos_lms.feature.admin.domain.usecase.FindHodForDepartmentUseCase
import com.example.uos_lms.feature.admin.domain.usecase.GetUserDetailUseCase
import com.example.uos_lms.feature.admin.domain.usecase.ResetUserPasswordUseCase
import com.example.uos_lms.feature.admin.domain.usecase.UpdateUserIdentifiersUseCase
import com.example.uos_lms.feature.admin.domain.usecase.UpdateUserProfileUseCase
import com.example.uos_lms.feature.admin.domain.usecase.UpdateUserRoleUseCase
import com.example.uos_lms.feature.admin.domain.usecase.UpdateUserStatusUseCase
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HodConflict(
    val departmentId: String,
    val departmentName: String,
    val existingHodUid: String,
    val existingHodName: String,
)

data class AdminUserDetailUiState(
    val user: User? = null,
    val departments: List<Department> = emptyList(),
    val semesters: List<Semester> = emptyList(),
    val sessions: List<Session> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
    val hodConflict: HodConflict? = null,
)

@HiltViewModel
class AdminUserDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getUserDetailUseCase: GetUserDetailUseCase,
    private val updateUserStatusUseCase: UpdateUserStatusUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val resetUserPasswordUseCase: ResetUserPasswordUseCase,
    private val assignDepartmentUseCase: AssignDepartmentUseCase,
    private val assignDepartmentsUseCase: AssignDepartmentsUseCase,
    private val findHodForDepartmentUseCase: FindHodForDepartmentUseCase,
    private val assignSemesterUseCase: AssignSemesterUseCase,
    private val assignSessionUseCase: AssignSessionUseCase,
    private val updateUserProfileUseCase: UpdateUserProfileUseCase,
    private val updateUserIdentifiersUseCase: UpdateUserIdentifiersUseCase,
    private val updateUserRoleUseCase: UpdateUserRoleUseCase,
    private val universityRepository: UniversityRepository,
) : ViewModel() {

    private val route: Routes.AdminUserDetail = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(AdminUserDetailUiState())
    val uiState: StateFlow<AdminUserDetailUiState> = _uiState.asStateFlow()

    private val _deleted = Channel<Unit>()
    val deleted = _deleted.receiveAsFlow()

    private var semestersJob: Job? = null
    private var sessionsJob: Job? = null

    init {
        loadUser()
        universityRepository.observeDepartments()
            .onEach { departments -> _uiState.value = _uiState.value.copy(departments = departments) }
            .launchIn(viewModelScope)
    }

    private fun loadUser() {
        viewModelScope.launch {
            when (val result = getUserDetailUseCase(route.uid)) {
                is AppResult.Success -> {
                    val user = result.data
                    _uiState.value = _uiState.value.copy(user = user, isLoading = false)
                    observeSemestersForDepartment(user.department)
                    observeSessionsForDepartment(user.department)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    private fun observeSemestersForDepartment(departmentId: String?) {
        semestersJob?.cancel()
        if (departmentId == null) {
            _uiState.value = _uiState.value.copy(semesters = emptyList())
            return
        }
        semestersJob = universityRepository.observeSemesters(departmentId)
            .onEach { semesters -> _uiState.value = _uiState.value.copy(semesters = semesters) }
            .launchIn(viewModelScope)
    }

    private fun observeSessionsForDepartment(departmentId: String?) {
        sessionsJob?.cancel()
        if (departmentId == null) {
            _uiState.value = _uiState.value.copy(sessions = emptyList())
            return
        }
        sessionsJob = universityRepository.observeSessionsForDepartment(departmentId)
            .onEach { sessions -> _uiState.value = _uiState.value.copy(sessions = sessions) }
            .launchIn(viewModelScope)
    }

    private fun updateStatus(newStatus: UserStatus, successMessage: String) {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            when (val result = updateUserStatusUseCase(uid, newStatus)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionMessage = successMessage)
                    loadUser()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun approve() = updateStatus(UserStatus.APPROVED, "User approved.")
    fun reject() = updateStatus(UserStatus.REJECTED, "User rejected.")
    fun suspendUser() = updateStatus(UserStatus.SUSPENDED, "User suspended.")
    fun activate() = updateStatus(UserStatus.APPROVED, "User activated.")

    fun resetPassword() {
        val email = _uiState.value.user?.email ?: return
        viewModelScope.launch {
            when (val result = resetUserPasswordUseCase(email)) {
                is AppResult.Success ->
                    _uiState.value = _uiState.value.copy(actionMessage = "Password reset email sent.")
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun delete() {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            when (val result = deleteUserUseCase(uid)) {
                is AppResult.Success -> _deleted.send(Unit)
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun requestAssignDepartment(department: Department) {
        val user = _uiState.value.user ?: return
        if (user.role != UserRole.HOD) {
            applyDepartment(department.id)
            return
        }
        viewModelScope.launch {
            when (val result = findHodForDepartmentUseCase(department.id)) {
                is AppResult.Success -> {
                    val existingHod = result.data
                    if (existingHod != null && existingHod.uid != user.uid) {
                        _uiState.value = _uiState.value.copy(
                            hodConflict = HodConflict(
                                departmentId = department.id,
                                departmentName = department.name,
                                existingHodUid = existingHod.uid,
                                existingHodName = existingHod.fullName,
                            ),
                        )
                    } else {
                        applyDepartment(department.id)
                    }
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun confirmHodReassignment() {
        val conflict = _uiState.value.hodConflict ?: return
        _uiState.value = _uiState.value.copy(hodConflict = null)
        viewModelScope.launch {
            assignDepartmentUseCase(conflict.existingHodUid, null)
            applyDepartment(conflict.departmentId)
        }
    }

    fun cancelHodConflict() {
        _uiState.value = _uiState.value.copy(hodConflict = null)
    }

    fun unassignDepartment() = applyDepartment(null)

    private fun applyDepartment(departmentId: String?) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            when (val result = assignDepartmentUseCase(user.uid, departmentId)) {
                is AppResult.Success -> {
                    // A Student's semester belongs to their old department; a
                    // department change invalidates it rather than leaving a
                    // stale cross-department reference.
                    if (user.role == UserRole.STUDENT && user.semester != null && departmentId != user.department) {
                        assignSemesterUseCase(user.uid, null)
                    }
                    _uiState.value = _uiState.value.copy(actionMessage = "Department updated.")
                    loadUser()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun assignDepartments(departmentIds: List<String>) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            when (val result = assignDepartmentsUseCase(user.uid, departmentIds)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionMessage = "Departments updated.")
                    loadUser()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun unassignAllDepartments() = assignDepartments(emptyList())

    fun assignSemester(semester: Semester) {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            when (val result = assignSemesterUseCase(uid, semester.id)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionMessage = "Semester updated.")
                    loadUser()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun unassignSemester() {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            when (val result = assignSemesterUseCase(uid, null)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionMessage = "Semester unassigned.")
                    loadUser()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun updateProfile(fullName: String, fatherName: String, phone: String, cnic: String) {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            when (val result = updateUserProfileUseCase(uid, fullName, fatherName, phone, cnic)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionMessage = "Profile updated.")
                    loadUser()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun updateIdentifiers(
        employeeId: String?,
        registrationNumber: String?,
        rollNumber: String?,
        designation: String?,
    ) {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            when (
                val result =
                    updateUserIdentifiersUseCase(uid, employeeId, registrationNumber, rollNumber, designation)
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionMessage = "Profile updated.")
                    loadUser()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun assignSession(session: Session) {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            when (val result = assignSessionUseCase(uid, session.id)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionMessage = "Session updated.")
                    loadUser()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun unassignSession() {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            when (val result = assignSessionUseCase(uid, null)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionMessage = "Session unassigned.")
                    loadUser()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun changeRole(role: UserRole) {
        val uid = _uiState.value.user?.uid ?: return
        viewModelScope.launch {
            when (val result = updateUserRoleUseCase(uid, role)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionMessage = "Role updated.")
                    loadUser()
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }
}
