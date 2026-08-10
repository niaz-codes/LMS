package com.example.uos_lms.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.ui.theme.roleGradientColors

fun UserRole.displayLabel(): String = name.lowercase().replaceFirstChar { it.uppercase() }

fun roleIcon(role: UserRole): ImageVector = when (role) {
    UserRole.ADMIN -> Icons.Filled.VerifiedUser
    UserRole.HOD -> Icons.Filled.AdminPanelSettings
    UserRole.TEACHER -> Icons.Filled.School
    else -> Icons.Filled.Person
}

/** Row of animated, gradient-filled role chips shared by the Login and Register
 * screens — selection drives both this chip's own fill and, on Login, the whole
 * screen's background gradient. Login shows all four roles (including Admin);
 * Register restricts to [UserRole.REGISTERABLE_ROLES] since Admin accounts can
 * only be created manually in the database. */
@Composable
fun RoleSelectorRow(
    selected: UserRole,
    onSelect: (UserRole) -> Unit,
    modifier: Modifier = Modifier,
    roles: List<UserRole> = UserRole.REGISTERABLE_ROLES,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        roles.forEach { role ->
            RoleChip(
                role = role,
                isSelected = role == selected,
                onClick = { onSelect(role) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun RoleChip(
    role: UserRole,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val (roleStart, roleEnd) = roleGradientColors(role)
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "chipScale",
    )
    val selectionProgress by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0f,
        animationSpec = tween(350),
        label = "chipSelection",
    )
    val contentColor by animateColorAsState(
        targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
        label = "chipContent",
    )
    val backgroundStart = lerp(Color.White.copy(alpha = 0.14f), roleStart, selectionProgress)
    val backgroundEnd = lerp(Color.White.copy(alpha = 0.08f), roleEnd, selectionProgress)

    Column(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(RoundedCornerShape(18.dp))
            .background(Brush.linearGradient(listOf(backgroundStart, backgroundEnd)))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = if (isSelected) 0.6f else 0.22f),
                shape = RoundedCornerShape(18.dp),
            )
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(imageVector = roleIcon(role), contentDescription = role.displayLabel(), tint = contentColor)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = role.displayLabel(),
            style = MaterialTheme.typography.labelMedium,
            color = contentColor,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
