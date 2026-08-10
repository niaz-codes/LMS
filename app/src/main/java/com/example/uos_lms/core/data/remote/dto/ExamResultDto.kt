package com.example.uos_lms.core.data.remote.dto

import com.example.uos_lms.core.domain.model.ExamResult
import com.example.uos_lms.core.domain.model.ResultStatus
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class ExamResultDto(
    val id: String = "",
    val subjectId: String = "",
    val subjectCode: String = "",
    val subjectTitle: String = "",
    val creditHours: Int = 0,
    val departmentId: String = "",
    val semesterId: String = "",
    val teacherUid: String = "",
    val teacherName: String = "",
    val studentUid: String = "",
    val studentName: String = "",
    val studentRollNumber: String? = null,
    val obtainedMarks: Int = 0,
    val totalMarks: Int = 100,
    val status: String = ResultStatus.DRAFT.name,
    val grade: String? = null,
    val gpaPoint: Double? = null,
    val rejectionReason: String? = null,
    @ServerTimestamp val createdAt: Date? = null,
    @ServerTimestamp val updatedAt: Date? = null,
    @ServerTimestamp val submittedAt: Date? = null,
    val reviewedBy: String? = null,
    @ServerTimestamp val reviewedAt: Date? = null,
) {
    fun toDomain() = ExamResult(
        id = id,
        subjectId = subjectId,
        subjectCode = subjectCode,
        subjectTitle = subjectTitle,
        creditHours = creditHours,
        departmentId = departmentId,
        semesterId = semesterId,
        teacherUid = teacherUid,
        teacherName = teacherName,
        studentUid = studentUid,
        studentName = studentName,
        studentRollNumber = studentRollNumber,
        obtainedMarks = obtainedMarks,
        totalMarks = totalMarks,
        status = ResultStatus.fromStringOrNull(status) ?: ResultStatus.DRAFT,
        grade = grade,
        gpaPoint = gpaPoint,
        rejectionReason = rejectionReason,
        createdAt = createdAt?.time ?: 0L,
        updatedAt = updatedAt?.time ?: 0L,
        submittedAt = submittedAt?.time,
        reviewedBy = reviewedBy,
        reviewedAt = reviewedAt?.time,
    )
}

/**
 * Full-document write used by the Teacher both to create a fresh DRAFT row
 * and to edit marks while still DRAFT/REJECTED (mirrors AssignmentSubmission's
 * toSubmitMap — grade/review fields are explicitly nulled so the keys exist,
 * which firestore.rules' create-time `== null` checks require). `createdAt`
 * reuses the existing timestamp on edits (passed in unchanged) so it's never
 * counted as an affected key by the update rule — only a brand-new row
 * (createdAt == 0L) gets a fresh server timestamp.
 */
fun ExamResult.toDraftMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "subjectId" to subjectId,
    "subjectCode" to subjectCode,
    "subjectTitle" to subjectTitle,
    "creditHours" to creditHours,
    "departmentId" to departmentId,
    "semesterId" to semesterId,
    "teacherUid" to teacherUid,
    "teacherName" to teacherName,
    "studentUid" to studentUid,
    "studentName" to studentName,
    "studentRollNumber" to studentRollNumber,
    "obtainedMarks" to obtainedMarks,
    "totalMarks" to totalMarks,
    "status" to ResultStatus.DRAFT.name,
    "grade" to null,
    "gpaPoint" to null,
    "rejectionReason" to null,
    "createdAt" to if (createdAt == 0L) FieldValue.serverTimestamp() else Date(createdAt),
    "updatedAt" to FieldValue.serverTimestamp(),
    "submittedAt" to null,
    "reviewedBy" to null,
    "reviewedAt" to null,
)

/** Teacher: DRAFT/REJECTED -> PENDING_APPROVAL. Partial update, only the
 * fields firestore.rules allows a Teacher to touch post-submission. */
fun submitForApprovalMap(): Map<String, Any?> = mapOf(
    "status" to ResultStatus.PENDING_APPROVAL.name,
    "submittedAt" to FieldValue.serverTimestamp(),
    "rejectionReason" to null,
)

/** HOD: PENDING_APPROVAL -> APPROVED. Grade/GPA point are computed by the
 * caller (via GradeScale) at approval time and stored, never recomputed later. */
fun approveMap(grade: String, gpaPoint: Double, reviewerUid: String): Map<String, Any?> = mapOf(
    "status" to ResultStatus.APPROVED.name,
    "grade" to grade,
    "gpaPoint" to gpaPoint,
    "reviewedBy" to reviewerUid,
    "reviewedAt" to FieldValue.serverTimestamp(),
    "rejectionReason" to null,
)

/** HOD: PENDING_APPROVAL -> REJECTED, with a reason surfaced back on the Teacher's draft. */
fun rejectMap(reason: String, reviewerUid: String): Map<String, Any?> = mapOf(
    "status" to ResultStatus.REJECTED.name,
    "reviewedBy" to reviewerUid,
    "reviewedAt" to FieldValue.serverTimestamp(),
    "rejectionReason" to reason,
)
