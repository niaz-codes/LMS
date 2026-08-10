package com.example.uos_lms.feature.teacher.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.AppBottomNavBar
import com.example.uos_lms.core.ui.components.NavTab
import com.example.uos_lms.core.ui.components.ProfileTabAvatarIcon
import com.example.uos_lms.ui.theme.roleGradientColors

enum class TeacherTab(val label: String, val outlinedIcon: ImageVector, val filledIcon: ImageVector) {
    HOME("Home", Icons.Outlined.Home, Icons.Filled.Home),
    STUDENTS("Students", Icons.Outlined.Group, Icons.Filled.Group),
    ATTENDANCE("Attendance", Icons.Outlined.EventAvailable, Icons.Filled.EventAvailable),
    REPORTS("Reports", Icons.Outlined.Assessment, Icons.Filled.Assessment),
    ANNOUNCEMENTS("Announcements", Icons.Outlined.Campaign, Icons.Filled.Campaign),
    PROFILE("Profile", Icons.Outlined.Home, Icons.Filled.Home), // icons unused — customIcon overrides
}

@Composable
fun TeacherBottomNavBar(
    selected: TeacherTab,
    onHomeClick: () -> Unit,
    onStudentsClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onReportsClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    AppBottomNavBar(
        tabs = TeacherTab.entries.map { tab ->
            if (tab == TeacherTab.PROFILE) {
                NavTab(
                    label = tab.label,
                    outlinedIcon = tab.outlinedIcon,
                    filledIcon = tab.filledIcon,
                    customIcon = { ProfileTabAvatarIcon(isSelected = selected == TeacherTab.PROFILE) },
                )
            } else {
                NavTab(tab.label, tab.outlinedIcon, tab.filledIcon)
            }
        },
        selectedIndex = TeacherTab.entries.indexOf(selected),
        onTabClick = { index ->
            when (TeacherTab.entries[index]) {
                TeacherTab.HOME -> onHomeClick()
                TeacherTab.STUDENTS -> onStudentsClick()
                TeacherTab.ATTENDANCE -> onAttendanceClick()
                TeacherTab.REPORTS -> onReportsClick()
                TeacherTab.ANNOUNCEMENTS -> onAnnouncementsClick()
                TeacherTab.PROFILE -> onProfileClick()
            }
        },
        accentColors = roleGradientColors(UserRole.TEACHER),
    )
}
