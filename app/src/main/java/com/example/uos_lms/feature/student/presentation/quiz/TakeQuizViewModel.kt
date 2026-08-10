package com.example.uos_lms.feature.student.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Quiz
import com.example.uos_lms.core.domain.model.QuizAttempt
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.quiz.domain.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

data class TakeQuizUiState(
    val quiz: Quiz? = null,
    val existingAttempt: QuizAttempt? = null,
    val answers: List<Int?> = emptyList(),
    val secondsRemaining: Int = 0,
    val isLoading: Boolean = true,
    val isSubmitting: Boolean = false,
    val submitted: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class TakeQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizRepository: QuizRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val route: Routes.TakeQuiz = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(TakeQuizUiState())
    val uiState: StateFlow<TakeQuizUiState> = _uiState.asStateFlow()

    private var studentUid: String? = null
    private var studentName: String = ""
    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val session = sessionManager.cachedSession.filterNotNull().first()
            studentUid = session.uid
            studentName = session.fullName

            when (val result = quizRepository.getQuiz(route.quizId)) {
                is AppResult.Success -> {
                    val quiz = result.data
                    _uiState.value = _uiState.value.copy(
                        quiz = quiz,
                        answers = quiz?.questions?.map { null } ?: emptyList(),
                    )
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = result.message)
            }

            quizRepository.observeMyAttempt(route.quizId, session.uid)
                .onEach { attempt ->
                    _uiState.value = _uiState.value.copy(existingAttempt = attempt, isLoading = false)
                    maybeStartTimer()
                }
                .catch { e -> _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = e.message) }
                .launchIn(viewModelScope)
        }
    }

    private fun maybeStartTimer() {
        if (timerJob != null) return
        val state = _uiState.value
        val quiz = state.quiz ?: return
        if (state.existingAttempt != null) return
        if (System.currentTimeMillis() > quiz.dueDateMillis) return

        var secondsLeft = quiz.timeLimitMinutes * 60
        _uiState.value = _uiState.value.copy(secondsRemaining = secondsLeft)
        timerJob = viewModelScope.launch {
            while (secondsLeft > 0) {
                delay(1000)
                secondsLeft--
                _uiState.value = _uiState.value.copy(secondsRemaining = secondsLeft)
            }
            submit()
        }
    }

    fun selectAnswer(questionIndex: Int, optionIndex: Int) {
        val answers = _uiState.value.answers.toMutableList()
        answers[questionIndex] = optionIndex
        _uiState.value = _uiState.value.copy(answers = answers)
    }

    fun submit() {
        val uid = studentUid ?: return
        val quiz = _uiState.value.quiz ?: return
        if (_uiState.value.isSubmitting || _uiState.value.existingAttempt != null) return
        timerJob?.cancel()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSubmitting = true, errorMessage = null)
            when (val result = quizRepository.submitAttempt(quiz, uid, studentName, _uiState.value.answers)) {
                is AppResult.Success ->
                    _uiState.value = _uiState.value.copy(isSubmitting = false, submitted = true)
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isSubmitting = false, errorMessage = result.message)
            }
        }
    }
}
