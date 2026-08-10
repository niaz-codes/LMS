package com.example.uos_lms.feature.student.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EventAvailable
import androidx.compose.material.icons.outlined.Home
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.ui.components.AppBottomNavBar
import com.example.uos_lms.core.ui.components.NavTab
import com.example.uos_lms.core.ui.components.ProfileTabAvatarIcon
import com.example.uos_lms.ui.theme.roleGradientColors

enum class StudentTab(val label: String, val outlinedIcon: ImageVector, val filledIcon: ImageVector) {
    HOME("Home", Icons.Outlined.Home, Icons.Filled.Home),
    ATTENDANCE("Attendance", Icons.Outlined.EventAvailable, Icons.Filled.EventAvailable),
    SUBMISSIONS("Submissions", Icons.Outlined.Assignment, Icons.Filled.Assignment),
    CALENDAR("Calendar", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    ANNOUNCEMENTS("Announcements", Icons.Outlined.Campaign, Icons.Filled.Campaign),
    PROFILE("Profile", Icons.Outlined.Home, Icons.Filled.Home), // icons unused — customIcon overrides
}

@Composable
fun StudentBottomNavBar(
    selected: StudentTab,
    onHomeClick: () -> Unit,
    onAttendanceClick: () -> Unit,
    onSubmissionsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onAnnouncementsClick: () -> Unit,
    onProfileClick: () -> Unit,
) {
    AppBottomNavBar(
        tabs = StudentTab.entries.map { tab ->
            if (tab == StudentTab.PROFILE) {
                NavTab(
                    label = tab.label,
                    outlinedIcon = tab.outlinedIcon,
                    filledIcon = tab.filledIcon,
                    customIcon = { ProfileTabAvatarIcon(isSelected = selected == StudentTab.PROFILE) },
                )
            } else {
                NavTab(tab.label, tab.outlinedIcon, tab.filledIcon)
            }
        },
        selectedIndex = StudentTab.entries.indexOf(selected),
        onTabClick = { index ->
            when (StudentTab.entries[index]) {
                StudentTab.HOME -> onHomeClick()
                StudentTab.ATTENDANCE -> onAttendanceClick()
                StudentTab.SUBMISSIONS -> onSubmissionsClick()
                StudentTab.CALENDAR -> onCalendarClick()
                StudentTab.ANNOUNCEMENTS -> onAnnouncementsClick()
                StudentTab.PROFILE -> onProfileClick()
            }
        },
        accentColors = roleGradientColors(UserRole.STUDENT),
    )
}
