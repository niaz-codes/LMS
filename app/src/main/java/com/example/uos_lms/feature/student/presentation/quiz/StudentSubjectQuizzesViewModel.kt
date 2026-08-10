package com.example.uos_lms.feature.student.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.domain.model.Quiz
import com.example.uos_lms.core.domain.model.QuizAttempt
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.quiz.domain.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizListItem(
    val quiz: Quiz,
    val attempt: QuizAttempt?,
)

data class StudentSubjectQuizzesUiState(
    val items: List<QuizListItem> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class StudentSubjectQuizzesViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizRepository: QuizRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    val route: Routes.StudentSubjectQuizzes = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(StudentSubjectQuizzesUiState())
    val uiState: StateFlow<StudentSubjectQuizzesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val studentUid = sessionManager.cachedSession.filterNotNull().first().uid
            quizRepository.observeQuizzesForSubject(route.subjectId)
                .flatMapLatest { quizzes ->
                    if (quizzes.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        combine(
                            quizzes.map { quiz ->
                                quizRepository.observeMyAttempt(quiz.id, studentUid)
                                    .map { attempt -> QuizListItem(quiz, attempt) }
                            },
                        ) { items -> items.toList() }
                    }
                }
                .onEach { items ->
                    _uiState.value = _uiState.value.copy(
                        items = items.sortedByDescending { it.quiz.dueDateMillis },
                        isLoading = false,
                    )
                }
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
                .launchIn(this)
        }
    }
}
