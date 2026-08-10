package com.example.uos_lms.core.domain.model

data class AttendanceRecord(
    val id: String,
    val subjectId: String,
    val departmentId: String,
    val semesterId: String,
    val dateKey: String,
    val dateMillis: Long,
    val studentUid: String,
    val studentName: String,
    val status: AttendanceStatus,
    val markedBy: String,
    val createdAt: Long = 0L,
)
