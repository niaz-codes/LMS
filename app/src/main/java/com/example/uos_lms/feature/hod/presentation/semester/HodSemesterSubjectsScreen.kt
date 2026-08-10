package com.example.uos_lms.feature.hod.presentation.semester

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.SelectDialog
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun HodSemesterSubjectsScreen(
    onBack: () -> Unit,
    viewModel: HodSemesterSubjectsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var assigningSubject by remember { mutableStateOf<Subject?>(null) }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = uiState.semester?.displayName ?: "Semester",
                onBack = onBack,
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.HOD).toList()),
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

            if (uiState.subjects.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    message = "No subjects have been created for this semester yet.",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.subjects, key = { it.id }) { subject ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Row(modifier = Modifier.padding(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Default.MenuBook,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(subject.title, style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        "${subject.code} • ${subject.creditHours} credit hours",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                    Text(
                                        subject.teacherName?.let { "Teacher: $it" } ?: "Unassigned",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (subject.teacherName == null) {
                                            MaterialTheme.colorScheme.error
                                        } else {
                                            MaterialTheme.colorScheme.primary
                                        },
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier.padding(top = 8.dp),
                                    ) {
                                        OutlinedButton(onClick = { assigningSubject = subject }) {
                                            Text(if (subject.teacherUid == null) "Assign Teacher" else "Change Teacher")
                                        }
                                        if (subject.teacherUid != null) {
                                            TextButton(onClick = { viewModel.unassignTeacher(subject.id) }) {
                                                Text("Unassign")
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

    assigningSubject?.let { subject ->
        SelectDialog(
            title = "Assign Teacher",
            options = uiState.teachers,
            itemLabel = { it.fullName },
            onSelect = { teacher ->
                viewModel.assignTeacher(subject.id, teacher)
                assigningSubject = null
            },
            onDismiss = { assigningSubject = null },
            emptyMessage = "No approved teachers in this department yet.",
        )
    }
}
