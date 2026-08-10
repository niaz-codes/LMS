package com.example.uos_lms.core.domain.model

data class Session(
    val id: String,
    val departmentId: String,
    val label: String,
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
)
