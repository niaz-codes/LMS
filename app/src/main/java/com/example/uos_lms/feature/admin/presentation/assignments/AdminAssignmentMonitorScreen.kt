package com.example.uos_lms.feature.admin.presentation.assignments

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment as AssignmentIcon
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.common.millisToDisplay
import com.example.uos_lms.core.domain.model.Assignment
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.ConfirmDialog
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun AdminAssignmentMonitorScreen(
    onBack: () -> Unit,
    onOpenAssignment: (assignmentId: String, title: String, maxMarks: Int) -> Unit,
    viewModel: AdminAssignmentMonitorViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var deletingAssignment by remember { mutableStateOf<Assignment?>(null) }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Assignment Monitor",
                onBack = onBack,
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.ADMIN).toList()),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            uiState.errorMessage?.let { ErrorState(message = it, onDismiss = viewModel::clearError) }

            if (uiState.assignments.isEmpty() && !uiState.isLoading) {
                EmptyState(message = "No assignments have been created yet.", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.assignments, key = { it.id }) { assignment ->
                        val subject = uiState.subjectsById[assignment.subjectId]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                                .clickable { onOpenAssignment(assignment.id, assignment.title, assignment.maxMarks) },
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(MaterialTheme.colorScheme.tertiaryContainer),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Icon(
                                            Icons.Default.AssignmentIcon,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                            modifier = Modifier.size(20.dp),
                                        )
                                    }
                                    Column(modifier = Modifier.padding(start = 12.dp)) {
                                        Text(assignment.title, style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            subject?.let { "${it.code} • ${it.title}" } ?: "Subject removed",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            "Due ${millisToDisplay(assignment.dueDateMillis)} • ${assignment.maxMarks} marks",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                IconButton(onClick = { deletingAssignment = assignment }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    deletingAssignment?.let { assignment ->
        ConfirmDialog(
            title = "Delete assignment?",
            message = "This removes \"${assignment.title}\" and all its submissions/grades. This cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteAssignment(assignment.id)
                deletingAssignment = null
            },
            onDismiss = { deletingAssignment = null },
        )
    }
}
