package com.example.uos_lms.core.data.remote

import com.example.uos_lms.core.data.remote.dto.AttendanceRecordDto
import com.example.uos_lms.core.data.remote.dto.toFirestoreMap
import com.example.uos_lms.core.domain.model.AttendanceRecord
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreAttendanceDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    private val attendanceCollection get() = firestore.collection("attendance")

    private fun recordId(subjectId: String, dateKey: String, studentUid: String) =
        "${subjectId}_${dateKey}_$studentUid"

    suspend fun saveAttendance(records: List<AttendanceRecord>) {
        if (records.isEmpty()) return
        val batch = firestore.batch()
        records.forEach { record ->
            val id = recordId(record.subjectId, record.dateKey, record.studentUid)
            val ref = attendanceCollection.document(id)
            batch.set(ref, record.copy(id = id).toFirestoreMap())
        }
        batch.commit().await()
    }

    fun observeSessionAttendance(subjectId: String, dateKey: String): Flow<List<AttendanceRecord>> =
        attendanceCollection
            .whereEqualTo("subjectId", subjectId)
            .whereEqualTo("dateKey", dateKey)
            .observeAs { it.toObject(AttendanceRecordDto::class.java)?.toDomain() }

    fun observeHistoryForSubject(subjectId: String): Flow<List<AttendanceRecord>> =
        attendanceCollection
            .whereEqualTo("subjectId", subjectId)
            .observeAs { it.toObject(AttendanceRecordDto::class.java)?.toDomain() }

    fun observeStudentAttendanceForSubject(subjectId: String, studentUid: String): Flow<List<AttendanceRecord>> =
        attendanceCollection
            .whereEqualTo("subjectId", subjectId)
            .whereEqualTo("studentUid", studentUid)
            .observeAs { it.toObject(AttendanceRecordDto::class.java)?.toDomain() }

    fun observeAttendanceForDepartment(departmentId: String): Flow<List<AttendanceRecord>> =
        attendanceCollection
            .whereEqualTo("departmentId", departmentId)
            .observeAs { it.toObject(AttendanceRecordDto::class.java)?.toDomain() }

    fun observeAttendanceForSemester(departmentId: String, semesterId: String): Flow<List<AttendanceRecord>> =
        attendanceCollection
            .whereEqualTo("departmentId", departmentId)
            .whereEqualTo("semesterId", semesterId)
            .observeAs { it.toObject(AttendanceRecordDto::class.java)?.toDomain() }
}
