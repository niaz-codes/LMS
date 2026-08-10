package com.example.uos_lms.core.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp

data class NavTab(
    val label: String,
    val outlinedIcon: ImageVector,
    val filledIcon: ImageVector,
    /** Overrides the vector icon when set — e.g. a live user-photo avatar for a Profile tab. */
    val customIcon: (@Composable () -> Unit)? = null,
    /** Shown as a small counter badge on the icon when > 0 (e.g. pending approvals). */
    val badgeCount: Int? = null,
)

/**
 * Premium animated bottom nav bar shared by every role — a sliding gradient "pill"
 * with a soft glow tracks the selected tab, each icon springs/bounces and morphs
 * between its outlined and filled variant, and a light haptic tick fires on tap.
 * Fully custom (not Material3's NavigationBar/NavigationBarItem) so the gradient
 * pill/glow can be drawn exactly as designed — no real blur is used anywhere here,
 * consistent with this app's established glassmorphism-approximation approach
 * (see GlassCard): the "glow" is a soft radial-gradient falloff, not RenderEffect.
 * [accentColors] is the role's brand gradient (see roleGradientColors) — the one
 * thing that visually distinguishes Admin/HOD/Teacher/Student while every other
 * behavior (layout, animation, badges) stays identical across all four.
 */
@Composable
fun AppBottomNavBar(
    tabs: List<NavTab>,
    selectedIndex: Int,
    onTabClick: (Int) -> Unit,
    accentColors: Pair<Color, Color> = MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.tertiary,
) {
    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 3.dp,
        shadowElevation = 16.dp,
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(vertical = 10.dp),
        ) {
            val tabWidth = maxWidth / tabs.size
            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
                label = "indicatorOffset",
            )

            // Soft glow behind the sliding pill — a radial-gradient falloff, not a blur.
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .height(44.dp),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            Brush.radialGradient(listOf(accentColors.first.copy(alpha = 0.35f), Color.Transparent)),
                            CircleShape,
                        ),
                )
            }

            // Sliding gradient pill.
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(tabWidth)
                    .height(44.dp)
                    .padding(horizontal = 14.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Brush.horizontalGradient(listOf(accentColors.first, accentColors.second))),
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavBarTabItem(
                        tab = tab,
                        isSelected = index == selectedIndex,
                        onClick = { onTabClick(index) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun NavBarTabItem(
    tab: NavTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptics = LocalHapticFeedback.current
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.18f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "navIconScale",
    )
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "navIconColor",
    )

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(50))
            .clickable {
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        BadgedBox(
            badge = {
                val count = tab.badgeCount
                if (count != null && count > 0) {
                    Badge { Text(if (count > 9) "9+" else count.toString()) }
                }
            },
        ) {
            if (tab.customIcon != null) {
                tab.customIcon.invoke()
            } else {
                Crossfade(targetState = isSelected, label = "iconMorph") { selected ->
                    Icon(
                        imageVector = if (selected) tab.filledIcon else tab.outlinedIcon,
                        contentDescription = tab.label,
                        tint = iconColor,
                        modifier = Modifier
                            .size(24.dp)
                            .graphicsLayer { scaleX = scale; scaleY = scale },
                    )
                }
            }
        }
    }
}
