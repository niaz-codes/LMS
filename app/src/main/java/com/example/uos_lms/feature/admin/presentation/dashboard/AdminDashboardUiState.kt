package com.example.uos_lms.feature.admin.presentation.dashboard

import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.feature.admin.domain.model.UserFilter

data class AdminDashboardUiState(
    val allUsers: List<User> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: UserFilter = UserFilter.ALL,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val actionMessage: String? = null,
) {
    val filteredUsers: List<User>
        get() = allUsers
            .filter { selectedFilter.status == null || it.status == selectedFilter.status }
            .filter {
                searchQuery.isBlank() ||
                    it.fullName.contains(searchQuery, ignoreCase = true) ||
                    it.email.contains(searchQuery, ignoreCase = true) ||
                    it.cnic.contains(searchQuery)
            }
}
