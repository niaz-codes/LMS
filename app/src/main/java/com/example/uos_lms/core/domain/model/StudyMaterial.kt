package com.example.uos_lms.core.domain.model

enum class MaterialType {
    PDF, PPT, VIDEO, NOTE, OTHER;

    companion object {
        fun fromStringOrNull(raw: String?): MaterialType? = entries.find { it.name == raw }
    }
}

data class StudyMaterial(
    val id: String,
    val subjectId: String,
    val departmentId: String,
    val semesterId: String,
    val title: String,
    val materialType: MaterialType,
    val fileUrl: String,
    val fileName: String,
    val filePublicId: String,
    val fileResourceType: String,
    val fileSize: Long = 0L,
    val uploadedBy: String,
    val uploadedByName: String,
    val uploadedAt: Long = 0L,
)
