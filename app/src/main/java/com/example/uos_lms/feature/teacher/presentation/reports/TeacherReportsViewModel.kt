package com.example.uos_lms.feature.teacher.presentation.reports

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Assignment
import com.example.uos_lms.core.domain.model.AssignmentSubmission
import com.example.uos_lms.core.domain.model.AttendanceStatus
import com.example.uos_lms.core.domain.model.Quiz
import com.example.uos_lms.core.domain.model.QuizAttempt
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.feature.assignment.domain.repository.AssignmentRepository
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.uos_lms.feature.quiz.domain.repository.QuizRepository
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

data class TeacherReportsUiState(
    val totalSubjects: Int = 0,
    val totalStudents: Int = 0,
    val totalAssignments: Int = 0,
    val totalQuizzes: Int = 0,
    val attendanceBySubject: List<Pair<String, Int>> = emptyList(),
    val performanceBySubject: List<Pair<String, Int>> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

private data class Counts(
    val studentCount: Int,
    val assignments: List<Assignment>,
    val quizzes: List<Quiz>,
    val attendanceBySubject: List<Pair<String, Int>>,
)

private data class Performance(
    val submissions: List<AssignmentSubmission>,
    val attempts: List<QuizAttempt>,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeacherReportsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val universityRepository: UniversityRepository,
    private val attendanceRepository: AttendanceRepository,
    private val assignmentRepository: AssignmentRepository,
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherReportsUiState())
    val uiState: StateFlow<TeacherReportsUiState> = _uiState.asStateFlow()

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
                        .flatMapLatest { subjects -> buildReportsFlow(subjects) }
                        .onEach { state -> _uiState.value = state }
                        .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
                        .launchIn(viewModelScope)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    private fun buildReportsFlow(subjects: List<Subject>) =
        if (subjects.isEmpty()) {
            flowOf(TeacherReportsUiState(isLoading = false))
        } else {
            val subjectPairs = subjects.map { it.departmentId to it.semesterId }.distinct()
            val countsFlow = combine(
                combine(subjectPairs.map { (d, s) -> attendanceRepository.observeStudentsInSemester(d, s) }) { lists ->
                    lists.toList().flatten().distinctBy { it.uid }.size
                },
                combine(subjects.map { assignmentRepository.observeAssignmentsForSubject(it.id) }) { it.toList().flatten() },
                combine(subjects.map { quizRepository.observeQuizzesForSubject(it.id) }) { it.toList().flatten() },
                combine(subjects.map { attendanceRepository.observeHistoryForSubject(it.id) }) { it.toList().flatten() },
            ) { studentCount, assignments, quizzes, attendanceRecords ->
                val attendanceBySubject = subjects.map { subject ->
                    val records = attendanceRecords.filter { it.subjectId == subject.id }
                    val present = records.count { it.status == AttendanceStatus.PRESENT }
                    val percentage = if (records.isEmpty()) 0 else (present * 100) / records.size
                    subject.code to percentage
                }
                Counts(studentCount, assignments, quizzes, attendanceBySubject)
            }
            val performanceFlow = combine(
                combine(subjects.map { assignmentRepository.observeSubmissionsForSubject(it.id) }) { it.toList().flatten() },
                combine(subjects.map { quizRepository.observeAttemptsForSubject(it.id) }) { it.toList().flatten() },
            ) { submissions, attempts -> Performance(submissions, attempts) }

            combine(countsFlow, performanceFlow) { counts, performance ->
                val performanceBySubject = subjects.map { subject ->
                    val assignment = counts.assignments.filter { it.subjectId == subject.id }.associateBy { it.id }
                    val submissionPercentages = performance.submissions
                        .filter { it.subjectId == subject.id && it.isGraded }
                        .mapNotNull { submission ->
                            val maxMarks = assignment[submission.assignmentId]?.maxMarks ?: return@mapNotNull null
                            if (maxMarks <= 0) return@mapNotNull null
                            (submission.marksObtained!! * 100) / maxMarks
                        }
                    val attemptPercentages = performance.attempts
                        .filter { it.subjectId == subject.id && it.totalMarks > 0 }
                        .map { (it.effectiveScore * 100) / it.totalMarks }
                    val all = submissionPercentages + attemptPercentages
                    subject.code to if (all.isEmpty()) 0 else all.sum() / all.size
                }
                TeacherReportsUiState(
                    totalSubjects = subjects.size,
                    totalStudents = counts.studentCount,
                    totalAssignments = counts.assignments.size,
                    totalQuizzes = counts.quizzes.size,
                    attendanceBySubject = counts.attendanceBySubject,
                    performanceBySubject = performanceBySubject,
                    isLoading = false,
                )
            }
        }
}
