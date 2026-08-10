package com.example.uos_lms.feature.teacher.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.domain.model.Quiz
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.feature.quiz.domain.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

data class TeacherSubjectQuizzesUiState(
    val quizzes: List<Quiz> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class TeacherSubjectQuizzesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizRepository: QuizRepository,
) : ViewModel() {

    val route: Routes.TeacherSubjectQuizzes = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(TeacherSubjectQuizzesUiState())
    val uiState: StateFlow<TeacherSubjectQuizzesUiState> = _uiState.asStateFlow()

    init {
        quizRepository.observeQuizzesForSubject(route.subjectId)
            .onEach { quizzes ->
                _uiState.value = _uiState.value.copy(
                    quizzes = quizzes.sortedByDescending { it.dueDateMillis },
                    isLoading = false,
                )
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }
}
