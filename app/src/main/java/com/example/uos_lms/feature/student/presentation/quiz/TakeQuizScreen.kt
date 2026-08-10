package com.example.uos_lms.feature.student.presentation.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.QuizAttempt
import com.example.uos_lms.core.domain.model.QuizQuestion
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.LoadingButton
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun TakeQuizScreen(
    onBack: () -> Unit,
    viewModel: TakeQuizViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = uiState.quiz?.title ?: "Quiz",
                onBack = onBack,
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.STUDENT).toList()),
            )
        },
    ) { padding ->
        val quiz = uiState.quiz
        val attempt = uiState.existingAttempt

        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }

            quiz == null -> EmptyState(
                message = uiState.errorMessage ?: "This quiz could not be found.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )

            attempt != null -> QuizResultView(quiz = quiz, attempt = attempt, padding = padding)

            System.currentTimeMillis() > quiz.dueDateMillis -> EmptyState(
                message = "This quiz is closed.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )

            else -> QuizAttemptForm(
                quiz = quiz,
                answers = uiState.answers,
                secondsRemaining = uiState.secondsRemaining,
                isSubmitting = uiState.isSubmitting,
                errorMessage = uiState.errorMessage,
                onSelectAnswer = viewModel::selectAnswer,
                onSubmit = viewModel::submit,
                padding = padding,
            )
        }
    }
}

@Composable
private fun QuizAttemptForm(
    quiz: com.example.uos_lms.core.domain.model.Quiz,
    answers: List<Int?>,
    secondsRemaining: Int,
    isSubmitting: Boolean,
    errorMessage: String?,
    onSelectAnswer: (questionIndex: Int, optionIndex: Int) -> Unit,
    onSubmit: () -> Unit,
    padding: androidx.compose.foundation.layout.PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Text(
                "Time remaining: ${formatSeconds(secondsRemaining)}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(16.dp),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))

        quiz.questions.forEachIndexed { index, question ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "${index + 1}. ${question.text}",
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text("${question.marks} marks", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    question.options.forEachIndexed { optionIndex, option ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            RadioButton(
                                selected = answers.getOrNull(index) == optionIndex,
                                onClick = { onSelectAnswer(index, optionIndex) },
                            )
                            Text(option)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
        }

        LoadingButton(
            text = "Submit",
            isLoading = isSubmitting,
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun QuizResultView(
    quiz: com.example.uos_lms.core.domain.model.Quiz,
    attempt: QuizAttempt,
    padding: androidx.compose.foundation.layout.PaddingValues,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Your Score", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${attempt.effectiveScore}/${attempt.totalMarks}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                attempt.feedback?.takeIf { it.isNotBlank() }?.let {
                    Text(
                        "Feedback: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        quiz.questions.forEachIndexed { index, question ->
            val yourAnswer = attempt.answers.getOrNull(index)
            QuestionResultCard(index = index, question = question, yourAnswer = yourAnswer)
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun QuestionResultCard(index: Int, question: QuizQuestion, yourAnswer: Int?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("${index + 1}. ${question.text}", style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(4.dp))
            question.options.forEachIndexed { optionIndex, option ->
                val isCorrect = optionIndex == question.correctOptionIndex
                val isYourAnswer = optionIndex == yourAnswer
                val color = when {
                    isCorrect -> MaterialTheme.colorScheme.primary
                    isYourAnswer -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Text(
                    when {
                        isCorrect && isYourAnswer -> "$option (your answer — correct)"
                        isCorrect -> "$option (correct answer)"
                        isYourAnswer -> "$option (your answer — incorrect)"
                        else -> option
                    },
                    color = color,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

private fun formatSeconds(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%02d:%02d".format(minutes, seconds)
}
