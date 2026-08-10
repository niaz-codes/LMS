package com.example.uos_lms.feature.admin.presentation.usermanagement.student

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.CountBadge
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.ExpandableRow
import com.example.uos_lms.core.ui.components.LoadingButton
import com.example.uos_lms.core.ui.components.StatusChip
import com.example.uos_lms.core.ui.components.TreeStatsRow
import com.example.uos_lms.core.ui.components.UserAvatar
import com.example.uos_lms.core.ui.components.UserQuickActionsRow

/**
 * Top of the Student Management hierarchy: Department -> Session. Tapping a Session
 * navigates to [AdminStudentSemesterListScreen] (Session -> Semester -> Students) via
 * [onOpenSession] rather than expanding further here.
 */
@Composable
fun AdminStudentTreeScreen(
    onOpenSession: (departmentId: String, sessionId: String) -> Unit,
    viewModel: AdminStudentTreeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        uiState.errorMessage?.let { message ->
            ErrorState(message = message, onDismiss = viewModel::consumeMessages)
        }

        TreeStatsRow(
            stats = listOf(
                "Departments" to uiState.nodes.size.toLong(),
                "Semesters" to uiState.totalSemesters,
                "Sessions" to uiState.totalSessions,
                "Students" to uiState.totalStudents,
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
            uiState.filteredNodes.forEach { deptNode ->
                item(key = deptNode.department.id) {
                    ExpandableRow(
                        title = deptNode.department.name,
                        subtitle = deptNode.department.code,
                        countLabel = deptNode.totalCount?.toString(),
                        isExpanded = deptNode.isExpanded,
                        onClick = { viewModel.toggleDepartment(deptNode.department.id) },
                    )
                }

                if (!deptNode.isExpanded) return@forEach

                item(key = "${deptNode.department.id}_toolbar") {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        AppTextField(
                            value = deptNode.sessionSearchQuery,
                            onValueChange = { viewModel.onSessionSearchChange(deptNode.department.id, it) },
                            label = "Search sessions",
                            modifier = Modifier.weight(1f),
                        )
                        TextButton(onClick = { viewModel.showAddSessionDialog(deptNode.department.id) }) {
                            Text("+ Session")
                        }
                    }
                }

                when {
                    deptNode.isLoadingSessions -> item(key = "${deptNode.department.id}_loading") {
                        Text(
                            "Loading sessions…",
                            modifier = Modifier.padding(start = 32.dp, top = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    deptNode.filteredSessions.isEmpty() -> item(key = "${deptNode.department.id}_empty") {
                        Text(
                            "No sessions yet. Tap + Session above to add one (e.g. \"2024-2028\").",
                            modifier = Modifier.padding(start = 32.dp, top = 4.dp),
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    else -> deptNode.filteredSessions.forEach { sessNode ->
                        item(key = "${deptNode.department.id}_${sessNode.session.id}") {
                            SessionRow(
                                label = sessNode.session.label,
                                subtitle = if (!sessNode.session.isActive) "Inactive" else null,
                                countLabel = sessNode.totalCount?.toString(),
                                onClick = { onOpenSession(deptNode.department.id, sessNode.session.id) },
                                modifier = Modifier.padding(start = 16.dp),
                            )
                        }
                    }
                }
            }
        }
    }

    uiState.nodes.find { it.showAddSessionDialog }?.let { deptNode ->
        AddSessionDialog(
            isSaving = deptNode.isSavingSession,
            errorMessage = deptNode.addSessionError,
            onDismiss = { viewModel.dismissAddSessionDialog(deptNode.department.id) },
            onSave = { label -> viewModel.createSession(deptNode.department.id, label) },
        )
    }
}

@Composable
private fun AddSessionDialog(
    isSaving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (label: String) -> Unit,
) {
    var label by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Session") },
        text = {
            Column {
                AppTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = "Session label (e.g. 2024-2028)",
                    modifier = Modifier.fillMaxWidth(),
                )
                (error ?: errorMessage)?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            LoadingButton(
                text = "Add",
                isLoading = isSaving,
                onClick = {
                    if (label.isBlank()) {
                        error = "Session label is required."
                    } else {
                        error = null
                        onSave(label.trim())
                    }
                },
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

/** A Session leaf under a Department — unlike [ExpandableRow] siblings elsewhere in this
 * tree family, tapping this navigates to a new screen rather than expanding in place, so
 * it uses a forward chevron instead of the down-pointing "expand" affordance. */
@Composable
private fun SessionRow(
    label: String,
    subtitle: String?,
    countLabel: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium)
                subtitle?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CountBadge(countLabel)
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Shared with [AdminStudentSemesterListScreen] — a single student row with quick actions. */
@Composable
internal fun StudentCard(
    student: User,
    departmentName: String,
    semesterLabel: String,
    sessionLabel: String,
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
                UserAvatar(photoUrl = student.profilePhotoUrl)
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(student.fullName, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Reg. No: ${student.registrationNumber ?: "Not assigned"}",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Text("Roll No: ${student.rollNumber ?: "Not assigned"}", style = MaterialTheme.typography.bodySmall)
                    Text("$departmentName • $sessionLabel • $semesterLabel", style = MaterialTheme.typography.bodySmall)
                    Text(student.email, style = MaterialTheme.typography.bodySmall)
                    StatusChip(status = student.status)
                }
            }
            UserQuickActionsRow(
                user = student,
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
