package com.example.uos_lms.feature.auth.presentation.status

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.runtime.Composable

@Composable
fun SuspendedScreen(onBackToLogin: () -> Unit) {
    StatusMessageScaffold(
        icon = Icons.Default.Block,
        title = "Account Suspended",
        message = "Your account has been suspended. Please contact the administrator.",
        onBackToLogin = onBackToLogin,
    )
}
