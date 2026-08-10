package com.example.uos_lms.feature.student.presentation.attendance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.uos_lms.feature.student.presentation.components.StudentBottomNavBar
import com.example.uos_lms.feature.student.presentation.components.StudentTab
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun StudentAttendanceScreen(
    onHomeClick: () -> Unit,
    onSubmissionsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: StudentAttendanceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Attendance",
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.STUDENT).toList()),
            )
        },
        bottomBar = {
            StudentBottomNavBar(
                selected = StudentTab.ATTENDANCE,
                onHomeClick = onHomeClick,
                onAttendanceClick = {},
                onSubmissionsClick = onSubmissionsClick,
                onCalendarClick = onCalendarClick,
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
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "${uiState.overallPercentage}% present overall",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                        Text(
                            "${uiState.presentCount} / ${uiState.totalCount} classes attended",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                    if (uiState.bySubject.isEmpty()) {
                        EmptyState(message = "No attendance recorded yet.", modifier = Modifier.padding(16.dp))
                    } else {
                        SimpleBarChart(
                            title = "Attendance % by Subject",
                            items = uiState.bySubject,
                            valueSuffix = "%",
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}
