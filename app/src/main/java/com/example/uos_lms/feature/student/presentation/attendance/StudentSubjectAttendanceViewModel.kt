package com.example.uos_lms.feature.student.presentation.attendance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.domain.model.AttendanceRecord
import com.example.uos_lms.core.domain.model.AttendanceStatus
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class StudentSubjectAttendanceUiState(
    val records: List<AttendanceRecord> = emptyList(),
    val presentCount: Int = 0,
    val totalCount: Int = 0,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val percentage: Int
        get() = if (totalCount == 0) 0 else (presentCount * 100) / totalCount
}

@HiltViewModel
class StudentSubjectAttendanceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attendanceRepository: AttendanceRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val route: Routes.StudentSubjectAttendance = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(StudentSubjectAttendanceUiState())
    val uiState: StateFlow<StudentSubjectAttendanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val uid = sessionManager.cachedSession.filterNotNull().first().uid
            attendanceRepository.observeStudentAttendanceForSubject(route.subjectId, uid)
                .map { records -> records.sortedByDescending { it.dateMillis } }
                .onEach { records ->
                    _uiState.value = _uiState.value.copy(
                        records = records,
                        presentCount = records.count { it.status == AttendanceStatus.PRESENT },
                        totalCount = records.size,
                        isLoading = false,
                    )
                }
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
                .launchIn(viewModelScope)
        }
    }
}
