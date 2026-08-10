package com.example.uos_lms.feature.student.presentation.assignment

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Assignment
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

data class SubmitAssignmentUiState(
    val assignment: Assignment? = null,
    val existingSubmission: AssignmentSubmission? = null,
    val textAnswer: String = "",
    val pickedFileUri: Uri? = null,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val uploadProgress: Int? = null,
    val submitted: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class SubmitAssignmentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val assignmentRepository: AssignmentRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val route: Routes.SubmitAssignment = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(SubmitAssignmentUiState())
    val uiState: StateFlow<SubmitAssignmentUiState> = _uiState.asStateFlow()

    private var studentUid: String? = null
    private var studentName: String = ""
    private var hasPrefilledTextAnswer = false

    init {
        viewModelScope.launch {
            val session = sessionManager.cachedSession.filterNotNull().first()
            studentUid = session.uid
            studentName = session.fullName

            when (val result = assignmentRepository.getAssignment(route.assignmentId)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(assignment = result.data)
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }

            assignmentRepository.observeMySubmission(route.assignmentId, session.uid)
                .onEach { submission ->
                    _uiState.value = _uiState.value.copy(
                        existingSubmission = submission,
                        textAnswer = if (!hasPrefilledTextAnswer && submission?.textAnswer != null) {
                            hasPrefilledTextAnswer = true
                            submission.textAnswer
                        } else {
                            _uiState.value.textAnswer
                        },
                        isLoading = false,
                    )
                }
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
                .launchIn(viewModelScope)
        }
    }

    fun onTextAnswerChange(value: String) {
        _uiState.value = _uiState.value.copy(textAnswer = value)
    }

    fun onFilePicked(uri: Uri) {
        _uiState.value = _uiState.value.copy(pickedFileUri = uri)
    }

    fun submit() {
        val uid = studentUid
        if (uid == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Session expired. Please log in again.")
            return
        }
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, uploadProgress = null, errorMessage = null)
            when (
                val result = assignmentRepository.submitAssignment(
                    assignmentId = route.assignmentId,
                    subjectId = route.subjectId,
                    studentUid = uid,
                    studentName = studentName,
                    textAnswer = state.textAnswer,
                    fileUri = state.pickedFileUri,
                    existingFileUrl = state.existingSubmission?.fileUrl,
                    existingFileName = state.existingSubmission?.fileName,
                    existingFilePublicId = state.existingSubmission?.filePublicId,
                    existingFileResourceType = state.existingSubmission?.fileResourceType,
                    onProgress = { progress ->
                        _uiState.value = _uiState.value.copy(uploadProgress = progress)
                    },
                )
            ) {
                is AppResult.Success ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        uploadProgress = null,
                        pickedFileUri = null,
                        submitted = true,
                    )
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(
                        isSubmitting = false,
                        uploadProgress = null,
                        errorMessage = result.message,
                    )
            }
        }
    }
}
