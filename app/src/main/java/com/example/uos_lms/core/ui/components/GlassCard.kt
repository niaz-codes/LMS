package com.example.uos_lms.core.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.uos_lms.ui.theme.glassBorderGradient
import com.example.uos_lms.ui.theme.glassSurfaceColor

/**
 * Approximated glassmorphism container — a semi-transparent surface with a
 * soft gradient hairline border and elevation, not real backdrop blur (see
 * [[uos-lms-toolchain-pins]]-style reasoning: Modifier.blur/RenderEffect is
 * Android 12+ only with no reliable pre-S fallback, and a blur library is an
 * avoidable new dependency for a purely cosmetic effect). Reads as "glass"
 * when placed over a gradient background, without either risk.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 28.dp,
    content: @Composable ColumnScope.() -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    Surface(
        modifier = modifier.border(width = 1.dp, brush = glassBorderGradient(), shape = shape),
        shape = shape,
        color = MaterialTheme.colorScheme.glassSurfaceColor(),
        tonalElevation = 4.dp,
        shadowElevation = 12.dp,
    ) {
        Column(content = content)
    }
}
