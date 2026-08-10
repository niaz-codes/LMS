package com.example.uos_lms.feature.hod.presentation.examresult

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.ExamResult
import com.example.uos_lms.core.domain.model.ResultStatus
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.ResultStatusChip
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun HodExamResultApprovalScreen(
    onBack: () -> Unit,
    viewModel: HodExamResultApprovalViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var rejectTarget by remember { mutableStateOf<ExamResult?>(null) }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessages()
        }
    }

    rejectTarget?.let { target ->
        RejectReasonDialog(
            studentName = target.studentName,
            onConfirm = { reason ->
                viewModel.reject(target, reason)
                rejectTarget = null
            },
            onDismiss = { rejectTarget = null },
        )
    }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Exam Result Approvals",
                onBack = onBack,
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.HOD).toList()),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = uiState.tab.ordinal) {
                Tab(
                    selected = uiState.tab == ResultReviewTab.PENDING,
                    onClick = { viewModel.selectTab(ResultReviewTab.PENDING) },
                    text = { Text("Pending (${uiState.pending.size})") },
                )
                Tab(
                    selected = uiState.tab == ResultReviewTab.ALL,
                    onClick = { viewModel.selectTab(ResultReviewTab.ALL) },
                    text = { Text("All") },
                )
            }

            uiState.errorMessage?.let { message ->
                ErrorState(message = message, onDismiss = viewModel::consumeMessages)
            }

            when {
                uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                uiState.visibleResults.isEmpty() -> EmptyState(
                    message = if (uiState.tab == ResultReviewTab.PENDING) {
                        "No results are waiting for your approval."
                    } else {
                        "No exam results have been submitted in your department yet."
                    },
                    modifier = Modifier.fillMaxSize(),
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(uiState.visibleResults, key = { it.id }) { result ->
                        val isProcessing = uiState.processingResultId == result.id
                        Card(
                            modifier = Modifier.fillMaxWidth().animateItem(),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(result.studentName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            "${result.subjectCode} • ${result.subjectTitle}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                    ResultStatusChip(status = result.status)
                                }
                                Text(
                                    "Marks: ${result.obtainedMarks} / ${result.totalMarks} (${"%.1f".format(result.percentage)}%)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = 8.dp),
                                )
                                Text(
                                    "Teacher: ${result.teacherName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                                if (result.status == ResultStatus.APPROVED) {
                                    Text(
                                        "Grade: ${result.grade} • GPA ${result.gpaPoint}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                                if (result.status == ResultStatus.REJECTED && result.rejectionReason != null) {
                                    Text(
                                        "Reason: ${result.rejectionReason}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.padding(top = 4.dp),
                                    )
                                }
                                if (result.status == ResultStatus.PENDING_APPROVAL) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 10.dp),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    ) {
                                        OutlinedButton(
                                            onClick = { rejectTarget = result },
                                            enabled = !isProcessing,
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Text("Reject")
                                        }
                                        Button(
                                            onClick = { viewModel.approve(result) },
                                            enabled = !isProcessing,
                                            modifier = Modifier.weight(1f),
                                        ) {
                                            Text("Approve")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RejectReasonDialog(
    studentName: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var reason by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reject $studentName's result", fontWeight = FontWeight.SemiBold) },
        text = {
            Column {
                Text(
                    "This sends the result back to the Teacher for correction.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AppTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = "Reason",
                    singleLine = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(reason) }, enabled = reason.isNotBlank()) {
                Text("Reject", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp),
    )
}
