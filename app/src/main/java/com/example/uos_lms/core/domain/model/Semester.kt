package com.example.uos_lms.core.domain.model

data class Semester(
    val id: String,
    val departmentId: String,
    val number: Int,
    val createdAt: Long = 0L,
) {
    val displayName: String get() = "Semester $number"
}
