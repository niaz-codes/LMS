package com.example.uos_lms.feature.admin.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.AppBottomNavBar
import com.example.uos_lms.core.ui.components.NavTab
import com.example.uos_lms.core.ui.components.ProfileTabAvatarIcon
import com.example.uos_lms.ui.theme.roleGradientColors

enum class AdminTab(val label: String, val outlinedIcon: ImageVector, val filledIcon: ImageVector) {
    HOME("Home", Icons.Outlined.Home, Icons.Filled.Home),
    USERS("Users", Icons.Outlined.Group, Icons.Filled.Group),
    DEPARTMENTS("Departments", Icons.Outlined.Apartment, Icons.Filled.Apartment),
    REPORTS("Reports", Icons.Outlined.Assessment, Icons.Filled.Assessment),
    PROFILE("Profile", Icons.Outlined.Home, Icons.Filled.Home), // icons unused — customIcon overrides
}

@Composable
fun AdminBottomNavBar(
    selected: AdminTab,
    onHomeClick: () -> Unit,
    onUsersClick: () -> Unit,
    onDepartmentsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onProfileClick: () -> Unit,
    badgeViewModel: AdminNavBadgeViewModel = hiltViewModel(),
) {
    val pendingCount by badgeViewModel.pendingCount.collectAsState()

    AppBottomNavBar(
        tabs = AdminTab.entries.map { tab ->
            when (tab) {
                AdminTab.PROFILE -> NavTab(
                    label = tab.label,
                    outlinedIcon = tab.outlinedIcon,
                    filledIcon = tab.filledIcon,
                    customIcon = { ProfileTabAvatarIcon(isSelected = selected == AdminTab.PROFILE) },
                )
                AdminTab.USERS -> NavTab(
                    label = tab.label,
                    outlinedIcon = tab.outlinedIcon,
                    filledIcon = tab.filledIcon,
                    badgeCount = pendingCount,
                )
                else -> NavTab(tab.label, tab.outlinedIcon, tab.filledIcon)
            }
        },
        selectedIndex = AdminTab.entries.indexOf(selected),
        onTabClick = { index ->
            when (AdminTab.entries[index]) {
                AdminTab.HOME -> onHomeClick()
                AdminTab.USERS -> onUsersClick()
                AdminTab.DEPARTMENTS -> onDepartmentsClick()
                AdminTab.REPORTS -> onReportsClick()
                AdminTab.PROFILE -> onProfileClick()
            }
        },
        accentColors = roleGradientColors(UserRole.ADMIN),
    )
}
