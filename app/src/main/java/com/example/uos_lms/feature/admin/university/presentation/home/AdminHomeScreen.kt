package com.example.uos_lms.feature.admin.university.presentation.home

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
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.uos_lms.core.ui.components.GradientHeader
import com.example.uos_lms.core.ui.components.StatCard
import com.example.uos_lms.feature.admin.presentation.components.AdminBottomNavBar
import com.example.uos_lms.feature.admin.presentation.components.AdminTab

@Composable
fun AdminHomeScreen(
    onLogout: () -> Unit,
    onUsersClick: () -> Unit,
    onDepartmentsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onAttendanceReportsClick: () -> Unit,
    onAssignmentMonitorClick: () -> Unit,
    onQuizMonitorClick: () -> Unit,
    onExamResultMonitorClick: () -> Unit,
    onAcademicCalendarClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    viewModel: AdminHomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        bottomBar = {
            AdminBottomNavBar(
                selected = AdminTab.HOME,
                onHomeClick = {},
                onUsersClick = onUsersClick,
                onDepartmentsClick = onDepartmentsClick,
                onReportsClick = onReportsClick,
                onProfileClick = onProfileClick,
            )
        },
    ) { padding ->
        LazyColumn(
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
                        title = "Administrator",
                        subtitle = "Here's what's happening across the university today.",
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
                val stats = listOf(
                    StatInfo(Icons.Default.People, "Total Users", uiState.totalUsers) to
                        (MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer),
                    StatInfo(Icons.Default.HourglassTop, "Pending", uiState.pendingApprovals) to
                        (MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer),
                    StatInfo(Icons.Default.Apartment, "Departments", uiState.totalDepartments) to
                        (MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer),
                    StatInfo(Icons.Default.School, "Students", uiState.totalStudents) to
                        (MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer),
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(stats) { (info, colors) ->
                        val (containerColor, contentColor) = colors
                        StatCard(info.icon, info.label, info.value, containerColor, contentColor)
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
                    AdminActionCard(
                        icon = Icons.Default.Group,
                        title = "Users",
                        subtitle = "HOD, Teacher, and Student hierarchies — approve, suspend, and manage accounts",
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = onUsersClick,
                    )
                    AdminActionCard(
                        icon = Icons.Default.Apartment,
                        title = "Departments",
                        subtitle = "Departments, semesters, and subjects",
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        onClick = onDepartmentsClick,
                    )
                    AdminActionCard(
                        icon = Icons.Default.Assessment,
                        title = "Reports & Analytics",
                        subtitle = "Live stats and charts across the university",
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = onReportsClick,
                    )
                    AdminActionCard(
                        icon = Icons.Default.EventAvailable,
                        title = "Attendance Reports",
                        subtitle = "Department and semester attendance, CSV export",
                        accentColor = MaterialTheme.colorScheme.secondary,
                        onClick = onAttendanceReportsClick,
                    )
                    AdminActionCard(
                        icon = Icons.Default.Assignment,
                        title = "Assignment Monitor",
                        subtitle = "View and delete assignments across all subjects",
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        onClick = onAssignmentMonitorClick,
                    )
                    AdminActionCard(
                        icon = Icons.Default.Quiz,
                        title = "Quiz & Exam Monitor",
                        subtitle = "View quizzes/exams and results across subjects",
                        accentColor = MaterialTheme.colorScheme.primary,
                        onClick = onQuizMonitorClick,
                    )
                    AdminActionCard(
                        icon = Icons.Default.Grade,
                        title = "Exam Result Monitor",
                        subtitle = "Oversee course results and HOD approval status university-wide",
                        accentColor = MaterialTheme.colorScheme.secondary,
                        onClick = onExamResultMonitorClick,
                    )
                    AdminActionCard(
                        icon = Icons.Default.CalendarMonth,
                        title = "Academic Calendar",
                        subtitle = "Holidays, events, and exam schedule markers",
                        accentColor = MaterialTheme.colorScheme.secondary,
                        onClick = onAcademicCalendarClick,
                    )
                    AdminActionCard(
                        icon = Icons.Default.Campaign,
                        title = "Announcements",
                        subtitle = "Post notices to everyone or a department",
                        accentColor = MaterialTheme.colorScheme.tertiary,
                        onClick = onAnnouncementsClick,
                    )
                }
            }
        }
    }
}

private data class StatInfo(val icon: ImageVector, val label: String, val value: Int)

@Composable
private fun AdminActionCard(
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
