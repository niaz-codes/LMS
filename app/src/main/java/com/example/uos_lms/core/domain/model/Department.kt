package com.example.uos_lms.core.domain.model

data class Department(
    val id: String,
    val name: String,
    val code: String,
    val description: String = "",
    val createdAt: Long = 0L,
)
