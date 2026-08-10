package com.example.uos_lms.feature.student.presentation.assignment

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.common.millisToDisplay
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.LoadingButton
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun SubmitAssignmentScreen(
    onBack: () -> Unit,
    viewModel: SubmitAssignmentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri -> uri?.let(viewModel::onFilePicked) }

    LaunchedEffect(uiState.submitted) {
        if (uiState.submitted) onBack()
    }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = uiState.assignment?.title ?: "Assignment",
                onBack = onBack,
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.STUDENT).toList()),
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val assignment = uiState.assignment
        val submission = uiState.existingSubmission

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            if (assignment != null) {
                Text(assignment.description, style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Due ${millisToDisplay(assignment.dueDateMillis)} • ${assignment.maxMarks} marks",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (assignment.fileUrl != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Download: ${assignment.fileName ?: "Attachment"}",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { uriHandler.openUri(assignment.fileUrl) },
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (submission != null && submission.isGraded) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(20.dp),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Your Submission",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        submission.textAnswer?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(it)
                        }
                        submission.fileName?.let { name ->
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "File: $name",
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.clickable { submission.fileUrl?.let(uriHandler::openUri) },
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Grade: ${submission.marksObtained}/${assignment?.maxMarks ?: ""}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        submission.feedback?.takeIf { it.isNotBlank() }?.let {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Feedback: $it")
                        }
                    }
                }
            } else {
                if (submission != null) {
                    Text(
                        "Submitted ${millisToDisplay(submission.submittedAt)}. You can resubmit until it's graded.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                AppTextField(
                    value = uiState.textAnswer,
                    onValueChange = viewModel::onTextAnswerChange,
                    label = "Answer / Notes (optional)",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        when {
                            uiState.pickedFileUri != null -> "File selected — tap to change"
                            submission?.fileName != null -> "Attached: ${submission.fileName} — tap to replace"
                            else -> "Attach File"
                        },
                    )
                }
                uiState.uploadProgress?.let { progress ->
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Uploading file… $progress%", style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(24.dp))
                LoadingButton(
                    text = if (submission != null) "Resubmit" else "Submit",
                    isLoading = uiState.isSubmitting,
                    onClick = viewModel::submit,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
