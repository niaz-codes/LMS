package com.example.uos_lms.feature.teacher.presentation.attendance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.common.dateKeyToDisplay
import com.example.uos_lms.core.common.dateKeyToMillis
import com.example.uos_lms.core.domain.model.AttendanceRecord
import com.example.uos_lms.core.domain.model.AttendanceStatus
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RosterRow(val student: User, val status: AttendanceStatus)

data class MarkAttendanceUiState(
    val dateLabel: String = "",
    val rows: List<RosterRow> = emptyList(),
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class MarkAttendanceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attendanceRepository: AttendanceRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val route: Routes.MarkAttendance = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(MarkAttendanceUiState(dateLabel = dateKeyToDisplay(route.dateKey)))
    val uiState: StateFlow<MarkAttendanceUiState> = _uiState.asStateFlow()

    private val _saved = Channel<Unit>()
    val saved = _saved.receiveAsFlow()

    private var teacherUid: String? = null

    init {
        sessionManager.cachedSession
            .onEach { session -> teacherUid = session?.uid }
            .launchIn(viewModelScope)

        combine(
            attendanceRepository.observeStudentsInSemester(route.departmentId, route.semesterId),
            attendanceRepository.observeSessionAttendance(route.subjectId, route.dateKey),
        ) { students, records ->
            students
                .sortedBy { it.fullName }
                .map { student ->
                    val existing = records.find { it.studentUid == student.uid }
                    RosterRow(student = student, status = existing?.status ?: AttendanceStatus.PRESENT)
                }
        }
            .onEach { rows -> _uiState.value = _uiState.value.copy(rows = rows, isLoading = false) }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    fun toggleStatus(studentUid: String) {
        _uiState.value = _uiState.value.copy(
            rows = _uiState.value.rows.map { row ->
                if (row.student.uid == studentUid) {
                    val next = if (row.status == AttendanceStatus.PRESENT) AttendanceStatus.ABSENT else AttendanceStatus.PRESENT
                    row.copy(status = next)
                } else {
                    row
                }
            },
        )
    }

    fun save() {
        val teacher = teacherUid
        if (teacher == null) {
            _uiState.value = _uiState.value.copy(errorMessage = "Session expired. Please log in again.")
            return
        }
        val dateMillis = dateKeyToMillis(route.dateKey)
        val records = _uiState.value.rows.map { row ->
            AttendanceRecord(
                id = "",
                subjectId = route.subjectId,
                departmentId = route.departmentId,
                semesterId = route.semesterId,
                dateKey = route.dateKey,
                dateMillis = dateMillis,
                studentUid = row.student.uid,
                studentName = row.student.fullName,
                status = row.status,
                markedBy = teacher,
            )
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            when (val result = attendanceRepository.saveAttendance(records)) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _saved.send(Unit)
                }
                is AppResult.Error -> _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
            }
        }
    }
}
