package com.example.uos_lms.feature.admin.university.presentation.semester

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.ConfirmDialog
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.LoadMoreFooter
import com.example.uos_lms.core.ui.components.LoadingButton
import com.example.uos_lms.core.ui.components.SelectDialog
import com.example.uos_lms.feature.admin.presentation.usermanagement.components.AdminUserSearchFilterBar
import com.example.uos_lms.feature.admin.presentation.usermanagement.student.StudentCard
import kotlinx.coroutines.launch

@Composable
fun SemesterSubjectsScreen(
    onBack: () -> Unit,
    onOpenUser: (uid: String) -> Unit,
    viewModel: SemesterSubjectsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingSubject by remember { mutableStateOf<Subject?>(null) }
    var deletingSubject by remember { mutableStateOf<Subject?>(null) }
    var assigningSubject by remember { mutableStateOf<Subject?>(null) }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = uiState.semester?.displayName ?: "Semester",
                onBack = onBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Subject")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            uiState.errorMessage?.let { message ->
                ErrorState(message = message, onDismiss = viewModel::clearError)
            }
            uiState.actionMessage?.let { message ->
                Text(
                    message,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    Text(
                        "Students",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                item {
                    AdminUserSearchFilterBar(
                        searchQuery = uiState.studentSearchQuery,
                        onSearchQueryChange = viewModel::onStudentSearchChange,
                        selectedFilter = uiState.studentFilter,
                        onFilterChange = viewModel::onStudentFilterChange,
                        sortOption = uiState.studentSortOption,
                        onSortOptionChange = viewModel::onStudentSortChange,
                        showSearchField = false,
                    )
                }
                when {
                    uiState.isLoadingStudents -> item {
                        Text(
                            "Loading students…",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    uiState.filteredStudents.isEmpty() -> item {
                        Text(
                            "No students found for this department, session, and semester.",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    else -> {
                        items(uiState.visibleStudents, key = { "student_${it.uid}" }) { student ->
                            StudentCard(
                                student = student,
                                departmentName = uiState.departmentName,
                                semesterLabel = uiState.semester?.displayName.orEmpty(),
                                sessionLabel = uiState.sessionLabel,
                                onClick = { onOpenUser(student.uid) },
                                onApprove = { viewModel.approveStudent(student.uid) },
                                onReject = { viewModel.rejectStudent(student.uid) },
                                onSuspend = { viewModel.suspendStudent(student.uid) },
                                onActivate = { viewModel.activateStudent(student.uid) },
                                onDelete = { viewModel.deleteStudent(student.uid) },
                                onResetPassword = { viewModel.resetStudentPassword(student.email) },
                                modifier = Modifier.animateItem(),
                            )
                        }
                        if (uiState.hasMoreStudents) {
                            item {
                                LoadMoreFooter(
                                    remaining = uiState.filteredStudents.size - uiState.visibleStudentCount,
                                    onLoadMore = viewModel::onStudentLoadMore,
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Subjects",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                }
                if (uiState.subjects.isEmpty() && !uiState.isLoading) {
                    item {
                        Text(
                            "No subjects yet. Tap + to add one.",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                } else {
                    items(uiState.subjects, key = { it.id }) { subject ->
                        SubjectRow(
                            subject = subject,
                            onEdit = { editingSubject = subject },
                            onDelete = { deletingSubject = subject },
                            onAssignTeacher = { assigningSubject = subject },
                            onUnassignTeacher = { viewModel.unassignTeacher(subject.id) },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        SubjectFormDialog(
            initial = null,
            onDismiss = { showAddDialog = false },
            onSubmit = { code, title, creditHours -> viewModel.createSubject(code, title, creditHours) },
            onSaved = { showAddDialog = false },
        )
    }
    editingSubject?.let { subject ->
        SubjectFormDialog(
            initial = subject,
            onDismiss = { editingSubject = null },
            onSubmit = { code, title, creditHours -> viewModel.updateSubject(subject.id, code, title, creditHours) },
            onSaved = { editingSubject = null },
        )
    }
    deletingSubject?.let { subject ->
        ConfirmDialog(
            title = "Delete subject?",
            message = "This removes \"${subject.title}\" (${subject.code}). This cannot be undone.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteSubject(subject.id)
                deletingSubject = null
            },
            onDismiss = { deletingSubject = null },
        )
    }

    assigningSubject?.let { subject ->
        SelectDialog(
            title = "Assign Teacher",
            options = uiState.teachers,
            itemLabel = { it.fullName },
            onSelect = { teacher ->
                viewModel.assignTeacher(subject.id, teacher)
                assigningSubject = null
            },
            onDismiss = { assigningSubject = null },
            emptyMessage = "No approved teachers in this department yet.",
        )
    }
}

@Composable
private fun SubjectRow(
    subject: Subject,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAssignTeacher: () -> Unit,
    onUnassignTeacher: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(subject.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "${subject.code} • ${subject.creditHours} credit hours",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            Text(
                subject.teacherName?.let { "Teacher: $it" } ?: "Teacher not assigned",
                style = MaterialTheme.typography.bodySmall,
                color = if (subject.teacherName == null) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp),
            ) {
                OutlinedButton(onClick = onAssignTeacher) {
                    Text(if (subject.teacherUid == null) "Assign Teacher" else "Change Teacher")
                }
                if (subject.teacherUid != null) {
                    TextButton(onClick = onUnassignTeacher) {
                        Text("Unassign")
                    }
                }
            }
        }
    }
}

@Composable
private fun SubjectFormDialog(
    initial: Subject?,
    onDismiss: () -> Unit,
    onSubmit: suspend (code: String, title: String, creditHours: Int) -> AppResult<Unit>,
    onSaved: () -> Unit,
) {
    var code by remember { mutableStateOf(initial?.code ?: "") }
    var title by remember { mutableStateOf(initial?.title ?: "") }
    var creditHoursText by remember { mutableStateOf(initial?.creditHours?.toString() ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Subject" else "Edit Subject") },
        text = {
            Column {
                AppTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = "Code (e.g. CS101)",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(value = title, onValueChange = { title = it }, label = "Title", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = creditHoursText,
                    onValueChange = { creditHoursText = it },
                    label = "Credit Hours",
                    keyboardType = KeyboardType.Number,
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
                    val trimmedCode = code.trim().uppercase()
                    val trimmedTitle = title.trim()
                    val creditHours = creditHoursText.trim().toIntOrNull()
                    if (trimmedCode.isEmpty() || trimmedTitle.isEmpty()) {
                        errorMessage = "Code and title are required"
                        return@LoadingButton
                    }
                    if (creditHours == null || creditHours <= 0) {
                        errorMessage = "Enter valid credit hours"
                        return@LoadingButton
                    }
                    scope.launch {
                        isSaving = true
                        errorMessage = null
                        when (val result = onSubmit(trimmedCode, trimmedTitle, creditHours)) {
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
