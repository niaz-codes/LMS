package com.example.uos_lms.feature.teacher.presentation.examresult

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.LoadingButton
import com.example.uos_lms.core.ui.components.ResultStatusChip
import androidx.compose.ui.graphics.Brush
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun TeacherExamResultScreen(
    onBack: () -> Unit,
    viewModel: TeacherExamResultViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.actionMessage) {
        uiState.actionMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessages()
        }
    }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Exam Result Entry",
                onBack = onBack,
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.TEACHER).toList()),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = viewModel::saveDraft,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f),
                ) {
                    Text("Save Draft")
                }
                LoadingButton(
                    text = "Submit for HOD Approval",
                    onClick = viewModel::submitForApproval,
                    isLoading = uiState.isSaving,
                    enabled = uiState.canSubmit && !uiState.isSaving,
                    modifier = Modifier.weight(1f),
                )
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            uiState.errorMessage?.let { message ->
                ErrorState(message = message, onDismiss = viewModel::consumeMessages)
            }

            when {
                uiState.isLoading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                uiState.rows.isEmpty() -> EmptyState(
                    message = "No approved students found in this subject's department/semester yet.",
                    modifier = Modifier.fillMaxSize(),
                )
                else -> {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text("Total Marks", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            AppTextField(
                                value = uiState.totalMarksInput,
                                onValueChange = viewModel::updateTotalMarks,
                                label = "Total",
                                keyboardType = KeyboardType.Number,
                                enabled = !uiState.isTotalMarksLocked,
                                modifier = Modifier.width(110.dp),
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.rows, key = { it.student.uid }) { row ->
                            Card(
                                modifier = Modifier.fillMaxWidth().animateItem(),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(row.student.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            row.student.rollNumber ?: row.student.email,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        row.existing?.let { existing ->
                                            if (existing.status.name != "DRAFT") {
                                                Box(modifier = Modifier.padding(top = 4.dp)) {
                                                    ResultStatusChip(status = existing.status)
                                                }
                                            }
                                            if (existing.rejectionReason != null) {
                                                Text(
                                                    "Reason: ${existing.rejectionReason}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.error,
                                                    modifier = Modifier.padding(top = 4.dp),
                                                )
                                            }
                                        }
                                    }
                                    AppTextField(
                                        value = row.marksInput,
                                        onValueChange = { viewModel.updateMarks(row.student.uid, it) },
                                        label = "Marks",
                                        keyboardType = KeyboardType.Number,
                                        enabled = row.isEditable,
                                        modifier = Modifier.width(90.dp),
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
