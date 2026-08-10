package com.example.uos_lms.feature.student.presentation.examresult

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.CgpaSummary
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.core.domain.model.computeCgpaSummary
import com.example.uos_lms.feature.auth.domain.usecase.GetCurrentUserUseCase
import com.example.uos_lms.feature.examresult.domain.repository.ExamResultRepository
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

data class StudentExamResultUiState(
    val cgpaSummary: CgpaSummary = CgpaSummary(0.0, 0, emptyList()),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
) {
    val hasResults: Boolean get() = cgpaSummary.semesters.isNotEmpty()
}

@HiltViewModel
class StudentExamResultViewModel @Inject constructor(
    private val getCurrentUserUseCase: GetCurrentUserUseCase,
    private val examResultRepository: ExamResultRepository,
    private val universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StudentExamResultUiState())
    val uiState: StateFlow<StudentExamResultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = getCurrentUserUseCase()) {
                is AppResult.Success -> {
                    val user = result.data
                    val departmentId = user?.department
                    if (user == null || departmentId == null) {
                        _uiState.value = _uiState.value.copy(isLoading = false)
                        return@launch
                    }
                    combine(
                        examResultRepository.observeApprovedResultsForStudent(user.uid),
                        universityRepository.observeSemesters(departmentId),
                    ) { results, semesters ->
                        computeCgpaSummary(results, semesters.associateBy(Semester::id))
                    }
                        .onEach { summary -> _uiState.value = _uiState.value.copy(cgpaSummary = summary, isLoading = false) }
                        .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
                        .launchIn(viewModelScope)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }
        }
    }

    fun consumeError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
