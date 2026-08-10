package com.example.uos_lms.feature.admin.presentation.usermanagement.teacher

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.ExpandableRow
import com.example.uos_lms.core.ui.components.LoadMoreFooter
import com.example.uos_lms.core.ui.components.StatusChip
import com.example.uos_lms.core.ui.components.TreeStatsRow
import com.example.uos_lms.core.ui.components.UserAvatar
import com.example.uos_lms.core.ui.components.UserQuickActionsRow
import com.example.uos_lms.feature.admin.presentation.usermanagement.components.AdminUserSearchFilterBar

@Composable
fun AdminTeacherTreeScreen(
    onOpenUser: (uid: String) -> Unit,
    viewModel: AdminTeacherTreeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        uiState.errorMessage?.let { message ->
            ErrorState(message = message, onDismiss = viewModel::consumeMessages)
        }

        TreeStatsRow(
            stats = listOf(
                "Departments" to uiState.nodes.size.toLong(),
                "Teachers" to uiState.totalTeachers,
            ),
        )

        AppTextField(
            value = uiState.departmentSearchQuery,
            onValueChange = viewModel::onDepartmentSearchChange,
            label = "Search departments",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )

        if (uiState.filteredNodes.isEmpty() && !uiState.isLoadingDepartments) {
            EmptyState(message = "No departments found", icon = Icons.Default.Search, modifier = Modifier.fillMaxSize())
            return@Column
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            uiState.filteredNodes.forEach { node ->
                item(key = node.department.id) {
                    ExpandableRow(
                        title = node.department.name,
                        subtitle = node.department.code,
                        countLabel = node.totalCount?.toString(),
                        isExpanded = node.isExpanded,
                        onClick = { viewModel.toggleDepartment(node.department.id) },
                    )
                }

                if (node.isExpanded) {
                    item(key = "${node.department.id}_filters") {
                        Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp)) {
                            AdminUserSearchFilterBar(
                                searchQuery = node.searchQuery,
                                onSearchQueryChange = { viewModel.onNodeSearchChange(node.department.id, it) },
                                selectedFilter = node.filter,
                                onFilterChange = { viewModel.onNodeFilterChange(node.department.id, it) },
                                sortOption = node.sortOption,
                                onSortOptionChange = { viewModel.onNodeSortChange(node.department.id, it) },
                                searchLabel = "Search teachers by name or email",
                            )
                        }
                    }

                    if (node.isLoadingTeachers) {
                        item(key = "${node.department.id}_loading") {
                            Text(
                                "Loading teachers…",
                                modifier = Modifier.padding(start = 32.dp, top = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    } else if (node.filteredTeachers.isEmpty()) {
                        item(key = "${node.department.id}_empty") {
                            Text(
                                "No teachers found for this department.",
                                modifier = Modifier.padding(start = 32.dp, top = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    } else {
                        items(node.visibleTeachers, key = { "${node.department.id}_${it.uid}" }) { teacher ->
                            TeacherCard(
                                teacher = teacher,
                                departmentName = node.department.name,
                                onClick = { onOpenUser(teacher.uid) },
                                onApprove = { viewModel.approve(teacher.uid) },
                                onReject = { viewModel.reject(teacher.uid) },
                                onSuspend = { viewModel.suspend(teacher.uid) },
                                onActivate = { viewModel.activate(teacher.uid) },
                                onDelete = { viewModel.delete(teacher.uid, node.department.id) },
                                onResetPassword = { viewModel.resetPassword(teacher.email) },
                                modifier = Modifier.padding(start = 16.dp),
                            )
                        }
                        if (node.hasMore) {
                            item(key = "${node.department.id}_more") {
                                LoadMoreFooter(
                                    remaining = node.filteredTeachers.size - node.visibleCount,
                                    onLoadMore = { viewModel.onNodeLoadMore(node.department.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Exactly the 6 fields the spec calls for: Photo, Full Name, Email, Department, Designation, Status. */
@Composable
private fun TeacherCard(
    teacher: User,
    departmentName: String,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onSuspend: () -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
    onResetPassword: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(modifier = Modifier.weight(1f).clickable(onClick = onClick)) {
                UserAvatar(photoUrl = teacher.profilePhotoUrl)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(teacher.fullName, style = MaterialTheme.typography.titleMedium)
                    Text(teacher.email, style = MaterialTheme.typography.bodySmall)
                    Text("Department: $departmentName", style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Designation: ${teacher.designation ?: "Not assigned"}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    StatusChip(status = teacher.status)
                }
            }
            UserQuickActionsRow(
                user = teacher,
                onView = onClick,
                onApprove = onApprove,
                onReject = onReject,
                onSuspend = onSuspend,
                onActivate = onActivate,
                onDelete = onDelete,
                onResetPassword = onResetPassword,
            )
        }
    }
}
