package com.example.uos_lms.feature.admin.university.presentation.department

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Session
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.ConfirmDialog
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.LoadingButton
import com.example.uos_lms.core.ui.components.SelectDialog
import kotlinx.coroutines.launch

@Composable
fun DepartmentDetailScreen(
    onBack: () -> Unit,
    onSessionClick: (String) -> Unit,
    viewModel: DepartmentDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showAddSessionDialog by remember { mutableStateOf(false) }
    var editingSession by remember { mutableStateOf<Session?>(null) }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = uiState.department?.name ?: "Department",
                onBack = onBack,
            )
        },
    ) { padding ->
        val department = uiState.department
        if (uiState.isLoading || department == null) {
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
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Apartment,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            )
                            Text(
                                department.code,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(start = 8.dp),
                            )
                        }
                        if (department.description.isNotBlank()) {
                            Text(
                                department.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(top = 8.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedButton(onClick = { showEditDialog = true }) { Text("Edit") }
                            OutlinedButton(onClick = { showDeleteConfirm = true }) { Text("Delete Department") }
                        }
                    }
                }
            }

            uiState.errorMessage?.let { message ->
                item { ErrorState(message = message, onDismiss = viewModel::clearError) }
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Sessions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    TextButton(onClick = { showAddSessionDialog = true }) { Text("+ Add") }
                }
            }

            if (uiState.sessions.isEmpty()) {
                item {
                    Text(
                        "No sessions yet. Tap + Add to create one (e.g. \"2024-2028\").",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            } else {
                items(uiState.sessions, key = { "session_${it.id}" }) { session ->
                    SessionRow(
                        session = session,
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .animateItem(),
                        onClick = { onSessionClick(session.id) },
                        onToggleActive = { isActive -> viewModel.setSessionActive(session, isActive) },
                        isStatusUpdating = session.id in uiState.updatingSessionIds,
                        onEdit = { editingSession = session },
                        onDelete = { viewModel.requestDeleteSession(session) },
                    )
                }
            }
        }

        if (showEditDialog) {
            DepartmentFormDialog(
                initial = department,
                onDismiss = { showEditDialog = false },
                onSubmit = { name, code, description -> viewModel.updateDepartment(name, code, description) },
                onSaved = { showEditDialog = false },
            )
        }

        if (showDeleteConfirm) {
            ConfirmDialog(
                title = "Delete department?",
                message = "This removes \"${department.name}\". Departments with semesters cannot be deleted.",
                confirmLabel = "Delete",
                onConfirm = {
                    showDeleteConfirm = false
                    viewModel.deleteDepartment(onDeleted = onBack)
                },
                onDismiss = { showDeleteConfirm = false },
            )
        }

        if (showAddSessionDialog) {
            SessionFormDialog(
                title = "Add Session",
                initialLabel = "",
                onDismiss = { showAddSessionDialog = false },
                onSubmit = { label -> viewModel.addSession(label) },
                onSaved = { showAddSessionDialog = false },
            )
        }

        editingSession?.let { session ->
            SessionFormDialog(
                title = "Edit Session",
                initialLabel = session.label,
                onDismiss = { editingSession = null },
                onSubmit = { label -> viewModel.editSessionLabel(session.id, label) },
                onSaved = { editingSession = null },
            )
        }

        uiState.sessionDeleteCheck?.let { check ->
            if (check.linkedStudentCount <= 0L) {
                ConfirmDialog(
                    title = "Delete \"${check.session.label}\"?",
                    message = "This cannot be undone.",
                    confirmLabel = "Delete",
                    onConfirm = { viewModel.deleteSession(check.session.id) },
                    onDismiss = viewModel::cancelSessionDelete,
                )
            } else {
                SessionReassignDialog(
                    session = check.session,
                    linkedStudentCount = check.linkedStudentCount,
                    otherActiveSessions = uiState.sessions.filter { it.id != check.session.id && it.isActive },
                    onReassignAndDelete = { targetSessionId ->
                        viewModel.reassignAndDeleteSession(check.session.id, targetSessionId)
                    },
                    onDismiss = viewModel::cancelSessionDelete,
                )
            }
        }
    }
}

@Composable
private fun SessionRow(
    session: Session,
    onClick: () -> Unit,
    onToggleActive: (Boolean) -> Unit,
    isStatusUpdating: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
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
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick),
            ) {
                Text(
                    session.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (session.isActive) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
                Text(
                    if (session.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (session.isActive) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            }
            Switch(
                checked = session.isActive,
                onCheckedChange = onToggleActive,
                enabled = !isStatusUpdating,
                thumbContent = {
                    Icon(
                        imageVector = if (session.isActive) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = if (session.isActive) "Active" else "Inactive",
                        modifier = Modifier.size(20.dp),
                    )
                },
            )
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Session")
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Session")
            }
        }
    }
}

@Composable
private fun SessionFormDialog(
    title: String,
    initialLabel: String,
    onDismiss: () -> Unit,
    onSubmit: suspend (label: String) -> AppResult<Unit>,
    onSaved: () -> Unit,
) {
    var label by remember { mutableStateOf(initialLabel) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                AppTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = "Session label (e.g. 2024-2028)",
                    modifier = Modifier.fillMaxWidth(),
                )
                errorMessage?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            LoadingButton(
                text = "Save",
                isLoading = isSaving,
                onClick = {
                    if (label.isBlank()) {
                        errorMessage = "Session label is required."
                        return@LoadingButton
                    }
                    scope.launch {
                        isSaving = true
                        errorMessage = null
                        when (val result = onSubmit(label)) {
                            is AppResult.Success -> {
                                isSaving = false
                                onSaved()
                            }
                            is AppResult.Error -> {
                                isSaving = false
                                errorMessage = result.message
                            }
                        }
                    }
                },
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun SessionReassignDialog(
    session: Session,
    linkedStudentCount: Long,
    otherActiveSessions: List<Session>,
    onReassignAndDelete: (targetSessionId: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var showTargetPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cannot delete \"${session.label}\" yet") },
        text = {
            Text(
                "$linkedStudentCount student(s) are currently assigned to this session. " +
                    if (otherActiveSessions.isEmpty()) {
                        "There's no other active session in this department to move them to — " +
                            "create one first, or update each student's session from their profile."
                    } else {
                        "Choose another session to move them to, then this session will be deleted."
                    },
            )
        },
        confirmButton = {
            if (otherActiveSessions.isNotEmpty()) {
                TextButton(onClick = { showTargetPicker = true }) { Text("Reassign & Delete") }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )

    if (showTargetPicker) {
        SelectDialog(
            title = "Move students to…",
            options = otherActiveSessions,
            itemLabel = { it.label },
            onSelect = { target ->
                showTargetPicker = false
                onReassignAndDelete(target.id)
            },
            onDismiss = { showTargetPicker = false },
        )
    }
}
