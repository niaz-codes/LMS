package com.example.uos_lms.feature.auth.presentation.status

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.runtime.Composable

@Composable
fun RejectedScreen(onBackToLogin: () -> Unit) {
    StatusMessageScaffold(
        icon = Icons.Default.Cancel,
        title = "Registration Rejected",
        message = "Your registration request has been rejected.",
        onBackToLogin = onBackToLogin,
    )
}
