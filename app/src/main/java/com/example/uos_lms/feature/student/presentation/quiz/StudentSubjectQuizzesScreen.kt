package com.example.uos_lms.feature.student.presentation.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.common.millisToDisplay
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun StudentSubjectQuizzesScreen(
    onBack: () -> Unit,
    onOpenQuiz: (quizId: String, subjectId: String) -> Unit,
    viewModel: StudentSubjectQuizzesViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Quizzes & Exams",
                onBack = onBack,
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.STUDENT).toList()),
            )
        },
    ) { padding ->
        if (uiState.items.isEmpty() && !uiState.isLoading) {
            EmptyState(
                message = "No quizzes or exams have been posted for this subject yet.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.items, key = { it.quiz.id }) { item ->
                    val quiz = item.quiz
                    val attempt = item.attempt
                    val isClosed = attempt == null && System.currentTimeMillis() > quiz.dueDateMillis
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .clickable { onOpenQuiz(quiz.id, viewModel.route.subjectId) },
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Quiz,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                AssistChip(onClick = {}, enabled = false, label = { Text(quiz.type.name) })
                                Text(quiz.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Due ${millisToDisplay(quiz.dueDateMillis)} • ${quiz.questions.size} questions",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Text(
                                    when {
                                        attempt != null -> "Score: ${attempt.effectiveScore}/${attempt.totalMarks}"
                                        isClosed -> "Closed"
                                        else -> "Not attempted"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = when {
                                        attempt != null -> MaterialTheme.colorScheme.primary
                                        isClosed -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                )
                                attempt?.feedback?.takeIf { it.isNotBlank() }?.let {
                                    Text(
                                        "Feedback: $it",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
