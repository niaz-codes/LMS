package com.example.uos_lms.core.domain.model

enum class UserRole {
    ADMIN, HOD, TEACHER, STUDENT;

    companion object {
        fun fromStringOrNull(raw: String?): UserRole? = entries.find { it.name == raw }

        val REGISTERABLE_ROLES = listOf(HOD, TEACHER, STUDENT)
        val LOGIN_ROLES = listOf(ADMIN, HOD, TEACHER, STUDENT)
    }
}
