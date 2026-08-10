package com.example.uos_lms.feature.teacher.presentation.quiz

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.QuizQuestion
import com.example.uos_lms.core.domain.model.QuizType
import com.example.uos_lms.core.navigation.Routes
import com.example.uos_lms.core.session.SessionManager
import com.example.uos_lms.feature.quiz.domain.repository.QuizRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class QuestionDraft(
    val text: String = "",
    val options: List<String> = listOf("", "", "", ""),
    val correctOptionIndex: Int? = null,
    val marksText: String = "",
)

data class CreateQuizUiState(
    val title: String = "",
    val description: String = "",
    val type: QuizType = QuizType.QUIZ,
    val timeLimitText: String = "",
    val dueDateMillis: Long? = null,
    val questions: List<QuestionDraft> = listOf(QuestionDraft()),
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
)

@HiltViewModel
class CreateQuizViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val quizRepository: QuizRepository,
    private val sessionManager: SessionManager,
) : ViewModel() {

    private val route: Routes.CreateQuiz = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(CreateQuizUiState())
    val uiState: StateFlow<CreateQuizUiState> = _uiState.asStateFlow()

    private val _created = Channel<Unit>()
    val created = _created.receiveAsFlow()

    fun onTitleChange(value: String) {
        _uiState.value = _uiState.value.copy(title = value, errorMessage = null)
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun onTypeChange(type: QuizType) {
        _uiState.value = _uiState.value.copy(type = type)
    }

    fun onTimeLimitChange(value: String) {
        _uiState.value = _uiState.value.copy(timeLimitText = value, errorMessage = null)
    }

    fun onDueDateChange(millis: Long) {
        _uiState.value = _uiState.value.copy(dueDateMillis = millis, errorMessage = null)
    }

    fun addQuestion() {
        _uiState.value = _uiState.value.copy(questions = _uiState.value.questions + QuestionDraft())
    }

    fun removeQuestion(index: Int) {
        val questions = _uiState.value.questions.toMutableList()
        if (questions.size <= 1) return
        questions.removeAt(index)
        _uiState.value = _uiState.value.copy(questions = questions)
    }

    fun onQuestionTextChange(index: Int, value: String) {
        updateQuestion(index) { it.copy(text = value) }
    }

    fun onOptionChange(index: Int, optionIndex: Int, value: String) {
        updateQuestion(index) { question ->
            val options = question.options.toMutableList()
            options[optionIndex] = value
            question.copy(options = options)
        }
    }

    fun onCorrectOptionChange(index: Int, optionIndex: Int) {
        updateQuestion(index) { it.copy(correctOptionIndex = optionIndex) }
    }

    fun onMarksChange(index: Int, value: String) {
        updateQuestion(index) { it.copy(marksText = value) }
    }

    private fun updateQuestion(index: Int, transform: (QuestionDraft) -> QuestionDraft) {
        val questions = _uiState.value.questions.toMutableList()
        questions[index] = transform(questions[index])
        _uiState.value = _uiState.value.copy(questions = questions, errorMessage = null)
    }

    fun save() {
        val state = _uiState.value
        val timeLimitMinutes = state.timeLimitText.trim().toIntOrNull()
        val dueDateMillis = state.dueDateMillis
        if (state.title.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Title is required")
            return
        }
        if (dueDateMillis == null) {
            _uiState.value = state.copy(errorMessage = "Pick a due date")
            return
        }
        if (timeLimitMinutes == null || timeLimitMinutes <= 0) {
            _uiState.value = state.copy(errorMessage = "Enter a valid time limit in minutes")
            return
        }
        val questions = mutableListOf<QuizQuestion>()
        for ((index, draft) in state.questions.withIndex()) {
            if (draft.text.isBlank()) {
                _uiState.value = state.copy(errorMessage = "Question ${index + 1} needs text")
                return
            }
            if (draft.options.any { it.isBlank() }) {
                _uiState.value = state.copy(errorMessage = "Question ${index + 1} needs all 4 options filled in")
                return
            }
            val correctOptionIndex = draft.correctOptionIndex
            if (correctOptionIndex == null) {
                _uiState.value = state.copy(errorMessage = "Question ${index + 1} needs a correct answer selected")
                return
            }
            val marks = draft.marksText.trim().toIntOrNull()
            if (marks == null || marks <= 0) {
                _uiState.value = state.copy(errorMessage = "Question ${index + 1} needs valid marks")
                return
            }
            questions.add(QuizQuestion(draft.text.trim(), draft.options.map { it.trim() }, correctOptionIndex, marks))
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, errorMessage = null)
            val teacherUid = sessionManager.cachedSession.filterNotNull().first().uid
            when (
                val result = quizRepository.createQuiz(
                    subjectId = route.subjectId,
                    departmentId = route.departmentId,
                    semesterId = route.semesterId,
                    title = state.title.trim(),
                    description = state.description.trim(),
                    type = state.type,
                    questions = questions,
                    timeLimitMinutes = timeLimitMinutes,
                    dueDateMillis = dueDateMillis,
                    createdBy = teacherUid,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = _uiState.value.copy(isSaving = false)
                    _created.send(Unit)
                }
                is AppResult.Error ->
                    _uiState.value = _uiState.value.copy(isSaving = false, errorMessage = result.message)
            }
        }
    }
}
