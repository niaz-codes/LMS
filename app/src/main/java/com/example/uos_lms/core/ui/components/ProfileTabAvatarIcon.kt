package com.example.uos_lms.core.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil3.compose.AsyncImage

/** Profile tab icon that shows the signed-in user's live photo once uploaded,
 * falling back to a generic Person icon — used as a NavTab's customIcon. */
@Composable
fun ProfileTabAvatarIcon(isSelected: Boolean) {
    val viewModel: NavAvatarViewModel = hiltViewModel()
    val photoUrl by viewModel.photoUrl.collectAsState()
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "profileTabIconScale",
    )

    if (photoUrl != null) {
        AsyncImage(
            model = photoUrl,
            contentDescription = "Profile",
            modifier = Modifier
                .size(24.dp)
                .scale(scale)
                .clip(CircleShape),
        )
    } else {
        Icon(
            imageVector = if (isSelected) Icons.Filled.Person else Icons.Outlined.Person,
            contentDescription = "Profile",
            modifier = Modifier
                .size(24.dp)
                .scale(scale),
        )
    }
}
