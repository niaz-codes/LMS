package com.example.uos_lms.core.data.remote.dto

import com.example.uos_lms.core.domain.model.AttendanceRecord
import com.example.uos_lms.core.domain.model.AttendanceStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class AttendanceRecordDto(
    val id: String = "",
    val subjectId: String = "",
    val departmentId: String = "",
    val semesterId: String = "",
    val dateKey: String = "",
    val dateMillis: Long = 0L,
    val studentUid: String = "",
    val studentName: String = "",
    val status: String = "",
    val markedBy: String = "",
    @ServerTimestamp val createdAt: Date? = null,
) {
    fun toDomain(): AttendanceRecord? {
        val statusEnum = AttendanceStatus.fromStringOrNull(status) ?: return null
        return AttendanceRecord(
            id = id,
            subjectId = subjectId,
            departmentId = departmentId,
            semesterId = semesterId,
            dateKey = dateKey,
            dateMillis = dateMillis,
            studentUid = studentUid,
            studentName = studentName,
            status = statusEnum,
            markedBy = markedBy,
            createdAt = createdAt?.time ?: 0L,
        )
    }
}

fun AttendanceRecord.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "subjectId" to subjectId,
    "departmentId" to departmentId,
    "semesterId" to semesterId,
    "dateKey" to dateKey,
    "dateMillis" to dateMillis,
    "studentUid" to studentUid,
    "studentName" to studentName,
    "status" to status.name,
    "markedBy" to markedBy,
    "createdAt" to FieldValue.serverTimestamp(),
)
