package com.example.uos_lms.core.domain.model

data class User(
    val uid: String,
    val fullName: String,
    val fatherName: String,
    val cnic: String,
    val phone: String,
    val email: String,
    val role: UserRole,
    val status: UserStatus,
    val profilePhotoUrl: String? = null,
    val profilePhotoPublicId: String? = null,
    // HOD/STUDENT: single department, `department` is authoritative.
    // TEACHER: multi-department, `departmentIds` is authoritative and `department`
    // mirrors departmentIds.first() for legacy single-department queries/display.
    val department: String? = null,
    val departmentIds: List<String> = emptyList(),
    val semester: String? = null,
    val employeeId: String? = null,
    val designation: String? = null,
    val registrationNumber: String? = null,
    val rollNumber: String? = null,
    val sessionId: String? = null,
    val createdAt: Long = 0L,
)
