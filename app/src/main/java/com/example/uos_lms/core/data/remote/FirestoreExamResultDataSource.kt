package com.example.uos_lms.core.data.remote

import com.example.uos_lms.core.data.remote.dto.ExamResultDto
import com.example.uos_lms.core.data.remote.dto.approveMap
import com.example.uos_lms.core.data.remote.dto.rejectMap
import com.example.uos_lms.core.data.remote.dto.submitForApprovalMap
import com.example.uos_lms.core.data.remote.dto.toDraftMap
import com.example.uos_lms.core.domain.model.ExamResult
import com.example.uos_lms.core.domain.model.ResultStatus
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreExamResultDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val resultsCollection get() = firestore.collection("examResults")

    private fun resultId(subjectId: String, studentUid: String) = "${subjectId}_$studentUid"

    /** One doc per (subject, student), batch-written so a whole class roster saves atomically. */
    suspend fun saveDrafts(results: List<ExamResult>) {
        if (results.isEmpty()) return
        val batch = firestore.batch()
        results.forEach { result ->
            val id = resultId(result.subjectId, result.studentUid)
            batch.set(resultsCollection.document(id), result.copy(id = id).toDraftMap())
        }
        batch.commit().await()
    }

    /** Moves every listed result from DRAFT/REJECTED to PENDING_APPROVAL in one batch. */
    suspend fun submitForApproval(resultIds: List<String>) {
        if (resultIds.isEmpty()) return
        val batch = firestore.batch()
        val map = submitForApprovalMap()
        resultIds.forEach { id -> batch.update(resultsCollection.document(id), map) }
        batch.commit().await()
    }

    suspend fun approve(resultId: String, grade: String, gpaPoint: Double, reviewerUid: String) {
        resultsCollection.document(resultId).update(approveMap(grade, gpaPoint, reviewerUid)).await()
    }

    suspend fun reject(resultId: String, reason: String, reviewerUid: String) {
        resultsCollection.document(resultId).update(rejectMap(reason, reviewerUid)).await()
    }

    /** Teacher's own result sheet for one subject — every status, so drafts/rejections stay visible to them. */
    fun observeResultsForSubject(subjectId: String): Flow<List<ExamResult>> =
        resultsCollection
            .whereEqualTo("subjectId", subjectId)
            .observeAs { it.toObject(ExamResultDto::class.java)?.toDomain() }

    /** Student's final results — status filter is required here (not just client-side) so the
     * query's own where-clause matches firestore.rules' read condition exactly. */
    fun observeApprovedResultsForStudent(studentUid: String): Flow<List<ExamResult>> =
        resultsCollection
            .whereEqualTo("studentUid", studentUid)
            .whereEqualTo("status", ResultStatus.APPROVED.name)
            .observeAs { it.toObject(ExamResultDto::class.java)?.toDomain() }

    /** HOD's approval queue — departmentId is denormalized directly onto the doc (not looked up
     * via subjectId) so this list query is provable against firestore.rules' isHodOfDepartment(). */
    fun observePendingApprovalsForDepartment(departmentId: String): Flow<List<ExamResult>> =
        resultsCollection
            .whereEqualTo("departmentId", departmentId)
            .whereEqualTo("status", ResultStatus.PENDING_APPROVAL.name)
            .observeAs { it.toObject(ExamResultDto::class.java)?.toDomain() }

    /** HOD's full department history (all statuses), for a "reviewed" tab alongside the pending queue. */
    fun observeAllResultsForDepartment(departmentId: String): Flow<List<ExamResult>> =
        resultsCollection
            .whereEqualTo("departmentId", departmentId)
            .observeAs { it.toObject(ExamResultDto::class.java)?.toDomain() }

    /** Admin oversight — isAdmin() doesn't reference resource.data, so an unfiltered list is safe. */
    fun observeAllResults(): Flow<List<ExamResult>> =
        resultsCollection.observeAs { it.toObject(ExamResultDto::class.java)?.toDomain() }
}
