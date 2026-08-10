package com.example.uos_lms.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserStatus

/**
 * Shared View/Approve/Reject/Suspend/Activate/Reset Password/Delete action
 * menu — extracted from the flat "Users" tab's original inline UserRow logic
 * so the new hierarchical HOD/Teacher/Student Management cards reuse the
 * exact same action set instead of re-implementing it three times.
 */
@Composable
fun UserQuickActionsRow(
    user: User,
    onView: () -> Unit,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    onSuspend: () -> Unit,
    onActivate: () -> Unit,
    onDelete: () -> Unit,
    onResetPassword: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { menuExpanded = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "Actions")
        }
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(text = { Text("View Profile") }, onClick = { menuExpanded = false; onView() })
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
