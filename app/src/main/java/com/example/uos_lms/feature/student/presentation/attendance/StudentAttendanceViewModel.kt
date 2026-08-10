package com.example.uos_lms.feature.student.presentation.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.AttendanceStatus
import com.example.uos_lms.feature.attendance.domain.repository.AttendanceRepository
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
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

data class StudentAttendanceUiState(
    val overallPercentage: Int = 0,
    val presentCount: Int = 0,
    val totalCount: Int = 0,
    val bySubject: List<Pair<String, Int>> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StudentAttendanceViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val universityRepository: UniversityRepository,
    private val attendanceRepository: AttendanceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentAttendanceUiState())
    val uiState: StateFlow<StudentAttendanceUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val user = result.data
                    val departmentId = user?.department
                    val semesterId = user?.semester
                    if (user == null || departmentId == null || semesterId == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@launch
                    }
                    universityRepository.observeSubjects(departmentId, semesterId)
                        .flatMapLatest { subjects ->
                            if (subjects.isEmpty()) {
                                flowOf(emptyList())
                            } else {
                                combine(
                                    subjects.map { subject ->
                                        attendanceRepository.observeStudentAttendanceForSubject(subject.id, user.uid)
                                    },
                                ) { lists ->
                                    subjects.mapIndexed { index, subject ->
                                        val records = lists[index]
                                        val present = records.count { it.status == AttendanceStatus.PRESENT }
                                        val percentage = if (records.isEmpty()) 0 else (present * 100) / records.size
                                        Triple(subject.code, percentage, records)
                                    }
                                }
                            }
                        }
                        .onEach { rows ->
                            val allRecords = rows.flatMap { it.third }
                            val present = allRecords.count { it.status == AttendanceStatus.PRESENT }
                            val overall = if (allRecords.isEmpty()) 0 else (present * 100) / allRecords.size
                            _uiState.value = _uiState.value.copy(
                                overallPercentage = overall,
                                presentCount = present,
                                totalCount = allRecords.size,
                                bySubject = rows.map { it.first to it.second },
                                isLoading = false,
                            )
                        }
                        .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
                        .launchIn(viewModelScope)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }
}
