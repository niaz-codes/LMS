package com.example.uos_lms.feature.teacher.presentation.material

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.MaterialType
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.LoadingButton
import com.example.uos_lms.core.ui.components.SelectDialog
import com.example.uos_lms.ui.theme.roleGradientColors

@Composable
fun TeacherSubjectMaterialsScreen(
    onBack: () -> Unit,
    viewModel: TeacherSubjectMaterialsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val uriHandler = LocalUriHandler.current
    var showUploadDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = "Study Material",
                onBack = onBack,
                gradient = Brush.horizontalGradient(roleGradientColors(UserRole.TEACHER).toList()),
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showUploadDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Upload Material")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            if (uiState.isUploading) {
                LinearProgressIndicator(
                    progress = { (uiState.uploadProgress ?: 0) / 100f },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (uiState.materials.isEmpty() && !uiState.isLoading) {
                EmptyState(
                    message = "No study material yet. Tap + to upload.",
                    icon = Icons.Default.FolderOpen,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.materials, key = { it.id }) { material ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(modifier = Modifier.clickable { uriHandler.openUri(material.fileUrl) }) {
                                    AssistChip(onClick = {}, enabled = false, label = { Text(material.materialType.name) })
                                    Text(material.title, style = MaterialTheme.typography.titleMedium)
                                    Text(material.fileName, style = MaterialTheme.typography.bodySmall)
                                }
                                IconButton(onClick = { viewModel.delete(material.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUploadDialog) {
        UploadMaterialDialog(
            isUploading = uiState.isUploading,
            errorMessage = uiState.errorMessage,
            onDismiss = { showUploadDialog = false },
            onUpload = { title, type, uri ->
                viewModel.upload(title, type, uri)
                showUploadDialog = false
            },
        )
    }
}

@Composable
private fun UploadMaterialDialog(
    isUploading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onUpload: (title: String, type: MaterialType, uri: Uri) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(MaterialType.PDF) }
    var pickedUri by remember { mutableStateOf<Uri?>(null) }
    var showTypePicker by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
    ) { uri -> pickedUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Upload Material") },
        text = {
            Column {
                AppTextField(value = title, onValueChange = { title = it }, label = "Title", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(12.dp))
                Text("Type", style = MaterialTheme.typography.labelLarge)
                FilterChip(
                    selected = true,
                    onClick = { showTypePicker = true },
                    label = { Text(selectedType.name) },
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { filePickerLauncher.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(if (pickedUri != null) "File selected — tap to change" else "Choose File")
                }
                (error ?: errorMessage)?.let {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(it, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            LoadingButton(
                text = "Upload",
                isLoading = isUploading,
                onClick = {
                    val uri = pickedUri
                    when {
                        title.isBlank() -> error = "Title is required."
                        uri == null -> error = "Choose a file to upload."
                        else -> {
                            error = null
                            onUpload(title, selectedType, uri)
                        }
                    }
                },
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )

    if (showTypePicker) {
        SelectDialog(
            title = "Material Type",
            options = MaterialType.entries,
            itemLabel = { it.name },
            onSelect = { type ->
                selectedType = type
                showTypePicker = false
            },
            onDismiss = { showTypePicker = false },
        )
    }
}
