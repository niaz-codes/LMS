package com.example.uos_lms.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// A more rounded shape scale than Material3's defaults, applied globally via
// MaterialTheme(shapes = AppShapes, ...) in Theme.kt — any Card/Button/
// TextField/Dialog/NavigationBar that doesn't already override its own shape
// picks this up automatically, app-wide, from this one place.
val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
