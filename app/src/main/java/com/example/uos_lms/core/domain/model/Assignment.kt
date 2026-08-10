package com.example.uos_lms.core.domain.model

data class Assignment(
    val id: String,
    val subjectId: String,
    val departmentId: String,
    val semesterId: String,
    val title: String,
    val description: String,
    val dueDateMillis: Long,
    val maxMarks: Int,
    val createdBy: String,
    val createdAt: Long = 0L,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val filePublicId: String? = null,
    val fileResourceType: String? = null,
    val fileSize: Long? = null,
)
