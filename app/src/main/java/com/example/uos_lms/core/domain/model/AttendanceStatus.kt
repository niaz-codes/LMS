package com.example.uos_lms.core.domain.model

enum class AttendanceStatus {
    PRESENT, ABSENT;

    companion object {
        fun fromStringOrNull(raw: String?): AttendanceStatus? = entries.find { it.name == raw }
    }
}
