package com.example.uos_lms.feature.admin.presentation.userdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.domain.model.UserStatus
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.ConfirmDialog
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.MultiSelectDialog
import com.example.uos_lms.core.ui.components.SelectDialog
import com.example.uos_lms.core.ui.components.StatusChip

@Composable
fun AdminUserDetailScreen(
    onBack: () -> Unit,
    viewModel: AdminUserDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDepartmentPicker by remember { mutableStateOf(false) }
    var showSemesterPicker by remember { mutableStateOf(false) }
    var showSessionPicker by remember { mutableStateOf(false) }
    var showEditProfile by remember { mutableStateOf(false) }
    var showChangeRole by remember { mutableStateOf(false) }
    val visibleState = remember { MutableTransitionState(false).apply { targetState = true } }

    LaunchedEffect(Unit) {
        viewModel.deleted.collect { onBack() }
    }

    Scaffold { padding ->
        val user = uiState.user
        if (uiState.isLoading || user == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            Box(modifier = Modifier.statusBarsPadding()) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState()),
        ) {
            AnimatedVisibility(
                visibleState = visibleState,
                enter = fadeIn(tween(450)) + slideInVertically(tween(450)) { -it / 3 },
            ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                            ),
                        ),
                    )
                    .statusBarsPadding(),
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.align(Alignment.TopStart),
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (user.profilePhotoUrl != null) {
                        AsyncImage(
                            model = user.profilePhotoUrl,
                            contentDescription = "Profile photo",
                            modifier = Modifier
                                .size(88.dp)
                                .clip(CircleShape),
                        )
                    } else {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(88.dp),
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        user.fullName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusChip(status = user.status)
                }
            }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        DetailRow("Father Name", user.fatherName)
                        DetailRow("CNIC", user.cnic)
                        DetailRow("Phone", user.phone)
                        DetailRow("Email", user.email)
                        DetailRow("Role", user.role.name)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                            OutlinedButton(onClick = { showEditProfile = true }) {
                                Text("Edit Profile")
                            }
                            if (user.role != UserRole.ADMIN) {
                                OutlinedButton(onClick = { showChangeRole = true }) {
                                    Text("Change Role")
                                }
                            }
                        }
                    }
                }

                if (user.role == UserRole.HOD || user.role == UserRole.STUDENT) {
                    Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val departmentName = uiState.departments.find { it.id == user.department }?.name
                                ?: "Not assigned"
                            DetailRow("Department", departmentName)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                                OutlinedButton(onClick = { showDepartmentPicker = true }) {
                                    Text(if (user.department == null) "Assign Department" else "Change Department")
                                }
                                if (user.department != null) {
                                    OutlinedButton(onClick = viewModel::unassignDepartment) {
                                        Text("Unassign")
                                    }
                                }
                            }
                        }
                    }
                }

                if (user.role == UserRole.TEACHER) {
                    Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val departmentNames = user.departmentIds.mapNotNull { id ->
                                uiState.departments.find { it.id == id }?.name
                            }
                            DetailRow(
                                "Departments",
                                departmentNames.joinToString(", ").ifEmpty { "Not assigned" },
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                                OutlinedButton(onClick = { showDepartmentPicker = true }) {
                                    Text(if (user.departmentIds.isEmpty()) "Assign Departments" else "Manage Departments")
                                }
                                if (user.departmentIds.isNotEmpty()) {
                                    OutlinedButton(onClick = viewModel::unassignAllDepartments) {
                                        Text("Unassign All")
                                    }
                                }
                            }
                        }
                    }
                }

                if (user.role == UserRole.STUDENT && user.department != null) {
                    Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val semesterName = uiState.semesters.find { it.id == user.semester }?.displayName
                                ?: "Not assigned"
                            DetailRow("Semester", semesterName)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                                OutlinedButton(onClick = { showSemesterPicker = true }) {
                                    Text(if (user.semester == null) "Assign Semester" else "Change Semester")
                                }
                                if (user.semester != null) {
                                    OutlinedButton(onClick = viewModel::unassignSemester) {
                                        Text("Unassign")
                                    }
                                }
                            }
                        }
                    }
                    Card(shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            val sessionLabel = uiState.sessions.find { it.id == user.sessionId }?.label
                                ?: "Not assigned"
                            DetailRow("Session", sessionLabel)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.padding(top = 8.dp)) {
                                OutlinedButton(onClick = { showSessionPicker = true }) {
                                    Text(if (user.sessionId == null) "Assign Session" else "Change Session")
                                }
                                if (user.sessionId != null) {
                                    OutlinedButton(onClick = viewModel::unassignSession) {
                                        Text("Unassign")
                                    }
                                }
                            }
                        }
                    }
                }

                uiState.errorMessage?.let { ErrorState(message = it, onDismiss = {}) }
                uiState.actionMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.primary)
                }

                when (user.status) {
                    UserStatus.PENDING -> Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(onClick = viewModel::approve, modifier = Modifier.weight(1f)) { Text("Approve") }
                        OutlinedButton(onClick = viewModel::reject, modifier = Modifier.weight(1f)) { Text("Reject") }
                    }
                    UserStatus.APPROVED -> OutlinedButton(onClick = viewModel::suspendUser, modifier = Modifier.fillMaxWidth()) {
                        Text("Suspend")
                    }
                    UserStatus.SUSPENDED -> Button(onClick = viewModel::activate, modifier = Modifier.fillMaxWidth()) {
                        Text("Activate")
                    }
                    UserStatus.REJECTED -> {}
                }

                OutlinedButton(onClick = viewModel::resetPassword, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.LockReset, contentDescription = null)
                    Text("Reset Password", modifier = Modifier.padding(start = 8.dp))
                }
                OutlinedButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null)
                    Text("Delete User", modifier = Modifier.padding(start = 8.dp))
                }
            }
        }

        if (showDeleteConfirm) {
            ConfirmDialog(
                title = "Delete user?",
                message = "This removes ${user.fullName}'s profile and blocks their login. This cannot be undone.",
                confirmLabel = "Delete",
                onConfirm = { showDeleteConfirm = false; viewModel.delete() },
                onDismiss = { showDeleteConfirm = false },
            )
        }

        if (showDepartmentPicker) {
            if (user.role == UserRole.TEACHER) {
                MultiSelectDialog(
                    title = "Assign Departments",
                    options = uiState.departments,
                    itemLabel = { it.name },
                    itemKey = { it.id },
                    initiallySelected = user.departmentIds.toSet(),
                    onConfirm = { selectedIds ->
                        showDepartmentPicker = false
                        viewModel.assignDepartments(selectedIds.toList())
                    },
                    onDismiss = { showDepartmentPicker = false },
                    emptyMessage = "No departments exist yet. Create one first.",
                )
            } else {
                SelectDialog(
                    title = "Assign Department",
                    options = uiState.departments,
                    itemLabel = { it.name },
                    onSelect = { department ->
                        showDepartmentPicker = false
                        viewModel.requestAssignDepartment(department)
                    },
                    onDismiss = { showDepartmentPicker = false },
                    emptyMessage = "No departments exist yet. Create one first.",
                )
            }
        }

        if (showSemesterPicker) {
            SelectDialog(
                title = "Assign Semester",
                options = uiState.semesters,
                itemLabel = { it.displayName },
                onSelect = { semester ->
                    showSemesterPicker = false
                    viewModel.assignSemester(semester)
                },
                onDismiss = { showSemesterPicker = false },
                emptyMessage = "This department has no semesters yet. Create one first.",
            )
        }

        if (showSessionPicker) {
            SelectDialog(
                title = "Assign Session",
                options = uiState.sessions.filter { it.isActive },
                itemLabel = { it.label },
                onSelect = { session ->
                    showSessionPicker = false
                    viewModel.assignSession(session)
                },
                onDismiss = { showSessionPicker = false },
                emptyMessage = "This department has no active sessions. Create or activate one from " +
                    "Departments → (this department) → Sessions first.",
            )
        }

        uiState.hodConflict?.let { conflict ->
            ConfirmDialog(
                title = "Replace current HOD?",
                message = "${conflict.existingHodName} is already HOD of ${conflict.departmentName}. " +
                    "Assigning ${user.fullName} will remove them as HOD of it. Continue?",
                confirmLabel = "Reassign",
                onConfirm = viewModel::confirmHodReassignment,
                onDismiss = viewModel::cancelHodConflict,
            )
        }

        if (showEditProfile) {
            EditProfileDialog(
                user = user,
                onDismiss = { showEditProfile = false },
                onSave = { fullName, fatherName, phone, cnic, employeeId, registrationNumber, rollNumber, designation ->
                    viewModel.updateProfile(fullName, fatherName, phone, cnic)
                    if (user.role == UserRole.HOD || user.role == UserRole.TEACHER) {
                        viewModel.updateIdentifiers(employeeId, null, null, designation)
                    } else if (user.role == UserRole.STUDENT) {
                        viewModel.updateIdentifiers(null, registrationNumber, rollNumber, null)
                    }
                    showEditProfile = false
                },
            )
        }

        if (showChangeRole) {
            SelectDialog(
                title = "Change Role",
                options = UserRole.REGISTERABLE_ROLES,
                itemLabel = { it.name },
                onSelect = { role ->
                    viewModel.changeRole(role)
                    showChangeRole = false
                },
                onDismiss = { showChangeRole = false },
            )
        }
    }
}

