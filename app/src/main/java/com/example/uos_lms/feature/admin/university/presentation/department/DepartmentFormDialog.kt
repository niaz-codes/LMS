package com.example.uos_lms.feature.admin.university.presentation.department

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.ui.components.AppTextField
import com.example.uos_lms.core.ui.components.LoadingButton
import kotlinx.coroutines.launch

@Composable
fun DepartmentFormDialog(
    initial: Department?,
    onDismiss: () -> Unit,
    onSubmit: suspend (name: String, code: String, description: String) -> AppResult<Unit>,
    onSaved: () -> Unit,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var code by remember { mutableStateOf(initial?.code ?: "") }
    var description by remember { mutableStateOf(initial?.description ?: "") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add Department" else "Edit Department") },
        text = {
            Column {
                AppTextField(value = name, onValueChange = { name = it }, label = "Name", modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = "Code (e.g. CS)",
                    modifier = Modifier.fillMaxWidth(),
                )
                Spacer(modifier = Modifier.height(8.dp))
                AppTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "Description (optional)",
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
                    val trimmedName = name.trim()
                    val trimmedCode = code.trim().uppercase()
                    if (trimmedName.isEmpty() || trimmedCode.isEmpty()) {
                        errorMessage = "Name and code are required"
                        return@LoadingButton
                    }
                    scope.launch {
                        isSaving = true
                        errorMessage = null
                        when (val result = onSubmit(trimmedName, trimmedCode, description.trim())) {
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
