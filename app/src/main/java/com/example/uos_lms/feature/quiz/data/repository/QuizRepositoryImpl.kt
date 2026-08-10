package com.example.uos_lms.feature.quiz.data.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.common.safeCall
import com.example.uos_lms.core.data.remote.FirestoreQuizDataSource
import com.example.uos_lms.core.domain.model.Quiz
import com.example.uos_lms.core.domain.model.QuizAttempt
import com.example.uos_lms.core.domain.model.QuizQuestion
import com.example.uos_lms.core.domain.model.QuizType
import com.example.uos_lms.feature.quiz.domain.repository.QuizRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuizRepositoryImpl @Inject constructor(
    private val quizDataSource: FirestoreQuizDataSource,
) : QuizRepository {

    override suspend fun getQuiz(quizId: String): AppResult<Quiz?> = safeCall {
        quizDataSource.getQuiz(quizId)
    }

    override fun observeQuizzesForSubject(subjectId: String): Flow<List<Quiz>> =
        quizDataSource.observeQuizzesForSubject(subjectId)

    override fun observeAllQuizzes(): Flow<List<Quiz>> =
        quizDataSource.observeAllQuizzes()

    override suspend fun createQuiz(
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
    ): AppResult<Unit> = safeCall {
        if (title.isBlank()) {
            error("Title is required.")
        }
        if (questions.isEmpty()) {
            error("Add at least one question.")
        }
        questions.forEachIndexed { index, question ->
            if (question.text.isBlank()) {
                error("Question ${index + 1} needs text.")
            }
            if (question.options.size != 4 || question.options.any { it.isBlank() }) {
                error("Question ${index + 1} needs all 4 options filled in.")
            }
            if (question.correctOptionIndex !in question.options.indices) {
                error("Question ${index + 1} needs a correct answer selected.")
            }
            if (question.marks <= 0) {
                error("Question ${index + 1} needs marks greater than zero.")
            }
        }
        if (timeLimitMinutes <= 0) {
            error("Time limit must be greater than zero.")
        }
        quizDataSource.createQuiz(
            Quiz(
                id = "",
                subjectId = subjectId,
                departmentId = departmentId,
                semesterId = semesterId,
                title = title,
                description = description,
                type = type,
                questions = questions,
                timeLimitMinutes = timeLimitMinutes,
                dueDateMillis = dueDateMillis,
                createdBy = createdBy,
            ),
        )
    }

    override fun observeAttemptsForQuiz(quizId: String): Flow<List<QuizAttempt>> =
        quizDataSource.observeAttemptsForQuiz(quizId)

    override fun observeAttemptsForSubject(subjectId: String): Flow<List<QuizAttempt>> =
        quizDataSource.observeAttemptsForSubject(subjectId)

    override fun observeMyAttempt(quizId: String, studentUid: String): Flow<QuizAttempt?> =
        quizDataSource.observeMyAttempt(quizId, studentUid)

    override suspend fun submitAttempt(
        quiz: Quiz,
        studentUid: String,
        studentName: String,
        answers: List<Int?>,
    ): AppResult<Unit> = safeCall {
        val score = quiz.questions.mapIndexed { index, question ->
            if (answers.getOrNull(index) == question.correctOptionIndex) question.marks else 0
        }.sum()
        quizDataSource.submitAttempt(
            QuizAttempt(
                id = "",
                quizId = quiz.id,
                subjectId = quiz.subjectId,
                studentUid = studentUid,
                studentName = studentName,
                answers = answers,
                score = score,
                totalMarks = quiz.totalMarks,
            ),
        )
    }

    override suspend fun gradeAttempt(
        attemptId: String,
        manualScore: Int,
        feedback: String,
        gradedBy: String,
    ): AppResult<Unit> = safeCall {
        quizDataSource.gradeAttempt(attemptId, manualScore, feedback, gradedBy)
    }
}
