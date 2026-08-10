package com.example.uos_lms.core.domain.model

/**
 * DRAFT and REJECTED are the only states a Teacher may edit marks in.
 * PENDING_APPROVAL is reached by the Teacher's "Submit for HOD Approval" action
 * and can only be resolved by an HOD into APPROVED or REJECTED — a Teacher can
 * never self-approve. APPROVED is the final, published state: it is the only
 * status a Student is ever allowed to see (see firestore.rules).
 */
enum class ResultStatus {
    DRAFT, PENDING_APPROVAL, APPROVED, REJECTED;

    companion object {
        fun fromStringOrNull(raw: String?): ResultStatus? = entries.find { it.name == raw }
    }
}

data class ExamResult(
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
    val status: ResultStatus = ResultStatus.DRAFT,
    val grade: String? = null,
    val gpaPoint: Double? = null,
    val rejectionReason: String? = null,
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L,
    val submittedAt: Long? = null,
    val reviewedBy: String? = null,
    val reviewedAt: Long? = null,
) {
    val percentage: Double
        get() = if (totalMarks == 0) 0.0 else (obtainedMarks * 100.0) / totalMarks
}
