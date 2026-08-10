package com.example.uos_lms.feature.teacher.presentation.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.AttendanceStatus
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.LoadingButton
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun MarkAttendanceScreen(
    onBack: () -> Unit,
    viewModel: MarkAttendanceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.saved.collect { onBack() }
    }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = uiState.dateLabel,
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
                ErrorState(message = message, onDismiss = {})
            }

            if (uiState.rows.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    message = "No students are enrolled in this semester yet.",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.rows, key = { it.student.uid }) { row ->
                        val isPresent = row.status == AttendanceStatus.PRESENT
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column {
                                    Text(row.student.fullName, style = MaterialTheme.typography.titleMedium)
                                    AssistChip(
                                        onClick = {},
                                        enabled = false,
                                        label = { Text(if (isPresent) "Present" else "Absent") },
                                        colors = AssistChipDefaults.assistChipColors(
                                            disabledContainerColor = if (isPresent) {
                                                MaterialTheme.colorScheme.primaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.errorContainer
                                            },
                                            disabledLabelColor = if (isPresent) {
                                                MaterialTheme.colorScheme.onPrimaryContainer
                                            } else {
                                                MaterialTheme.colorScheme.onErrorContainer
                                            },
                                        ),
                                    )
                                }
                                Switch(
                                    checked = isPresent,
                                    onCheckedChange = { viewModel.toggleStatus(row.student.uid) },
                                )
                            }
                        }
                    }
                }

                LoadingButton(
                    text = "Save Attendance",
                    isLoading = uiState.isSaving,
                    onClick = viewModel::save,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                )
            }
        }
    }
}
