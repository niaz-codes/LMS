package com.example.uos_lms.feature.assignment.domain.repository

import android.net.Uri
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.domain.model.Assignment
import com.example.uos_lms.core.domain.model.AssignmentSubmission
import kotlinx.coroutines.flow.Flow

interface AssignmentRepository {
    suspend fun getAssignment(assignmentId: String): AppResult<Assignment?>
    fun observeAssignmentsForSubject(subjectId: String): Flow<List<Assignment>>
    fun observeAllAssignments(): Flow<List<Assignment>>
    suspend fun deleteAssignment(assignmentId: String): AppResult<Unit>
    suspend fun createAssignment(
        subjectId: String,
        departmentId: String,
        semesterId: String,
        title: String,
        description: String,
        dueDateMillis: Long,
        maxMarks: Int,
        createdBy: String,
        fileUri: Uri?,
        onProgress: (Int) -> Unit = {},
    ): AppResult<Unit>

    fun observeSubmissionsForAssignment(assignmentId: String): Flow<List<AssignmentSubmission>>
    fun observeSubmissionsForSubject(subjectId: String): Flow<List<AssignmentSubmission>>
    fun observeSubmissionsForStudent(studentUid: String): Flow<List<AssignmentSubmission>>
    fun observeMySubmission(assignmentId: String, studentUid: String): Flow<AssignmentSubmission?>

    suspend fun submitAssignment(
        assignmentId: String,
        subjectId: String,
        studentUid: String,
        studentName: String,
        textAnswer: String?,
        fileUri: Uri?,
        existingFileUrl: String?,
        existingFileName: String?,
        existingFilePublicId: String?,
        existingFileResourceType: String?,
        onProgress: (Int) -> Unit = {},
    ): AppResult<Unit>

    suspend fun gradeSubmission(
        submissionId: String,
        marksObtained: Int,
        feedback: String,
        gradedBy: String,
    ): AppResult<Unit>
}
