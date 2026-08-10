package com.example.uos_lms.core.data.remote

import com.example.uos_lms.core.data.remote.dto.QuizAttemptDto
import com.example.uos_lms.core.data.remote.dto.QuizDto
import com.example.uos_lms.core.data.remote.dto.toCreateMap
import com.example.uos_lms.core.data.remote.dto.toSubmitMap
import com.example.uos_lms.core.domain.model.Quiz
import com.example.uos_lms.core.domain.model.QuizAttempt
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreQuizDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val quizzesCollection get() = firestore.collection("quizzes")
    private val attemptsCollection get() = firestore.collection("quizAttempts")

    private fun attemptId(quizId: String, studentUid: String) = "${quizId}_$studentUid"

    suspend fun createQuiz(quiz: Quiz) {
        val ref = quizzesCollection.document()
        ref.set(quiz.copy(id = ref.id).toCreateMap()).await()
    }

    suspend fun getQuiz(quizId: String): Quiz? {
        val snapshot = quizzesCollection.document(quizId).get().await()
        if (!snapshot.exists()) return null
        return snapshot.toObject(QuizDto::class.java)?.toDomain()
    }

    fun observeQuizzesForSubject(subjectId: String): Flow<List<Quiz>> =
        quizzesCollection.whereEqualTo("subjectId", subjectId)
            .observeAs { it.toObject(QuizDto::class.java)?.toDomain() }

    fun observeAllQuizzes(): Flow<List<Quiz>> =
        quizzesCollection.observeAs { it.toObject(QuizDto::class.java)?.toDomain() }

    fun observeAttemptsForQuiz(quizId: String): Flow<List<QuizAttempt>> =
        attemptsCollection.whereEqualTo("quizId", quizId)
            .observeAs { it.toObject(QuizAttemptDto::class.java)?.toDomain() }

    fun observeMyAttempt(quizId: String, studentUid: String): Flow<QuizAttempt?> = callbackFlow {
        val ref = attemptsCollection.document(attemptId(quizId, studentUid))
        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val attempt = if (snapshot != null && snapshot.exists()) {
                snapshot.toObject(QuizAttemptDto::class.java)?.toDomain()
            } else {
                null
            }
            trySend(attempt)
        }
        awaitClose { registration.remove() }
    }

    suspend fun submitAttempt(attempt: QuizAttempt) {
        val id = attemptId(attempt.quizId, attempt.studentUid)
        attemptsCollection.document(id).set(attempt.copy(id = id).toSubmitMap()).await()
    }

    fun observeAttemptsForSubject(subjectId: String): Flow<List<QuizAttempt>> =
        attemptsCollection.whereEqualTo("subjectId", subjectId)
            .observeAs { it.toObject(QuizAttemptDto::class.java)?.toDomain() }

    suspend fun gradeAttempt(attemptId: String, manualScore: Int, feedback: String, gradedBy: String) {
        attemptsCollection.document(attemptId).update(
            mapOf(
                "manualScore" to manualScore,
                "feedback" to feedback,
                "gradedBy" to gradedBy,
                "gradedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }
}
