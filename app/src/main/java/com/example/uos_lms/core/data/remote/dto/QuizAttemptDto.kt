package com.example.uos_lms.core.data.remote.dto

import com.example.uos_lms.core.domain.model.QuizAttempt
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class QuizAttemptDto(
    val id: String = "",
    val quizId: String = "",
    val subjectId: String = "",
    val studentUid: String = "",
    val studentName: String = "",
    val answers: List<Int?> = emptyList(),
    val score: Int = 0,
    val totalMarks: Int = 0,
    @ServerTimestamp val submittedAt: Date? = null,
    val manualScore: Int? = null,
    val feedback: String? = null,
    val gradedBy: String? = null,
    @ServerTimestamp val gradedAt: Date? = null,
) {
    fun toDomain() = QuizAttempt(
        id = id,
        quizId = quizId,
        subjectId = subjectId,
        studentUid = studentUid,
        studentName = studentName,
        answers = answers,
        score = score,
        totalMarks = totalMarks,
        submittedAt = submittedAt?.time ?: 0L,
        manualScore = manualScore,
        feedback = feedback,
        gradedBy = gradedBy,
        gradedAt = gradedAt?.time,
    )
}

fun QuizAttempt.toSubmitMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "quizId" to quizId,
    "subjectId" to subjectId,
    "studentUid" to studentUid,
    "studentName" to studentName,
    "answers" to answers,
    "score" to score,
    "totalMarks" to totalMarks,
    "submittedAt" to FieldValue.serverTimestamp(),
)
