package com.example.uos_lms.feature.hod.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.AppBottomNavBar
import com.example.uos_lms.core.ui.components.NavTab
import com.example.uos_lms.core.ui.components.ProfileTabAvatarIcon
import com.example.uos_lms.ui.theme.roleGradientColors

enum class HodTab(val label: String, val outlinedIcon: ImageVector, val filledIcon: ImageVector) {
    HOME("Home", Icons.Outlined.Home, Icons.Filled.Home),
    TEACHERS("Teachers", Icons.Outlined.Group, Icons.Filled.Group),
    STUDENTS("Students", Icons.Outlined.School, Icons.Filled.School),
    ATTENDANCE("Attendance", Icons.Outlined.EventAvailable, Icons.Filled.EventAvailable),
    REPORTS("Reports", Icons.Outlined.Assessment, Icons.Filled.Assessment),
    PROFILE("Profile", Icons.Outlined.Home, Icons.Filled.Home), // icons unused — customIcon overrides
}

@Composable
fun HodBottomNavBar(
    selected: HodTab,
    onHomeClick: () -> Unit,
    onTeachersClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onReportsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    AppBottomNavBar(
        tabs = HodTab.entries.map { tab ->
            if (tab == HodTab.PROFILE) {
                NavTab(
                    label = tab.label,
                    outlinedIcon = tab.outlinedIcon,
                    filledIcon = tab.filledIcon,
                    customIcon = { ProfileTabAvatarIcon(isSelected = selected == HodTab.PROFILE) },
                )
            } else {
                NavTab(tab.label, tab.outlinedIcon, tab.filledIcon)
            }
        },
        selectedIndex = HodTab.entries.indexOf(selected),
        onTabClick = { index ->
            when (HodTab.entries[index]) {
                HodTab.HOME -> onHomeClick()
                HodTab.TEACHERS -> onTeachersClick()
                HodTab.STUDENTS -> onStudentsClick()
                HodTab.ATTENDANCE -> onAttendanceClick()
                HodTab.REPORTS -> onReportsClick()
                HodTab.PROFILE -> onProfileClick()
            }
        },
        accentColors = roleGradientColors(UserRole.HOD),
    )
}
