package com.example.uos_lms.feature.student.presentation.assignment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.domain.model.Assignment
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.feature.assignment.domain.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class StudentSubjectAssignmentsUiState(
    val assignments: List<Assignment> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class StudentSubjectAssignmentsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val assignmentRepository: AssignmentRepository,
) : ViewModel() {

    val route: Routes.StudentSubjectAssignments = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(StudentSubjectAssignmentsUiState())
    val uiState: StateFlow<StudentSubjectAssignmentsUiState> = _uiState.asStateFlow()

    init {
        assignmentRepository.observeAssignmentsForSubject(route.subjectId)
            .onEach { assignments ->
                _uiState.value = _uiState.value.copy(
                    assignments = assignments.sortedByDescending { it.dueDateMillis },
                    isLoading = false,
                )
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }
}
