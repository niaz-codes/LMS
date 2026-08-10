package com.example.uos_lms.feature.teacher.presentation.students

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
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.SelectDialog
import com.example.uos_lms.core.ui.components.StatusChip
import com.example.uos_lms.feature.teacher.presentation.components.TeacherBottomNavBar
import com.example.uos_lms.feature.teacher.presentation.components.TeacherTab
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun TeacherStudentsScreen(
    onHomeClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onReportsClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: TeacherStudentsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSubjectPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Students",
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.TEACHER).toList()),
            )
        },
        bottomBar = {
            TeacherBottomNavBar(
                selected = TeacherTab.STUDENTS,
                onHomeClick = onHomeClick,
                onStudentsClick = {},
                onAttendanceClick = onAttendanceClick,
                onReportsClick = onReportsClick,
                onAnnouncementsClick = onAnnouncementsClick,
                onProfileClick = onProfileClick,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            AppTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = "Search by name, email, or CNIC",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )
            Row(modifier = Modifier.padding(horizontal = 16.dp)) {
                FilterChip(
                    selected = uiState.selectedSubject != null,
                    onClick = { showSubjectPicker = true },
                    label = { Text(uiState.selectedSubject?.let { "${it.code} • ${it.title}" } ?: "All Subjects") },
                )
            }
            Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(
                    "${uiState.filteredStudents.size} student(s)",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (uiState.filteredStudents.isEmpty() && !uiState.isLoading) {
                EmptyState(message = "No students found", icon = Icons.Default.Search)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.filteredStudents, key = { it.uid }) { student ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            shape = RoundedCornerShape(18.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.secondaryContainer),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        Icons.Default.Person,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Column(modifier = Modifier.padding(start = 12.dp)) {
                                    Text(student.fullName, style = MaterialTheme.typography.titleMedium)
                                    Text(student.email, style = MaterialTheme.typography.bodySmall)
                                    StatusChip(status = student.status)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSubjectPicker) {
        SelectDialog(
            title = "Filter by Subject",
            options = uiState.subjects,
            itemLabel = { "${it.code} • ${it.title}" },
            onSelect = { subject ->
                viewModel.onSubjectSelected(subject)
                showSubjectPicker = false
            },
            onDismiss = { showSubjectPicker = false },
            emptyMessage = "You have no assigned subjects yet.",
        )
    }
}
