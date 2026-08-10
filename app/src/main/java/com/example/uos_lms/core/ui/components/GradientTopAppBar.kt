package com.example.uos_lms.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.uos_lms.ui.theme.accentGradient

/**
 * Universal gradient replacement for the flat `containerColor = surface` TopAppBar
 * every non-auth screen used previously — same Scaffold(topBar = { ... }) slot, so
 * it's a drop-in swap per screen. Defaults to the app's general brand gradient
 * ([accentGradient]); role-specific screens should pass a `Brush.horizontalGradient`
 * built from `roleGradientColors(role)` instead, so a screen's app bar matches that
 * role's bottom-nav/dashboard identity.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradientTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    gradient: Brush = accentGradient(),
    actions: @Composable RowScope.() -> Unit = {},
) {
    Box(modifier = modifier.background(gradient)) {
        TopAppBar(
            title = { Text(title, fontWeight = FontWeight.Bold, color = Color.White) },
            navigationIcon = {
                if (onBack != null) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                }
            },
            actions = actions,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent,
                titleContentColor = Color.White,
                navigationIconContentColor = Color.White,
                actionIconContentColor = Color.White,
            ),
        )
    }
}
