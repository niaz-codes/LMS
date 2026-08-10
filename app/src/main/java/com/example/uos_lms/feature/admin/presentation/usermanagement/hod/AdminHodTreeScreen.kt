package com.example.uos_lms.feature.admin.presentation.usermanagement.hod

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
fun AdminHodTreeScreen(
    onOpenUser: (uid: String) -> Unit,
    viewModel: AdminHodTreeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        uiState.errorMessage?.let { message ->
            ErrorState(message = message, onDismiss = viewModel::consumeMessages)
        }

        TreeStatsRow(
            stats = listOf(
                "Departments" to uiState.nodes.size.toLong(),
                "HODs" to uiState.totalHods,
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
                                searchLabel = "Search HODs by name or email",
                            )
                        }
                    }

                    if (node.isLoadingHods) {
                        item(key = "${node.department.id}_loading") {
                            Text(
                                "Loading HODs…",
                                modifier = Modifier.padding(start = 32.dp, top = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    } else if (node.filteredHods.isEmpty()) {
                        item(key = "${node.department.id}_empty") {
                            Text(
                                "No HODs found for this department.",
                                modifier = Modifier.padding(start = 32.dp, top = 4.dp),
                                style = MaterialTheme.typography.bodySmall,
                            )
                        }
                    } else {
                        items(node.visibleHods, key = { "${node.department.id}_${it.uid}" }) { hod ->
                            HodCard(
                                hod = hod,
                                onClick = { onOpenUser(hod.uid) },
                                onApprove = { viewModel.approve(hod.uid) },
                                onReject = { viewModel.reject(hod.uid) },
                                onSuspend = { viewModel.suspend(hod.uid) },
                                onActivate = { viewModel.activate(hod.uid) },
                                onDelete = { viewModel.delete(hod.uid, node.department.id) },
                                onResetPassword = { viewModel.resetPassword(hod.email) },
                                modifier = Modifier.padding(start = 16.dp),
                            )
                        }
                        if (node.hasMore) {
                            item(key = "${node.department.id}_more") {
                                LoadMoreFooter(
                                    remaining = node.filteredHods.size - node.visibleCount,
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

@Composable
private fun HodCard(
    hod: User,
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
                UserAvatar(photoUrl = hod.profilePhotoUrl)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(hod.fullName, style = MaterialTheme.typography.titleMedium)
                    Text(hod.email, style = MaterialTheme.typography.bodySmall)
                    hod.designation?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    StatusChip(status = hod.status)
                }
            }
            UserQuickActionsRow(
                user = hod,
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
