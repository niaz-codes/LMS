package com.example.uos_lms.feature.hod.presentation.semester

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.navigation.Routes
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

data class HodSemesterSubjectsUiState(
    val semester: Semester? = null,
    val subjects: List<Subject> = emptyList(),
    val teachers: List<User> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class HodSemesterSubjectsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val universityRepository: UniversityRepository,
    private val hodRepository: HodRepository,
) : ViewModel() {

    private val route: Routes.HodSemesterSubjects = savedStateHandle.toRoute()
    val departmentId: String get() = route.departmentId
    val semesterId: String get() = route.semesterId

    private val _uiState = MutableStateFlow(HodSemesterSubjectsUiState())
    val uiState: StateFlow<HodSemesterSubjectsUiState> = _uiState.asStateFlow()

    init {
        combine(
            universityRepository.observeSemesters(departmentId),
            universityRepository.observeSubjects(departmentId, semesterId),
            hodRepository.observeTeachersInDepartment(departmentId),
        ) { semesters, subjects, teachers ->
            Triple(semesters.find { it.id == semesterId }, subjects.sortedBy { it.code }, teachers)
        }
            .onEach { (semester, subjects, teachers) ->
                _uiState.value = _uiState.value.copy(
                    semester = semester,
                    subjects = subjects,
                    teachers = teachers,
                    isLoading = false,
                )
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    fun assignTeacher(subjectId: String, teacher: User) {
        viewModelScope.launch {
            when (val result = universityRepository.assignTeacherToSubject(subjectId, teacher.uid, teacher.fullName)) {
                is AppResult.Success -> {}
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun unassignTeacher(subjectId: String) {
        viewModelScope.launch {
            when (val result = universityRepository.assignTeacherToSubject(subjectId, null, null)) {
                is AppResult.Success -> {}
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
