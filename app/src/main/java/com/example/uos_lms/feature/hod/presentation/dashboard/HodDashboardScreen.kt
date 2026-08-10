package com.example.uos_lms.feature.hod.presentation.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.GradientHeader
import com.example.uos_lms.core.ui.components.StatCard
import com.example.uos_lms.feature.hod.presentation.components.HodBottomNavBar
import com.example.uos_lms.feature.hod.presentation.components.HodTab

@Composable
fun HodDashboardScreen(
    onLogout: () -> Unit,
    onSemesterClick: (departmentId: String, semesterId: String) -> Unit,
    onTeachersClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onReportsClick: () -> Unit,
    onAssignmentMonitorClick: () -> Unit,
    onQuizMonitorClick: () -> Unit,
    onExamResultApprovalsClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HodDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.departmentName.ifBlank { "HOD Dashboard" }, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.logout(onLogout) }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
            )
        },
        bottomBar = {
            HodBottomNavBar(
                selected = HodTab.HOME,
                onHomeClick = {},
                onTeachersClick = onTeachersClick,
                onStudentsClick = onStudentsClick,
                onAttendanceClick = onAttendanceClick,
                onReportsClick = onReportsClick,
                onProfileClick = onProfileClick,
            )
        },
    ) { padding ->
        val departmentId = uiState.departmentId

        when {
            uiState.isLoading -> Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            departmentId == null -> EmptyState(
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
                            subtitle = "Head of Department",
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
                                icon = Icons.Default.CalendarMonth,
                                label = "Semesters",
                                value = uiState.semesters.size,
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                        }
                        item {
                            StatCard(
                                icon = Icons.Default.Group,
                                label = "Teachers",
                                value = uiState.teacherCount,
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                            )
                        }
                        item {
                            StatCard(
                                icon = Icons.Default.School,
                                label = "Students",
                                value = uiState.studentCount,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }
                    }
                }

                item {
                    Text(
                        "Manage",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp),
                    )
                }

                item {
                    Column(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        HodActionCard(
                            icon = Icons.Default.Group,
                            title = "Teachers",
                            subtitle = "View teachers and their subject load",
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = onTeachersClick,
                        )
                        HodActionCard(
                            icon = Icons.Default.School,
                            title = "Students",
                            subtitle = "Semester-wise student lists",
                            accentColor = MaterialTheme.colorScheme.secondary,
                            onClick = onStudentsClick,
                        )
                        HodActionCard(
                            icon = Icons.Default.EventAvailable,
                            title = "Attendance",
                            subtitle = "Semester attendance reports, CSV export",
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            onClick = onAttendanceClick,
                        )
                        HodActionCard(
                            icon = Icons.Default.Assessment,
                            title = "Reports",
                            subtitle = "Department analytics and charts",
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = onReportsClick,
                        )
                        HodActionCard(
                            icon = Icons.Default.Assignment,
                            title = "Assignment Monitor",
                            subtitle = "Monitor submissions across your department",
                            accentColor = MaterialTheme.colorScheme.secondary,
                            onClick = onAssignmentMonitorClick,
                        )
                        HodActionCard(
                            icon = Icons.Default.Quiz,
                            title = "Quiz & Exam Monitor",
                            subtitle = "Monitor quizzes/exams and results",
                            accentColor = MaterialTheme.colorScheme.tertiary,
                            onClick = onQuizMonitorClick,
                        )
                        HodActionCard(
                            icon = Icons.Default.Grade,
                            title = "Exam Result Approvals",
                            subtitle = "Review and approve/reject teacher-submitted results",
                            accentColor = MaterialTheme.colorScheme.primary,
                            onClick = onExamResultApprovalsClick,
                        )
                    }
                }

                item {
                    Text(
                        "Semesters",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(start = 20.dp, top = 24.dp, bottom = 8.dp),
                    )
                }

                if (uiState.semesters.isEmpty()) {
                    item {
                        EmptyState(
                            message = "No semesters have been created in your department yet.",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                } else {
                    items(uiState.semesters, key = { it.id }) { semester ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp, vertical = 6.dp)
                                .clickable { onSemesterClick(departmentId, semester.id) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(semester.displayName, style = MaterialTheme.typography.titleMedium)
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HodActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    accentColor: Color,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale = if (isPressed) 0.98f else 1f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = accentColor)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Icon(
                Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
