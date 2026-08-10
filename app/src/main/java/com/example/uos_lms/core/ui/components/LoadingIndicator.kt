package com.example.uos_lms.core.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Branded loading spinner — brand-primary color with a rounded stroke cap,
 * for reuse anywhere a screen wants the premium look instead of the bare
 * default CircularProgressIndicator(). Not swept into every existing loading
 * state in this pass (see plan Decision 4) — available going forward. Relies
 * on Material3's own built-in indeterminate rotation animation.
 */
@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    CircularProgressIndicator(
        modifier = modifier.size(size),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
        strokeWidth = 4.dp,
        strokeCap = StrokeCap.Round,
    )
}
