package com.example.uos_lms.core.data.remote.dto

import com.example.uos_lms.core.domain.model.User
import com.example.uos_lms.core.domain.model.UserRole
import com.example.uos_lms.core.domain.model.UserStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class UserDto(
    val uid: String = "",
    val fullName: String = "",
    val fatherName: String = "",
    val cnic: String = "",
    val phone: String = "",
    val email: String = "",
    val role: String = "",
    val status: String = "",
    val profilePhotoUrl: String? = null,
    val profilePhotoPublicId: String? = null,
    val department: String? = null,
    val departmentIds: List<String> = emptyList(),
    val semester: String? = null,
    val employeeId: String? = null,
    val designation: String? = null,
    val registrationNumber: String? = null,
    val rollNumber: String? = null,
    val sessionId: String? = null,
    @ServerTimestamp val createdAt: Date? = null,
) {
    fun toDomain(): User? {
        val roleEnum = UserRole.fromStringOrNull(role) ?: return null
        val statusEnum = UserStatus.fromStringOrNull(status) ?: return null
        return User(
            uid = uid,
            fullName = fullName,
            fatherName = fatherName,
            cnic = cnic,
            phone = phone,
            email = email,
            role = roleEnum,
            status = statusEnum,
            profilePhotoUrl = profilePhotoUrl,
            profilePhotoPublicId = profilePhotoPublicId,
            department = department,
            departmentIds = departmentIds.ifEmpty { department?.let { listOf(it) } ?: emptyList() },
            semester = semester,
            employeeId = employeeId,
            designation = designation,
            registrationNumber = registrationNumber,
            rollNumber = rollNumber,
            sessionId = sessionId,
            createdAt = createdAt?.time ?: 0L,
        )
    }
}

fun User.toFirestoreMap(): Map<String, Any?> = mapOf(
    "uid" to uid,
    "fullName" to fullName,
    "fatherName" to fatherName,
    "cnic" to cnic,
    "phone" to phone,
    "email" to email,
    "role" to role.name,
    "status" to status.name,
    "profilePhotoUrl" to profilePhotoUrl,
    "profilePhotoPublicId" to profilePhotoPublicId,
    "department" to department,
    "departmentIds" to departmentIds,
    "semester" to semester,
    "employeeId" to employeeId,
    "designation" to designation,
    "registrationNumber" to registrationNumber,
    "rollNumber" to rollNumber,
    "sessionId" to sessionId,
    "createdAt" to FieldValue.serverTimestamp(),
)
