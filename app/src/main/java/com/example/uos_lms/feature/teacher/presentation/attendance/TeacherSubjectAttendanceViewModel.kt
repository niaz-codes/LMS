package com.example.uos_lms.feature.teacher.presentation.attendance

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class TeacherSubjectAttendanceUiState(
    val historyDates: List<String> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class TeacherSubjectAttendanceViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val attendanceRepository: AttendanceRepository,
) : ViewModel() {

    val route: Routes.TeacherSubjectAttendance = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(TeacherSubjectAttendanceUiState())
    val uiState: StateFlow<TeacherSubjectAttendanceUiState> = _uiState.asStateFlow()

    init {
        attendanceRepository.observeHistoryForSubject(route.subjectId)
            .map { records -> records.map { it.dateKey }.distinct().sortedDescending() }
            .onEach { dates -> _uiState.value = _uiState.value.copy(historyDates = dates, isLoading = false) }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }
}
