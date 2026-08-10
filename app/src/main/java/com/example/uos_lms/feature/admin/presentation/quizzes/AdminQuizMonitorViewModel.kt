package com.example.uos_lms.feature.admin.presentation.quizzes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uos_lms.core.domain.model.Quiz
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.feature.quiz.domain.repository.QuizRepository
import com.example.uos_lms.feature.university.domain.repository.UniversityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class AdminQuizMonitorUiState(
    val quizzes: List<Quiz> = emptyList(),
    val subjectsById: Map<String, Subject> = emptyMap(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class AdminQuizMonitorViewModel @Inject constructor(
    quizRepository: QuizRepository,
    universityRepository: UniversityRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminQuizMonitorUiState())
    val uiState: StateFlow<AdminQuizMonitorUiState> = _uiState.asStateFlow()

    init {
        combine(
            quizRepository.observeAllQuizzes(),
            universityRepository.observeAllSubjects(),
        ) { quizzes, subjects ->
            quizzes.sortedByDescending { it.dueDateMillis } to subjects.associateBy { it.id }
        }
            .onEach { (quizzes, subjectsById) ->
                _uiState.value = _uiState.value.copy(quizzes = quizzes, subjectsById = subjectsById, isLoading = false)
            }
            .launchIn(viewModelScope)
    }
}
