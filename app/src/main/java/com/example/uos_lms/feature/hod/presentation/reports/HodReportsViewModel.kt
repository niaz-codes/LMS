package com.example.uos_lms.feature.hod.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.AttendanceStatus
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.uos_lms.feature.hod.domain.repository.HodRepository
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HodReportsUiState(
    val totalTeachers: Int = 0,
    val totalStudents: Int = 0,
    val totalSubjects: Int = 0,
    val totalSemesters: Int = 0,
    val semesterAttendance: List<Pair<String, Int>> = emptyList(),
    val teacherLoad: List<Pair<String, Int>> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

private data class ReportsBase(
    val teachers: List<User>,
    val students: List<User>,
    val subjects: List<Subject>,
    val semesters: List<Semester>,
)

@HiltViewModel
class HodReportsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val hodRepository: HodRepository,
    private val universityRepository: UniversityRepository,
    private val attendanceRepository: AttendanceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HodReportsUiState())
    val uiState: StateFlow<HodReportsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val departmentId = result.data?.department
                    if (departmentId == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@launch
                    }
                    combine(
                        hodRepository.observeUsersInDepartment(departmentId),
                        universityRepository.observeAllSubjects(),
                        universityRepository.observeSemesters(departmentId),
                    ) { users, allSubjects, semesters ->
                        ReportsBase(
                            teachers = users.filter { it.role == UserRole.TEACHER },
                            students = users.filter { it.role == UserRole.STUDENT },
                            subjects = allSubjects.filter { it.departmentId == departmentId },
                            semesters = semesters.sortedBy { it.number },
                        )
                    }.flatMapLatest { base ->
                        if (base.semesters.isEmpty()) {
                            flowOf(base to emptyList())
                        } else {
                            combine(
                                base.semesters.map { semester ->
                                    attendanceRepository.observeAttendanceForSemester(departmentId, semester.id).map { records ->
                                        val present = records.count { it.status == AttendanceStatus.PRESENT }
                                        val percentage = if (records.isEmpty()) 0 else (present * 100) / records.size
                                        semester.displayName to percentage
                                    }
                                },
                            ) { pairs -> base to pairs.toList() }
                        }
                    }.onEach { (base, semesterAttendance) ->
                        val teacherLoad = base.subjects
                            .mapNotNull { it.teacherName }
                            .groupingBy { it }
                            .eachCount()
                            .toList()
                            .sortedByDescending { it.second }
                            .take(5)
                        _uiState.value = HodReportsUiState(
                            totalTeachers = base.teachers.size,
                            totalStudents = base.students.size,
                            totalSubjects = base.subjects.size,
                            totalSemesters = base.semesters.size,
                            semesterAttendance = semesterAttendance,
                            teacherLoad = teacherLoad,
                            isLoading = false,
                        )
                    }.launchIn(viewModelScope)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }
}
