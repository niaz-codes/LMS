package com.example.uos_lms.feature.teacher.presentation.assignment

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.AssignmentSubmission
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.assignment.domain.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AssignmentSubmissionsUiState(
    val submissions: List<AssignmentSubmission> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class AssignmentSubmissionsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val assignmentRepository: AssignmentRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    val route: Routes.AssignmentSubmissions = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(AssignmentSubmissionsUiState())
    val uiState: StateFlow<AssignmentSubmissionsUiState> = _uiState.asStateFlow()

    init {
        assignmentRepository.observeSubmissionsForAssignment(route.assignmentId)
            .onEach { submissions ->
                _uiState.value = _uiState.value.copy(
                    submissions = submissions.sortedBy { it.studentName },
                    isLoading = false,
                )
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    fun grade(submission: AssignmentSubmission, marksObtained: Int, feedback: String) {
        viewModelScope.launch {
            val teacherUid = sessionManager.cachedSession.filterNotNull().first().uid
            when (
                val result = assignmentRepository.gradeSubmission(submission.id, marksObtained, feedback, teacherUid)
            ) {
                is AppResult.Success -> {}
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
