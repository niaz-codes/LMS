package com.example.uos_lms.core.data.remote.dto

import com.example.uos_lms.core.domain.model.AssignmentSubmission
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class AssignmentSubmissionDto(
    val id: String = "",
    val assignmentId: String = "",
    val subjectId: String = "",
    val studentUid: String = "",
    val studentName: String = "",
    val textAnswer: String? = null,
    val fileUrl: String? = null,
    val fileName: String? = null,
    val filePublicId: String? = null,
    val fileResourceType: String? = null,
    val fileSize: Long? = null,
    @ServerTimestamp val submittedAt: Date? = null,
    val marksObtained: Int? = null,
    val feedback: String? = null,
    val gradedBy: String? = null,
    @ServerTimestamp val gradedAt: Date? = null,
) {
    fun toDomain() = AssignmentSubmission(
        id = id,
        assignmentId = assignmentId,
        subjectId = subjectId,
        studentUid = studentUid,
        studentName = studentName,
        textAnswer = textAnswer,
        fileUrl = fileUrl,
        fileName = fileName,
        filePublicId = filePublicId,
        fileResourceType = fileResourceType,
        fileSize = fileSize,
        submittedAt = submittedAt?.time ?: 0L,
        marksObtained = marksObtained,
        feedback = feedback,
        gradedBy = gradedBy,
        gradedAt = gradedAt?.time,
    )
}

/** Full-document write used for both first submission and resubmission (grade fields stay null/unchanged). */
fun AssignmentSubmission.toSubmitMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "assignmentId" to assignmentId,
    "subjectId" to subjectId,
    "studentUid" to studentUid,
    "studentName" to studentName,
    "textAnswer" to textAnswer,
    "fileUrl" to fileUrl,
    "fileName" to fileName,
    "filePublicId" to filePublicId,
    "fileResourceType" to fileResourceType,
    "fileSize" to fileSize,
    "submittedAt" to FieldValue.serverTimestamp(),
    "marksObtained" to marksObtained,
    "feedback" to feedback,
    "gradedBy" to gradedBy,
    "gradedAt" to null,
)
