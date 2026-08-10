package com.example.uos_lms.feature.admin.university.presentation.department

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.ui.components.ConfirmDialog
import com.example.uos_lms.core.ui.components.EmptyState
import com.example.uos_lms.core.ui.components.ErrorState
import com.example.uos_lms.core.ui.components.GradientTopAppBar
import com.example.uos_lms.feature.admin.presentation.components.AdminBottomNavBar
import com.example.uos_lms.feature.admin.presentation.components.AdminTab

@Composable
fun DepartmentListScreen(
    onDepartmentClick: (String) -> Unit,
    onHomeClick: () -> Unit,
    onUsersClick: () -> Unit,
    onReportsClick: () -> Unit,
    onProfileClick: () -> Unit,
    viewModel: DepartmentListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var editingDepartment by remember { mutableStateOf<Department?>(null) }
    var deletingDepartment by remember { mutableStateOf<Department?>(null) }

    Scaffold(
        topBar = {
            GradientTopAppBar(title = "Departments")
        },
        bottomBar = {
            AdminBottomNavBar(
                selected = AdminTab.DEPARTMENTS,
                onHomeClick = onHomeClick,
                onUsersClick = onUsersClick,
                onDepartmentsClick = {},
                onReportsClick = onReportsClick,
                onProfileClick = onProfileClick,
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Department")
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            uiState.listErrorMessage?.let { message ->
                ErrorState(message = message, onDismiss = viewModel::clearListError)
            }

            if (uiState.departments.isEmpty() && !uiState.isLoading) {
                EmptyState(message = "No departments yet. Tap + to add one.", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(uiState.departments, key = { it.id }) { department ->
                        DepartmentRow(
                            department = department,
                            onClick = { onDepartmentClick(department.id) },
                            onEdit = { editingDepartment = department },
                            onDelete = { deletingDepartment = department },
                            modifier = Modifier.animateItem(),
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        DepartmentFormDialog(
            initial = null,
            onDismiss = { showAddDialog = false },
            onSubmit = { name, code, description -> viewModel.createDepartment(name, code, description) },
            onSaved = { showAddDialog = false },
        )
    }
    editingDepartment?.let { department ->
        DepartmentFormDialog(
            initial = department,
            onDismiss = { editingDepartment = null },
            onSubmit = { name, code, description -> viewModel.updateDepartment(department.id, name, code, description) },
            onSaved = { editingDepartment = null },
        )
    }
    deletingDepartment?.let { department ->
        ConfirmDialog(
            title = "Delete department?",
            message = "This removes \"${department.name}\". Departments with semesters cannot be deleted.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteDepartment(department.id)
                deletingDepartment = null
            },
            onDismiss = { deletingDepartment = null },
        )
    }
}

@Composable
private fun DepartmentRow(
    department: Department,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable(onClick = onClick),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.tertiaryContainer),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Default.Apartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiaryContainer,
                        modifier = Modifier.size(22.dp),
                    )
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(department.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(department.code, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (department.description.isNotBlank()) {
                        Text(
                            department.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
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
    }
}
