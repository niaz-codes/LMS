package com.example.uos_lms.feature.examresult.data.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.common.safeCall
import com.example.uos_lms.core.data.remote.FirestoreExamResultDataSource
import com.example.uos_lms.core.domain.model.ExamResult
import com.example.uos_lms.feature.examresult.domain.repository.ExamResultRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExamResultRepositoryImpl @Inject constructor(
    private val dataSource: FirestoreExamResultDataSource,
) : ExamResultRepository {

    override fun observeResultsForSubject(subjectId: String): Flow<List<ExamResult>> =
        dataSource.observeResultsForSubject(subjectId)

    override fun observeApprovedResultsForStudent(studentUid: String): Flow<List<ExamResult>> =
        dataSource.observeApprovedResultsForStudent(studentUid)

    override fun observePendingApprovalsForDepartment(departmentId: String): Flow<List<ExamResult>> =
        dataSource.observePendingApprovalsForDepartment(departmentId)

    override fun observeAllResultsForDepartment(departmentId: String): Flow<List<ExamResult>> =
        dataSource.observeAllResultsForDepartment(departmentId)

    override fun observeAllResults(): Flow<List<ExamResult>> = dataSource.observeAllResults()

    override suspend fun saveDrafts(results: List<ExamResult>): AppResult<Unit> = safeCall {
        dataSource.saveDrafts(results)
    }

    override suspend fun submitForApproval(resultIds: List<String>): AppResult<Unit> = safeCall {
        dataSource.submitForApproval(resultIds)
    }

    override suspend fun approve(resultId: String, grade: String, gpaPoint: Double, reviewerUid: String): AppResult<Unit> =
        safeCall { dataSource.approve(resultId, grade, gpaPoint, reviewerUid) }

    override suspend fun reject(resultId: String, reason: String, reviewerUid: String): AppResult<Unit> = safeCall {
        dataSource.reject(resultId, reason, reviewerUid)
    }
}
