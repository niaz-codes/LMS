package com.example.uos_lms.core.domain.model

data class AssignmentSubmission(
    val id: String,
    val assignmentId: String,
    val subjectId: String,
    val studentUid: String,
    val studentName: String,
    val textAnswer: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val filePublicId: String? = null,
    val fileResourceType: String? = null,
    val fileSize: Long? = null,
    val submittedAt: Long = 0L,
    val marksObtained: Int? = null,
    val feedback: String? = null,
    val gradedBy: String? = null,
    val gradedAt: Long? = null,
) {
    val isGraded: Boolean get() = marksObtained != null
}
