package com.example.uos_lms.core.domain.model

data class QuizAttempt(
    val id: String,
    val quizId: String,
    val subjectId: String,
    val studentUid: String,
    val studentName: String,
    val answers: List<Int?>,
    val score: Int,
    val totalMarks: Int,
    val submittedAt: Long = 0L,
    val manualScore: Int? = null,
    val feedback: String? = null,
    val gradedBy: String? = null,
    val gradedAt: Long? = null,
) {
    val effectiveScore: Int get() = manualScore ?: score
    val isManuallyGraded: Boolean get() = manualScore != null
}
