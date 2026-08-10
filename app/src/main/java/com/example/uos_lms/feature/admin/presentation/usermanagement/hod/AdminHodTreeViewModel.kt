package com.example.uos_lms.feature.admin.presentation.usermanagement.hod

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.domain.model.UserStatus
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

data class DepartmentHodNode(
    val department: Department,
    val isExpanded: Boolean = false,
    val totalCount: Long? = null,
    val hods: List<User> = emptyList(),
    val isLoadingHods: Boolean = false,
    val searchQuery: String = "",
    val filter: UserFilter = UserFilter.ALL,
    val sortOption: UserSortOption = UserSortOption.NAME_ASC,
    val visibleCount: Int = PAGE_SIZE,
) {
    val filteredHods: List<User>
        get() = hods
            .filter { filter.status == null || it.status == filter.status }
            .filter {
                searchQuery.isBlank() ||
                    it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true)
            }
            .sortedByOption(sortOption)

    val visibleHods: List<User> get() = filteredHods.take(visibleCount)
    val hasMore: Boolean get() = filteredHods.size > visibleCount
}

data class AdminHodTreeUiState(
    val departmentSearchQuery: String = "",
    val nodes: List<DepartmentHodNode> = emptyList(),
    val isLoadingDepartments: Boolean = true,
    val totalHods: Long? = null,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
) {
    val filteredNodes: List<DepartmentHodNode>
        get() = nodes.filter {
            departmentSearchQuery.isBlank() ||
                it.department.name.contains(departmentSearchQuery, ignoreCase = true) ||
                it.department.code.contains(departmentSearchQuery, ignoreCase = true)
        }
}

@HiltViewModel
class AdminHodTreeViewModel @Inject constructor(
    private val universityRepository: UniversityRepository,
    private val adminRepository: AdminRepository,
    private val updateUserStatusUseCase: UpdateUserStatusUseCase,
    private val deleteUserUseCase: DeleteUserUseCase,
    private val resetUserPasswordUseCase: ResetUserPasswordUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminHodTreeUiState())
    val uiState: StateFlow<AdminHodTreeUiState> = _uiState.asStateFlow()

    private val hodJobs = mutableMapOf<String, Job>()

    init {
        universityRepository.observeDepartments()
            .onEach { departments -> onDepartmentsLoaded(departments) }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoadingDepartments = false, errorMessage = e.message) }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            when (val result = adminRepository.countUsersByRole(UserRole.HOD)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(totalHods = result.data)
                is AppResult.Error -> Unit
            }
        }
    }

    private fun onDepartmentsLoaded(departments: List<Department>) {
        val existing = _uiState.value.nodes.associateBy { it.department.id }
        val nodes = departments.sortedBy { it.name }.map { dept ->
            existing[dept.id] ?: DepartmentHodNode(department = dept).also { loadCount(dept.id) }
        }
        _uiState.value = _uiState.value.copy(nodes = nodes, isLoadingDepartments = false)
    }

    private fun loadCount(departmentId: String) {
        viewModelScope.launch {
            when (val result = adminRepository.countUsersInDepartmentByRole(departmentId, UserRole.HOD)) {
                is AppResult.Success -> updateNode(departmentId) { it.copy(totalCount = result.data) }
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
            hodJobs.remove(departmentId)?.cancel()
            updateNode(departmentId) { it.copy(isExpanded = false) }
            return
        }
        updateNode(departmentId) { it.copy(isExpanded = true, isLoadingHods = true) }
        hodJobs[departmentId]?.cancel()
        hodJobs[departmentId] = adminRepository.observeUsersInDepartmentByRole(departmentId, UserRole.HOD)
            .onEach { hods -> updateNode(departmentId) { it.copy(hods = hods, isLoadingHods = false) } }
            .catch { e -> _uiState.value = _uiState.value.copy(errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    fun onNodeSearchChange(departmentId: String, query: String) {
        updateNode(departmentId) { it.copy(searchQuery = query, visibleCount = PAGE_SIZE) }
    }

    fun onNodeFilterChange(departmentId: String, filter: UserFilter) {
        updateNode(departmentId) { it.copy(filter = filter, visibleCount = PAGE_SIZE) }
    }

    fun onNodeSortChange(departmentId: String, sort: UserSortOption) {
        updateNode(departmentId) { it.copy(sortOption = sort) }
    }

    fun onNodeLoadMore(departmentId: String) {
        updateNode(departmentId) { it.copy(visibleCount = it.visibleCount + PAGE_SIZE) }
    }

    private inline fun updateNode(departmentId: String, transform: (DepartmentHodNode) -> DepartmentHodNode) {
        _uiState.value = _uiState.value.copy(
            nodes = _uiState.value.nodes.map { if (it.department.id == departmentId) transform(it) else it },
        )
    }

    fun approve(uid: String) = updateStatus(uid, UserStatus.APPROVED, "HOD approved.")
    fun reject(uid: String) = updateStatus(uid, UserStatus.REJECTED, "HOD rejected.")
    fun suspend(uid: String) = updateStatus(uid, UserStatus.SUSPENDED, "HOD suspended.")
    fun activate(uid: String) = updateStatus(uid, UserStatus.APPROVED, "HOD activated.")

    private fun updateStatus(uid: String, status: UserStatus, successMessage: String) {
        viewModelScope.launch {
            when (val result = updateUserStatusUseCase(uid, status)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(actionMessage = successMessage)
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun delete(uid: String, departmentId: String) {
        viewModelScope.launch {
            when (val result = deleteUserUseCase(uid)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(actionMessage = "HOD deleted.")
                    loadCount(departmentId)
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
