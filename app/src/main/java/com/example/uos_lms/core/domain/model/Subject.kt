package com.example.uos_lms.core.domain.model

data class Subject(
    val id: String,
    val departmentId: String,
    val semesterId: String,
    val code: String,
    val title: String,
    val creditHours: Int,
    val teacherUid: String? = null,
    val teacherName: String? = null,
    val createdAt: Long = 0L,
)
