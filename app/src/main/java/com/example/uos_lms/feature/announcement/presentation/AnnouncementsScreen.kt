package com.example.uos_lms.feature.announcement.presentation

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.common.millisToDisplay
import com.example.uos_lms.core.domain.model.Announcement
import com.example.uos_lms.core.domain.model.AnnouncementScope
import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.domain.model.Subject
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.LoadingButton
import com.example.uos_lms.core.ui.components.SelectDialog
import com.example.uos_lms.ui.theme.accentGradient
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun AnnouncementsScreen(
    onBack: () -> Unit,
    viewModel: AnnouncementsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showPostDialog by remember { mutableStateOf(false) }
    val canPost = uiState.role == UserRole.ADMIN || uiState.role == UserRole.HOD || uiState.role == UserRole.TEACHER

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Announcements",
                onBack = onBack,
                gradient = uiState.role?.let { Brush.horizontalGradient(roleGradientColors(it).toList()) } ?: accentGradient(),
            )
        },
        floatingActionButton = {
            if (canPost) {
                FloatingActionButton(onClick = { showPostDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Post Announcement")
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            uiState.errorMessage?.let { ErrorState(message = it, onDismiss = viewModel::clearError) }

            if (uiState.announcements.isEmpty() && !uiState.isLoading) {
                EmptyState(message = "No announcements yet.", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.announcements, key = { it.id }) { announcement ->
                        AnnouncementCard(
                            announcement = announcement,
                            canDelete = uiState.role == UserRole.ADMIN,
                            onDelete = { viewModel.deleteAnnouncement(announcement.id) },
                        )
                    }
                }
            }
        }
    }

    if (showPostDialog) {
        PostAnnouncementDialog(
            uiState = uiState,
            onDismiss = { showPostDialog = false },
            onPostAll = { title, body -> viewModel.postAll(title, body); showPostDialog = false },
            onPostToDepartment = { title, body, dept ->
                viewModel.postToDepartment(title, body, dept)
                showPostDialog = false
            },
            onPostToMyDepartment = { title, body ->
                viewModel.postToMyDepartment(title, body)
                showPostDialog = false
            },
            onPostToSubject = { title, body, subject ->
                viewModel.postToSubject(title, body, subject)
                showPostDialog = false
            },
        )
    }
}

@Composable
private fun AnnouncementCard(announcement: Announcement, canDelete: Boolean, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                AssistChip(
                    onClick = {},
                    enabled = false,
                    label = {
                        Text(
                            when (announcement.scope) {
                                AnnouncementScope.ALL -> "All"
                                AnnouncementScope.DEPARTMENT -> announcement.departmentName ?: "Department"
                                AnnouncementScope.SUBJECT -> announcement.subjectName ?: "Subject"
                            },
                        )
                    },
                )
                if (canDelete) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            }
            Text(announcement.title, style = MaterialTheme.typography.titleMedium)
            Text(announcement.body, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${announcement.authorName} • ${millisToDisplay(announcement.createdAt)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun PostAnnouncementDialog(
    uiState: AnnouncementsUiState,
    onDismiss: () -> Unit,
    onPostAll: (title: String, body: String) -> Unit,
    onPostToDepartment: (title: String, body: String, department: Department) -> Unit,
    onPostToMyDepartment: (title: String, body: String) -> Unit,
    onPostToSubject: (title: String, body: String, subject: Subject) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var selectedDepartment by remember { mutableStateOf<Department?>(null) }
    var selectedSubject by remember { mutableStateOf<Subject?>(null) }
    var showDepartmentPicker by remember { mutableStateOf(false) }
    var showSubjectPicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Post Announcement") },
        text = {
            Column {
                AppTextField(value = title, onValueChange = { title = it }, label = "Title", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = body,
                    onValueChange = { body = it },
                    label = "Message",
                    singleLine = false,
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(12.dp))
                when (uiState.role) {
                    UserRole.ADMIN -> {
                        Text("Audience", style = MaterialTheme.typography.labelLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedDepartment == null,
                                onClick = { selectedDepartment = null },
                                label = { Text("All Users") },
                            )
                            FilterChip(
                                selected = selectedDepartment != null,
                                onClick = { showDepartmentPicker = true },
                                label = { Text(selectedDepartment?.name ?: "Choose Department") },
                            )
                        }
                    }
                    UserRole.HOD -> {
                        Text(
                            "Posting to: ${uiState.myDepartmentName ?: "your department"}",
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    UserRole.TEACHER -> {
                        Text("Subject", style = MaterialTheme.typography.labelLarge)
                        FilterChip(
                            selected = selectedSubject != null,
                            onClick = { showSubjectPicker = true },
                            label = { Text(selectedSubject?.title ?: "Choose Subject") },
                        )
                    }
                    else -> {}
                }
                error?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            LoadingButton(
                text = "Post",
                isLoading = uiState.isPosting,
                onClick = {
                    if (title.isBlank() || body.isBlank()) {
                        error = "Title and message are required"
                        return@LoadingButton
                    }
                    when (uiState.role) {
                        UserRole.ADMIN -> {
                            val dept = selectedDepartment
                            if (dept == null) onPostAll(title.trim(), body.trim()) else onPostToDepartment(title.trim(), body.trim(), dept)
                        }
                        UserRole.HOD -> onPostToMyDepartment(title.trim(), body.trim())
                        UserRole.TEACHER -> {
                            val subject = selectedSubject
                            if (subject == null) {
                                error = "Choose a subject"
                                return@LoadingButton
                            }
                            onPostToSubject(title.trim(), body.trim(), subject)
                        }
                        else -> {}
                    }
                },
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )

    if (showDepartmentPicker) {
        SelectDialog(
            title = "Choose Department",
            options = uiState.departments,
            itemLabel = { it.name },
            onSelect = { department ->
                selectedDepartment = department
                showDepartmentPicker = false
            },
            onDismiss = { showDepartmentPicker = false },
            emptyMessage = "No departments exist yet.",
        )
    }

    if (showSubjectPicker) {
        SelectDialog(
            title = "Choose Subject",
            options = uiState.mySubjects,
            itemLabel = { "${it.code} • ${it.title}" },
            onSelect = { subject ->
                selectedSubject = subject
                showSubjectPicker = false
            },
            onDismiss = { showSubjectPicker = false },
            emptyMessage = "You have no assigned subjects yet.",
        )
    }
}
