package com.example.uos_lms.feature.teacher.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.AttendanceRecord
import com.example.uos_lms.core.domain.model.AttendanceStatus
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherAttendanceReportsUiState(
    val subjects: List<Subject> = emptyList(),
    val selectedSubject: Subject? = null,
    val records: List<AttendanceRecord> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val presentCount: Int get() = records.count { it.status == AttendanceStatus.PRESENT }
    val percentage: Int get() = if (records.isEmpty()) 0 else (presentCount * 100) / records.size
}

@HiltViewModel
class TeacherAttendanceReportsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val attendanceRepository: AttendanceRepository,
    private val universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherAttendanceReportsUiState())
    val uiState: StateFlow<TeacherAttendanceReportsUiState> = _uiState.asStateFlow()

    private var recordsJob: Job? = null

    init {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val uid = result.data?.uid
                    if (uid == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@launch
                    }
                    universityRepository.observeSubjectsForTeacher(uid)
                        .onEach { subjects ->
                            _uiState.value = _uiState.value.copy(subjects = subjects)
                            loadRecords(subjects, _uiState.value.selectedSubject?.id)
                        }
                        .launchIn(viewModelScope)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    fun onSubjectSelected(subject: Subject?) {
        _uiState.value = _uiState.value.copy(selectedSubject = subject)
        loadRecords(_uiState.value.subjects, subject?.id)
    }

    private fun loadRecords(subjects: List<Subject>, subjectId: String?) {
        recordsJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = true)
        val targetSubjects = if (subjectId != null) subjects.filter { it.id == subjectId } else subjects
        recordsJob = if (targetSubjects.isEmpty()) {
            _uiState.value = _uiState.value.copy(records = emptyList(), isLoading = false)
            null
        } else {
            combine(
                targetSubjects.map { attendanceRepository.observeHistoryForSubject(it.id) },
            ) { lists -> lists.toList().flatten() }
                .onEach { records -> _uiState.value = _uiState.value.copy(records = records, isLoading = false) }
                .launchIn(viewModelScope)
        }
    }

    fun buildCsv(): String {
        val header = "Student Name,Subject ID,Date,Status"
        val rows = _uiState.value.records.joinToString("\n") { record ->
            "${record.studentName},${record.subjectId},${record.dateKey},${record.status.name}"
        }
        return "$header\n$rows"
    }
}
