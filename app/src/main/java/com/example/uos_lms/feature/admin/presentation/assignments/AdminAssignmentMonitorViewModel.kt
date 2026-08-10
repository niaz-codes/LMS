package com.example.uos_lms.feature.admin.presentation.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Assignment
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.feature.assignment.domain.repository.AssignmentRepository
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

data class AdminAssignmentMonitorUiState(
    val assignments: List<Assignment> = emptyList(),
    val subjectsById: Map<String, Subject> = emptyMap(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class AdminAssignmentMonitorViewModel @Inject constructor(
    private val assignmentRepository: AssignmentRepository,
    universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminAssignmentMonitorUiState())
    val uiState: StateFlow<AdminAssignmentMonitorUiState> = _uiState.asStateFlow()

    init {
        combine(
            assignmentRepository.observeAllAssignments(),
            universityRepository.observeAllSubjects(),
        ) { assignments, subjects ->
            assignments.sortedByDescending { it.dueDateMillis } to subjects.associateBy { it.id }
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

    fun deleteAssignment(id: String) {
        viewModelScope.launch {
            when (val result = assignmentRepository.deleteAssignment(id)) {
                is AppResult.Success -> {}
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