@Composable
private fun EditProfileDialog(
    user: User,
    onDismiss: () -> Unit,
    onSave: (
        fullName: String,
        fatherName: String,
        phone: String,
        cnic: String,
        employeeId: String?,
        registrationNumber: String?,
        rollNumber: String?,
        designation: String?,
    ) -> Unit,
) {
    var fullName by remember { mutableStateOf(user.fullName) }
    var fatherName by remember { mutableStateOf(user.fatherName) }
    var phone by remember { mutableStateOf(user.phone) }
    var cnic by remember { mutableStateOf(user.cnic) }
    var employeeId by remember { mutableStateOf(user.employeeId.orEmpty()) }
    var designation by remember { mutableStateOf(user.designation.orEmpty()) }
    var registrationNumber by remember { mutableStateOf(user.registrationNumber.orEmpty()) }
    var rollNumber by remember { mutableStateOf(user.rollNumber.orEmpty()) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 420.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                AppTextField(value = fullName, onValueChange = { fullName = it }, label = "Full Name", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = fatherName, onValueChange = { fatherName = it }, label = "Father Name", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = phone, onValueChange = { phone = it }, label = "Phone", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = cnic, onValueChange = { cnic = it }, label = "CNIC", modifier = Modifier.fillMaxWidth())
                if (user.role == UserRole.HOD || user.role == UserRole.TEACHER) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        value = employeeId,
                        onValueChange = { employeeId = it },
                        label = "Employee ID",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        value = designation,
                        onValueChange = { designation = it },
                        label = "Designation",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                if (user.role == UserRole.STUDENT) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        value = registrationNumber,
                        onValueChange = { registrationNumber = it },
                        label = "Registration Number",
                        modifier = Modifier.fillMaxWidth(),
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    AppTextField(
                        value = rollNumber,
                        onValueChange = { rollNumber = it },
                        label = "Roll Number",
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (fullName.isBlank()) {
                    error = "Full name is required"
                    return@TextButton
                }
                onSave(
                    fullName,
                    fatherName,
                    phone,
                    cnic,
                    employeeId.ifBlank { null },
                    registrationNumber.ifBlank { null },
                    rollNumber.ifBlank { null },
                    designation.ifBlank { null },
                )
            }) { Text("Save") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}
