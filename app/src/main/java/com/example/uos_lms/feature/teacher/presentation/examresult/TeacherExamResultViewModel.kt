package com.example.uos_lms.feature.teacher.presentation.examresult

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.ExamResult
import com.example.uos_lms.core.domain.model.ResultStatus
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import com.example.uos_lms.feature.examresult.domain.repository.ExamResultRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ExamResultRow(
    val student: User,
    val existing: ExamResult?,
    val marksInput: String,
) {
    /** A row can only be (re)entered by the Teacher while it has never been submitted,
     * or was bounced back by the HOD — matches firestore.rules' update condition exactly. */
    val isEditable: Boolean
        get() = existing == null || existing.status == ResultStatus.DRAFT || existing.status == ResultStatus.REJECTED
}

data class TeacherExamResultUiState(
    val totalMarksInput: String = "100",
    val rows: List<ExamResultRow> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
) {
    val editableRows: List<ExamResultRow> get() = rows.filter { it.isEditable }

    /** Once any row has left DRAFT, total marks is locked so the whole class stays on one scale. */
    val isTotalMarksLocked: Boolean get() = rows.any { !it.isEditable }

    val canSubmit: Boolean
        get() {
            val totalMarks = totalMarksInput.toIntOrNull() ?: return false
            if (totalMarks <= 0) return false
            val editable = editableRows
            if (editable.isEmpty()) return false
            return editable.all { row ->
                val marks = row.marksInput.toIntOrNull()
                marks != null && marks in 0..totalMarks
            }
        }
}

@HiltViewModel
class TeacherExamResultViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val examResultRepository: ExamResultRepository,
    private val attendanceRepository: AttendanceRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val route: Routes.TeacherSubjectExamResults = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(TeacherExamResultUiState())
    val uiState: StateFlow<TeacherExamResultUiState> = _uiState.asStateFlow()

    private var teacherUid: String? = null
    private var teacherName: String = ""

    init {
        sessionManager.cachedSession
            .onEach { session ->
                teacherUid = session?.uid
                teacherName = session?.fullName.orEmpty()
            }
            .launchIn(viewModelScope)

        combine(
            attendanceRepository.observeStudentsInSemester(route.departmentId, route.semesterId),
            examResultRepository.observeResultsForSubject(route.subjectId),
        ) { students, results ->
            students.sortedBy { it.fullName }.map { student ->
                val existing = results.find { it.studentUid == student.uid }
                ExamResultRow(
                    student = student,
                    existing = existing,
                    marksInput = existing?.obtainedMarks?.toString() ?: "",
                )
            }
        }
            .onEach { rows ->
                val existingTotal = rows.firstNotNullOfOrNull { it.existing?.totalMarks }
                _uiState.value = _uiState.value.copy(
                    rows = rows,
                    totalMarksInput = existingTotal?.toString() ?: _uiState.value.totalMarksInput,
                    isLoading = false,
                )
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    fun updateTotalMarks(value: String) {
        if (_uiState.value.isTotalMarksLocked) return
        if (value.length <= 4 && value.all { it.isDigit() }) {
            _uiState.value = _uiState.value.copy(totalMarksInput = value)
        }
    }

    fun updateMarks(studentUid: String, value: String) {
        if (value.isNotEmpty() && (!value.all { it.isDigit() } || value.length > 4)) return
        _uiState.value = _uiState.value.copy(
            rows = _uiState.value.rows.map { row ->
                if (row.student.uid == studentUid) row.copy(marksInput = value) else row
            },
        )
    }

    fun saveDraft() {
        val teacher = teacherUid
        if (teacher == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Session expired. Please log in again.")
            return
        }
        val totalMarks = _uiState.value.totalMarksInput.toIntOrNull()
        if (totalMarks == null || totalMarks <= 0) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter a valid total marks value first.")
            return
        }
        val toSave = _uiState.value.editableRows.mapNotNull { row ->
            val marks = row.marksInput.toIntOrNull() ?: return@mapNotNull null
            if (marks !in 0..totalMarks) return@mapNotNull null
            buildResult(row, marks, totalMarks, teacher)
        }
        if (toSave.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter marks for at least one student first.")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            when (val result = examResultRepository.saveDrafts(toSave)) {
                is AppResult.Success ->
                    _uiState.value = _uiState.value.copy(isSaving = false, actionMessage = "Draft saved.")
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
            }
        }
    }

    fun submitForApproval() {
        val teacher = teacherUid
        if (teacher == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Session expired. Please log in again.")
            return
        }
        if (!_uiState.value.canSubmit) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter valid marks for every student before submitting.")
            return
        }
        val totalMarks = _uiState.value.totalMarksInput.toIntOrNull() ?: return
        val toSave = _uiState.value.editableRows.map { row ->
            buildResult(row, row.marksInput.toIntOrNull()!!, totalMarks, teacher)
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            when (val saveResult = examResultRepository.saveDrafts(toSave)) {
                is AppResult.Error -> {
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = saveResult.message)
                    return@launch
                }
                is AppResult.Success -> Unit
            }
            val ids = toSave.map { "${it.subjectId}_${it.studentUid}" }
            when (val submitResult = examResultRepository.submitForApproval(ids)) {
                is AppResult.Success ->
                    _uiState.value = _uiState.value.copy(isSaving = false, actionMessage = "Submitted for HOD approval.")
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = submitResult.message)
            }
        }
    }

    private fun buildResult(row: ExamResultRow, marks: Int, totalMarks: Int, teacher: String): ExamResult {
        val existing = row.existing
        return ExamResult(
            id = existing?.id ?: "",
            subjectId = route.subjectId,
            subjectCode = route.subjectCode,
            subjectTitle = route.subjectTitle,
            creditHours = route.creditHours,
            departmentId = route.departmentId,
            semesterId = route.semesterId,
            teacherUid = teacher,
            teacherName = teacherName,
            studentUid = row.student.uid,
            studentName = row.student.fullName,
            studentRollNumber = row.student.rollNumber,
            obtainedMarks = marks,
            totalMarks = totalMarks,
            status = ResultStatus.DRAFT,
            createdAt = existing?.createdAt ?: 0L,
        )
    }

    fun consumeMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, actionMessage = null)
    }
}
