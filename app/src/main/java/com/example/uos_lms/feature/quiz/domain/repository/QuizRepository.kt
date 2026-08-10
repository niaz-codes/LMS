package com.example.uos_lms.feature.quiz.domain.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Quiz
import com.example.uos_lms.core.domain.model.QuizAttempt
import com.example.uos_lms.core.domain.model.QuizQuestion
import com.example.uos_lms.core.domain.model.QuizType
import kotlinx.coroutines.flow.Flow

interface QuizRepository {
    suspend fun getQuiz(quizId: String): AppResult<Quiz?>
    fun observeQuizzesForSubject(subjectId: String): Flow<List<Quiz>>
    fun observeAllQuizzes(): Flow<List<Quiz>>

    suspend fun createQuiz(
        subjectId: String,
        departmentId: String,
        semesterId: String,
        title: String,
        description: String,
        type: QuizType,
        questions: List<QuizQuestion>,
        timeLimitMinutes: Int,
        dueDateMillis: Long,
        createdBy: String,
    ): AppResult<Unit>

    fun observeAttemptsForQuiz(quizId: String): Flow<List<QuizAttempt>>
    fun observeAttemptsForSubject(subjectId: String): Flow<List<QuizAttempt>>
    fun observeMyAttempt(quizId: String, studentUid: String): Flow<QuizAttempt?>

    suspend fun submitAttempt(
        quiz: Quiz,
        studentUid: String,
        studentName: String,
        answers: List<Int?>,
    ): AppResult<Unit>

    suspend fun gradeAttempt(
        attemptId: String,
        manualScore: Int,
        feedback: String,
        gradedBy: String,
    ): AppResult<Unit>
}
