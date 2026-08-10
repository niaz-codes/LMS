package com.example.uos_lms.feature.admin.presentation.attendance

import android.content.Intent
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.common.dateKeyToDisplay
import com.example.uos_lms.core.domain.model.AttendanceStatus
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.SelectDialog
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun AdminAttendanceReportsScreen(
    onBack: () -> Unit,
    viewModel: AdminAttendanceReportsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showDepartmentPicker by remember { mutableStateOf(false) }
    var showSemesterPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Attendance Reports",
                onBack = onBack,
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.ADMIN).toList()),
            )
        },
        floatingActionButton = {
            if (uiState.records.isNotEmpty()) {
                FloatingActionButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Attendance Report")
                            putExtra(Intent.EXTRA_TEXT, viewModel.buildCsv())
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Export Attendance"))
                    },
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Export CSV")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                FilterChip(
                    selected = uiState.selectedDepartment != null,
                    onClick = { showDepartmentPicker = true },
                    label = { Text(uiState.selectedDepartment?.name ?: "Choose Department") },
                )
                FilterChip(
                    selected = uiState.selectedSemester != null,
                    onClick = { if (uiState.selectedDepartment != null) showSemesterPicker = true },
                    label = { Text(uiState.selectedSemester?.displayName ?: "All Semesters") },
                )
            }

            if (uiState.selectedDepartment == null) {
                EmptyState(message = "Choose a department to view its attendance report.", modifier = Modifier.fillMaxSize())
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "${uiState.percentage}% present",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            "${uiState.presentCount} / ${uiState.records.size} records",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }

                if (uiState.records.isEmpty() && !uiState.isLoading) {
                    EmptyState(message = "No attendance recorded yet.", modifier = Modifier.fillMaxSize())
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(uiState.records, key = { it.id }) { record ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateItem(),
                                shape = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Column {
                                        Text(record.studentName, style = MaterialTheme.typography.titleSmall)
                                        Text(dateKeyToDisplay(record.dateKey), style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text(
                                        if (record.status == AttendanceStatus.PRESENT) "Present" else "Absent",
                                        color = if (record.status == AttendanceStatus.PRESENT) {
                                            MaterialTheme.colorScheme.primary
                                        } else {
                                            MaterialTheme.colorScheme.error
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDepartmentPicker) {
        SelectDialog(
            title = "Choose Department",
            options = uiState.departments,
            itemLabel = { it.name },
            onSelect = { department ->
                viewModel.onDepartmentSelected(department)
                showDepartmentPicker = false
            },
            onDismiss = { showDepartmentPicker = false },
            emptyMessage = "No departments exist yet.",
        )
    }

    if (showSemesterPicker) {
        SelectDialog(
            title = "Filter by Semester",
            options = uiState.semesters,
            itemLabel = { it.displayName },
            onSelect = { semester ->
                viewModel.onSemesterSelected(semester)
                showSemesterPicker = false
            },
            onDismiss = { showSemesterPicker = false },
            emptyMessage = "This department has no semesters yet.",
        )
    }
}
