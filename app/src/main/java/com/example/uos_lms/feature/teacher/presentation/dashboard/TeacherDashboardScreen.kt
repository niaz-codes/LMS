package com.example.uos_lms.feature.teacher.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.GradientHeader
import com.example.uos_lms.core.ui.components.StatCard
import com.example.uos_lms.feature.teacher.presentation.components.TeacherBottomNavBar
import com.example.uos_lms.feature.teacher.presentation.components.TeacherTab

@Composable
fun TeacherDashboardScreen(
    onLogout: () -> Unit,
    onTakeAttendance: (subjectId: String, departmentId: String, semesterId: String) -> Unit,
    onAssignments: (subjectId: String, departmentId: String, semesterId: String) -> Unit,
    onQuizzes: (subjectId: String, departmentId: String, semesterId: String) -> Unit,
    onMaterials: (subjectId: String, departmentId: String, semesterId: String) -> Unit,
    onExamResults: (subjectId: String, subjectCode: String, subjectTitle: String, creditHours: Int, departmentId: String, semesterId: String) -> Unit,
    onStudentsClick: () -> Unit,
    onAttendanceReportsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: TeacherDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.departmentName.ifBlank { "Teacher Dashboard" }, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            TeacherBottomNavBar(
                selected = TeacherTab.HOME,
                onHomeClick = {},
                onStudentsClick = onStudentsClick,
                onAttendanceClick = onAttendanceReportsClick,
                onReportsClick = onReportsClick,
                onAnnouncementsClick = onAnnouncementsClick,
                onProfileClick = onProfileClick,
            )
        },
    ) { padding ->
        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            uiState.departmentId == null -> EmptyState(
                message = "You haven't been assigned to a department yet. Contact the Administrator.",
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    AnimatedVisibility(
                        visibleState = remember { MutableTransitionState(false).apply { targetState = true } },
                        enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { -it / 3 },
                    ) {
                        GradientHeader(
                            greeting = "Welcome back,",
                            title = uiState.fullName,
                            subtitle = "Here are your assigned subjects.",
                        )
                    }
                }

                item {
                    Text(
                        "Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp),
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            StatCard(
                                icon = Icons.Default.MenuBook,
                                label = "Subjects",
                                value = uiState.subjects.size,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                    }
                }

                item {
                    Text(
                        "My Subjects",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp),
                    )
                }

                if (uiState.subjects.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No subjects have been assigned to you yet.",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    items(uiState.subjects, key = { it.subject.id }) { assigned ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .animateItem(),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                        Text(
                                            assigned.subject.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            "${assigned.subject.code} • ${assigned.subject.creditHours} credit hours",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            assigned.semesterLabel,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier
                                        .padding(top = 8.dp)
                                        .horizontalScroll(rememberScrollState()),
                                ) {
                                    AssistChip(
                                        onClick = {
                                            onTakeAttendance(
                                                assigned.subject.id,
                                                assigned.subject.departmentId,
                                                assigned.subject.semesterId,
                                            )
                                        },
                                        label = { Text("Attendance") },
                                        leadingIcon = {
                                            Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                                        },
                                    )
                                    AssistChip(
                                        onClick = {
                                            onAssignments(
                                                assigned.subject.id,
                                                assigned.subject.departmentId,
                                                assigned.subject.semesterId,
                                            )
                                        },
                                        label = { Text("Assignments") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                                        },
                                    )
                                    AssistChip(
                                        onClick = {
                                            onQuizzes(
                                                assigned.subject.id,
                                                assigned.subject.departmentId,
                                                assigned.subject.semesterId,
                                            )
                                        },
                                        label = { Text("Quizzes") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                                        },
                                    )
                                    AssistChip(
                                        onClick = {
                                            onMaterials(
                                                assigned.subject.id,
                                                assigned.subject.departmentId,
                                                assigned.subject.semesterId,
                                            )
                                        },
                                        label = { Text("Materials") },
                                        leadingIcon = {
                                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                                        },
                                    )
                                    AssistChip(
                                        onClick = {
                                            onExamResults(
                                                assigned.subject.id,
                                                assigned.subject.code,
                                                assigned.subject.title,
                                                assigned.subject.creditHours,
                                                assigned.subject.departmentId,
                                                assigned.subject.semesterId,
                                            )
                                        },
                                        label = { Text("Exam Result") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Grade, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
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
}
