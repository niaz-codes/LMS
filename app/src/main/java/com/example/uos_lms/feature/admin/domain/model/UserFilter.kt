package com.example.uos_lms.feature.admin.domain.model

import com.example.uos_lms.core.domain.model.UserStatus

enum class UserFilter(val label: String, val status: UserStatus?) {
    ALL("All", null),
    PENDING("Pending", UserStatus.PENDING),
    APPROVED("Approved", UserStatus.APPROVED),
    REJECTED("Rejected", UserStatus.REJECTED),
    SUSPENDED("Suspended", UserStatus.SUSPENDED),
}
