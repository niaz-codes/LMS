package com.example.uos_lms.feature.admin.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.domain.model.Assignment
import com.example.uos_lms.core.domain.model.AttendanceStatus
import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.domain.model.Quiz
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.feature.admin.domain.usecase.ObserveUsersUseCase
import com.example.uos_lms.feature.assignment.domain.repository.AssignmentRepository
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import com.example.uos_lms.feature.quiz.domain.repository.QuizRepository
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
import javax.inject.Inject

data class ReportsUiState(
    val totalStudents: Int = 0,
    val totalTeachers: Int = 0,
    val totalHods: Int = 0,
    val totalDepartments: Int = 0,
    val totalSubjects: Int = 0,
    val totalAssignments: Int = 0,
    val totalQuizzes: Int = 0,
    val departmentAttendance: List<Pair<String, Int>> = emptyList(),
    val teacherLoad: List<Pair<String, Int>> = emptyList(),
    val isLoading: Boolean = true,
)

private data class ReportsBase(
    val users: List<User>,
    val departments: List<Department>,
    val subjects: List<Subject>,
    val assignments: List<Assignment>,
    val quizzes: List<Quiz>,
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    observeUsersUseCase: ObserveUsersUseCase,
    universityRepository: UniversityRepository,
    attendanceRepository: AttendanceRepository,
    assignmentRepository: AssignmentRepository,
    quizRepository: QuizRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        combine(
            observeUsersUseCase(),
            universityRepository.observeDepartments(),
            universityRepository.observeAllSubjects(),
            assignmentRepository.observeAllAssignments(),
            quizRepository.observeAllQuizzes(),
        ) { users, departments, subjects, assignments, quizzes ->
            ReportsBase(users, departments, subjects, assignments, quizzes)
        }.flatMapLatest { base ->
            if (base.departments.isEmpty()) {
                flowOf(base to emptyList())
            } else {
                combine(
                    base.departments.map { department ->
                        attendanceRepository.observeAttendanceForDepartment(department.id).map { records ->
                            val present = records.count { it.status == AttendanceStatus.PRESENT }
                            val percentage = if (records.isEmpty()) 0 else (present * 100) / records.size
                            department.name to percentage
                        }
                    },
                ) { pairs -> base to pairs.toList() }
            }
        }.onEach { (base, departmentAttendance) ->
            val teacherLoad = base.subjects
                .mapNotNull { it.teacherName }
                .groupingBy { it }
                .eachCount()
                .toList()
                .sortedByDescending { it.second }
                .take(5)

            _uiState.value = ReportsUiState(
                totalStudents = base.users.count { it.role == UserRole.STUDENT },
                totalTeachers = base.users.count { it.role == UserRole.TEACHER },
                totalHods = base.users.count { it.role == UserRole.HOD },
                totalDepartments = base.departments.size,
                totalSubjects = base.subjects.size,
                totalAssignments = base.assignments.size,
                totalQuizzes = base.quizzes.size,
                departmentAttendance = departmentAttendance,
                teacherLoad = teacherLoad,
                isLoading = false,
            )
        }.launchIn(viewModelScope)
    }
}
