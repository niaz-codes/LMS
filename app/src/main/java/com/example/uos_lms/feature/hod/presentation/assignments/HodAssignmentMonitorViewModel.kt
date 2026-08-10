package com.example.uos_lms.feature.hod.presentation.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Assignment
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.feature.assignment.domain.repository.AssignmentRepository
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HodAssignmentMonitorUiState(
    val assignments: List<Assignment> = emptyList(),
    val subjectsById: Map<String, Subject> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class HodAssignmentMonitorViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val assignmentRepository: AssignmentRepository,
    private val universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HodAssignmentMonitorUiState())
    val uiState: StateFlow<HodAssignmentMonitorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val departmentId = result.data?.department
                    if (departmentId == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@launch
                    }
                    combine(
                        assignmentRepository.observeAllAssignments(),
                        universityRepository.observeAllSubjects(),
                    ) { assignments, subjects ->
                        val subjectsInDept = subjects.filter { it.departmentId == departmentId }
                        val subjectIds = subjectsInDept.map { it.id }.toSet()
                        assignments.filter { it.subjectId in subjectIds }.sortedByDescending { it.dueDateMillis } to
                            subjectsInDept.associateBy { it.id }
                    }
                        .onEach { (assignments, subjectsById) ->
                            _uiState.value = _uiState.value.copy(
                                assignments = assignments,
                                subjectsById = subjectsById,
                                isLoading = false,
                            )
                        }
                        .launchIn(viewModelScope)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }
}
