package com.example.uos_lms.feature.teacher.presentation.students

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.core.domain.model.User
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

data class TeacherStudentsUiState(
    val subjects: List<Subject> = emptyList(),
    val allStudents: List<User> = emptyList(),
    val searchQuery: String = "",
    val selectedSubject: Subject? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val filteredStudents: List<User>
        get() = allStudents
            .filter {
                val subject = selectedSubject
                subject == null || (it.department == subject.departmentId && it.semester == subject.semesterId)
            }
            .filter {
                searchQuery.isBlank() ||
                    it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true) ||
                    it.cnic.contains(searchQuery, ignoreCase = true)
            }
            .sortedBy { it.fullName }
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeacherStudentsViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val universityRepository: UniversityRepository,
    private val attendanceRepository: AttendanceRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TeacherStudentsUiState())
    val uiState: StateFlow<TeacherStudentsUiState> = _uiState.asStateFlow()

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
                        .onEach { subjects -> _uiState.value = _uiState.value.copy(subjects = subjects) }
                        .flatMapLatest { subjects ->
                            val pairs = subjects.map { it.departmentId to it.semesterId }.distinct()
                            if (pairs.isEmpty()) {
                                flowOf(emptyList())
                            } else {
                                combine(
                                    pairs.map { (departmentId, semesterId) ->
                                        attendanceRepository.observeStudentsInSemester(departmentId, semesterId)
                                    },
                                ) { lists -> lists.toList().flatten().distinctBy { it.uid } }
                            }
                        }
                        .onEach { students ->
                            _uiState.value = _uiState.value.copy(allStudents = students, isLoading = false)
                        }
                        .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
                        .launchIn(viewModelScope)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun onSubjectSelected(subject: Subject?) {
        _uiState.value = _uiState.value.copy(selectedSubject = subject)
    }
}
