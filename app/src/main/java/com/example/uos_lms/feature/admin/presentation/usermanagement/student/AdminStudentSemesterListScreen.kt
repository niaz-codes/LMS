package com.example.uos_lms.feature.admin.presentation.usermanagement.student

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.ExpandableRow
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.LoadMoreFooter
import com.example.uos_lms.feature.admin.presentation.usermanagement.components.AdminUserSearchFilterBar

/**
 * Bottom of the Student Management hierarchy: shows every Semester in the Department ->
 * Session the admin drilled into from [AdminStudentTreeScreen]; expanding a Semester
 * reveals only the students assigned to that exact Department + Session + Semester.
 */
@Composable
fun AdminStudentSemesterListScreen(
    onBack: () -> Unit,
    onOpenUser: (uid: String) -> Unit,
    viewModel: AdminStudentSemesterListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = if (uiState.departmentName.isBlank() && uiState.sessionLabel.isBlank()) {
                    "Students"
                } else {
                    "${uiState.departmentName} • ${uiState.sessionLabel}"
                },
                onBack = onBack,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            uiState.errorMessage?.let { message ->
                ErrorState(message = message, onDismiss = viewModel::consumeMessages)
            }

            AppTextField(
                value = uiState.semesterSearchQuery,
                onValueChange = viewModel::onSemesterSearchChange,
                label = "Search semesters",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            )

            if (uiState.filteredSemesters.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    message = "No semesters have been created in this department yet.",
                    icon = Icons.Default.Search,
                    modifier = Modifier.fillMaxSize(),
                )
                return@Column
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                uiState.filteredSemesters.forEach { semNode ->
                    item(key = semNode.semester.id) {
                        ExpandableRow(
                            title = semNode.semester.displayName,
                            countLabel = semNode.totalCount?.toString(),
                            isExpanded = semNode.isExpanded,
                            onClick = { viewModel.toggleSemester(semNode.semester.id) },
                        )
                    }

                    if (!semNode.isExpanded) return@forEach

                    val idPrefix = semNode.semester.id
                    item(key = "${idPrefix}_filters") {
                        Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                            AdminUserSearchFilterBar(
                                searchQuery = semNode.searchQuery,
                                onSearchQueryChange = { viewModel.onStudentSearchChange(semNode.semester.id, it) },
                                selectedFilter = semNode.filter,
                                onFilterChange = { viewModel.onStudentFilterChange(semNode.semester.id, it) },
                                sortOption = semNode.sortOption,
                                onSortOptionChange = { viewModel.onStudentSortChange(semNode.semester.id, it) },
                                searchLabel = "Search by name, email, reg. no., or roll no.",
                            )
                        }
                    }

                    when {
                        semNode.isLoadingStudents -> item(key = "${idPrefix}_loading") {
                            Text(
                                "Loading students…",
                                modifier = Modifier.padding(start = 32.dp, top = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        semNode.filteredStudents.isEmpty() -> item(key = "${idPrefix}_empty") {
                            Text(
                                "No students found for this department, session, and semester.",
                                modifier = Modifier.padding(start = 32.dp, top = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                        else -> {
                            items(semNode.visibleStudents, key = { "${idPrefix}_${it.uid}" }) { student ->
                                StudentCard(
                                    student = student,
                                    departmentName = uiState.departmentName,
                                    semesterLabel = semNode.semester.displayName,
                                    sessionLabel = uiState.sessionLabel,
                                    onClick = { onOpenUser(student.uid) },
                                    onApprove = { viewModel.approve(student.uid) },
                                    onReject = { viewModel.reject(student.uid) },
                                    onSuspend = { viewModel.suspend(student.uid) },
                                    onActivate = { viewModel.activate(student.uid) },
                                    onDelete = { viewModel.delete(student.uid, semNode.semester.id) },
                                    onResetPassword = { viewModel.resetPassword(student.email) },
                                    modifier = Modifier.padding(start = 16.dp),
                                )
                            }
                            if (semNode.hasMore) {
                                item(key = "${idPrefix}_more") {
                                    LoadMoreFooter(
                                        remaining = semNode.filteredStudents.size - semNode.visibleCount,
                                        onLoadMore = { viewModel.onStudentLoadMore(semNode.semester.id) },
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
