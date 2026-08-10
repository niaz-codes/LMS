package com.example.uos_lms.feature.teacher.presentation.assignment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.AssignmentSubmission
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun AssignmentSubmissionsScreen(
    onBack: () -> Unit,
    viewModel: AssignmentSubmissionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var gradingSubmission by remember { mutableStateOf<AssignmentSubmission?>(null) }
    val uriHandler = LocalUriHandler.current

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

            if (uiState.submissions.isEmpty() && !uiState.isLoading) {
                EmptyState(message = "No submissions yet.", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.submissions, key = { it.id }) { submission ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(submission.studentName, style = MaterialTheme.typography.titleMedium)
                                submission.textAnswer?.let {
                                    Text(it, style = MaterialTheme.typography.bodySmall)
                                }
                                submission.fileName?.let { name ->
                                    Text(
                                        "File: $name",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable {
                                            submission.fileUrl?.let(uriHandler::openUri)
                                        },
                                    )
                                }
                                Text(
                                    if (submission.isGraded) {
                                        "Graded: ${submission.marksObtained}/${viewModel.route.maxMarks}"
                                    } else {
                                        "Pending grading"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (submission.isGraded) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.error
                                    },
                                )
                                OutlinedButton(
                                    onClick = { gradingSubmission = submission },
                                    modifier = Modifier.padding(top = 8.dp),
                                ) {
                                    Text(if (submission.isGraded) "Update Grade" else "Grade")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    gradingSubmission?.let { submission ->
        GradeDialog(
            submission = submission,
            maxMarks = viewModel.route.maxMarks,
            onDismiss = { gradingSubmission = null },
            onSave = { marks, feedback ->
                viewModel.grade(submission, marks, feedback)
                gradingSubmission = null
            },
        )
    }
}

@Composable
private fun GradeDialog(
    submission: AssignmentSubmission,
    maxMarks: Int,
    onDismiss: () -> Unit,
    onSave: (marks: Int, feedback: String) -> Unit,
) {
    var marksText by remember { mutableStateOf(submission.marksObtained?.toString() ?: "") }
    var feedback by remember { mutableStateOf(submission.feedback ?: "") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Grade Submission") },
        text = {
            Column {
                AppTextField(
                    value = marksText,
                    onValueChange = { marksText = it },
                    label = "Marks (out of $maxMarks)",
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
                if (marks == null || marks < 0 || marks > maxMarks) {
                    error = "Enter marks between 0 and $maxMarks"
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
