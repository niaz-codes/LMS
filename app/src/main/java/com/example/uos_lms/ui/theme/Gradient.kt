package com.example.uos_lms.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.uos_lms.core.domain.model.UserRole

// Centralized gradient tokens sourced from the active ColorScheme, so every
// screen shares the same brand gradients instead of each one inventing its
// own listOf(primary, tertiary) pair ad hoc.

/** Full-bleed hero background — auth screens, GradientHeader. */
@Composable
fun heroGradient(): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.verticalGradient(
        colors = listOf(scheme.primary, scheme.primaryContainer, scheme.background),
    )
}

/** Horizontal accent gradient for compact header bands. */
@Composable
fun accentGradient(): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.horizontalGradient(colors = listOf(scheme.primary, scheme.tertiary))
}

/** Subtle gradient for card surfaces — barely-there tint, not a loud background. */
@Composable
fun cardGradient(): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.verticalGradient(
        colors = listOf(
            scheme.surface,
            scheme.primaryContainer.copy(alpha = 0.18f),
        ),
    )
}

/** Soft hairline border for the glassmorphism-approximation card. */
@Composable
fun glassBorderGradient(): Brush {
    val scheme = MaterialTheme.colorScheme
    return Brush.linearGradient(
        colors = listOf(
            scheme.onSurface.copy(alpha = 0.18f),
            scheme.onSurface.copy(alpha = 0.04f),
        ),
    )
}

internal fun ColorScheme.glassSurfaceColor(): Color = surface.copy(alpha = 0.72f)

/** Each role's brand gradient — HOD blue→purple, Teacher green→teal, Student
 * orange→pink, Admin indigo→violet. Drives the Login/Register screens' animated
 * background *and* each role's bottom nav bar accent, so the same color identity
 * carries through the whole app for that role. */
fun roleGradientColors(role: UserRole): Pair<Color, Color> = when (role) {
    UserRole.HOD -> LoginHodGradientStart to LoginHodGradientEnd
    UserRole.TEACHER -> LoginTeacherGradientStart to LoginTeacherGradientEnd
    UserRole.STUDENT -> LoginStudentGradientStart to LoginStudentGradientEnd
    UserRole.ADMIN -> AdminGradientStart to AdminGradientEnd
}
