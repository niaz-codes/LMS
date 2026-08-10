package com.example.uos_lms.ui.theme

import androidx.compose.ui.graphics.Color

// Brand palette — premium indigo primary + emerald secondary + amber accent,
// replacing the earlier Blue/Slate/Gold set. Token names are unchanged so
// every screen/component that reads MaterialTheme.colorScheme picks up the
// new palette automatically with no per-screen edits.

// Light scheme
val IndigoPrimaryLight = Color(0xFF4F46E5)
val OnIndigoPrimaryLight = Color(0xFFFFFFFF)
val IndigoPrimaryContainerLight = Color(0xFFE0E1FF)
val OnIndigoPrimaryContainerLight = Color(0xFF2B2483)
val EmeraldSecondaryLight = Color(0xFF0D9488)
val OnEmeraldSecondaryLight = Color(0xFFFFFFFF)
val EmeraldSecondaryContainerLight = Color(0xFFCCFBF1)
val OnEmeraldSecondaryContainerLight = Color(0xFF0F3D39)
val AmberTertiaryLight = Color(0xFFB45309)
val OnAmberTertiaryLight = Color(0xFFFFFFFF)
val AmberTertiaryContainerLight = Color(0xFFFEF3C7)
val OnAmberTertiaryContainerLight = Color(0xFF78350F)
val ErrorLight = Color(0xFFDC2626)
val OnErrorLight = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFEE2E2)
val OnErrorContainerLight = Color(0xFF7F1D1D)
val BackgroundLight = Color(0xFFF7F7FC)
val OnBackgroundLight = Color(0xFF1A1B2E)
val SurfaceLight = Color(0xFFFFFFFF)
val OnSurfaceLight = Color(0xFF1A1B2E)
val SurfaceVariantLight = Color(0xFFE7E6F5)
val OnSurfaceVariantLight = Color(0xFF49475E)
val OutlineLight = Color(0xFF9997B0)

// Dark scheme
val IndigoPrimaryDark = Color(0xFFC2C1FF)
val OnIndigoPrimaryDark = Color(0xFF2B2483)
val IndigoPrimaryContainerDark = Color(0xFF3F37A8)
val OnIndigoPrimaryContainerDark = Color(0xFFE0E1FF)
val EmeraldSecondaryDark = Color(0xFF5EEAD4)
val OnEmeraldSecondaryDark = Color(0xFF0F3D39)
val EmeraldSecondaryContainerDark = Color(0xFF115E56)
val OnEmeraldSecondaryContainerDark = Color(0xFFCCFBF1)
val AmberTertiaryDark = Color(0xFFFCD34D)
val OnAmberTertiaryDark = Color(0xFF78350F)
val AmberTertiaryContainerDark = Color(0xFF92400E)
val OnAmberTertiaryContainerDark = Color(0xFFFEF3C7)
val ErrorDark = Color(0xFFF87171)
val OnErrorDark = Color(0xFF7F1D1D)
val ErrorContainerDark = Color(0xFF991B1B)
val OnErrorContainerDark = Color(0xFFFEE2E2)
val BackgroundDark = Color(0xFF15162A)
val OnBackgroundDark = Color(0xFFE5E4F5)
val SurfaceDark = Color(0xFF1E1F38)
val OnSurfaceDark = Color(0xFFE5E4F5)
val SurfaceVariantDark = Color(0xFF39395A)
val OnSurfaceVariantDark = Color(0xFFC7C5DE)
val OutlineDark = Color(0xFF7A7896)

// Fixed (theme-independent) role-branding gradients for the Login screen's
// animated background — deliberately literal hex values rather than derived
// from the ColorScheme, since these represent a role's identity color, not
// the app's light/dark brand palette above.
val LoginHodGradientStart = Color(0xFF2563EB) // blue-600
val LoginHodGradientEnd = Color(0xFF9333EA) // purple-600
val LoginTeacherGradientStart = Color(0xFF059669) // green/emerald-600
val LoginTeacherGradientEnd = Color(0xFF0D9488) // teal-600
val LoginStudentGradientStart = Color(0xFFF97316) // orange-500
val LoginStudentGradientEnd = Color(0xFFDB2777) // pink-600
val AdminGradientStart = Color(0xFF4F46E5) // indigo-600 (matches brand primary)
val AdminGradientEnd = Color(0xFF7C3AED) // violet-600
