package com.example.uos_lms.core.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.PI
import kotlin.math.sin

/** Soft, slowly-bobbing radial-glow orbs — a cheap ambient background effect shared
 * by the gradient auth screens: one Canvas, one animated float driving trig math
 * for every orb (no per-orb state), so it stays smooth on low-end devices. */
@Composable
fun FloatingOrbsBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(26000, easing = LinearEasing)),
        label = "orbPhase",
    )

    Canvas(modifier = modifier) {
        val orbs = listOf(
            Triple(0.15f, 0.18f, 0.22f),
            Triple(0.88f, 0.14f, 0.16f),
            Triple(0.80f, 0.78f, 0.28f),
            Triple(0.18f, 0.86f, 0.18f),
            Triple(0.52f, 0.42f, 0.15f),
        )
        val minSide = size.minDimension
        orbs.forEachIndexed { index, (xFrac, yFrac, radiusFrac) ->
            val bob = sin(phase + index * 1.7f) * (minSide * 0.03f)
            val center = Offset(size.width * xFrac, size.height * yFrac + bob)
            val radius = minSide * radiusFrac
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White.copy(alpha = 0.16f), Color.Transparent),
                    center = center,
                    radius = radius,
                ),
                radius = radius,
                center = center,
            )
        }
    }
}
