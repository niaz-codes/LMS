package com.example.uos_lms.feature.examresult.domain.repository

import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.ExamResult
import kotlinx.coroutines.flow.Flow

interface ExamResultRepository {
    fun observeResultsForSubject(subjectId: String): Flow<List<ExamResult>>
    fun observeApprovedResultsForStudent(studentUid: String): Flow<List<ExamResult>>
    fun observePendingApprovalsForDepartment(departmentId: String): Flow<List<ExamResult>>
    fun observeAllResultsForDepartment(departmentId: String): Flow<List<ExamResult>>
    fun observeAllResults(): Flow<List<ExamResult>>

    suspend fun saveDrafts(results: List<ExamResult>): AppResult<Unit>
    suspend fun submitForApproval(resultIds: List<String>): AppResult<Unit>
    suspend fun approve(resultId: String, grade: String, gpaPoint: Double, reviewerUid: String): AppResult<Unit>
    suspend fun reject(resultId: String, reason: String, reviewerUid: String): AppResult<Unit>
}
