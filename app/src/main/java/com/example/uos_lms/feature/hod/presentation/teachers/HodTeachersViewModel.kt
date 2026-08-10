package com.example.uos_lms.feature.hod.presentation.teachers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.uos_lms.feature.hod.domain.repository.HodRepository
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TeacherWithLoad(val teacher: User, val subjectCount: Int)

data class HodTeachersUiState(
    val teachers: List<TeacherWithLoad> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class HodTeachersViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val hodRepository: HodRepository,
    private val universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HodTeachersUiState())
    val uiState: StateFlow<HodTeachersUiState> = _uiState.asStateFlow()

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
                        hodRepository.observeTeachersInDepartment(departmentId),
                        universityRepository.observeAllSubjects(),
                    ) { teachers, subjects ->
                        val subjectsInDept = subjects.filter { it.departmentId == departmentId }
                        teachers
                            .map { teacher ->
                                TeacherWithLoad(teacher, subjectsInDept.count { it.teacherUid == teacher.uid })
                            }
                            .sortedByDescending { it.subjectCount }
                    }
                        .onEach { list -> _uiState.value = _uiState.value.copy(teachers = list, isLoading = false) }
                        .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
                        .launchIn(viewModelScope)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }
}
