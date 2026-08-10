package com.example.uos_lms.core.domain.model

enum class UserStatus {
    PENDING, APPROVED, REJECTED, SUSPENDED;

    companion object {
        fun fromStringOrNull(raw: String?): UserStatus? = entries.find { it.name == raw }
    }
}
