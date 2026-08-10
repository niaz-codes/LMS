package com.example.uos_lms.core.data.remote.dto

import com.example.uos_lms.core.domain.model.Quiz
import com.example.uos_lms.core.domain.model.QuizQuestion
import com.example.uos_lms.core.domain.model.QuizType
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

data class QuizQuestionDto(
    val text: String = "",
    val options: List<String> = emptyList(),
    val correctOptionIndex: Int = 0,
    val marks: Int = 0,
) {
    fun toDomain() = QuizQuestion(
        text = text,
        options = options,
        correctOptionIndex = correctOptionIndex,
        marks = marks,
    )
}

fun QuizQuestion.toMap(): Map<String, Any?> = mapOf(
    "text" to text,
    "options" to options,
    "correctOptionIndex" to correctOptionIndex,
    "marks" to marks,
)

data class QuizDto(
    val id: String = "",
    val subjectId: String = "",
    val departmentId: String = "",
    val semesterId: String = "",
    val title: String = "",
    val description: String = "",
    val type: String = QuizType.QUIZ.name,
    val questions: List<QuizQuestionDto> = emptyList(),
    val timeLimitMinutes: Int = 0,
    val dueDateMillis: Long = 0L,
    val createdBy: String = "",
    @ServerTimestamp val createdAt: Date? = null,
) {
    fun toDomain() = Quiz(
        id = id,
        subjectId = subjectId,
        departmentId = departmentId,
        semesterId = semesterId,
        title = title,
        description = description,
        type = QuizType.fromStringOrNull(type) ?: QuizType.QUIZ,
        questions = questions.map { it.toDomain() },
        timeLimitMinutes = timeLimitMinutes,
        dueDateMillis = dueDateMillis,
        createdBy = createdBy,
        createdAt = createdAt?.time ?: 0L,
    )
}

fun Quiz.toCreateMap(): Map<String, Any?> = mapOf(
    "id" to id,
    "subjectId" to subjectId,
    "departmentId" to departmentId,
    "semesterId" to semesterId,
    "title" to title,
    "description" to description,
    "type" to type.name,
    "questions" to questions.map { it.toMap() },
    "timeLimitMinutes" to timeLimitMinutes,
    "dueDateMillis" to dueDateMillis,
    "createdBy" to createdBy,
    "createdAt" to FieldValue.serverTimestamp(),
)
