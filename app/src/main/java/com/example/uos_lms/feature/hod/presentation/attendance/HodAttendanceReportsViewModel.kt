package com.example.uos_lms.feature.hod.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.AttendanceRecord
import com.example.uos_lms.core.domain.model.AttendanceStatus
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HodAttendanceReportsUiState(
    val departmentId: String? = null,
    val semesters: List<Semester> = emptyList(),
    val selectedSemester: Semester? = null,
    val records: List<AttendanceRecord> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val presentCount: Int get() = records.count { it.status == AttendanceStatus.PRESENT }
    val percentage: Int get() = if (records.isEmpty()) 0 else (presentCount * 100) / records.size
}

@HiltViewModel
class HodAttendanceReportsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val attendanceRepository: AttendanceRepository,
    private val universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HodAttendanceReportsUiState())
    val uiState: StateFlow<HodAttendanceReportsUiState> = _uiState.asStateFlow()

    private var recordsJob: Job? = null

    init {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val departmentId = result.data?.department
                    _uiState.value = _uiState.value.copy(departmentId = departmentId)
                    if (departmentId == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@launch
                    }
                    universityRepository.observeSemesters(departmentId)
                        .onEach { semesters -> _uiState.value = _uiState.value.copy(semesters = semesters.sortedBy { it.number }) }
                        .launchIn(viewModelScope)
                    loadRecords(departmentId, null)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    fun onSemesterSelected(semester: Semester?) {
        val departmentId = _uiState.value.departmentId ?: return
        _uiState.value = _uiState.value.copy(selectedSemester = semester)
        loadRecords(departmentId, semester?.id)
    }

    private fun loadRecords(departmentId: String, semesterId: String?) {
        recordsJob?.cancel()
        _uiState.value = _uiState.value.copy(isLoading = true)
        val flow = if (semesterId != null) {
            attendanceRepository.observeAttendanceForSemester(departmentId, semesterId)
        } else {
            attendanceRepository.observeAttendanceForDepartment(departmentId)
        }
        recordsJob = flow
            .onEach { records -> _uiState.value = _uiState.value.copy(records = records, isLoading = false) }
            .launchIn(viewModelScope)
    }

    fun buildCsv(): String {
        val header = "Student Name,Subject ID,Date,Status"
        val rows = _uiState.value.records.joinToString("\n") { record ->
            "${record.studentName},${record.subjectId},${record.dateKey},${record.status.name}"
        }
        return "$header\n$rows"
    }
}
