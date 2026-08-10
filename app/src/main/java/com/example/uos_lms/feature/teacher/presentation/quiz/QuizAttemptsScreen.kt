package com.example.uos_lms.feature.teacher.presentation.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.common.millisToDisplay
import com.example.uos_lms.core.domain.model.QuizAttempt
import com.example.uos_lms.core.domain.model.QuizType
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun QuizAttemptsScreen(
    onBack: () -> Unit,
    viewModel: QuizAttemptsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var gradingAttempt by remember { mutableStateOf<QuizAttempt?>(null) }
    val isExam = uiState.quizType == QuizType.EXAM

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = viewModel.route.title,
                onBack = onBack,
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.TEACHER).toList()),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            uiState.errorMessage?.let { message ->
                ErrorState(message = message, onDismiss = viewModel::clearError)
            }

            if (uiState.attempts.isEmpty() && !uiState.isLoading) {
                EmptyState(message = "No attempts yet.", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.attempts, key = { it.id }) { attempt ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column {
                                        Text(attempt.studentName, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            "Submitted ${millisToDisplay(attempt.submittedAt)}",
                                            style = MaterialTheme.typography.bodySmall,
                                        )
                                    }
                                    Text(
                                        "${attempt.effectiveScore}/${attempt.totalMarks}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                    )
                                }
                                attempt.feedback?.takeIf { it.isNotBlank() }?.let {
                                    Text(
                                        "Feedback: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                                if (isExam) {
                                    OutlinedButton(
                                        onClick = { gradingAttempt = attempt },
                                        modifier = Modifier.padding(top = 8.dp),
                                    ) {
                                        Text(if (attempt.isManuallyGraded) "Update Grade" else "Grade")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    gradingAttempt?.let { attempt ->
        GradeAttemptDialog(
            attempt = attempt,
            totalMarks = viewModel.route.totalMarks,
            onDismiss = { gradingAttempt = null },
            onSave = { marks, feedback ->
                viewModel.gradeAttempt(attempt.id, marks, feedback)
                gradingAttempt = null
            },
        )
    }
}

@Composable
private fun GradeAttemptDialog(
    attempt: QuizAttempt,
    totalMarks: Int,
    onDismiss: () -> Unit,
    onSave: (marks: Int, feedback: String) -> Unit,
) {
    var marksText by remember { mutableStateOf((attempt.manualScore ?: attempt.score).toString()) }
    var feedback by remember { mutableStateOf(attempt.feedback ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Grade Exam") },
        text = {
            Column {
                AppTextField(
                    value = marksText,
                    onValueChange = { marksText = it },
                    label = "Marks (out of $totalMarks)",
                    keyboardType = KeyboardType.Number,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = feedback,
                    onValueChange = { feedback = it },
                    label = "Feedback (optional)",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                )
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val marks = marksText.trim().toIntOrNull()
                if (marks == null || marks < 0 || marks > totalMarks) {
                    error = "Enter marks between 0 and $totalMarks"
                    return@TextButton
                }
                onSave(marks, feedback.trim())
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
