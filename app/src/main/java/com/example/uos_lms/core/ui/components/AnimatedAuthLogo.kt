package com.example.uos_lms.core.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.uos_lms.R

/** Entrance scale/fade + a continuous gentle float + an expanding-fading pulse
 * ring behind the app logo, like a soft radar ping. Shared by the gradient auth
 * screens (Login/Register) for a consistent premium first impression. */
@Composable
fun AnimatedAuthLogo(
    visibleState: MutableTransitionState<Boolean>,
    modifier: Modifier = Modifier,
    logoSize: Dp = 110.dp,
) {
    val bobTransition = rememberInfiniteTransition(label = "logoBobTransition")
    val bobOffset by bobTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse),
        label = "logoBob",
    )

    val pulseTransition = rememberInfiniteTransition(label = "logoPulseTransition")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing)),
        label = "pulseScale",
    )
    val pulseAlpha by pulseTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(1800, easing = FastOutSlowInEasing)),
        label = "pulseAlpha",
    )

    AnimatedVisibility(
        visibleState = visibleState,
        enter = fadeIn(tween(600)) + scaleIn(
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
            initialScale = 0.5f,
        ),
    ) {
        Box(modifier = modifier.size(logoSize + 30.dp), contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(logoSize)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = pulseAlpha
                    }
                    .background(Color.White, CircleShape),
            )
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "University of Shangla logo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .offset(y = bobOffset.dp)
                    .size(logoSize)
                    .shadow(elevation = 14.dp, shape = CircleShape)
                    .clip(CircleShape)
                    .border(3.dp, Color.White, CircleShape),
            )
        }
    }
}
