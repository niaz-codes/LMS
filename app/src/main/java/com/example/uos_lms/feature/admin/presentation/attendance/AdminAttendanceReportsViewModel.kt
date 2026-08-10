package com.example.uos_lms.feature.admin.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.domain.model.AttendanceRecord
import com.example.uos_lms.core.domain.model.AttendanceStatus
import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminAttendanceReportsUiState(
    val departments: List<Department> = emptyList(),
    val semesters: List<Semester> = emptyList(),
    val selectedDepartment: Department? = null,
    val selectedSemester: Semester? = null,
    val records: List<AttendanceRecord> = emptyList(),
    val isLoading: Boolean = false,
) {
    val presentCount: Int get() = records.count { it.status == AttendanceStatus.PRESENT }
    val percentage: Int get() = if (records.isEmpty()) 0 else (presentCount * 100) / records.size
}

@HiltViewModel
class AdminAttendanceReportsViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminAttendanceReportsUiState())
    val uiState: StateFlow<AdminAttendanceReportsUiState> = _uiState.asStateFlow()

    private var semestersJob: Job? = null
    private var recordsJob: Job? = null

    init {
        universityRepository.observeDepartments()
            .onEach { departments -> _uiState.value = _uiState.value.copy(departments = departments) }
            .launchIn(viewModelScope)
    }

    fun onDepartmentSelected(department: Department?) {
        semestersJob?.cancel()
        recordsJob?.cancel()
        _uiState.value = _uiState.value.copy(
            selectedDepartment = department,
            selectedSemester = null,
            semesters = emptyList(),
            records = emptyList(),
        )
        if (department == null) return
        semestersJob = universityRepository.observeSemesters(department.id)
            .onEach { semesters -> _uiState.value = _uiState.value.copy(semesters = semesters.sortedBy { it.number }) }
            .launchIn(viewModelScope)
        loadRecords(department.id, null)
    }

    fun onSemesterSelected(semester: Semester?) {
        val department = _uiState.value.selectedDepartment ?: return
        _uiState.value = _uiState.value.copy(selectedSemester = semester)
        loadRecords(department.id, semester?.id)
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
