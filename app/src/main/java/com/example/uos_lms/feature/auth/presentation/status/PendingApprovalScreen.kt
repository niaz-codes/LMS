package com.example.uos_lms.feature.auth.presentation.status

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.runtime.Composable

@Composable
fun PendingApprovalScreen(onBackToLogin: () -> Unit) {
    StatusMessageScaffold(
        icon = Icons.Default.HourglassEmpty,
        title = "Pending Approval",
        message = "Your account is waiting for Admin approval.",
        onBackToLogin = onBackToLogin,
    )
}
