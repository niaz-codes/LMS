package com.example.uos_lms.core.data.remote.dto

import com.example.uos_lms.core.domain.model.Assignment
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class AssignmentDto(
    val id: String = "",
    val subjectId: String = "",
    val departmentId: String = "",
    val semesterId: String = "",
    val title: String = "",
    val description: String = "",
    val dueDateMillis: Long = 0L,
    val maxMarks: Int = 0,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Date? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val filePublicId: String? = null,
    val fileResourceType: String? = null,
    val fileSize: Long? = null,
) {
    fun toDomain() = Assignment(
        id = id,
        subjectId = subjectId,
        departmentId = departmentId,
        semesterId = semesterId,
        title = title,
        description = description,
        dueDateMillis = dueDateMillis,
        maxMarks = maxMarks,
        createdBy = createdBy,
        createdAt = createdAt?.time ?: 0L,
        fileUrl = fileUrl,
        fileName = fileName,
        filePublicId = filePublicId,
        fileResourceType = fileResourceType,
        fileSize = fileSize,
    )
}

fun Assignment.toCreateMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "subjectId" to subjectId,
    "departmentId" to departmentId,
    "semesterId" to semesterId,
    "title" to title,
    "description" to description,
    "dueDateMillis" to dueDateMillis,
    "maxMarks" to maxMarks,
    "createdBy" to createdBy,
    "createdAt" to FieldValue.serverTimestamp(),
    "fileUrl" to fileUrl,
    "fileName" to fileName,
    "filePublicId" to filePublicId,
    "fileResourceType" to fileResourceType,
    "fileSize" to fileSize,
)
