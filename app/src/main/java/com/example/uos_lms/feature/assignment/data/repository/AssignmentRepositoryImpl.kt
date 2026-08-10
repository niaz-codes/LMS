package com.example.uos_lms.feature.assignment.data.repository

import android.net.Uri
import com.example.uos_lms.core.common.AppResult
import com.example.uos_lms.core.common.safeCall
import com.example.uos_lms.core.data.remote.CloudinaryDataSource
import com.example.uos_lms.core.data.remote.FirestoreAssignmentDataSource
import com.example.uos_lms.core.domain.model.Assignment
import com.example.uos_lms.core.domain.model.AssignmentSubmission
import com.example.uos_lms.feature.assignment.domain.repository.AssignmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssignmentRepositoryImpl @Inject constructor(
    private val assignmentDataSource: FirestoreAssignmentDataSource,
    private val cloudinaryDataSource: CloudinaryDataSource,
) : AssignmentRepository {

    override suspend fun getAssignment(assignmentId: String): AppResult<Assignment?> = safeCall {
        assignmentDataSource.getAssignment(assignmentId)
    }

    override fun observeAssignmentsForSubject(subjectId: String): Flow<List<Assignment>> =
        assignmentDataSource.observeAssignmentsForSubject(subjectId)

    override fun observeAllAssignments(): Flow<List<Assignment>> =
        assignmentDataSource.observeAllAssignments()

    override suspend fun deleteAssignment(assignmentId: String): AppResult<Unit> = safeCall {
        val assignment = assignmentDataSource.getAssignment(assignmentId)
        if (assignment?.filePublicId != null && assignment.fileResourceType != null) {
            runCatching { cloudinaryDataSource.delete(assignment.filePublicId, assignment.fileResourceType) }
        }
        assignmentDataSource.deleteAssignment(assignmentId)
    }

    override suspend fun createAssignment(
        subjectId: String,
        departmentId: String,
        semesterId: String,
        title: String,
        description: String,
        dueDateMillis: Long,
        maxMarks: Int,
        createdBy: String,
        fileUri: Uri?,
        onProgress: (Int) -> Unit,
    ): AppResult<Unit> = safeCall {
        if (title.isBlank()) {
            error("Title is required.")
        }
        if (maxMarks <= 0) {
            error("Max marks must be greater than zero.")
        }
        val assignmentId = assignmentDataSource.newAssignmentId()
        val file = fileUri?.let {
            cloudinaryDataSource.uploadAssignmentFile(subjectId, assignmentId, it, onProgress)
        }
        assignmentDataSource.createAssignment(
            Assignment(
                id = assignmentId,
                subjectId = subjectId,
                departmentId = departmentId,
                semesterId = semesterId,
                title = title,
                description = description,
                dueDateMillis = dueDateMillis,
                maxMarks = maxMarks,
                createdBy = createdBy,
                fileUrl = file?.url,
                fileName = file?.fileName,
                filePublicId = file?.publicId,
                fileResourceType = file?.resourceType,
                fileSize = file?.bytes,
            ),
        )
    }

    override fun observeSubmissionsForAssignment(assignmentId: String): Flow<List<AssignmentSubmission>> =
        assignmentDataSource.observeSubmissionsForAssignment(assignmentId)

    override fun observeSubmissionsForSubject(subjectId: String): Flow<List<AssignmentSubmission>> =
        assignmentDataSource.observeSubmissionsForSubject(subjectId)

    override fun observeSubmissionsForStudent(studentUid: String): Flow<List<AssignmentSubmission>> =
        assignmentDataSource.observeSubmissionsForStudent(studentUid)

    override fun observeMySubmission(assignmentId: String, studentUid: String): Flow<AssignmentSubmission?> =
        assignmentDataSource.observeMySubmission(assignmentId, studentUid)

    override suspend fun submitAssignment(
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
        onProgress: (Int) -> Unit,
    ): AppResult<Unit> = safeCall {
        if (textAnswer.isNullOrBlank() && fileUri == null && existingFileUrl == null) {
            error("Attach a file or write a note before submitting.")
        }
        var fileUrl = existingFileUrl
        var fileName = existingFileName
        var filePublicId = existingFilePublicId
        var fileResourceType = existingFileResourceType
        var fileSize: Long? = null
        if (fileUri != null) {
            val file = cloudinaryDataSource.uploadSubmissionFile(assignmentId, studentUid, fileUri, onProgress)
            if (existingFilePublicId != null && existingFileResourceType != null) {
                runCatching { cloudinaryDataSource.delete(existingFilePublicId, existingFileResourceType) }
            }
            fileUrl = file.url
            fileName = file.fileName
            filePublicId = file.publicId
            fileResourceType = file.resourceType
            fileSize = file.bytes
        }
        val submission = AssignmentSubmission(
            id = "",
            assignmentId = assignmentId,
            subjectId = subjectId,
            studentUid = studentUid,
            studentName = studentName,
            textAnswer = textAnswer?.takeIf { it.isNotBlank() },
            fileUrl = fileUrl,
            fileName = fileName,
            filePublicId = filePublicId,
            fileResourceType = fileResourceType,
            fileSize = fileSize,
        )
        assignmentDataSource.submitAssignment(submission)
    }

    override suspend fun gradeSubmission(
        submissionId: String,
        marksObtained: Int,
        feedback: String,
        gradedBy: String,
    ): AppResult<Unit> = safeCall {
        assignmentDataSource.gradeSubmission(submissionId, marksObtained, feedback, gradedBy)
    }
}
