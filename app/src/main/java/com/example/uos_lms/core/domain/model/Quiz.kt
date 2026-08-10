package com.example.uos_lms.core.domain.model

enum class QuizType {
    QUIZ, EXAM;

    companion object {
        fun fromStringOrNull(raw: String?): QuizType? = entries.find { it.name == raw }
    }
}

data class QuizQuestion(
    val text: String,
    val options: List<String>,
    val correctOptionIndex: Int,
    val marks: Int,
)

data class Quiz(
    val id: String,
    val subjectId: String,
    val departmentId: String,
    val semesterId: String,
    val title: String,
    val description: String,
    val type: QuizType,
    val questions: List<QuizQuestion>,
    val timeLimitMinutes: Int,
    val dueDateMillis: Long,
    val createdBy: String,
    val createdAt: Long = 0L,
) {
    val totalMarks: Int get() = questions.sumOf { it.marks }
}
