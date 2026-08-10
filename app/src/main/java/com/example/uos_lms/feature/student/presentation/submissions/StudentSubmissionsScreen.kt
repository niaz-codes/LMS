package com.example.uos_lms.feature.student.presentation.submissions

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.common.millisToDisplay
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.feature.student.presentation.components.StudentBottomNavBar
import com.example.uos_lms.feature.student.presentation.components.StudentTab
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun StudentSubmissionsScreen(
    onHomeClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    onProfileClick: () -> Unit,
    onOpenAssignment: (assignmentId: String, subjectId: String, maxMarks: Int) -> Unit,
    viewModel: StudentSubmissionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Submissions",
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.STUDENT).toList()),
            )
        },
        bottomBar = {
            StudentBottomNavBar(
                selected = StudentTab.SUBMISSIONS,
                onHomeClick = onHomeClick,
                onAttendanceClick = onAttendanceClick,
                onSubmissionsClick = {},
                onCalendarClick = onCalendarClick,
                onAnnouncementsClick = onAnnouncementsClick,
                onProfileClick = onProfileClick,
            )
        },
    ) { padding ->
        if (uiState.rows.isEmpty() && !uiState.isLoading) {
            EmptyState(
                message = "No assignments have been posted yet.",
                icon = Icons.Default.Search,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.rows, key = { it.assignment.id }) { row ->
                    val submission = row.submission
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem()
                            .clickable {
                                onOpenAssignment(row.assignment.id, row.assignment.subjectId, row.assignment.maxMarks)
                            },
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.tertiaryContainer),
                                contentAlignment = Alignment.Center,
                            ) {
                                Icon(
                                    Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onTertiaryContainer,
                                    modifier = Modifier.size(20.dp),
                                )
                            }
                            Column(modifier = Modifier.padding(start = 12.dp)) {
                                Text(row.assignment.title, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "Due ${millisToDisplay(row.assignment.dueDateMillis)} • ${row.assignment.maxMarks} marks",
                                    style = MaterialTheme.typography.bodySmall,
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        when {
                                            submission?.isGraded == true ->
                                                "Graded: ${submission.marksObtained}/${row.assignment.maxMarks}"
                                            submission != null -> "Submitted ${millisToDisplay(submission.submittedAt)}"
                                            else -> "Not submitted"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = when {
                                            submission?.isGraded == true -> MaterialTheme.colorScheme.primary
                                            submission != null -> MaterialTheme.colorScheme.onSurfaceVariant
                                            else -> MaterialTheme.colorScheme.error
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
