package com.example.uos_lms.core.ui.components

import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.uos_lms.core.domain.model.ResultStatus

/** Shared across Teacher/HOD/Student/Admin exam-result screens so the status
 * vocabulary (and its colors) stay identical everywhere a result is shown. */
@Composable
fun ResultStatusChip(status: ResultStatus) {
    val (containerColor, contentColor, label) = when (status) {
        ResultStatus.DRAFT -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant,
            "Draft",
        )
        ResultStatus.PENDING_APPROVAL -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer,
            "Pending Approval",
        )
        ResultStatus.APPROVED -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "Approved",
        )
        ResultStatus.REJECTED -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "Rejected",
        )
    }
    AssistChip(
        onClick = {},
        enabled = false,
        label = { Text(label) },
        colors = AssistChipDefaults.assistChipColors(
            disabledContainerColor = containerColor,
            disabledLabelColor = contentColor,
        ),
    )
}
