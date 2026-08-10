package com.example.uos_lms.core.data.remote

import com.example.uos_lms.core.data.remote.dto.AssignmentDto
import com.example.uos_lms.core.data.remote.dto.AssignmentSubmissionDto
import com.example.uos_lms.core.data.remote.dto.toCreateMap
import com.example.uos_lms.core.data.remote.dto.toSubmitMap
import com.example.uos_lms.core.domain.model.Assignment
import com.example.uos_lms.core.domain.model.AssignmentSubmission
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreAssignmentDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val assignmentsCollection get() = firestore.collection("assignments")
    private val submissionsCollection get() = firestore.collection("submissions")

    private fun submissionId(assignmentId: String, studentUid: String) = "${assignmentId}_$studentUid"

    fun newAssignmentId(): String = assignmentsCollection.document().id

    suspend fun createAssignment(assignment: Assignment) {
        assignmentsCollection.document(assignment.id).set(assignment.toCreateMap()).await()
    }

    suspend fun getAssignment(assignmentId: String): Assignment? {
        val snapshot = assignmentsCollection.document(assignmentId).get().await()
        if (!snapshot.exists()) return null
        return snapshot.toObject(AssignmentDto::class.java)?.toDomain()
    }

    fun observeAssignmentsForSubject(subjectId: String): Flow<List<Assignment>> =
        assignmentsCollection.whereEqualTo("subjectId", subjectId)
            .observeAs { it.toObject(AssignmentDto::class.java)?.toDomain() }

    fun observeAllAssignments(): Flow<List<Assignment>> =
        assignmentsCollection.observeAs { it.toObject(AssignmentDto::class.java)?.toDomain() }

    suspend fun deleteAssignment(assignmentId: String) {
        assignmentsCollection.document(assignmentId).delete().await()
    }

    fun observeSubmissionsForAssignment(assignmentId: String): Flow<List<AssignmentSubmission>> =
        submissionsCollection.whereEqualTo("assignmentId", assignmentId)
            .observeAs { it.toObject(AssignmentSubmissionDto::class.java)?.toDomain() }

    fun observeSubmissionsForSubject(subjectId: String): Flow<List<AssignmentSubmission>> =
        submissionsCollection.whereEqualTo("subjectId", subjectId)
            .observeAs { it.toObject(AssignmentSubmissionDto::class.java)?.toDomain() }

    fun observeSubmissionsForStudent(studentUid: String): Flow<List<AssignmentSubmission>> =
        submissionsCollection.whereEqualTo("studentUid", studentUid)
            .observeAs { it.toObject(AssignmentSubmissionDto::class.java)?.toDomain() }

    fun observeMySubmission(assignmentId: String, studentUid: String): Flow<AssignmentSubmission?> = callbackFlow {
        val ref = submissionsCollection.document(submissionId(assignmentId, studentUid))
        val registration = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val submission = if (snapshot != null && snapshot.exists()) {
                snapshot.toObject(AssignmentSubmissionDto::class.java)?.toDomain()
            } else {
                null
            }
            trySend(submission)
        }
        awaitClose { registration.remove() }
    }

    suspend fun getSubmission(assignmentId: String, studentUid: String): AssignmentSubmission? {
        val snapshot = submissionsCollection.document(submissionId(assignmentId, studentUid)).get().await()
        if (!snapshot.exists()) return null
        return snapshot.toObject(AssignmentSubmissionDto::class.java)?.toDomain()
    }

    suspend fun submitAssignment(submission: AssignmentSubmission) {
        val id = submissionId(submission.assignmentId, submission.studentUid)
        submissionsCollection.document(id).set(submission.copy(id = id).toSubmitMap()).await()
    }

    suspend fun gradeSubmission(submissionId: String, marksObtained: Int, feedback: String, gradedBy: String) {
        submissionsCollection.document(submissionId).update(
            mapOf(
                "marksObtained" to marksObtained,
                "feedback" to feedback,
                "gradedBy" to gradedBy,
                "gradedAt" to FieldValue.serverTimestamp(),
            ),
        ).await()
    }
}
