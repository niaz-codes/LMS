package com.example.uos_lms.feature.student.presentation.dashboard

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
import androidx.compose.material.icons.filled.HourglassTop
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
import com.example.uos_lms.feature.student.presentation.components.StudentBottomNavBar
import com.example.uos_lms.feature.student.presentation.components.StudentTab

@Composable
fun StudentDashboardScreen(
    onLogout: () -> Unit,
    onViewAttendance: (subjectId: String) -> Unit,
    onAssignments: (subjectId: String) -> Unit,
    onQuizzes: (subjectId: String) -> Unit,
    onMaterials: (subjectId: String) -> Unit,
    onResultsClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onSubmissionsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: StudentDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.semesterLabel.ifBlank { "Student Dashboard" }, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            StudentBottomNavBar(
                selected = StudentTab.HOME,
                onHomeClick = {},
                onAttendanceClick = onAttendanceClick,
                onSubmissionsClick = onSubmissionsClick,
                onCalendarClick = onCalendarClick,
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
            uiState.semesterId == null -> EmptyState(
                message = "You haven't been assigned to a semester yet. Contact the Administrator.",
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
                            subtitle = uiState.departmentName.ifBlank { null },
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
                        item {
                            StatCard(
                                icon = Icons.Default.EventAvailable,
                                label = "Attendance %",
                                value = uiState.attendancePercentage,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                        item {
                            StatCard(
                                icon = Icons.Default.HourglassTop,
                                label = "Pending",
                                value = uiState.pendingAssignments,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
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
                            message = "No subjects have been added to your semester yet.",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    items(uiState.subjects, key = { it.id }) { subject ->
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
                                            subject.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                        )
                                        Text(
                                            "${subject.code} • ${subject.creditHours} credit hours",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                        Text(
                                            subject.teacherName?.let { "Teacher: $it" } ?: "Teacher not yet assigned",
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
                                        onClick = { onViewAttendance(subject.id) },
                                        label = { Text("Attendance") },
                                        leadingIcon = {
                                            Icon(Icons.Default.EventAvailable, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                                        },
                                    )
                                    AssistChip(
                                        onClick = { onAssignments(subject.id) },
                                        label = { Text("Assignments") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Assignment, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                                        },
                                    )
                                    AssistChip(
                                        onClick = { onQuizzes(subject.id) },
                                        label = { Text("Quizzes") },
                                        leadingIcon = {
                                            Icon(Icons.Default.Quiz, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                                        },
                                    )
                                    AssistChip(
                                        onClick = { onMaterials(subject.id) },
                                        label = { Text("Materials") },
                                        leadingIcon = {
                                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(AssistChipDefaults.IconSize))
                                        },
                                    )
                                    AssistChip(
                                        onClick = onResultsClick,
                                        label = { Text("Result") },
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
