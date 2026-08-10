package com.example.uos_lms.feature.teacher.presentation.assignment

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.assignment.domain.repository.AssignmentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CreateAssignmentUiState(
    val title: String = "",
    val description: String = "",
    val dueDateMillis: Long? = null,
    val maxMarksText: String = "",
    val pickedFileUri: Uri? = null,
    val isSaving: Boolean = false,
    val uploadProgress: Int? = null,
    val errorMessage: String? = null,
)

@HiltViewModel
class CreateAssignmentViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val assignmentRepository: AssignmentRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val route: Routes.CreateAssignment = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(CreateAssignmentUiState())
    val uiState: StateFlow<CreateAssignmentUiState> = _uiState.asStateFlow()

    private val _created = Channel<Unit>()
    val created = _created.receiveAsFlow()

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value, errorMessage = null)
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun onDueDateChange(millis: Long) {
        _uiState.value = _uiState.value.copy(dueDateMillis = millis, errorMessage = null)
    }

    fun onMaxMarksChange(value: String) {
        _uiState.value = _uiState.value.copy(maxMarksText = value, errorMessage = null)
    }

    fun onFilePicked(uri: Uri) {
        _uiState.value = _uiState.value.copy(pickedFileUri = uri)
    }

    fun save() {
        val state = _uiState.value
        val dueDateMillis = state.dueDateMillis
        val maxMarks = state.maxMarksText.trim().toIntOrNull()
        if (state.title.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Title is required")
            return
        }
        if (dueDateMillis == null) {
            _uiState.value = state.copy(errorMessage = "Pick a due date")
            return
        }
        if (maxMarks == null || maxMarks <= 0) {
            _uiState.value = state.copy(errorMessage = "Enter valid max marks")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, uploadProgress = null, errorMessage = null)
            val teacherUid = sessionManager.cachedSession.filterNotNull().first().uid
            when (
                val result = assignmentRepository.createAssignment(
                    subjectId = route.subjectId,
                    departmentId = route.departmentId,
                    semesterId = route.semesterId,
                    title = state.title.trim(),
                    description = state.description.trim(),
                    dueDateMillis = dueDateMillis,
                    maxMarks = maxMarks,
                    createdBy = teacherUid,
                    fileUri = state.pickedFileUri,
                    onProgress = { progress ->
                        _uiState.value = _uiState.value.copy(uploadProgress = progress)
                    },
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, uploadProgress = null)
                    _created.send(Unit)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, uploadProgress = null, errorMessage = result.message)
            }
        }
    }
}
