package com.example.uos_lms.feature.student.presentation.submissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Assignment
import com.example.uos_lms.core.domain.model.AssignmentSubmission
import com.example.uos_lms.feature.assignment.domain.repository.AssignmentRepository
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
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

data class SubmissionRow(
    val assignment: Assignment,
    val submission: AssignmentSubmission?,
)

data class StudentSubmissionsUiState(
    val rows: List<SubmissionRow> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudentSubmissionsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val universityRepository: UniversityRepository,
    private val assignmentRepository: AssignmentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentSubmissionsUiState())
    val uiState: StateFlow<StudentSubmissionsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val user = result.data
                    val departmentId = user?.department
                    val semesterId = user?.semester
                    if (user == null || departmentId == null || semesterId == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@launch
                    }
                    universityRepository.observeSubjects(departmentId, semesterId)
                        .flatMapLatest { subjects ->
                            if (subjects.isEmpty()) {
                                flowOf(emptyList<Assignment>() to emptyList<AssignmentSubmission>())
                            } else {
                                combine(
                                    combine(
                                        subjects.map { assignmentRepository.observeAssignmentsForSubject(it.id) },
                                    ) { lists -> lists.toList().flatten() },
                                    assignmentRepository.observeSubmissionsForStudent(user.uid),
                                ) { assignments, submissions -> assignments to submissions }
                            }
                        }
                        .onEach { (assignments, submissions) ->
                            val submissionsByAssignment = submissions.associateBy { it.assignmentId }
                            val rows = assignments
                                .sortedByDescending { it.dueDateMillis }
                                .map { assignment -> SubmissionRow(assignment, submissionsByAssignment[assignment.id]) }
                            _uiState.value = _uiState.value.copy(rows = rows, isLoading = false)
                        }
                        .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
                        .launchIn(viewModelScope)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }
}
