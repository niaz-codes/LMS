package com.example.uos_lms.feature.hod.presentation.teachers

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
import androidx.compose.material.icons.filled.Person
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
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.feature.hod.presentation.components.HodBottomNavBar
import com.example.uos_lms.feature.hod.presentation.components.HodTab
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun HodTeachersScreen(
    onHomeClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onReportsClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HodTeachersViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Teachers",
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.HOD).toList()),
            )
        },
        bottomBar = {
            HodBottomNavBar(
                selected = HodTab.TEACHERS,
                onHomeClick = onHomeClick,
                onTeachersClick = {},
                onStudentsClick = onStudentsClick,
                onAttendanceClick = onAttendanceClick,
                onReportsClick = onReportsClick,
                onProfileClick = onProfileClick,
            )
        },
    ) { padding ->
        if (uiState.teachers.isEmpty() && !uiState.isLoading) {
            EmptyState(
                message = "No approved teachers in your department yet.",
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
                items(uiState.teachers, key = { it.teacher.uid }) { entry ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItem(),
                        shape = RoundedCornerShape(18.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    Text(entry.teacher.fullName, style = MaterialTheme.typography.titleMedium)
                                    Text(entry.teacher.email, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            Text(
                                "${entry.subjectCount} subject(s)",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}
