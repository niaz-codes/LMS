package com.example.uos_lms.feature.teacher.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.QuizAttempt
import com.example.uos_lms.core.domain.model.QuizType
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.quiz.domain.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuizAttemptsUiState(
    val attempts: List<QuizAttempt> = emptyList(),
    val quizType: QuizType? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

@HiltViewModel
class QuizAttemptsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizRepository: QuizRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    val route: Routes.QuizAttempts = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(QuizAttemptsUiState())
    val uiState: StateFlow<QuizAttemptsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = quizRepository.getQuiz(route.quizId)) {
                is AppResult.Success -> _uiState.value = _uiState.value.copy(quizType = result.data?.type)
                is AppResult.Error -> Unit
            }
        }

        quizRepository.observeAttemptsForQuiz(route.quizId)
            .onEach { attempts ->
                _uiState.value = _uiState.value.copy(
                    attempts = attempts.sortedBy { it.studentName },
                    isLoading = false,
                )
            }
            .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
            .launchIn(viewModelScope)
    }

    fun gradeAttempt(attemptId: String, manualScore: Int, feedback: String) {
        viewModelScope.launch {
            val teacherUid = sessionManager.cachedSession.filterNotNull().first().uid
            when (val result = quizRepository.gradeAttempt(attemptId, manualScore, feedback, teacherUid)) {
                is AppResult.Error -> _uiState.value = _uiState.value.copy(errorMessage = result.message)
                is AppResult.Success -> Unit
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}
