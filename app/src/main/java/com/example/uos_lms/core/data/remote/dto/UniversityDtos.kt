package com.example.uos_lms.core.data.remote.dto

import com.example.uos_lms.core.domain.model.Department
import com.example.uos_lms.core.domain.model.Semester
import com.example.uos_lms.core.domain.model.Session
import com.example.uos_lms.core.domain.model.Subject
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class DepartmentDto(
    val id: String = "",
    val name: String = "",
    val code: String = "",
    val description: String = "",
    @ServerTimestamp val createdAt: Date? = null,
) {
    fun toDomain() = Department(
        id = id,
        name = name,
        code = code,
        description = description,
        createdAt = createdAt?.time ?: 0L,
    )
}

fun Department.toCreateMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "name" to name,
    "code" to code,
    "description" to description,
    "createdAt" to FieldValue.serverTimestamp(),
)

fun Department.toUpdateMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "code" to code,
    "description" to description,
    "updatedAt" to FieldValue.serverTimestamp(),
)

data class SemesterDto(
    val id: String = "",
    val departmentId: String = "",
    val number: Int = 0,
    @ServerTimestamp val createdAt: Date? = null,
) {
    fun toDomain() = Semester(
        id = id,
        departmentId = departmentId,
        number = number,
        createdAt = createdAt?.time ?: 0L,
    )
}

fun Semester.toCreateMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "departmentId" to departmentId,
    "number" to number,
    "createdAt" to FieldValue.serverTimestamp(),
)

data class SessionDto(
    val id: String = "",
    val departmentId: String = "",
    val label: String = "",
    val isActive: Boolean = true,
    @ServerTimestamp val createdAt: Date? = null,
) {
    fun toDomain() = Session(
        id = id,
        departmentId = departmentId,
        label = label,
        isActive = isActive,
        createdAt = createdAt?.time ?: 0L,
    )
}

fun Session.toCreateMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "departmentId" to departmentId,
    "label" to label,
    "isActive" to isActive,
    "createdAt" to FieldValue.serverTimestamp(),
)

fun Session.toUpdateMap(): Map<String, Any?> = mapOf(
    "label" to label,
    "isActive" to isActive,
    "updatedAt" to FieldValue.serverTimestamp(),
)

data class SubjectDto(
    val id: String = "",
    val departmentId: String = "",
    val semesterId: String = "",
    val code: String = "",
    val title: String = "",
    val creditHours: Int = 0,
    val teacherUid: String? = null,
    val teacherName: String? = null,
    @ServerTimestamp val createdAt: Date? = null,
) {
    fun toDomain() = Subject(
        id = id,
        departmentId = departmentId,
        semesterId = semesterId,
        code = code,
        title = title,
        creditHours = creditHours,
        teacherUid = teacherUid,
        teacherName = teacherName,
        createdAt = createdAt?.time ?: 0L,
    )
}

fun Subject.toCreateMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "departmentId" to departmentId,
    "semesterId" to semesterId,
    "code" to code,
    "title" to title,
    "creditHours" to creditHours,
    "createdAt" to FieldValue.serverTimestamp(),
)

fun Subject.toUpdateMap(): Map<String, Any?> = mapOf(
    "code" to code,
    "title" to title,
    "creditHours" to creditHours,
    "updatedAt" to FieldValue.serverTimestamp(),
)
