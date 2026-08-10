package com.example.uos_lms.core.data.remote.dto

import com.example.uos_lms.core.domain.model.MaterialType
import com.example.uos_lms.core.domain.model.StudyMaterial
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class StudyMaterialDto(
    val id: String = "",
    val subjectId: String = "",
    val departmentId: String = "",
    val semesterId: String = "",
    val title: String = "",
    val materialType: String = MaterialType.OTHER.name,
    val fileUrl: String = "",
    val fileName: String = "",
    val filePublicId: String = "",
    val fileResourceType: String = "",
    val fileSize: Long = 0L,
    val uploadedBy: String = "",
    val uploadedByName: String = "",
    @ServerTimestamp val uploadedAt: Date? = null,
) {
    fun toDomain() = StudyMaterial(
        id = id,
        subjectId = subjectId,
        departmentId = departmentId,
        semesterId = semesterId,
        title = title,
        materialType = MaterialType.fromStringOrNull(materialType) ?: MaterialType.OTHER,
        fileUrl = fileUrl,
        fileName = fileName,
        filePublicId = filePublicId,
        fileResourceType = fileResourceType,
        fileSize = fileSize,
        uploadedBy = uploadedBy,
        uploadedByName = uploadedByName,
        uploadedAt = uploadedAt?.time ?: 0L,
    )
}

fun StudyMaterial.toCreateMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "subjectId" to subjectId,
    "departmentId" to departmentId,
    "semesterId" to semesterId,
    "title" to title,
    "materialType" to materialType.name,
    "fileUrl" to fileUrl,
    "fileName" to fileName,
    "filePublicId" to filePublicId,
    "fileResourceType" to fileResourceType,
    "fileSize" to fileSize,
    "uploadedBy" to uploadedBy,
    "uploadedByName" to uploadedByName,
    "uploadedAt" to FieldValue.serverTimestamp(),
)
