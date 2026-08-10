package com.example.uos_lms.feature.admin.presentation.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.domain.model.UserStatus
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.ConfirmDialog
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.StatusChip
import com.example.uos_lms.feature.admin.domain.model.UserFilter
import com.example.uos_lms.feature.admin.presentation.components.AdminBottomNavBar
import com.example.uos_lms.feature.admin.presentation.components.AdminTab
import com.example.uos_lms.feature.admin.presentation.usermanagement.hod.AdminHodTreeScreen
import com.example.uos_lms.feature.admin.presentation.usermanagement.student.AdminStudentTreeScreen
import com.example.uos_lms.feature.admin.presentation.usermanagement.teacher.AdminTeacherTreeScreen
import com.example.uos_lms.ui.theme.roleGradientColors

private val USER_MANAGEMENT_TABS = listOf("All", "HOD", "Teacher", "Student")

@Composable
fun AdminDashboardScreen(
    onUserClick: (String) -> Unit,
    onOpenSession: (departmentId: String, sessionId: String) -> Unit,
    onHomeClick: () -> Unit,
    onDepartmentsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    val (adminStart, adminEnd) = roleGradientColors(UserRole.ADMIN)

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Users",
                gradient = Brush.horizontalGradient(listOf(adminStart, adminEnd)),
            )
        },
        bottomBar = {
            AdminBottomNavBar(
                selected = AdminTab.USERS,
                onHomeClick = onHomeClick,
                onUsersClick = {},
                onDepartmentsClick = onDepartmentsClick,
                onReportsClick = onReportsClick,
                onProfileClick = onProfileClick,
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                USER_MANAGEMENT_TABS.forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(label) },
                    )
                }
            }

            when (selectedTab) {
                0 -> AllUsersTab(onUserClick = onUserClick)
                1 -> AdminHodTreeScreen(onOpenUser = onUserClick)
                2 -> AdminTeacherTreeScreen(onOpenUser = onUserClick)
                3 -> AdminStudentTreeScreen(onOpenSession = onOpenSession)
            }
        }
    }
}

/** Flat, unscoped, all-roles/all-statuses user directory + moderation console — the
 * only place to see/approve a brand-new pending registration before an Admin has
 * placed them in a department (the HOD/Teacher/Student trees only show already-placed
 * users, since they're queried by department). */
@Composable
private fun AllUsersTab(
    onUserClick: (String) -> Unit,
    viewModel: AdminUserListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        AppTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::onSearchQueryChange,
            label = "Search by name, email, or CNIC",
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            UserFilter.entries.forEach { filter ->
                FilterChip(
                    selected = uiState.selectedFilter == filter,
                    onClick = { viewModel.onFilterChange(filter) },
                    label = { Text(filter.label) },
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        if (uiState.filteredUsers.isEmpty() && !uiState.isLoading) {
            EmptyState(message = "No users found", icon = Icons.Default.Search)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(uiState.filteredUsers, key = { it.uid }) { user ->
                    UserRow(
                        user = user,
                        onClick = { onUserClick(user.uid) },
                        onApprove = { viewModel.approve(user.uid) },
                        onReject = { viewModel.reject(user.uid) },
                        onSuspend = { viewModel.suspendUser(user.uid) },
                        onActivate = { viewModel.activate(user.uid) },
                        onDelete = { viewModel.delete(user.uid) },
                        onResetPassword = { viewModel.resetPassword(user.email) },
                        modifier = Modifier.animateItem(),
                    )
                }
            }
        }
    }
}

@Composable
private fun UserRow(
    user: User,
    onClick: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onSuspend: () -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
    onResetPassword: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
            ) {
                Text(user.fullName, style = MaterialTheme.typography.titleMedium)
                Text(user.email, style = MaterialTheme.typography.bodySmall)
                Text(user.role.name, style = MaterialTheme.typography.bodySmall)
                Spacer(modifier = Modifier.height(4.dp))
                StatusChip(status = user.status)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Actions")
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(text = { Text("View Profile") }, onClick = { menuExpanded = false; onClick() })
                    if (user.status == UserStatus.PENDING) {
                        DropdownMenuItem(text = { Text("Approve") }, onClick = { menuExpanded = false; onApprove() })
                        DropdownMenuItem(text = { Text("Reject") }, onClick = { menuExpanded = false; onReject() })
                    }
                    if (user.status == UserStatus.APPROVED) {
                        DropdownMenuItem(text = { Text("Suspend") }, onClick = { menuExpanded = false; onSuspend() })
                    }
                    if (user.status == UserStatus.SUSPENDED) {
                        DropdownMenuItem(text = { Text("Activate") }, onClick = { menuExpanded = false; onActivate() })
                    }
                    DropdownMenuItem(
                        text = { Text("Reset Password") },
                        onClick = { menuExpanded = false; onResetPassword() },
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = { menuExpanded = false; showDeleteConfirm = true },
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        ConfirmDialog(
            title = "Delete user?",
            message = "This removes ${user.fullName}'s profile and blocks their login. This cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = { showDeleteConfirm = false; onDelete() },
            onDismiss = { showDeleteConfirm = false },
        )
    }
}
