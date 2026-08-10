package com.example.uos_lms.feature.admin.university.presentation.session

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.ConfirmDialog
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.core.ui.components.LoadingButton
import kotlinx.coroutines.launch

/**
 * Session -> Semester step of the Admin Department Management hierarchy. Selecting a
 * Semester here moves on to [com.example.uos_lms.feature.admin.university.presentation.semester.SemesterSubjectsScreen],
 * which now also shows the Students assigned to this Department + Session + Semester.
 */
@Composable
fun SessionSemesterListScreen(
    onBack: () -> Unit,
    onSemesterClick: (String) -> Unit,
    viewModel: SessionSemesterListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddSemesterDialog by remember { mutableStateOf(false) }
    var deletingSemester by remember { mutableStateOf<Semester?>(null) }

    Scaffold(
        topBar = {
            GradientTopAppBar(
                title = if (uiState.departmentName.isBlank() && uiState.sessionLabel.isBlank()) {
                    "Semesters"
                } else {
                    "${uiState.departmentName} • ${uiState.sessionLabel}"
                },
                onBack = onBack,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddSemesterDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Semester")
            }
        },
    ) { padding ->
        if (uiState.isLoading) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            uiState.errorMessage?.let { message ->
                ErrorState(message = message, onDismiss = viewModel::clearError)
            }

            if (uiState.semesters.isEmpty()) {
                EmptyState(
                    message = "No semesters yet. Tap + to add one.",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(uiState.semesters, key = { it.id }) { semester ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
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
                                Text(
                                    semester.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onSemesterClick(semester.id) },
                                )
                                IconButton(onClick = { deletingSemester = semester }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete Semester")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddSemesterDialog) {
        AddSemesterDialog(
            onDismiss = { showAddSemesterDialog = false },
            onSubmit = { number -> viewModel.addSemester(number) },
            onSaved = { showAddSemesterDialog = false },
        )
    }

    deletingSemester?.let { semester ->
        ConfirmDialog(
            title = "Delete ${semester.displayName}?",
            message = "Semesters with subjects cannot be deleted.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteSemester(semester.id)
                deletingSemester = null
            },
            onDismiss = { deletingSemester = null },
        )
    }
}

@Composable
private fun AddSemesterDialog(
    onDismiss: () -> Unit,
    onSubmit: suspend (number: Int) -> AppResult<Unit>,
    onSaved: () -> Unit,
) {
    var numberText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Semester") },
        text = {
            Column {
                AppTextField(
                    value = numberText,
                    onValueChange = { numberText = it },
                    label = "Semester number (e.g. 1)",
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
                    val number = numberText.trim().toIntOrNull()
                    if (number == null || number <= 0) {
                        errorMessage = "Enter a valid semester number"
                        return@LoadingButton
                    }
                    scope.launch {
                        isSaving = true
                        errorMessage = null
                        when (val result = onSubmit(number)) {
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
