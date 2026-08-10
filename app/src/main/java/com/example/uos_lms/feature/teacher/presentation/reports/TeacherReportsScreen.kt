package com.example.uos_lms.feature.teacher.presentation.reports

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.SimpleBarChart
import com.example.uos_lms.core.ui.components.StatCard
import com.example.uos_lms.feature.teacher.presentation.components.TeacherBottomNavBar
import com.example.uos_lms.feature.teacher.presentation.components.TeacherTab
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun TeacherReportsScreen(
    onHomeClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: TeacherReportsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Reports",
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.TEACHER).toList()),
            )
        },
        bottomBar = {
            TeacherBottomNavBar(
                selected = TeacherTab.REPORTS,
                onHomeClick = onHomeClick,
                onStudentsClick = onStudentsClick,
                onAttendanceClick = onAttendanceClick,
                onReportsClick = {},
                onAnnouncementsClick = onAnnouncementsClick,
                onProfileClick = onProfileClick,
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

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
                    Column {
                        Text(
                            "Overview",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp),
                        )
                        val stats = listOf(
                            Triple(Icons.Default.MenuBook, "Subjects", uiState.totalSubjects) to
                                (MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer),
                            Triple(Icons.Default.School, "Students", uiState.totalStudents) to
                                (MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer),
                            Triple(Icons.Default.Assignment, "Assignments", uiState.totalAssignments) to
                                (MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer),
                            Triple(Icons.Default.Quiz, "Quizzes", uiState.totalQuizzes) to
                                (MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.onErrorContainer),
                        )
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(stats.size) { index ->
                                val (info, colors) = stats[index]
                                val (icon, label, value) = info
                                val (containerColor, contentColor) = colors
                                StatCard(icon, label, value, containerColor, contentColor)
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    if (uiState.attendanceBySubject.isEmpty()) {
                        EmptyState(message = "No attendance data yet.", modifier = Modifier.padding(16.dp))
                    } else {
                        SimpleBarChart(
                            title = "Attendance % by Subject",
                            items = uiState.attendanceBySubject,
                            valueSuffix = "%",
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                ) {
                    if (uiState.performanceBySubject.isEmpty()) {
                        EmptyState(message = "No graded work yet.", modifier = Modifier.padding(16.dp))
                    } else {
                        SimpleBarChart(
                            title = "Performance % by Subject",
                            items = uiState.performanceBySubject,
                            valueSuffix = "%",
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}
