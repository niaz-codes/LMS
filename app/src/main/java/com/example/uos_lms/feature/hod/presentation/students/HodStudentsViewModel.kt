package com.example.uos_lms.feature.hod.presentation.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
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

data class HodStudentsUiState(
    val departmentId: String? = null,
    val allStudents: List<User> = emptyList(),
    val semesters: List<Semester> = emptyList(),
    val searchQuery: String = "",
    val selectedSemester: Semester? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val filteredStudents: List<User>
        get() = allStudents
            .filter { selectedSemester == null || it.semester == selectedSemester.id }
            .filter {
                searchQuery.isBlank() ||
                    it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true) ||
                    it.cnic.contains(searchQuery, ignoreCase = true)
            }
            .sortedBy { it.fullName }
}

@HiltViewModel
class HodStudentsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val hodRepository: HodRepository,
    private val universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HodStudentsUiState())
    val uiState: StateFlow<HodStudentsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val departmentId = result.data?.department
                    _uiState.value = _uiState.value.copy(departmentId = departmentId, isLoading = departmentId != null)
                    if (departmentId == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@launch
                    }
                    hodRepository.observeUsersInDepartment(departmentId)
                        .onEach { users ->
                            _uiState.value = _uiState.value.copy(
                                allStudents = users.filter { it.role == UserRole.STUDENT },
                                isLoading = false,
                            )
                        }
                        .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
                        .launchIn(viewModelScope)

                    universityRepository.observeSemesters(departmentId)
                        .onEach { semesters -> _uiState.value = _uiState.value.copy(semesters = semesters.sortedBy { it.number }) }
                        .launchIn(viewModelScope)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onSemesterSelected(semester: Semester?) {
        _uiState.value = _uiState.value.copy(selectedSemester = semester)
    }
}
