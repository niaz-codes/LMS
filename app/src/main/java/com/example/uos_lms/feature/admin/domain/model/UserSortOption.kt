package com.example.uos_lms.feature.admin.domain.model

import com.example.uos_lms.core.domain.model.User

enum class UserSortOption(val label: String) {
    NAME_ASC("Name (A-Z)"),
    NAME_DESC("Name (Z-A)"),
    STATUS("Status"),
    NEWEST("Newest"),
}

fun List<User>.sortedByOption(option: UserSortOption): List<User> = when (option) {
    UserSortOption.NAME_ASC -> sortedBy { it.fullName.lowercase() }
    UserSortOption.NAME_DESC -> sortedByDescending { it.fullName.lowercase() }
    UserSortOption.STATUS -> sortedBy { it.status.name }
    UserSortOption.NEWEST -> sortedByDescending { it.createdAt }
}
